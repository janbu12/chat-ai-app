package com.example.aichatapi.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.BulletSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aichatapi.R;
import com.example.aichatapi.models.ChatMessage;
import com.example.aichatapi.utils.MarkdownParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<ChatMessage> messageList;
    private Context context;

    public MessageAdapter(Context context, List<ChatMessage> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        // Memformat teks sebelum menampilkannya
        SpannableString formattedContent = formatMarkdownText(message.getContent());
        holder.tvMessageContent.setText(formattedContent);

        // Atur tata letak dan warna bubble berdasarkan pengirim
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.tvMessageContent.getLayoutParams();
        Drawable backgroundDrawable;

        if (message.getSender() == ChatMessage.SENDER_AI) {
            params.gravity = Gravity.LEFT;
            backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.chat_bubble_ai_bg);
            holder.tvMessageContent.setTextColor(Color.BLACK);
        } else {
            params.gravity = Gravity.RIGHT;
            backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.chat_bubble_user_bg);
            holder.tvMessageContent.setTextColor(Color.BLACK); // Atau warna teks lain yang sesuai
        }
        holder.tvMessageContent.setLayoutParams(params);
        holder.tvMessageContent.setBackground(backgroundDrawable);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void addMessage(ChatMessage message) {
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    // --- Metode Baru untuk Memformat Markdown ---
    private SpannableString formatMarkdownText(String text) {
        return MarkdownParser.parse(text);
    }

    static class SpanInfo {
        public static final int TYPE_BOLD = 0;
        public static final int TYPE_BULLET_MARKER = 1;

        int type;
        int start;
        int end;

        public SpanInfo(int type, int start, int end) {
            this.type = type;
            this.start = start;
            this.end = end;
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageContent;

        MessageViewHolder(View itemView) {
            super(itemView);
            tvMessageContent = itemView.findViewById(R.id.tv_message_content);
        }
    }
}