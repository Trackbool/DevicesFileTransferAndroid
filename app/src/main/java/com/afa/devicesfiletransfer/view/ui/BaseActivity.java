package com.afa.devicesfiletransfer.view.ui;

import android.graphics.Color;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    public void showError(final String title, final String message) {
        Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content),
                title + ". " + message,
                Snackbar.LENGTH_LONG);
        snackbar.getView().setBackgroundColor(Color.RED);
        snackbar.show();
    }

    public void showAlert(final String title, final String message) {
        Snackbar snackbar = Snackbar.make(this.findViewById(android.R.id.content),
                title + ". " + message,
                Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    protected static String generateViewPagerFragmentTag(int viewId, long id) {
        return "android:switcher:" + viewId + ":" + id;
    }
}
