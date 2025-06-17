package com.example.aichatapi.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.JsonObject; // Penting untuk parsing respons JSON
import com.google.gson.JsonArray; // Penting untuk parsing array di root

import com.example.aichatapi.models.ErrorResponse;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// Anda bisa menggunakan UnsafeOkHttpClient jika masih di development
import com.example.aichatapi.utils.UnsafeOkHttpClient;
import com.example.aichatapi.utils.AuthInterceptor; // Import interceptor

public class ChatAITask extends AsyncTask<String, Void, String> { // Mengembalikan String (respons AI)

    private static final String TAG = "ChatAITask";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface ChatAICallback {
        void onChatAISuccess(String aiResponse);
        void onChatAIFailure(String errorMessage, int httpCode);
    }

    private ChatAICallback callback;
    private String baseUrl; // Base URL backend n8n Anda
    private OkHttpClient client;
    private Gson gson;

    public ChatAITask(String baseUrl, ChatAICallback callback, Context context) { // Tambahkan context
        this.baseUrl = baseUrl;
        this.callback = callback;
        this.gson = new Gson();

        // Inisialisasi OkHttpClient dengan AuthInterceptor
        // OkHttpClient.Builder builder = new OkHttpClient.Builder(); // Untuk produksi
        OkHttpClient.Builder builder = UnsafeOkHttpClient.getUnsafeOkHttpClient().newBuilder(); // HANYA UNTUK DEVELOPMENT!
        builder.addInterceptor(new AuthInterceptor(context)); // AuthInterceptor akan menambahkan token
        this.client = builder.build();
    }

    @Override
    protected String doInBackground(String... params) {
        // params[0] = prompt
        String prompt = params[0];
        String chatApiUrl = baseUrl + "/chatAI"; // Sesuaikan endpoint Anda

        // Buat JSON body untuk request AI
        String jsonBody = "{\"prompt\":\"" + prompt + "\"}"; // Sesuaikan format body yang diharapkan AI
        RequestBody body = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(chatApiUrl)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBodyString = response.body().string();
            Log.d(TAG, "Chat AI API Response Code: " + response.code());
            Log.d(TAG, "Chat AI API Response Body: " + responseBodyString);

            if (response.isSuccessful()) {
                // Perbaikan: Parsing respons API AI yang berbentuk [ { "output": "..." } ]
                try {
                    JsonArray jsonArray = gson.fromJson(responseBodyString, JsonArray.class);
                    if (jsonArray != null && jsonArray.size() > 0) {
                        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject(); // Ambil objek pertama dari array
                        if (jsonObject.has("output") && !jsonObject.get("output").isJsonNull()) {
                            return jsonObject.get("output").getAsString(); // Ambil nilai dari key "output"
                        } else {
                            return "Error: Invalid AI response format (missing 'output' key in first object).";
                        }
                    } else {
                        return "Error: Empty or invalid AI response array.";
                    }
                } catch (JsonSyntaxException | NullPointerException | IllegalStateException e) {
                    Log.e(TAG, "Error parsing AI response: " + e.getMessage(), e);
                    return "Error: Failed to parse AI response JSON. " + e.getMessage();
                }

            } else {
                // Jika respons API tidak sukses (misal 4xx, 5xx)
                try {
                    ErrorResponse errorResponse = gson.fromJson(responseBodyString, ErrorResponse.class);
                    // Gunakan pesan dari ErrorResponse jika ada, tambahkan HTTP code
                    return "Error: " + (errorResponse != null ? errorResponse.getMessage() : "Unknown error") + " (HTTP:" + response.code() + ")";
                } catch (JsonSyntaxException e) {
                    // Jika respons error body bukan JSON yang diharapkan
                    Log.e(TAG, "Error parsing error response for chat AI: " + e.getMessage());
                    return "Error: Unknown error from AI (HTTP:" + response.code() + ")";
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Network or API call error for chat AI: " + e.getMessage(), e);
            return "Error: Network connection problem.";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (callback != null) {
            if (result != null && !result.startsWith("Error:")) { // Asumsi "Error:" adalah indikator kegagalan
                callback.onChatAISuccess(result);
            } else {
                // Pesan error dari doInBackground sudah membawa HTTP code
                // Kita hanya perlu meneruskannya
                int httpCode = -1; // Default
                String errorMessage = result; // result sudah berupa string "Error: ..."
                if (result.contains("(HTTP:")) {
                    try {
                        String codePart = result.substring(result.indexOf("(HTTP:") + 6, result.lastIndexOf(")"));
                        httpCode = Integer.parseInt(codePart);
                        errorMessage = result.substring(0, result.indexOf("(HTTP:")).trim();
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse HTTP code from AI chat message", e);
                    }
                }
                callback.onChatAIFailure(errorMessage, httpCode);
            }
        }
    }
}
