package com.afa.devicesfiletransfer.view.components;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LabeledImageView extends FrameLayout {
    private static final int DEFAULT_LABEL_HEIGHT = 100;
    private static final int DEFAULT_LABEL_OPACITY = 100;

    private FrameLayout label;
    private TextView labelText;
    private ImageView imageView;

    public LabeledImageView(Context context, ImageView imageView) {
        super(context);
        init(context, imageView);
    }

    public LabeledImageView(@NonNull Context context, @Nullable AttributeSet attrs,
                            ImageView imageView) {
        super(context, attrs);
        this.imageView = imageView;
        init(context, imageView);
    }

    public LabeledImageView(@NonNull Context context, @Nullable AttributeSet attrs,
                            int defStyleAttr, ImageView imageView) {
        super(context, attrs, defStyleAttr);
        init(context, imageView);
    }

    public LabeledImageView(@NonNull Context context, @Nullable AttributeSet attrs,
                            int defStyleAttr, int defStyleRes, ImageView imageView) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, imageView);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public TextView getLabelText() {
        return labelText;
    }

    public void setLabelBackgroundColor(int color) {
        label.setBackgroundColor(color);
    }

    /**
     * Set opacity of label background
     * @param opacity the opacity of the background 0-255
     */
    public void setLabelBackgroundOpacity(int opacity) {
        label.getBackground().setAlpha(opacity);
    }

    private void init(Context context, ImageView imageView) {
        this.imageView = imageView;

        labelText = new TextView(context);
        labelText.setWidth(FrameLayout.LayoutParams.MATCH_PARENT);
        labelText.setHeight(FrameLayout.LayoutParams.WRAP_CONTENT);
        labelText.setGravity(Gravity.CENTER);
        labelText.setTextColor(Color.WHITE);
        labelText.setMaxLines(1);

        label = new FrameLayout(context);
        FrameLayout.LayoutParams labelLayoutParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, DEFAULT_LABEL_HEIGHT);
        labelLayoutParams.gravity = Gravity.BOTTOM;
        label.setLayoutParams(labelLayoutParams);
        label.setBackgroundColor(Color.BLACK);
        label.getBackground().setAlpha(DEFAULT_LABEL_OPACITY);
        label.addView(labelText);

        this.addView(imageView);
        this.addView(label);
    }
}
