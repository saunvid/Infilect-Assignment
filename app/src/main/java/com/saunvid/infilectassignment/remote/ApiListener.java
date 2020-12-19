package com.saunvid.infilectassignment.remote;

public interface ApiListener {
    void onSuccess(String response);
    void onError(String ERROR, String errorMessage);
}
