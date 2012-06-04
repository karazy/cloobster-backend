package net.eatsense.controller;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.eatsense.domain.Account;
import net.eatsense.representation.ImageUploadDTO;
import net.eatsense.util.IdHelper;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class UploadController {
	private BlobstoreService blobStoreService;

	@Inject
	public UploadController(BlobstoreService blobStoreService) {
		super();
		this.blobStoreService = blobStoreService;
	}
	
	public String getUploadUrl(Account account, String successUrl) {
		checkNotNull(account, "account was null");
		checkArgument(!Strings.nullToEmpty(successUrl).isEmpty(), "successUrl was null or empty");
		String token = IdHelper.generateId();
		
		
		return blobStoreService.createUploadUrl(successUrl);
	}

	public Collection<ImageUploadDTO> parseUploadRequest(HttpServletRequest request) {
		Map<String, List<BlobKey>> uploads = blobStoreService.getUploads(request);
		
		return null;
	}
}
