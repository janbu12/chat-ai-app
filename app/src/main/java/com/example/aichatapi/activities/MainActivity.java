package com.example.aichatapi.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.activity.EdgeToEdge;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aichatapi.adapters.MessageAdapter;
import com.example.aichatapi.models.ChatMessage;
import com.example.aichatapi.models.ChatHistoryResponse; // Import model baru
import com.example.aichatapi.models.MessageEntry;     // Import model baru
import com.example.aichatapi.models.LogoutResponse;
import com.example.aichatapi.tasks.ChatAITask;
import com.example.aichatapi.tasks.LogoutTask;
import com.example.aichatapi.tasks.GetChatHistoryTask; // Import task baru
import com.google.gson.JsonObject;

import com.example.aichatapi.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvUsernameDisplay;
    private Button btnLogout;
    private RecyclerView rvChatMessages;
    private LinearLayout llHeaderLayout, llInputMessageLayout;
    private EditText etMessageInput;
    private ImageButton btnSendMessage;
    private ProgressBar progressBar;
    private ProgressBar progressBarChatHistory; // Tambahkan ProgressBar baru untuk riwayat chat

    private MessageAdapter messageAdapter;
    private List<ChatMessage> messageList;

    // URL Backend n8n Anda
    private static final String N8N_BASE_URL = "https://allowing-perfectly-lynx.ngrok-free.app/webhook/fec1022f-7840-4b53-9dcd-cce4f525a9fc/api";
    private static final String CHAT_AI_ENDPOINT = N8N_BASE_URL + "/chatAI";
    private static final String LOGOUT_ENDPOINT = N8N_BASE_URL + "/logout";
    private static final String GET_CHAT_HISTORY_ENDPOINT = N8N_BASE_URL + "/getChat"; // Endpoint riwayat chat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi UI components
        tvUsernameDisplay = findViewById(R.id.tv_username_display);
        btnLogout = findViewById(R.id.btnLogout);
        rvChatMessages = findViewById(R.id.rv_chat_messages);
        etMessageInput = findViewById(R.id.et_message_input);
        btnSendMessage = findViewById(R.id.btnKirim);
        progressBar = findViewById(R.id.progress_barLogout); // ProgressBar untuk logout/kirim chat
        progressBarChatHistory = findViewById(R.id.progress_barChatHistory); // Asumsikan Anda menambahkan ini di activity_main.xml
        llHeaderLayout = findViewById(R.id.header_layout);
        llInputMessageLayout = findViewById(R.id.message_input_layout);

        // --- Setup Chat RecyclerView ---
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        rvChatMessages.setAdapter(messageAdapter);

        // --- Tampilkan Username ---
        displayUsername();

        // --- Muat riwayat chat dari API saat aplikasi dimulai ---
        loadChatHistory();

        // --- Listener untuk Tombol Logout ---
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
            }
        });

        // --- Listener untuk Tombol Kirim Chat ---
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = etMessageInput.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    addMessage(new ChatMessage(userMessage, ChatMessage.SENDER_USER));
                    etMessageInput.setText("");
                    // Gulir ke posisi terakhir, akan dilakukan di onChatAISuccess juga
                    rvChatMessages.scrollToPosition(messageList.size() - 1);

                    // Tampilkan loading dan nonaktifkan UI saat mengirim pesan
                    showLoading(true);

                    // Kirim pesan ke API AI
                    new ChatAITask(N8N_BASE_URL, new ChatAITask.ChatAICallback() {
                        @Override
                        public void onChatAISuccess(String aiResponse) {
                            showLoading(false);
                            addMessage(new ChatMessage(aiResponse, ChatMessage.SENDER_AI));
                            rvChatMessages.scrollToPosition(messageList.size() - 1);
                        }

                        @Override
                        public void onChatAIFailure(String errorMessage, int httpCode) {
                            showLoading(false);
                            Log.e("CHAT_AI_FAIL", "Error: " + errorMessage + " HTTP Code: " + httpCode);
                            Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                            // Optional: tambahkan pesan error ke chat
                            // addMessage(new ChatMessage("Error: " + errorMessage, ChatMessage.SENDER_AI));
                            rvChatMessages.scrollToPosition(messageList.size() - 1);
                        }
                    }, MainActivity.this).execute(userMessage);
                }
            }
        });
    }

    // --- Metode Baru: Muat Riwayat Chat ---
    private void loadChatHistory() {
        showChatHistoryLoading(true); // Tampilkan ProgressBar untuk riwayat chat

        new GetChatHistoryTask(N8N_BASE_URL, new GetChatHistoryTask.GetChatHistoryCallback() {
            @Override
            public void onChatHistorySuccess(List<ChatHistoryResponse> chatHistory) {
                showChatHistoryLoading(false); // Sembunyikan ProgressBar

                if (chatHistory != null && !chatHistory.isEmpty()) {
                    // Ambil pesan dari entri terakhir (asumsi hanya ada satu session per user)
                    // Atau Anda bisa memilih session berdasarkan sessionId yang disimpan
                    ChatHistoryResponse latestSession = chatHistory.get(0); // Ambil yang pertama atau logika lain

                    // Bersihkan pesan awal "Halo ada yang bisa saya bantu?" jika ada riwayat
                    messageList.clear();

                    for (MessageEntry entry : latestSession.getMessages()) {
                        String content = entry.getData().getContent();
                        int senderType = -1;
                        if ("human".equals(entry.getType())) {
                            senderType = ChatMessage.SENDER_USER;
                        } else if ("ai".equals(entry.getType())) {
                            senderType = ChatMessage.SENDER_AI;
                        }

                        if (senderType != -1) {
                            messageList.add(new ChatMessage(content, senderType));
                        }
                    }
                    messageAdapter.notifyDataSetChanged();
                    rvChatMessages.scrollToPosition(messageList.size() - 1); // Gulir ke pesan terakhir
                } else {
                    // Jika tidak ada riwayat, tambahkan pesan awal dari AI
                    addMessage(new ChatMessage("Halo ada yang bisa saya bantu?", ChatMessage.SENDER_AI));
                }
            }

            @Override
            public void onChatHistoryFailure(String errorMessage, int httpCode) {
                showChatHistoryLoading(false); // Sembunyikan ProgressBar
                Log.e("CHAT_HISTORY_FAIL", "Error: " + errorMessage + " HTTP Code: " + httpCode);
                Toast.makeText(MainActivity.this, "Anda Belum Memiliki History Chat: " + errorMessage, Toast.LENGTH_LONG).show();
                // Jika gagal memuat riwayat, tetap tampilkan pesan awal dari AI
                addMessage(new ChatMessage("Halo ada yang bisa saya bantu?", ChatMessage.SENDER_AI));
            }
        }, MainActivity.this).execute();
    }


    // --- Metode Bantuan untuk Chat ---
    private void addMessage(ChatMessage message) {
        messageAdapter.addMessage(message);
        // Adapter sudah memanggil notifyItemInserted. Gulir di sini.
        rvChatMessages.scrollToPosition(messageList.size() - 1);
    }

    // --- Metode Bantuan untuk Tampilkan Username ---
    private void displayUsername() {
        SharedPreferences sharedPref = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "Pengguna");
        Log.d("USERNAME:", username);
        tvUsernameDisplay.setText(username);
    }

    // --- Metode untuk Logout ---
    private void performLogout() {
        showLoading(true); // Gunakan progressBar utama

        new LogoutTask(N8N_BASE_URL, new LogoutTask.LogoutCallback() {
            @Override
            public void onLogoutSuccess(LogoutResponse response) {
                showLoading(false);
                clearTokens();
                Toast.makeText(MainActivity.this, response.getMessage(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onLogoutFailure(String errorMessage, int httpCode) {
                showLoading(false);
                Log.e("LOGOUT_FAIL", "Error: " + errorMessage + " HTTP Code: " + httpCode);
                Toast.makeText(MainActivity.this, "Logout gagal: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        }, this).execute();
    }


    // --- Metode Bantuan untuk Mengelola Token ---
    private void clearTokens() {
        SharedPreferences sharedPref = getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove("token");
        editor.remove("username");
        editor.apply();
    }

    // Mengelola visibilitas ProgressBar dan status UI lainnya untuk pengiriman pesan/logout
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogout.setEnabled(false);
            btnSendMessage.setEnabled(false);
            etMessageInput.setEnabled(false);
            rvChatMessages.animate().alpha(0.5f).setDuration(300).start();
            llHeaderLayout.animate().alpha(0.5f).setDuration(300).start();
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogout.setEnabled(true);
            btnSendMessage.setEnabled(true);
            etMessageInput.setEnabled(true);
            rvChatMessages.animate().alpha(1.0f).setDuration(300).start();
            llHeaderLayout.animate().alpha(1.0f).setDuration(300).start();

        }
    }

    // Mengelola visibilitas ProgressBar khusus untuk memuat riwayat chat
    private void showChatHistoryLoading(boolean isLoading) {
        if (isLoading) {
            progressBarChatHistory.setVisibility(View.VISIBLE);
            rvChatMessages.setVisibility(View.GONE); // Sembunyikan RecyclerView saat memuat
        } else {
            progressBarChatHistory.setVisibility(View.GONE);
            rvChatMessages.setVisibility(View.VISIBLE); // Tampilkan kembali RecyclerView
        }
    }
}