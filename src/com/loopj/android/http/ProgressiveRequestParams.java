package com.loopj.android.http;

import org.apache.http.HttpEntity;

public class ProgressiveRequestParams extends RequestParams {
	
	private TransmissionProgress progress_callback = null;
	
	public TransmissionProgress getProgressCallback() {
		return progress_callback;
	}
	
	public void setProgressCallback(TransmissionProgress progress_callback) {
		this.progress_callback = progress_callback;
	}
	
	@Override
	public HttpEntity getEntity() {
		HttpEntity entity = super.getEntity();
		if (null != progress_callback)
			entity = new ProgressiveSimpleMultipartEntity(entity, progress_callback);
		
		return entity;
	}

}
