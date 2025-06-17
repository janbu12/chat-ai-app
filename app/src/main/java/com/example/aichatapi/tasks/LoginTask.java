package com.example.aichatapi.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.aichatapi.utils.UnsafeOkHttpClient;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.example.aichatapi.models.LoginResponse;
import com.example.aichatapi.models.ErrorResponse;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginTask extends AsyncTask<String, Void, LoginResponse> {

    private static final String TAG = "LoginTask";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // Callback interface untuk mengembalikan hasil ke Activity
    public interface LoginCallback {
        void onLoginSuccess(LoginResponse response);
        void onLoginFailure(String errorMessage, int httpCode);
    }

    private LoginCallback callback;
    private String baseUrl; // Base URL backend n8n Anda (misal: "https://your-n8n-domain.com/webhook-test/api")

    public LoginTask(String baseUrl, LoginCallback callback) {
        this.baseUrl = baseUrl;
        this.callback = callback;
    }

    @Override
    protected LoginResponse doInBackground(String... params) {
        // params[0] = username, params[1] = password
        String username = params[0];
        String password = params[1];
        String loginApiUrl = baseUrl + "/login";

//        OkHttpClient client = new OkHttpClient();

        OkHttpClient client = UnsafeOkHttpClient.getUnsafeOkHttpClient();
        Gson gson = new Gson();

        // Buat JSON body untuk request
        String jsonBody = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(loginApiUrl)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBodyString = response.body().string();
            Log.d(TAG, "Login API Response Code: " + response.code());
            Log.d(TAG, "Login API Response Body: " + responseBodyString);

            if (response.isSuccessful()) {
                // Jika sukses (kode 2xx)
                return gson.fromJson(responseBodyString, LoginResponse.class);
            } else {
                // Jika gagal (kode 4xx atau 5xx)
                try {
                    ErrorResponse errorResponse = gson.fromJson(responseBodyString, ErrorResponse.class);
                    // Buat LoginResponse palsu untuk membawa pesan error
                    LoginResponse loginResponse = new LoginResponse(false, errorResponse.getMessage(), null, null, null);
                    // Simpan HTTP code di message untuk callback
                    loginResponse.setMessage(loginResponse.getMessage() + " (HTTP:" + response.code() + ")");
                    return loginResponse;
                } catch (JsonSyntaxException e) {
                    // Jika respons error bukan JSON yang diharapkan
                    Log.e(TAG, "Error parsing error response: " + e.getMessage());
                    LoginResponse loginResponse = new LoginResponse(false, "Unknown error (HTTP:" + response.code() + ")", null, null, null);
                    return loginResponse;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Network or API call error: " + e.getMessage(), e);
            LoginResponse loginResponse = new LoginResponse(false, "Network error: " + e.getMessage(), null, null, null);
            return loginResponse;
        }
    }

    @Override
    protected void onPostExecute(LoginResponse result) {
        // Ini akan dieksekusi di UI Thread
        if (callback != null) {
            if (result.isSuccess()) {
                callback.onLoginSuccess(result);
            } else {
                // Ekstrak HTTP code dari message jika ada
                int httpCode = -1;
                String errorMessage = result.getMessage();
                if (errorMessage.contains("(HTTP:")) {
                    try {
                        String codeStr = errorMessage.substring(errorMessage.indexOf("(HTTP:") + 6, errorMessage.lastIndexOf(")"));
                        httpCode = Integer.parseInt(codeStr);
                        errorMessage = errorMessage.substring(0, errorMessage.indexOf("(HTTP:")).trim();
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse HTTP code from message", e);
                    }
                }
                callback.onLoginFailure(errorMessage, httpCode);
            }
        }
    }
}