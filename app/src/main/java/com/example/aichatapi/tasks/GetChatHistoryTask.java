package com.example.aichatapi.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import com.example.aichatapi.models.ChatHistoryResponse; // Import model yang Anda buat
import com.example.aichatapi.models.ErrorResponse;     // Import model error response
import com.example.aichatapi.utils.AuthInterceptor;    // Import AuthInterceptor
import com.example.aichatapi.utils.UnsafeOkHttpClient; // Import UnsafeOkHttpClient (HANYA UNTUK DEVELOPMENT)

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetChatHistoryTask extends AsyncTask<Void, Void, List<ChatHistoryResponse>> {

    private static final String TAG = "GetChatHistoryTask";
    private String baseUrl;
    private GetChatHistoryCallback callback;
    private OkHttpClient client;
    private Gson gson;

    // Untuk menyimpan error message dan http code
    private String errorMessage = null;
    private int httpResponseCode = -1;

    public interface GetChatHistoryCallback {
        void onChatHistorySuccess(List<ChatHistoryResponse> chatHistory);
        void onChatHistoryFailure(String errorMessage, int httpCode);
    }

    public GetChatHistoryTask(String baseUrl, GetChatHistoryCallback callback, Context context) {
        this.baseUrl = baseUrl;
        this.callback = callback;
        this.gson = new Gson();

        // Inisialisasi OkHttpClient dengan AuthInterceptor
        // Gunakan UnsafeOkHttpClient untuk development, ganti dengan OkHttpClient.Builder() untuk produksi
        OkHttpClient.Builder builder = UnsafeOkHttpClient.getUnsafeOkHttpClient().newBuilder(); // HANYA UNTUK DEVELOPMENT!
        builder.addInterceptor(new AuthInterceptor(context)); // AuthInterceptor akan menambahkan token jika ada
        this.client = builder.build();
    }

    @Override
    protected List<ChatHistoryResponse> doInBackground(Void... voids) {
        String GET_CHAT_HISTORY_ENDPOINT = baseUrl + "/getChat"; // Sesuaikan endpoint riwayat chat Anda

        Request request = new Request.Builder()
                .url(GET_CHAT_HISTORY_ENDPOINT)
                .get() // Metode GET untuk mengambil data
                .build();

        try {
            Response response = client.newCall(request).execute();
            httpResponseCode = response.code(); // Simpan kode respons
            String responseBodyString = response.body() != null ? response.body().string() : "";
            Log.d(TAG, "Chat History API Response Code: " + httpResponseCode);
            Log.d(TAG, "Chat History API Response Body: " + responseBodyString);

            if (response.isSuccessful()) { // Kode 2xx
                try {
                    // Endpoint /getChat mengembalikan array di root, seperti: [ { "_id": "...", "messages": [...] } ]
                    Type listType = new TypeToken<List<ChatHistoryResponse>>() {}.getType();
                    List<ChatHistoryResponse> chatHistory = gson.fromJson(responseBodyString, listType);
                    return chatHistory;
                } catch (JsonSyntaxException | NullPointerException | IllegalStateException e) {
                    Log.e(TAG, "Error parsing chat history response: " + e.getMessage(), e);
                    errorMessage = "Error: Failed to parse chat history JSON. " + e.getMessage();
                    return null;
                }
            } else { // Kode 4xx atau 5xx
                try {
                    ErrorResponse errorResp = gson.fromJson(responseBodyString, ErrorResponse.class);
                    errorMessage = "Error: " + (errorResp != null && errorResp.getMessage() != null ? errorResp.getMessage() : "Unknown error") + " (HTTP:" + httpResponseCode + ")";
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "Error parsing error response for chat history: " + e.getMessage());
                    errorMessage = "Error: Unknown error from server (HTTP:" + httpResponseCode + ")";
                }
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network or API call error for chat history: " + e.getMessage(), e);
            errorMessage = "Error: Network connection problem. " + e.getMessage();
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<ChatHistoryResponse> chatHistory) {
        if (callback != null) {
            if (chatHistory != null) { // Jika hasilnya tidak null, berarti sukses diurai
                callback.onChatHistorySuccess(chatHistory);
            } else { // Jika null, berarti ada kesalahan
                callback.onChatHistoryFailure(errorMessage != null ? errorMessage : "Unknown failure.", httpResponseCode);
            }
        }
    }
}