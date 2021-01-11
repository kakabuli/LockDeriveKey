package com.revolo.lock.util;

import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * author : Jack
 * time   : 2020/12/25
 * E-mail : wengmaowei@kaadas.com
 * desc   : 文字点击
 */
public class LinkClickableSpan extends ClickableSpan {
    @Override
    public void onClick(@NonNull View widget) {

    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        ds.setColor(Color.parseColor("#2C68FF"));
        ds.setUnderlineText(false);
    }
}
