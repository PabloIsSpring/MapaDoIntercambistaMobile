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
        String token = sessionManager.getToken();

        Request originalRequest = chain.request();

        if (token != null && !token.trim().isEmpty() && sessionManager.isModoApi()) {
            Request newRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            return chain.proceed(newRequest);
        }

        return chain.proceed(originalRequest);
    }
}