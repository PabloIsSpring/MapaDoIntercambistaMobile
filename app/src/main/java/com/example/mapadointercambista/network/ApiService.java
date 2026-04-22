package com.example.mapadointercambista.network;

import com.example.mapadointercambista.dto.request.AgenciaUpdateRequestDto;
import com.example.mapadointercambista.dto.request.CreateAgenciaRequestDto;
import com.example.mapadointercambista.dto.request.IntercambistaUpdtRequestDto;
import com.example.mapadointercambista.dto.request.LoginRequestDto;
import com.example.mapadointercambista.dto.request.RegisterUserRequestDto;
import com.example.mapadointercambista.dto.response.AgenciaResponseDto;
import com.example.mapadointercambista.dto.response.IntercambistaResponseDto;
import com.example.mapadointercambista.dto.response.LoginResponseDto;
import com.example.mapadointercambista.dto.response.RegisterUserResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    @POST("auth/login")
    Call<LoginResponseDto> login(@Body LoginRequestDto request);

    @POST("auth/register/intercambista")
    Call<RegisterUserResponseDto> registerIntercambista(@Body RegisterUserRequestDto request);

    @POST("auth/register/agencia")
    Call<AgenciaResponseDto> registerAgencia(@Body CreateAgenciaRequestDto request);

    @GET("intercambista/{username}")
    Call<IntercambistaResponseDto> getIntercambista(@Path("username") String username);

    @PUT("intercambista")
    Call<IntercambistaResponseDto> updateIntercambista(@Body IntercambistaUpdtRequestDto request);

    @GET("agencia/{username}")
    Call<AgenciaResponseDto> getAgencia(@Path("username") String username);

    @PUT("agencia")
    Call<AgenciaResponseDto> updateAgencia(@Body AgenciaUpdateRequestDto request);

    @DELETE("agencia/{username}")
    Call<Void> deleteAgencia(@Path("username") String username);
}