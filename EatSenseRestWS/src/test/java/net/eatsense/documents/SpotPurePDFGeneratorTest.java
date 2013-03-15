package net.eatsense.documents;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.eatsense.configuration.Configuration;
import net.eatsense.configuration.SpotPurePDFConfiguration;
import net.eatsense.documents.DocumentGeneratorFactory;
import net.eatsense.documents.SpotPurePDFGenerator;
import net.eatsense.domain.Business;
import net.eatsense.domain.Document;
import net.eatsense.domain.Spot;
import net.eatsense.persistence.SpotRepository;
import net.eatsense.service.QRCodeGeneratorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.googlecode.objectify.Key;
import com.pdfjet.CoreFont;
import com.pdfjet.Font;
import com.pdfjet.Image;
import com.pdfjet.ImageType;
import com.pdfjet.PDF;
import com.pdfjet.Page;
import com.pdfjet.TextLine;

@RunWith(MockitoJUnitRunner.class)
public class SpotPurePDFGeneratorTest {

	private SpotPurePDFGenerator ctrl;
	@Mock
	private Configuration config;
	@Mock
	private QRCodeGeneratorService qrImageService;
	@Mock
	private SpotRepository spotRepo;
	@Mock
	private Key<Business> businessKey;
	private SpotPurePDFConfiguration spotPurePDFConfig;

	@Before
	public void setUp() throws Exception {
		spotPurePDFConfig = Configuration.getDefaultSpotPurePdfConfiguration();
		when(config.getSpotPurePdfConfiguration()).thenReturn(spotPurePDFConfig);
		
		ctrl =  spy(new SpotPurePDFGenerator(config, qrImageService, spotRepo));
	}
	
	@Test
	public void testGenerate() throws Exception {
		
		Document doc = mock(Document.class);
		when(doc.getEntity()).thenReturn(Spot.class.getName());
		when(doc.getRepresentation()).thenReturn("pure");
		when(doc.getBusiness()).thenReturn(businessKey);
		String docName = "Test Document";
		when(doc.getName()).thenReturn(docName);
		List<Long> entityIds = Arrays.asList(1l,2l,3l);
		when(doc.getEntityIds()).thenReturn(entityIds );
		
		List<Key<Spot>> keyList = new ArrayList<Key<Spot>>();
		when(spotRepo.getKeys(businessKey, entityIds)).thenReturn(keyList );
		Spot spot = mock(Spot.class);
		Collection<Spot> spots = Arrays.asList(spot,spot,spot );
		when(spotRepo.getByKeys(keyList)).thenReturn(spots);
		String barcode = "test";
		String spotName = "Spot";

		when(spot.getBarcodeWithDownloadURL()).thenReturn(barcode);
		when(spot.getName()).thenReturn(spotName);
		InputStream imageStream = mock(InputStream.class);
		Image image = mock(Image.class);
		PDF pdf = mock(PDF.class);
		Page page = mock(Page.class);
		doReturn(page ).when(ctrl).makePage(pdf, new double[]{ spotPurePDFConfig.getPageWidth(), spotPurePDFConfig.getPageHeight()});
		doReturn(pdf).when(ctrl).makePDF(any(OutputStream.class));
		Font font = mock(Font.class);
		doReturn(font).when(ctrl).makeFont(pdf, CoreFont.HELVETICA);
		doReturn(image).when(ctrl).makeImage(pdf , imageStream, ImageType.PNG);
		TextLine textLine = mock(TextLine.class);
		doReturn(textLine ).when(ctrl).makeTextLine(font, spotName);
		
		when(qrImageService.loadQRImageAsStream(barcode , spotPurePDFConfig.getQrImageDPI(), spotPurePDFConfig.getQrImageDPI())).thenReturn(imageStream );
		
		ctrl.generate(doc );
		
		verify(font).setSize(spotPurePDFConfig.getFontSize());
		verify(pdf).setTitle(docName);
		verify(image, times(3)).setPosition(spotPurePDFConfig.getBarcodePositionX(), spotPurePDFConfig.getBarcodePositionY());
		verify(image, times(3)).scaleBy(72.0 / spotPurePDFConfig.getQrImageDPI());
		verify(image, times(3)).drawOn(page);
		
		verify(textLine, times(3)).setPosition(spotPurePDFConfig.getTextPositionX(), spotPurePDFConfig.getTextPositionY());
		verify(textLine, times(3)).drawOn(page);
		
		verify(pdf).flush();
	}
}
