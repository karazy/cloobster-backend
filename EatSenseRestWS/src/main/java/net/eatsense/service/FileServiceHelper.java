package net.eatsense.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.eatsense.exceptions.ServiceException;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.LockException;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProcessor;

/**
 * Contains methods for Accessing the Appengine File API.
 * 
 * @author Nils Weiher
 *
 */
public class FileServiceHelper {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private final com.google.appengine.api.files.FileService fileService;
	private final BlobstoreService blobService;
	private final BlobInfoFactory blobInfoFact;
	
	@Inject
	public FileServiceHelper(FileService fileService, BlobstoreService blobService, BlobInfoFactory blobInfoFact) {
		this.fileService = fileService;
		this.blobService = blobService;
		this.blobInfoFact = blobInfoFact;
	}
	
	/**
	 * Remove a File from the BlobStore
	 * 
	 * @param blobKey
	 */
	public void delete(BlobKey blobKey) {
		blobService.delete(blobKey);
	}
	
	/**
	 * Create a new Blob in the BlobStore and write the supplied bytes to it.
	 * 
	 * @param name indentifier for this file
	 * @param mimeType MIME of the data in the byte array
	 * @param bytes to write to the blobstore File
	 * @return BlobKey for the newly created blob.
	 */
	public BlobKey saveNewBlob(String name, String mimeType, byte[] bytes) {
		AppEngineFile file; 
		
		try {
			file = fileService.createNewBlobFile(mimeType, name);
		} catch (IOException e) {
			logger.error("Error while communicating with blobstore, could not create new file");
			throw new ServiceException("Could not save to blobstore", e);
		}
		// Start writing the new file.
		FileWriteChannel writeChannel;
		try {
			writeChannel = fileService.openWriteChannel(file, true);
		} catch (FileNotFoundException e) {
			logger.error("Could not find created file for writing",e);
			throw new ServiceException(e);
		} catch (FinalizationException e) {
			logger.error("Could not write to newly created file, already finalized!",e);
			throw new ServiceException(e);
		} catch (LockException e) {
			logger.error("Could not write to newly created file, already locked!",e);
			throw new ServiceException(e);
		} catch (IOException e) {
			logger.error("Could not write to newly created file, unknown problem.",e);
			throw new ServiceException("Error while writing to blobstore file.", e);
		}
		
		int byteCount;
		try {
			byteCount = writeChannel.write(ByteBuffer.wrap(bytes));
		} catch (IOException e) {
			logger.error("Exception while writing to blob",e);
			throw new ServiceException("Error while writing to blobstore file.", e);
		}
		
		try {
			writeChannel.closeFinally();
		} catch (Exception e) {
			logger.error("Unable to finalize file",e);
			throw new ServiceException("Error while finallizing file.", e);
		}
		
		BlobKey blobKey = fileService.getBlobKey(file);
		
		logger.info("Written {} bytes to blob: {}", byteCount, blobKey);
		
		return blobKey;
	}
	
	/**
	 * Completely load a blob file into memory.
	 * 
	 * @param blobKey
	 * @return all bytes of this blob
	 * @throws FileNotFoundException
	 * @throws LockException
	 * @throws IOException
	 */
	public byte[] readBlob(BlobKey blobKey) throws FileNotFoundException, LockException, IOException {
		AppEngineFile blobFile = fileService.getBlobFile(blobKey);
		FileReadChannel readChannel = fileService.openReadChannel(blobFile, false);
		
		byte[] bytes = ByteStreams.toByteArray(Channels.newInputStream(readChannel));
		readChannel.close();
		return bytes;
	}
	
	/**
	 * Load an existing blob file and create a new file with the same content.
	 * 
	 * @param originalBlobKey
	 * @return blobKey identifying the copy of the blob
	 */
	public BlobKey copyBlob( BlobKey originalBlobKey) {
		checkNotNull(originalBlobKey, "originalBlobKey was null");
		BlobInfo originalBlobInfo = blobInfoFact.loadBlobInfo(originalBlobKey);
		byte[] bytes;
		
		//TODO write test
		
		try {
			bytes = readBlob(originalBlobInfo.getBlobKey());
		} catch (FileNotFoundException e) {
			logger.error("Could not find original blob for reading",e);
			throw new ServiceException(e);
		} catch (LockException e) {
			logger.error("Could not read from original blob, already locked!",e);
			throw new ServiceException(e);
		} catch (IOException e) {
			logger.error("Could not read from original blob, unknown problem.",e);
			throw new ServiceException("Error while reading blobstore file.", e);
		}
		
		return saveNewBlob(originalBlobInfo.getFilename(), originalBlobInfo.getContentType(), bytes);
	}
}
