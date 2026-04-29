package com.example.memoriva.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MemorivaApiService {

    @GET("users/search")
    Call<ApiResponse<List<UserSearchResponse>>> searchUsers(
            @Query("username") String username,
            @Header("Authorization") String token
    );

    @POST("friends/request")
    Call<ApiResponse<Void>> sendFriendRequest(
            @Body FriendRequestBody body,
            @Header("Authorization") String token
    );

    @GET("geocoding/forward")
    Call<ApiResponse<List<PlaceResponse>>> forwardGeocode(
            @Query("query") String query
    );

    @GET("geocoding/reverse")
    Call<ApiResponse<PlaceResponse>> reverseGeocode(
            @Query("lat") double lat,
            @Query("lng") double lng
    );

    @GET("feed")
    Call<ApiResponse<List<FeedItem>>> getFeed(
            @Query("userId") int userId,
            @Header("Authorization") String token
    );
}
