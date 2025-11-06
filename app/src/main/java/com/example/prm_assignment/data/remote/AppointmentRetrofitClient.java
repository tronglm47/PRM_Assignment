package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppointmentRetrofitClient {
    private static AppointmentRetrofitClient instance;
    private final Retrofit retrofit;

    private AppointmentRetrofitClient() {
        String baseUrl = BuildConfig.BASE_URL;
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        // Logging interceptor (giúp debug request/response dễ hơn)
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // OkHttpClient cấu hình timeout và logging
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Retrofit builder
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized AppointmentRetrofitClient getInstance() {
        if (instance == null) {
            instance = new AppointmentRetrofitClient();
        }
        return instance;
    }

    public AppointmentApi getAppointmentApi() {
        return retrofit.create(AppointmentApi.class);
    }
}
