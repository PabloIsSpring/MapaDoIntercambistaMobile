package com.example.mapadointercambista.network;

import android.content.Context;

import com.example.mapadointercambista.model.user.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        SessionManager sessionManager = new SessionManager(context);
        Request originalRequest = chain.request();

        if (!sessionManager.isModoApi()) {
            return chain.proceed(originalRequest);
        }

        String token = sessionManager.getToken();

        if (token == null || token.trim().isEmpty() || !sessionManager.isTokenValido()) {
            sessionManager.logout();
            return chain.proceed(originalRequest);
        }

        Request newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token.trim())
                .build();

        return chain.proceed(newRequest);
    }
}