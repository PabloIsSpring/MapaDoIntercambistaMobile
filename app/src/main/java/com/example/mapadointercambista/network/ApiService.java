package com.example.mapadointercambista.network;

import com.example.mapadointercambista.dto.request.LoginRequestDto;
import com.example.mapadointercambista.dto.request.RegisterUserRequestDto;
import com.example.mapadointercambista.dto.response.LoginResponseDto;
import com.example.mapadointercambista.dto.response.RegisterUserResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("auth/login")
    Call<LoginResponseDto> login(@Body LoginRequestDto request);

    @POST("auth/registeruser")
    Call<RegisterUserResponseDto> registerUser(@Body RegisterUserRequestDto request);
}