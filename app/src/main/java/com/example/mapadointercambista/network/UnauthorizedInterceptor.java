package com.example.mapadointercambista.network;

import android.content.Context;

import com.example.mapadointercambista.model.user.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class UnauthorizedInterceptor implements Interceptor {

    private final Context context;

    public UnauthorizedInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        if (response.code() == 401 || response.code() == 403) {
            SessionManager sessionManager = new SessionManager(context);

            if (sessionManager.isModoApi()) {
                sessionManager.logout();
            }
        }

        return response;
    }
}