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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.domain.Account;
import net.eatsense.domain.embedded.UploadToken;
import net.eatsense.exceptions.NotFoundException;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.representation.ImageUploadDTO;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.images.ImagesService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class UploadController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private BlobstoreService blobStoreService;
	private final AccountRepository accountRepo;
	private final ImagesService imagesService;

	@Inject
	public UploadController(BlobstoreService blobStoreService, AccountRepository accountRepo, ImagesService imagesService) {
		super();
		this.blobStoreService = blobStoreService;
		this.accountRepo = accountRepo;
		this.imagesService = imagesService;
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
	 * @return List of image data.
	 */
	public Collection<ImageUploadDTO> parseUploadRequest(String token, HttpServletRequest request) {
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
				image.setName(key);
				image.setBlobKey(blobKey.getKeyString());
				image.setUrl(imagesService.getServingUrl(blobKey));
				images.add(image);
			}
		}
		account.getImageUploads().addAll(images);
				
		return images;
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
		
		if(account.getImageUploads() == null || account.getImageUploads().isEmpty())
			return;
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
