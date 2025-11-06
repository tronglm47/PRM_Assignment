package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class CenterRetrofitClient {
    private static CenterRetrofitClient instance;
    private final Retrofit retrofit;

    private CenterRetrofitClient() {
        String baseUrl = BuildConfig.BASE_URL;
        if (!baseUrl.endsWith("/")) baseUrl += "/";

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized CenterRetrofitClient getInstance() {
        if (instance == null) instance = new CenterRetrofitClient();
        return instance;
    }

    public CenterApi getCenterApi() {
        return retrofit.create(CenterApi.class);
    }
}
