package net.eatsense.restws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
//import com.google.zxing.web.BitMatrix;
//import com.google.zxing.web.ChartServletRequestParameters;
//import com.google.zxing.web.EncodeHintType;
//import com.google.zxing.web.QRCodeWriter;
//import com.google.zxing.web.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

@Path("barcodes")
public class BarcodeResource {
	
	@Context
	private HttpServletResponse response;
	
	@GET
	@Produces("image/png")
	public Response generateBarcode() throws IOException {
		
//		 ChartServletRequestParameters parameters;
//		    try {
//		      parameters = doParseParameters(request, isPost);
//		    } catch (IllegalArgumentException | NullPointerException e) {
//		      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.toString());
//		      Response.serverError().build();
//		    }

		    
//		    hints.put(EncodeHintType.MARGIN, parameters.getMargin());
//		    if (!StandardCharsets.ISO_8859_1.equals(parameters.getOutputEncoding())) {
		      // Only set if not QR code default
//		      hints.put(EncodeHintType.CHARACTER_SET, parameters.getOutputEncoding().name());
//		    }
//		    hints.put(EncodeHintType.ERROR_CORRECTION, parameters.getEcLevel());
			BitMatrix matrix = null;
		
		
			Map<EncodeHintType,Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
			
		    
		    try {
		      matrix = new QRCodeWriter().encode("cloobster",
		                                         BarcodeFormat.QR_CODE,
		                                         100,
		                                         100,
		                                         hints);
		    } catch (WriterException we) {
		      response.sendError(HttpServletResponse.SC_BAD_REQUEST, we.toString());
		      Response.serverError().build();
		    }

		    ByteArrayOutputStream pngOut = new ByteArrayOutputStream();
		    MatrixToImageWriter.writeToStream(matrix, "PNG", pngOut);
		    
		    byte[] pngData = pngOut.toByteArray();

		    response.setContentType("image/png");
		    response.setContentLength(pngData.length);
		    response.setHeader("Cache-Control", "public");
//		    response.getOutputStream().write(pngData);
		    
		    return Response.ok(pngData).build();
	}

}
