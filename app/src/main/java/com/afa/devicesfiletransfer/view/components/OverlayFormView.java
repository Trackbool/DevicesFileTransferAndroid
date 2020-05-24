package com.afa.devicesfiletransfer.view.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afa.devicesfiletransfer.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class OverlayFormView extends FrameLayout {
    private static final int FADE_SHOW_ANIMATION_DURATION = 220;
    private static final int FADE_HIDE_ANIMATION_DURATION = 180;
    private FrameLayout backgroundOverlay;
    private ConstraintLayout dialogContainer;
    private TextView formTitle;
    private LinearLayout formFieldsContainer;
    private Button formSubmitButton;

    public OverlayFormView(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public OverlayFormView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public OverlayFormView(@NonNull Context context, @Nullable AttributeSet attrs,
                           int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public OverlayFormView(@NonNull Context context, @Nullable AttributeSet attrs,
                           int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        View root = inflate(context, R.layout.overlay_form, this);
        this.setFocusable(true);
        this.setClickable(true);
        backgroundOverlay = root.findViewById(R.id.backgroundOverlay);
        dialogContainer = root.findViewById(R.id.dialogContainer);
        formTitle = root.findViewById(R.id.formTitleTextView);
        formFieldsContainer = root.findViewById(R.id.formFieldsContainer);
        formSubmitButton = root.findViewById(R.id.formSubmitButton);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OverlayFormView);

            int backgroundOverlayColor = typedArray.getColor(
                    R.styleable.OverlayFormView_backgroundOverlayColor,
                    Color.parseColor("#000000"));
            int backgroundOverlayOpacity = typedArray.getInteger(
                    R.styleable.OverlayFormView_backgroundOverlayOpacity, 171);
            String formTitle = typedArray.getString(R.styleable.OverlayFormView_formTitle);
            String submitButtonText = typedArray.getString(
                    R.styleable.OverlayFormView_submitButtonText);
            setBackgroundOverlayColor(backgroundOverlayColor);
            setBackgroundOverlayOpacity(backgroundOverlayOpacity);
            setFormTitle(formTitle);
            setSubmitButtonText(submitButtonText);

            typedArray.recycle();
        }
    }

    public void setBackgroundOverlayColor(int color) {
        backgroundOverlay.setBackgroundColor(color);
    }

    /**
     * Set overlay background opacity (0-255)
     *
     * @param opacity the opacity of the background
     */
    public void setBackgroundOverlayOpacity(int opacity) {
        backgroundOverlay.getBackground().setAlpha(opacity);
    }

    public void setFormTitle(String value) {
        formTitle.setText(value);
    }

    public String getFormTitle() {
        return String.valueOf(formTitle.getText());
    }

    public void addFormField(int id, String placeHolder) {
        EditText formField = new EditText(getContext());
        formField.setId(id);
        formField.setHint(placeHolder);
        formField.setEms(10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formField.setImportantForAutofill(IMPORTANT_FOR_AUTOFILL_NO);
        }
        formFieldsContainer.addView(formField);
    }

    public String getFormFieldValue(int id) {
        EditText formField = formFieldsContainer.findViewById(id);
        return String.valueOf(formField.getText());
    }

    public void setSubmitButtonText(String text) {
        formSubmitButton.setText(text);
    }

    public String getSubmitButtonText() {
        return String.valueOf(formSubmitButton.getText());
    }

    public Button getSubmitButton() {
        return formSubmitButton;
    }

    public void showFade() {
        if (getVisibility() == VISIBLE) {
            return;
        }

        setAlpha(0f);
        setVisibility(View.VISIBLE);
        animate().alpha(1f)
                .setDuration(FADE_SHOW_ANIMATION_DURATION)
                .setListener(null);
    }

    public void hideFade() {
        if (getVisibility() == INVISIBLE || getVisibility() == GONE) {
            return;
        }

        animate().alpha(0f)
                .setDuration(FADE_HIDE_ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setVisibility(View.INVISIBLE);
                    }
                });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();

        final int viewX = (int) dialogContainer.getX();
        final int viewY = (int) dialogContainer.getY();
        final int viewWidth = dialogContainer.getWidth();
        final int viewHeight = dialogContainer.getHeight();

        Rect viewRect = new Rect(viewX, viewY, viewX + viewWidth, viewY + viewHeight);
        if ((event.getAction() == MotionEvent.ACTION_DOWN) && !viewRect.contains(x, y)) {
            hideFade();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            hideFade();
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }
}
