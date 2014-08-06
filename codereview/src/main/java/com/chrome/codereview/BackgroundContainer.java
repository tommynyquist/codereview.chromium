/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chrome.codereview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.AttributeSet;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class BackgroundContainer extends PullToRefreshLayout {

    private boolean mShowing;
    private int mOpenAreaTop;
    private int mOpenAreaHeight;
    private TextPaint textPaint;
    private String backgroundText;

    public BackgroundContainer(Context context) {
        super(context);
        init();
    }

    public BackgroundContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BackgroundContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        Resources resources = getResources();
        textPaint = new TextPaint();
        textPaint.setColor(Color.RED);
        textPaint.setTextAlign(TextPaint.Align.CENTER);
        textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.swipe_background_text_size));
        backgroundText = resources.getString(R.string.swipe_background_text_to_left);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void showBackground(int top, int bottom) {
        setWillNotDraw(false);
        mOpenAreaTop = top;
        mOpenAreaHeight = bottom;
        mShowing = true;
    }

    public void hideBackground() {
        setWillNotDraw(true);
        mShowing = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mShowing) {
            canvas.save();
            canvas.drawText(backgroundText, getWidth() / 4, mOpenAreaTop + mOpenAreaHeight / 2, textPaint);
            canvas.restore();
        }
        super.onDraw(canvas);
    }

}
