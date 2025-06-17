package com.example.aichatapi.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.aichatapi.models.RegisterTaskResult;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import com.example.aichatapi.models.RegisterResponse; // Import model respons API

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// Anda bisa menggunakan UnsafeOkHttpClient jika masih di development
import com.example.aichatapi.utils.UnsafeOkHttpClient;

public class RegisterTask extends AsyncTask<String, Void, RegisterTaskResult> { // Mengembalikan RegisterTaskResult

    private static final String TAG = "RegisterTask";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface RegisterCallback {
        void onRegisterSuccess(); // Tidak ada parameter, karena sukses tidak ada pesan dari API
        void onRegisterFailure(String errorMessage, int httpCode);
    }

    private RegisterCallback callback;
    private String baseUrl;
    private OkHttpClient client;
    private Gson gson;

    public RegisterTask(String baseUrl, RegisterCallback callback, Context context) {
        this.baseUrl = baseUrl;
        this.callback = callback;
        this.gson = new Gson();

        // Inisialisasi OkHttpClient (gunakan UnsafeOkHttpClient untuk dev)
        // OkHttpClient.Builder builder = new OkHttpClient.Builder(); // Untuk produksi
        OkHttpClient.Builder builder = UnsafeOkHttpClient.getUnsafeOkHttpClient().newBuilder(); // HANYA UNTUK DEVELOPMENT!
        this.client = builder.build();
    }

    @Override
    protected RegisterTaskResult doInBackground(String... params) {
        // params[0] = username, params[1] = name, params[2] = password, params[3] = confirmPassword
        String username = params[0];
        String name = params[1];
        String password = params[2];
        String confirmPassword = params[3]; // Digunakan untuk validasi, tidak dikirim ke API jika API tidak butuh

        String registerApiUrl = baseUrl + "/register"; // Endpoint register Anda

        // Buat JSON body untuk request
        String jsonBody = "{\"username\":\"" + username + "\",\"name\":\"" + name + "\",\"password\":\"" + password + "\",\"confirmPassword\":\"" + confirmPassword + "\"}";
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(registerApiUrl)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            // Baca response body. Ini mungkin kosong untuk sukses, atau berisi JSON untuk gagal.
            String responseBodyString = response.body().string();
            Log.d(TAG, "Register API Response Code: " + response.code());
            Log.d(TAG, "Register API Response Body: " + responseBodyString); // Log body untuk debug

            if (response.isSuccessful()) { // Kode 2xx (misal 200 OK)
                return new RegisterTaskResult(true, "Registrasi berhasil!", response.code());
            } else {
                // Jika tidak sukses, coba parsing respons error jika ada body JSON
                if (responseBodyString != null && !responseBodyString.isEmpty()) {
                    try {
                        RegisterResponse errorResponse = gson.fromJson(responseBodyString, RegisterResponse.class);
                        return new RegisterTaskResult(false, errorResponse.getMessage(), response.code());
                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, "Error parsing non-successful response JSON: " + e.getMessage());
                        return new RegisterTaskResult(false, "Unknown server response format", response.code());
                    }
                } else {
                    // Jika respons gagal tapi body kosong (misal 400 Bad Request tanpa pesan)
                    return new RegisterTaskResult(false, "Server error: No message provided", response.code());
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Network or API call error: " + e.getMessage(), e);
            // Error jaringan
            return new RegisterTaskResult(false, "Network error: " + e.getMessage(), -1);
        }
    }

    @Override
    protected void onPostExecute(RegisterTaskResult result) {
        if (callback != null) {
            if (result.success) {
                callback.onRegisterSuccess();
            } else {
                callback.onRegisterFailure(result.message, result.httpCode);
            }
        }
    }
}
