package com.example.aichatapi.utils;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

import java.util.ArrayList;
import java.util.List;

public class MarkdownParser {

    public static SpannableString parse(String rawText) {
        String processed = rawText.replace("\\n", "\n");

        StringBuilder cleanText = new StringBuilder();
        List<SpanInfo> spans = new ArrayList<>();

        int i = 0;
        while (i < processed.length()) {
            if (i + 1 < processed.length() && processed.substring(i, i + 2).equals("**")) {
                int spanStart = cleanText.length();
                i += 2;
                while (i + 1 < processed.length() && !processed.substring(i, i + 2).equals("**")) {
                    cleanText.append(processed.charAt(i));
                    i++;
                }
                int spanEnd = cleanText.length();
                spans.add(new SpanInfo(spanStart, spanEnd, Typeface.BOLD));
                i += 2; // skip **
            } else if ((i == 0 || processed.charAt(i - 1) == '\n') && processed.startsWith("* ", i)) {
                cleanText.append("\u2022 ");
                i += 2;
            } else {
                cleanText.append(processed.charAt(i));
                i++;
            }
        }

        SpannableString result = new SpannableString(cleanText.toString());

        for (SpanInfo span : spans) {
            result.setSpan(new StyleSpan(span.style), span.start, span.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return result;
    }

    static class SpanInfo {
        int start, end;
        int style;

        SpanInfo(int start, int end, int style) {
            this.start = start;
            this.end = end;
            this.style = style;
        }
    }
}
