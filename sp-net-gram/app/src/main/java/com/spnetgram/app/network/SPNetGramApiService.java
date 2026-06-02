package com.spnetgram.app.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.Map;

/**
 * SP NET GRAM backend API service.
 * Base URL: https://api.spnetgram.com
 */
public interface SPNetGramApiService {

    // Auth
    @POST("v1/auth/register-device")
    Call<Map<String, Object>> registerDevice(@Body Map<String, Object> body);

    @POST("v1/auth/refresh-token")
    Call<Map<String, Object>> refreshToken(@Body Map<String, Object> body);

    // User profile
    @GET("v1/users/{uid}")
    Call<Map<String, Object>> getUser(
        @Header("Authorization") String token,
        @Path("uid") String uid);

    @POST("v1/users/{uid}/update")
    Call<Map<String, Object>> updateUser(
        @Header("Authorization") String token,
        @Path("uid") String uid,
        @Body Map<String, Object> body);

    // Diamond subscription validation
    @POST("v1/premium/validate-purchase")
    Call<Map<String, Object>> validatePurchase(
        @Header("Authorization") String token,
        @Body Map<String, Object> body);

    @GET("v1/premium/status")
    Call<Map<String, Object>> getPremiumStatus(
        @Header("Authorization") String token);

    // SP Coins
    @GET("v1/coins/balance")
    Call<Map<String, Object>> getCoinsBalance(
        @Header("Authorization") String token);

    @POST("v1/coins/earn")
    Call<Map<String, Object>> earnCoins(
        @Header("Authorization") String token,
        @Body Map<String, Object> body);

    @POST("v1/coins/spend")
    Call<Map<String, Object>> spendCoins(
        @Header("Authorization") String token,
        @Body Map<String, Object> body);

    // Referrals
    @POST("v1/referrals/generate")
    Call<Map<String, Object>> generateReferralCode(
        @Header("Authorization") String token);

    @POST("v1/referrals/redeem")
    Call<Map<String, Object>> redeemReferralCode(
        @Header("Authorization") String token,
        @Body Map<String, Object> body);

    // Feature flags
    @GET("v1/config/feature-flags")
    Call<Map<String, Object>> getFeatureFlags(
        @Header("Authorization") String token,
        @Query("appVersion") String appVersion);

    // Announcements
    @GET("v1/announcements")
    Call<Map<String, Object>> getAnnouncements(
        @Header("Authorization") String token);

    // Analytics (only called when user has consented)
    @POST("v1/analytics/event")
    Call<Void> logAnalyticsEvent(
        @Header("Authorization") String token,
        @Body Map<String, Object> body);

    // Health check
    @GET("v1/health")
    Call<Map<String, Object>> healthCheck();
}
