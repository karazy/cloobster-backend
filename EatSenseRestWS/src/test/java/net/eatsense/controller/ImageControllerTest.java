package net.eatsense.controller;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import net.eatsense.controller.ImageController.UpdateImagesResult;
import net.eatsense.persistence.AccountRepository;
import net.eatsense.representation.ImageDTO;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.images.ImagesService;

@RunWith(MockitoJUnitRunner.class)
public class ImageControllerTest {
	
	ImageController ctrl;
	@Mock
	private BlobstoreService blobstoreService;
	@Mock
	private ImagesService imagesService;
	@Mock
	private AccountRepository accountRepo;
	

	@Before
	public void setUp() throws Exception {
		ctrl = new ImageController(blobstoreService, imagesService, accountRepo);
	}

	@Test
	public void testRemoveImage() {
		String imageId = "test";
		List<ImageDTO> images = getTestImageList();
		
		UpdateImagesResult result = ctrl.removeImage(imageId , images);
		
		BlobKey blobKey = new BlobKey(result.getUpdatedImage().getBlobKey());
		verify(blobstoreService).delete(blobKey);
		verify(imagesService).deleteServingUrl(blobKey);
				
		assertThat(result.isDirty(), is(true));
		assertThat(images, not(hasItem(result.getUpdatedImage())));
	}

	private List<ImageDTO> getTestImageList() {
		
		
		List<ImageDTO> list = new ArrayList<ImageDTO>();
		ImageDTO image = new ImageDTO();
		image.setId("test");
		image.setBlobKey("testBlob1");
		image.setUrl("testurl");
		list.add(image);
		ImageDTO image2 = new ImageDTO();
		image2.setId("test2");
		image2.setBlobKey("testBlob2");
		image2.setUrl("anothertesturl");
		list.add(image2);
		
		return list ;
	}
}
