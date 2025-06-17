package com.example.aichatapi.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull; // Gunakan androidx untuk kompatibilitas lebih luas
import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        // Mendapatkan Access Token dari SharedPreferences
        SharedPreferences sharedPref = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String accessToken = sharedPref.getString("token", null);

        // Membangun request baru dari request asli
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();

        // Jika Access Token ada dan tidak kosong, tambahkan ke header Authorization
        if (accessToken != null && !accessToken.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + accessToken);
        }

        // Lanjutkan request dengan header yang sudah ditambahkan (jika ada)
        return chain.proceed(requestBuilder.build());
    }
}
