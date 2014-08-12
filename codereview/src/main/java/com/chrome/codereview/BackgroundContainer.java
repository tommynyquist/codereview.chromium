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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.chrome.codereview.utils.SwipeListView;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public class BackgroundContainer extends PullToRefreshLayout implements SwipeListView.BackgroundToggle {

    private Drawable backgroundDrawable;
    private boolean mShowing;
    private int mOpenAreaTop;
    private int mOpenAreaHeight;
    private TextPaint textPaint;
    private String backgroundTextLeft;
    private String backgroundTextRight;
    private String currentText;
    boolean mUpdateBounds = false;
    private Rect textBounds;

    public BackgroundContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BackgroundContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        backgroundDrawable = getResources().getDrawable(R.drawable.issue_backcontainer_bg);
        textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(TextPaint.Align.CENTER);

        textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.swipe_background_text_size));
        textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        textPaint.setAntiAlias(true);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BackgroundContainer);
        backgroundTextLeft = typedArray.getString(R.styleable.BackgroundContainer_textOnLeftSwipe);
        backgroundTextRight = typedArray.getString(R.styleable.BackgroundContainer_textOnRightSwipe);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void showBackground(int top, int bottom, int direction) {
        setWillNotDraw(false);
        mOpenAreaTop = top;
        mOpenAreaHeight = bottom;
        mShowing = true;
        mUpdateBounds = true;
        changeDirection(direction);
    }

    @Override
    public void changeDirection(int swipeDirection) {
        currentText = swipeDirection == SwipeListView.DIRECTION_LEFT ? backgroundTextLeft : backgroundTextRight;
        textBounds = new Rect();
        textPaint.getTextBounds(currentText, 0, currentText.length(), textBounds);
        invalidate();
    }

    public void hideBackground() {
        setWillNotDraw(true);
        mShowing = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mShowing) {
            if (mUpdateBounds) {
                backgroundDrawable.setBounds(0, 0, getWidth(), mOpenAreaHeight);
            }
            canvas.save();
            canvas.translate(0, mOpenAreaTop);
            backgroundDrawable.draw(canvas);
            canvas.drawText(currentText, getWidth() / 2, mOpenAreaHeight / 2 + textBounds.height() / 2, textPaint);
            canvas.restore();
        }
        super.onDraw(canvas);
    }

}
