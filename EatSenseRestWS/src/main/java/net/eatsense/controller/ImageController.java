package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.eatsense.domain.Account;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.ImageUploadDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesService.OutputEncoding;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.images.Transform;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ImageController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private BlobstoreService blobstoreService;
	private ImagesService imagesService;
	private AccountRepository accountRepo;
	
	
	@Inject
	public ImageController(BlobstoreService blobstoreService,
			ImagesService imagesService, AccountRepository accountRepo) {
		super();
		this.blobstoreService = blobstoreService;
		this.imagesService = imagesService;
		this.accountRepo = accountRepo;
	}
	
	public static class UpdateImagesResult {
		private final List<ImageDTO> images;
		private final boolean dirty;
		private final ImageDTO updatedImage;

		public UpdateImagesResult(List<ImageDTO> images, boolean dirty, ImageDTO updatedImage) {
			super();
			this.images = images;
			this.dirty = dirty;
			this.updatedImage = updatedImage;
		}

		public List<ImageDTO> getImages() {
			return images;
		}

		public boolean isDirty() {
			return dirty;
		}

		public ImageDTO getUpdatedImage() {
			return updatedImage;
		}		
	}
	
	public Image cropImage(String blobKey, OutputEncoding outputEncoding, double leftX, double topY, double rightX, double bottomY) {
		checkNotNull(blobKey, "blobKey was null");
		
		Image oldImage = ImagesServiceFactory.makeImageFromBlob(new BlobKey(blobKey));
		logger.info("Cropping blob: {}", blobKey);
		logger.info(String.format("Cropping coordinates: %f, %f; %f, %f", leftX, topY, rightX, bottomY));
		
		Transform cropTransform = ImagesServiceFactory.makeCrop(leftX, topY, rightX, bottomY);
		
		Image croppedImage = imagesService.applyTransform(cropTransform, oldImage, outputEncoding);
		logger.info(String.format("New image dimensions: %d x %d. Format: %s",
				croppedImage.getWidth(), croppedImage.getHeight(), croppedImage.getFormat().toString()));
		
		return croppedImage;
	}

	/**
	 * Update the existing image or add a new to the given list,
	 * also manages deletion of old blobstore file and update of serving url.
	 * 
	 * @param account - Account that uploaded the image.
	 * @param images - List of images to update.
	 * @param updatedImage - Image object to update or create.
	 * @return Result object, {@link UpdateImagesResult}.
	 */
	public UpdateImagesResult updateImages(Account account, List<ImageDTO> images, ImageDTO updatedImage) {
		checkNotNull(updatedImage, "updatedImage was null ");
		checkArgument(!Strings.isNullOrEmpty(updatedImage.getId()), "updatedImage id was null or empty");
		
		ImageDTO image = null;
		// Check if there already images.
		if(images == null) {
			images = new ArrayList<ImageDTO>();
		}
		// Look if we already have an image saved under this id.
		for (ImageDTO imageDTO : images) {
			if(imageDTO.getId().equals(updatedImage.getId()))
				image = imageDTO;
		}
		// It's an unknown image, create a new one.
		if( image == null ) {
			image = new ImageDTO();
			image.setId(updatedImage.getId());
			images.add(image);
		}
		boolean dirty = false;
		// Check if the blobKey has changed.
		if(!Strings.isNullOrEmpty(updatedImage.getBlobKey()) && !updatedImage.getBlobKey().equals(image.getBlobKey())) {
			if(image.getBlobKey() != null) {
				blobstoreService.delete(new BlobKey(image.getBlobKey()));
			}
			image.setBlobKey(updatedImage.getBlobKey());
			// Check if we got an image serving url supplied.
			if(!Strings.isNullOrEmpty(updatedImage.getUrl()) && !updatedImage.getUrl().equals(image.getUrl())) {
				// Use the supplied url.
				image.setUrl(updatedImage.getUrl());
			}
			else {
				// Create new serving url from the new blob key.
				image.setUrl(createServingUrl(image.getBlobKey()));
			}
			if(account.getImageUploads() == null || account.getImageUploads().isEmpty()) {
				throw new ValidationException("No uploaded images for Account"+ account.getId());
			}
			else {
				boolean found = false;
				for (Iterator<ImageUploadDTO> iterator = account.getImageUploads().iterator(); iterator.hasNext();) {
					ImageUploadDTO upload = iterator.next();
					if(upload.getBlobKey().equals(updatedImage.getBlobKey())) {
						// Found the corresponding upload, delete the object.
						found = true;
						dirty = true;
						
						iterator.remove();
						accountRepo.saveOrUpdate(account);
					}
				}
				if(!found) {
					throw new ValidationException("No image with that blobKey was uploaded by account "+ account.getId());
				}
			}
		}
		
		return new UpdateImagesResult(images, dirty, image);
	}

	/**
	 * @param image
	 * @return
	 */
	public String createServingUrl(String blobKey) {
		return imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey( new BlobKey( blobKey) ));
	}
	
	/**
	 * Remove an image from the list and from the blobstore.
	 * 
	 * @param id Unique identifier for the image
	 * @param images Collection of images
	 * @return
	 */
	public UpdateImagesResult removeImage(String id, List<ImageDTO> images) {
		if( images == null || images.isEmpty()) {
			return new UpdateImagesResult(images, false, null);
		}
		
		boolean dirty = false;
		ImageDTO removedImage = null;
		
		// Look if we have an image saved under this id.
		for (Iterator<ImageDTO> iterator = images.iterator(); iterator.hasNext();) {
			ImageDTO imageDTO = iterator.next();
			
			if(imageDTO.getId().equals(id)) {
				dirty = true;
				iterator.remove();
				
				if(!Strings.isNullOrEmpty(imageDTO.getBlobKey())) {
					BlobKey blobKey = new BlobKey(imageDTO.getBlobKey());
					blobstoreService.delete(blobKey);
					imagesService.deleteServingUrl(blobKey);
				}
				else {
					logger.warn("No BlobKey saved for image (id={}), probably test data.", id);
				}
				removedImage = imageDTO;
			}
		}
		
		return new UpdateImagesResult(images, dirty, removedImage);
	}
}
