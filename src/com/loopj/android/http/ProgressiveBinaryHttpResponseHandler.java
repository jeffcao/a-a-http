package com.loopj.android.http;

//import static com.loopj.android.http.BinaryHttpResponseHandler.mAllowedContentTypes;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;

import android.os.Message;
import android.util.Log;

public class ProgressiveBinaryHttpResponseHandler extends
		BinaryHttpResponseHandler {

	protected static final int PROGRESS_MESSAGE = 100;
	// private Object refObj = null;
	private static String[] defaultAllowedContentTypes = new String[] {
			"image/jpeg", "image/png", "image/gif", "audio/", "application/"};

	public ProgressiveBinaryHttpResponseHandler() {
		super(defaultAllowedContentTypes);
	}

	public ProgressiveBinaryHttpResponseHandler(String[] allowedContentTypes) {
		super(allowedContentTypes);
	}

	@Override
	void sendResponseMessage(HttpResponse response) {
		StatusLine status = response.getStatusLine();
		Header[] contentTypeHeaders = response.getHeaders("Content-Type");
		byte[] responseBody = null;
		if (contentTypeHeaders.length != 1) {
			// malformed/ambiguous HTTP Header, ABORT!
			sendFailureMessage(new HttpResponseException(
					status.getStatusCode(),
					"None, or more than one, Content-Type Header found!"),
					responseBody);
			return;
		}
		Header contentTypeHeader = contentTypeHeaders[0];
		String type_header_value = contentTypeHeader.getValue();
		boolean foundAllowedContentType = false;
		if (status.getStatusCode() < 300) {
			for (String anAllowedContentType : mAllowedContentTypes) {
				if (type_header_value.startsWith(anAllowedContentType)) {
					foundAllowedContentType = true;
				}
			}
			if (!foundAllowedContentType) {
				// Content-Type not in allowed list, ABORT!
				sendFailureMessage(
						new HttpResponseException(status.getStatusCode(),
								"Content-Type [" + type_header_value + "] not allowed!"), responseBody);
				return;
			}
		}

		try {
			// HttpEntity entity = null;
			// HttpEntity temp = response.getEntity();
			// if(temp != null) {
			// entity = new BufferedHttpEntity(temp);
			// }
			// responseBody = EntityUtils.toByteArray(entity);
			HttpEntity entity = response.getEntity();
			responseBody = toByteArray(entity, new TransmissionProgress() {

				@Override
				public void on_progress(int total_size, int transfered_size) {
					// TODO Auto-generated method stub
					sendMessage(obtainMessage(PROGRESS_MESSAGE, new Object[] {
							new Integer(total_size),
							new Integer(transfered_size) }));
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("ProgressiveBinaryHttpResponseHandler", "failed to retrieve data", e);
			sendFailureMessage(e, (byte[]) null);
		}

		if (status.getStatusCode() >= 300 || responseBody == null) {
			sendFailureMessage(new HttpResponseException(
					status.getStatusCode(), status.getReasonPhrase()),
					responseBody);
		} else {
			sendSuccessMessage(status.getStatusCode(), responseBody);
		}
	}

	@Override
	protected void handleMessage(Message msg) {
		Object[] response;
		switch (msg.what) {
		case PROGRESS_MESSAGE:
			response = (Object[]) msg.obj;
			handleProgressMessage(((Integer) response[0]).intValue(),
					((Integer) response[1]).intValue());
			break;
		default:
			super.handleMessage(msg);
			break;
		}

	}

	protected void handleProgressMessage(int total_size, int transferred_size) {
		onTransfer(total_size, transferred_size);
	}

	public void onTransfer(int total_size, int transferred_size) {
	}

	public static byte[] toByteArray(final HttpEntity entity,
			final TransmissionProgress progress_callback) throws IOException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = entity.getContent();
		if (instream == null) {
			return new byte[] {};
		}
		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					"HTTP entity too large to be buffered in memory");
		}

		int content_length = (int) entity.getContentLength();
		int read_size = 0;

		int i = content_length;
		if (i < 0) {
			i = 4096;
		}
		ByteArrayBuffer buffer = new ByteArrayBuffer(i);
		try {
			byte[] tmp = new byte[4096];
			int l;
			while ((l = instream.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
				read_size = read_size + l;
				if (progress_callback != null)
					try {
						progress_callback
								.on_progress(content_length, read_size);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
			}
		} finally {
			instream.close();
		}
		return buffer.toByteArray();
	}

}
