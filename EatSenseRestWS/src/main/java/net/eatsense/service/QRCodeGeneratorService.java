package net.eatsense.service;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.exceptions.ServiceException;

import com.google.common.base.Strings;

public class QRCodeGeneratorService {
	public final static String SERVICE_URL = "https://chart.googleapis.com/chart";
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final UriBuilder serviceUriBuilder;
	
	public QRCodeGeneratorService() {
		serviceUriBuilder = UriBuilder.fromUri(SERVICE_URL);
		serviceUriBuilder.queryParam("cht", "qr");
	}
	
	public URI buildUri(String text, int width, int height) {
		checkArgument(width > 50, "width must be greater than 50");
		checkArgument(height > 50, "height must be greater than 50");
		checkNotNull(Strings.emptyToNull(text), "text was null or empty");
		
		serviceUriBuilder.replaceQueryParam("chs", String.format("%dx%d", width, height))
						 .replaceQueryParam("chl", text);
		
		return serviceUriBuilder.build();
	}
	
	public InputStream loadQRImageAsStream(String text, int width, int height) {
		URL url;
		try {
			url = buildUri(text, width, height).toURL();
		} catch (MalformedURLException e) {
			logger.error("Error while building QR code image url");
			throw new ServiceException("Error generating QR code image.");
		}
		
		try {
			return url.openStream();
		} catch (IOException e) {
			logger.error("Error while loading QR code from external service");
			throw new ServiceException("Error generating QR code image");
		}
	}
}
