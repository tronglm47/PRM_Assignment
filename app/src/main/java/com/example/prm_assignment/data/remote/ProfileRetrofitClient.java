package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ProfileRetrofitClient {
    private static ProfileRetrofitClient instance;
    private final Retrofit retrofit;

    private ProfileRetrofitClient() {
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

    public static synchronized ProfileRetrofitClient getInstance() {
        if (instance == null) {
            instance = new ProfileRetrofitClient();
        }
        return instance;
    }

    public ProfileApi getProfileApi() {
        return retrofit.create(ProfileApi.class);
    }
}

