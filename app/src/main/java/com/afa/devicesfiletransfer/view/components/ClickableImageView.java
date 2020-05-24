package com.afa.devicesfiletransfer.view.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import com.afa.devicesfiletransfer.R;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

public class ClickableImageView extends AppCompatImageView {

    private ColorStateList pressedColor;

    public ClickableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
        setFocusable(true);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClickableImageView);
        pressedColor = typedArray.getColorStateList(R.styleable.ClickableImageView_pressedColor);
        typedArray.recycle();
        if (pressedColor == null) {
            pressedColor = ContextCompat.getColorStateList(context, R.color.clickable_view_colors);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        if (pressedColor == null) {
            return;
        }

        int color = pressedColor.getColorForState(getDrawableState(), Color.TRANSPARENT);
        setColorFilter(color);
        invalidate();
    }
}
