package com.saunvid.infilectassignment.remote;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ApiRequest {

    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    public static final String ERROR_CODE = "500";
    public static final String SUCCESS_STATUS_CODE = "200";
    public static final String ALREADY_EXISTS_STATUS_CODE = "409";


    public static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");
    public final static String TIMEOUT_ERROR = "TIMEOUT_ERROR";
    public final static String UNKNOWN_HOST_EXCEPTION = "UNKNOWN_HOST_EXCEPTION";
    public final static String JSON_PARSER_ERROR = "JSON_PARSER_ERROR";
    private static final String TAG = ApiRequest.class.getSimpleName();
    private OkHttpClient okHttpClient;
    private Context context;


    private final static String JSON_PARSER_ERROR_STRING = "Unable to parse response";

    public ApiRequest(Context context) {
        okHttpClient = getOkHttpClient();
        okHttpClient.dispatcher().setMaxRequestsPerHost(10);
        okHttpClient.dispatcher().setMaxRequests(10);
        this.context = context;
    }

    private OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(2, TimeUnit.MINUTES).writeTimeout(2, TimeUnit.MINUTES).readTimeout(2, TimeUnit.MINUTES).retryOnConnectionFailure(false);
        return builder.build();
    }

    public void get(String url, final ApiListener apiListener) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, final @NotNull IOException e) {
                    if (e instanceof SocketTimeoutException) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onError(ApiRequest.TIMEOUT_ERROR, "Something went wrong");
                            }
                        });
                    } else if (e instanceof UnknownHostException) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onError(ApiRequest.UNKNOWN_HOST_EXCEPTION, "Something went wrong");
                            }
                        });
                    } else {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onError(e.getMessage(), "Something went wrong");
                            }
                        });
                    }
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull final okhttp3.Response response) throws IOException {
                    try {
                        final String responseString = response.body().string();
                        Log.i("GET RESPONE", responseString);
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onSuccess(responseString);
                            }
                        });
                    } catch (final Exception e) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onError(JSON_PARSER_ERROR, JSON_PARSER_ERROR_STRING);
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            apiListener.onError(e.getMessage(), e.getMessage());
        }
    }

    public Call post(String url, String body, final ApiListener apiListener) {
        Call call = null;
        try {
            RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, body);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                    if (e instanceof SocketTimeoutException) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onError(ApiRequest.TIMEOUT_ERROR, "Something went wrong");
                            }
                        });
                    } else if (e instanceof UnknownHostException) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onError(ApiRequest.UNKNOWN_HOST_EXCEPTION, "Something went wrong");
                            }
                        });
                    } else {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onError(e.getMessage(), "Something went wrong");
                            }
                        });
                    }

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull final okhttp3.Response response) throws IOException {
                    try {
                        final String responseString = response.body().string();
                        Log.i("API REQUEST DATA", responseString);
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onSuccess(responseString);
                            }
                        });
                    } catch (ClassCastException c) {
                        c.getMessage();
                    } catch (NullPointerException n) {
                        n.getMessage();
                    } catch (ActivityNotFoundException ex) {
                        ex.getMessage();
                    } catch (final Exception e) {
                        Log.d(TAG, "onResponse: " + call.request().url());
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("ERROR", e.toString());
                                apiListener.onError(JSON_PARSER_ERROR, "Something went wrong on server!");
                            }
                        });
                    }

                }
            });

        } catch (Exception e) {
            apiListener.onError(e.getMessage(), e.getMessage());
        }
        return call;
    }

    public Call postBody(String url, RequestBody body, final ApiListener apiListener) {
        Call call = null;
        try {
            //RequestBody requestBody = RequestBody.create(body,Constant.JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                    Log.i("ERROR", e.toString());
                    if (e instanceof SocketTimeoutException) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onError(ApiRequest.TIMEOUT_ERROR, "Something went wrong");
                            }
                        });
                    } else if (e instanceof UnknownHostException) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onError(ApiRequest.UNKNOWN_HOST_EXCEPTION, "Something went wrong");
                            }
                        });
                    } else {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onError(e.getMessage(), "Something went wrong");
                            }
                        });
                    }

                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull final okhttp3.Response response) throws IOException {
                    try {
                        final String responseString = response.body().string();
                        Log.i("API REQUEST DATA", responseString);
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                apiListener.onSuccess(responseString);
                            }
                        });
                    } catch (final Exception e) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("ERROR", e.toString());
                                apiListener.onError(JSON_PARSER_ERROR, JSON_PARSER_ERROR_STRING);
                            }
                        });
                    }

                }
            });

        } catch (Exception e) {
            apiListener.onError(e.getMessage(), e.getMessage());
        }
        return call;
    }

}