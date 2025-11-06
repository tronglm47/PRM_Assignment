package com.example.prm_assignment.data.remote;

import com.example.prm_assignment.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class VehicleSubscriptionRetrofitClient {
    private static VehicleSubscriptionRetrofitClient instance;
    private final VehicleSubscriptionApi vehicleSubscriptionApi;

    private VehicleSubscriptionRetrofitClient() {
        String BASE_URL = BuildConfig.BASE_URL;
        if (!BASE_URL.endsWith("/")) {
            BASE_URL = BASE_URL + "/";
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        vehicleSubscriptionApi = retrofit.create(VehicleSubscriptionApi.class);
    }

    public static synchronized VehicleSubscriptionRetrofitClient getInstance() {
        if (instance == null) {
            instance = new VehicleSubscriptionRetrofitClient();
        }
        return instance;
    }

    public VehicleSubscriptionApi getVehicleSubscriptionApi() {
        return vehicleSubscriptionApi;
    }
}

