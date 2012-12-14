package net.eatsense.documents;



import static com.google.common.base.Preconditions.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.pdfjet.A4;
import com.pdfjet.A5;
import com.pdfjet.CoreFont;
import com.pdfjet.Font;
import com.pdfjet.Image;
import com.pdfjet.ImageType;
import com.pdfjet.PDF;
import com.pdfjet.Page;
import com.pdfjet.TextLine;

import net.eatsense.configuration.Configuration;
import net.eatsense.configuration.SpotPurePDFConfiguration;
import net.eatsense.domain.Document;
import net.eatsense.domain.Spot;
import net.eatsense.exceptions.ServiceException;
import net.eatsense.service.QRCodeGeneratorService;

public class SpotPurePDFGenerator extends AbstractDocumentGenerator<Spot>{
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final ByteArrayOutputStream byteOutput;
	private final SpotPurePDFConfiguration pdfConfig;
	private Font font;
	private final QRCodeGeneratorService qrImageService;
	
	@Inject
	public SpotPurePDFGenerator(Configuration config, QRCodeGeneratorService qrImageService) {
		this.qrImageService = qrImageService;
		byteOutput = new ByteArrayOutputStream();
		
		this.pdfConfig = config.getSpotPurePdfConfiguration();
	}

	
	@Override
	public String getMimeType() {
		return "application/pdf"; 
	}

	@Override
	public byte[] generate(Iterable<Spot> entities, Document document) {
		checkNotNull(entities, "entities was null");
		checkNotNull(document, "document was null");
		
		if(!entities.iterator().hasNext()) {
			logger.error("No Spot entities supplied for PDF generation, for Document with key={}", document.getKey());
			throw new ServiceException("Internal Error, no Spot entities supplied for PDF generation.");
		}
		
		PDF pdf;
		try {
			pdf = new PDF(byteOutput);
			font = new Font(pdf, CoreFont.HELVETICA);
		} catch (Exception e) {
			logger.error("Unable to create PDF or Font", e);
			throw new ServiceException("Internal error while initializing PDF generation.", e);
		}
		font.setSize(pdfConfig.getFontSize());
		pdf.setTitle(document.getName());
		pdf.setAuthor("Karazy GmbH");
		
		logger.info("Using Config: {}", pdfConfig);
		
		try {
			for (Spot spot : entities) {
				generatePage(pdf, spot);
			}
		} catch (Exception e) {
			logger.error("Error during Page generation for Document with key={}", document.getKey());
			throw new ServiceException("Internal Error while creating PDF page", e);
		}
		
		try {
			pdf.flush();
		} catch (Exception e) {
			logger.error("Error while flushing PDF output for Document with key={}", document.getKey());
			throw new ServiceException("Internal Error while generating PDF output", e);
		}
		
		return byteOutput.toByteArray();
	}
	
	/**
	 * Generate one PDF page with Data from the Spot entity.
	 * 
	 * @param pdf
	 * @param spot
	 * @return
	 * @throws Exception
	 */
	private Page generatePage(PDF pdf, Spot spot) throws Exception {
		double[] pageDimensions = {pdfConfig.getPageWidth(), pdfConfig.getPageHeight()};
		Page page = new Page(pdf, pageDimensions);
		
	    // Create QR code with 
	    InputStream qrImageStream = qrImageService.loadQRImageAsStream(spot.getBarcodeWithDownloadURL(), pdfConfig.getQrImageDPI(), pdfConfig.getQrImageDPI());
	    
		Image barcodeImage = new Image(pdf, qrImageStream , ImageType.PNG);
	    barcodeImage.setPosition(pdfConfig.getBarcodePositionX(), pdfConfig.getBarcodePositionY());
	    double imageScale = 72.0 / pdfConfig.getQrImageDPI();
		barcodeImage.scaleBy(imageScale );
	    barcodeImage.drawOn(page);
	    
		TextLine text = new TextLine(font, spot.getName());
	    text.setPosition(pdfConfig.getTextPositionX(), pdfConfig.getTextPositionY());
	    text.drawOn(page);
	    
		return page;
	}
}
