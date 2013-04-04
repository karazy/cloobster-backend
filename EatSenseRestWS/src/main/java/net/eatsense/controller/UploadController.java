package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.eatsense.domain.Account;
import net.eatsense.domain.embedded.UploadToken;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.exceptions.ValidationException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.representation.ImageCropDTO;
import net.eatsense.representation.ImageUploadDTO;
import net.eatsense.service.FileServiceHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesService.OutputEncoding;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class UploadController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * The limit for uploaded file size, appengine limit is 32mb, but we limit it to 5mb.
	 */
	public final static long MAX_UPLOADSIZE = 5242880;
	private BlobstoreService blobStoreService;
	private final AccountRepository accountRepo;
	private final ImagesService imagesService;

	private FileServiceHelper fileService;

	private ImageController imageController;

	private BlobInfoFactory blobInfoFactory;

	@Inject
	public UploadController(BlobstoreService blobStoreService, AccountRepository accountRepo, ImagesService imagesService, FileServiceHelper fileService, ImageController imageCtrl, BlobInfoFactory blobInfoFactory) {
		super();
		this.blobStoreService = blobStoreService;
		this.accountRepo = accountRepo;
		this.imagesService = imagesService;
		this.fileService = fileService;
		this.imageController = imageCtrl;
		this.blobInfoFactory = blobInfoFactory;
	}
	
	/**
	 * Generate an upload url valid for some time. The upload token will be saved with the account.
	 * 
	 * @param account - {@link Account} for which the upload token should be created.
	 * @param successUrl - path to the upload handler
	 * @return uploadUrl - valid for some time to handle uploads, with the callback set as successUrl + /{uploadToken}
	 */
	public String getUploadUrl(Account account, String successUrl) {
		checkNotNull(account, "account was null");
		checkArgument(!Strings.nullToEmpty(successUrl).isEmpty(), "successUrl was null or empty");
		
		if(account.getUploadToken() == null || hasTokenExpired(account.getUploadToken())) {
			account.setUploadToken(accountRepo.newUploadToken());
			accountRepo.saveOrUpdate(account);
		}
		
		String uploadUrl = blobStoreService.createUploadUrl(successUrl + "/" + account.getUploadToken().getToken());
		logger.info("new upload url created for {}: {}", account.getLogin(), uploadUrl);
		return uploadUrl;
	}

	/**
	 * Callback to handle upload request of an BlobStore upload.
	 * 
	 * @param token - Challenge token created by {@link #getUploadUrl}, supplied as part of the URL in the callback.
	 * @param request - Request data.
	 * @param imageId - String identifier for the uploaded image. Used to check restrictions.
	 * @return List of image data.
	 */
	public Collection<ImageUploadDTO> parseUploadRequest(String token, HttpServletRequest request, String imageId) {
		Account account = accountRepo.getByProperty("uploadToken.token", token);
		if(account == null)
			throw new NotFoundException("upload token not found");
		
		if(hasTokenExpired(account.getUploadToken())) {
			account.setUploadToken(null);
			accountRepo.saveOrUpdate(account);
			throw new ServiceException("upload token expired");
		}
		
		Map<String, List<BlobKey>> uploads = blobStoreService.getUploads(request);
		
		List<ImageUploadDTO> images = new ArrayList<ImageUploadDTO>();
		if(account.getImageUploads() == null) {
			account.setImageUploads(new ArrayList<ImageUploadDTO>());
		}
		for (String key : uploads.keySet()) {
			for(BlobKey blobKey : uploads.get(key)) {
				ImageUploadDTO image = new ImageUploadDTO();
				BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);				
				if(!isValidContentType(blobInfo.getContentType())) {
					blobStoreService.delete(blobKey);
					throw new ValidationException("Upload has invalid mime type", "fileupload.error.type");
				}
				else if(blobInfo.getSize() > MAX_UPLOADSIZE) {
					blobStoreService.delete(blobKey);
					throw new ValidationException("Upload size too big. Limit is " + MAX_UPLOADSIZE, "fileupload.error.size");					
				}
				else {
					// Add to uploaded images for this account.
					// Generate public URL.
					image.setName(key);
					image.setBlobKey(blobKey.getKeyString());
					image.setUrl(imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(blobKey)));
					images.add(image);
				}
			}
		}
		
		account.getImageUploads().addAll(images);
		accountRepo.saveOrUpdate(account);
				
		return images;
	}
	
	private boolean isValidContentType(String contentType) {
		if(contentType.equals("image/png") || contentType.equals("image/jpeg") || contentType.equals("image/gif")) {
			return true;
		}
		return false;
	}

	/**
	 * Delete an unused uploaded image.
	 * 
	 * @param account - The account that uploaded the image.
	 * @param blobKey - Identifying the blob that was uploaded.
	 */
	public void deleteUpload(Account account, String blobKey) {
		checkNotNull(account, "account was null");
		checkArgument(!Strings.isNullOrEmpty(blobKey), "blobKey was null or empty");
		
		if(account.getImageUploads() == null || account.getImageUploads().isEmpty()) {
			logger.warn("Received delete for image upload, but no uploads found for account {}", account.getLogin());
			return;
		}
		else {
			for (Iterator<ImageUploadDTO> iterator = account.getImageUploads().iterator(); iterator.hasNext();) {
				ImageUploadDTO upload = iterator.next();
				if(upload.getBlobKey().equals(blobKey)) {
					// Found the corresponding upload, delete the blob and the object.
					iterator.remove();
					blobStoreService.delete(new BlobKey(blobKey));
					accountRepo.saveOrUpdate(account);
				}
			}	
		}
	}
	
	public ImageUploadDTO cropUpload(Account account, String blobKeyString, ImageCropDTO cropData) {
		checkNotNull(account, "account was null");
		checkArgument(!Strings.isNullOrEmpty(blobKeyString), "blobKeyString was null or empty");
		
		ImageUploadDTO imageDto = null;
		
		for (ImageUploadDTO upload :account.getImageUploads()) {
			if(upload.getBlobKey().equals(blobKeyString)) {
				imageDto = upload;
			}
		}
		
		if(imageDto == null) {
			logger.error("Could not find blob under uploads for this account, aborting crop operation.");
			throw new ValidationException("Unknown blobkey specified");
		}
		
		//Retrieve info about this blob. For mime type check.
		BlobKey blobKey = new BlobKey(blobKeyString);
		BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
		OutputEncoding outputEnc;
		String outputType;
		if(blobInfo.getContentType().equals("image/png")) {
			outputEnc = OutputEncoding.PNG;
			outputType = blobInfo.getContentType(); 
		} else if (blobInfo.getContentType().equals("image/jpeg")) {
			outputEnc = OutputEncoding.JPEG;
			outputType = blobInfo.getContentType();
		}
		else if (blobInfo.getContentType().equals("image/gif")) {
			outputEnc = OutputEncoding.PNG;
			outputType = "image/png";
		}
		else {
			throw new ValidationException("Uploaded file has incorrect mime type.");
		}
		
		Image image = imageController.cropImage(blobKeyString, outputEnc, cropData.getLeftX(), cropData.getTopY(), cropData.getRightX(), cropData.getBottomY());
		
		BlobKey newBlobKey = fileService.saveNewBlob(imageDto.getName(), outputType, image.getImageData());
		
		// Delete old blob.
		blobStoreService.delete(blobKey);
		
		imageDto.setBlobKey(newBlobKey.getKeyString());
		imageDto.setUrl(imagesService.getServingUrl(ServingUrlOptions.Builder.withBlobKey(newBlobKey)));
		
		accountRepo.saveOrUpdate(account);
				
		return imageDto;		
	}
	
	/**
	 * Check if the supplied token has expired.
	 * 
	 * @param token
	 * @return <code>true</code> if the creation date is older than the timeout,<br>
	 * 		<code>false</code> otherwise.
	 */
	public boolean hasTokenExpired(UploadToken token) {
		Calendar calendar = Calendar.getInstance();
		Integer timeout;
		try {
			timeout = Integer.valueOf(System.getProperty("net.karazy.uploads.token.timeout"));
		} catch (NumberFormatException e) {
			timeout = 120;
		}
		calendar.add(Calendar.MINUTE, -timeout);
		
		if(token.getCreation().before(calendar.getTime())) {
			return true;
		}
		else
			return false;
	}
}
