package com.loopj.android.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

public class ProgressiveSimpleMultipartEntity implements HttpEntity {
	
	private HttpEntity entity = null;
	private TransmissionProgress progress_callback = null;
	
	public ProgressiveSimpleMultipartEntity(HttpEntity entity, TransmissionProgress progress_callback) {
		this.entity = entity;
		this.progress_callback = progress_callback;
	}

	@Override
	public void consumeContent() throws IOException {
		// TODO Auto-generated method stub
		entity.consumeContent();
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		// TODO Auto-generated method stub
		return entity.getContent();
	}

	@Override
	public Header getContentEncoding() {
		// TODO Auto-generated method stub
		return entity.getContentEncoding();
	}

	@Override
	public long getContentLength() {
		// TODO Auto-generated method stub
		return entity.getContentLength();
	}

	@Override
	public Header getContentType() {
		// TODO Auto-generated method stub
		return entity.getContentType();
	}

	@Override
	public boolean isChunked() {
		// TODO Auto-generated method stub
		return entity.isChunked();
	}

	@Override
	public boolean isRepeatable() {
		// TODO Auto-generated method stub
		return entity.isRepeatable();
	}

	@Override
	public boolean isStreaming() {
		// TODO Auto-generated method stub
		return entity.isStreaming();
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		// TODO Auto-generated method stub
		int total_size = (int) entity.getContentLength();
		int transferred_size = 0;
		
		byte[] buf = new byte[4096];
		int l = 0;
		
		InputStream instream = entity.getContent();

		while ((l = instream.read(buf)) != -1) {
			outstream.write(buf, 0, l);
			transferred_size = transferred_size + l;
			if (progress_callback != null)
				try {
					progress_callback.on_progress(total_size , transferred_size);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
		}
	}
}
