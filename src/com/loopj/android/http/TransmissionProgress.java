package com.loopj.android.http;

public interface TransmissionProgress {
	void on_progress(int total_size, int transfered_size);
}
