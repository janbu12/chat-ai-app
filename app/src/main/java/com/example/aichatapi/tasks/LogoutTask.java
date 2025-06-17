package com.example.aichatapi.tasks; // Sesuaikan dengan package Anda

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.example.aichatapi.models.LogoutResponse; // Import LogoutResponse yang baru
import com.example.aichatapi.models.ErrorResponse; // Tetap pakai ErrorResponse untuk fallback

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// Anda bisa menggunakan UnsafeOkHttpClient jika masih di development
import com.example.aichatapi.utils.UnsafeOkHttpClient;
import com.example.aichatapi.utils.AuthInterceptor; // Import interceptor

public class LogoutTask extends AsyncTask<Void, Void, LogoutResponse> { // Mengembalikan LogoutResponse

    private static final String TAG = "LogoutTask";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface LogoutCallback {
        void onLogoutSuccess(LogoutResponse response); // Callback menerima LogoutResponse
        void onLogoutFailure(String errorMessage, int httpCode);
    }

    private LogoutCallback callback;
    private String baseUrl;
    private Context context; // Perlu context untuk AuthInterceptor
    private OkHttpClient client;
    private Gson gson; // Inisialisasi Gson di sini

    public LogoutTask(String baseUrl, LogoutCallback callback, Context context) {
        this.baseUrl = baseUrl;
        this.callback = callback;
        this.context = context;
        this.gson = new Gson(); // Inisialisasi Gson

        // Inisialisasi OkHttpClient dengan AuthInterceptor
        // OkHttpClient.Builder builder = new OkHttpClient.Builder(); // Untuk produksi
        OkHttpClient.Builder builder = UnsafeOkHttpClient.getUnsafeOkHttpClient().newBuilder(); // HANYA UNTUK DEVELOPMENT!
        builder.addInterceptor(new AuthInterceptor(context)); // AuthInterceptor akan menambahkan token
        this.client = builder.build();
    }

    @Override
    protected LogoutResponse doInBackground(Void... voids) {
        String logoutApiUrl = baseUrl + "/logout"; // Endpoint logout Anda

        // Body request kosong karena tidak ada data yang perlu dikirim selain token di header
        RequestBody emptyBody = RequestBody.create("{}", JSON); // Umumnya POST request memiliki body, meskipun kosong

        Request request = new Request.Builder()
                .url(logoutApiUrl)
                .post(emptyBody) // Menggunakan emptyBody
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBodyString = response.body().string();
            Log.d(TAG, "Logout API Response Code: " + response.code());
            Log.d(TAG, "Logout API Response Body: " + responseBodyString);

            if (response.isSuccessful()) {
                // Jika sukses (kode 2xx), parse respons ke LogoutResponse
                return gson.fromJson(responseBodyString, LogoutResponse.class);
            } else {
                // Jika gagal (kode 4xx atau 5xx), coba parse ke ErrorResponse
                try {
                    ErrorResponse errorResponse = gson.fromJson(responseBodyString, ErrorResponse.class);
                    // Buat LogoutResponse untuk membawa pesan error dan HTTP code
                    return new LogoutResponse(false, errorResponse.getMessage() + " (HTTP:" + response.code() + ")");
                } catch (JsonSyntaxException e) {
                    // Jika respons error bukan JSON yang diharapkan
                    Log.e(TAG, "Error parsing error response for logout: " + e.getMessage());
                    return new LogoutResponse(false, "Unknown error from server (HTTP:" + response.code() + ")");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Network or API call error during logout: " + e.getMessage(), e);
            // Untuk error jaringan, kembalikan LogoutResponse dengan pesan error jaringan
            return new LogoutResponse(false, "Network error: " + e.getMessage());
        }
    }

    @Override
    protected void onPostExecute(LogoutResponse result) {
        if (callback != null) {
            if (result.isSuccess()) {
                callback.onLogoutSuccess(result); // Teruskan LogoutResponse
            } else {
                int httpCode = -1;
                String errorMessage = result.getMessage();
                // Ekstrak HTTP code dari message jika ada
                if (errorMessage.contains("(HTTP:")) {
                    try {
                        String codeStr = errorMessage.substring(errorMessage.indexOf("(HTTP:") + 6, errorMessage.lastIndexOf(")"));
                        httpCode = Integer.parseInt(codeStr);
                        errorMessage = errorMessage.substring(0, errorMessage.indexOf("(HTTP:")).trim();
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse HTTP code from message: " + e.getMessage());
                    }
                }
                callback.onLogoutFailure(errorMessage, httpCode);
            }
        }
    }
}
