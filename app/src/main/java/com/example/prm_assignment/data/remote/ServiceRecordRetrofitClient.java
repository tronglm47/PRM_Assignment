package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceRecordRetrofitClient {
    private static ServiceRecordRetrofitClient instance;
    private final Retrofit retrofit;

    private ServiceRecordRetrofitClient() {
        String baseUrl = BuildConfig.BASE_URL;
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized ServiceRecordRetrofitClient getInstance() {
        if (instance == null) {
            instance = new ServiceRecordRetrofitClient();
        }
        return instance;
    }

    public ServiceRecordApi getServiceRecordApi() {
        return retrofit.create(ServiceRecordApi.class);
    }

    public ServiceChecklistApi getServiceChecklistApi() {
        return retrofit.create(ServiceChecklistApi.class);
    }
}

