package com.spnetgram.app.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spnetgram.app.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static volatile ApiClient instance;
    private final Retrofit retrofit;
    private final SPNetGramApiService apiService;

    private ApiClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG_LOGGING
            ? HttpLoggingInterceptor.Level.BODY
            : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor(chain -> {
                okhttp3.Request req = chain.request().newBuilder()
                    .addHeader("X-App-Name", "SP-NET-GRAM")
                    .addHeader("X-App-Version", BuildConfig.APP_VERSION)
                    .build();
                return chain.proceed(req);
            })
            .build();

        Gson gson = new GsonBuilder()
            .setLenient()
            .create();

        retrofit = new Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

        apiService = retrofit.create(SPNetGramApiService.class);
    }

    public static ApiClient getInstance() {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) instance = new ApiClient();
            }
        }
        return instance;
    }

    public SPNetGramApiService getApiService() { return apiService; }
}
