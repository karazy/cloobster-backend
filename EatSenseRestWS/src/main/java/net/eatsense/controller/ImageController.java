package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.eatsense.domain.Account;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.representation.ImageDTO;
import net.eatsense.representation.ImageUploadDTO;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.images.ImagesService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ImageController {
	
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
				image.setUrl(imagesService.getServingUrl( new BlobKey( image.getBlobKey() ) ) );
			}
			if(account.getImageUploads() == null || account.getImageUploads().isEmpty()) {
				throw new ServiceException("No uploaded images for account "+ account.getLogin());
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
					throw new ServiceException("Updated image was not uploaded by account "+ account.getLogin());
				}
			}
		}
		
		return new UpdateImagesResult(images, dirty, image);
	}
}
