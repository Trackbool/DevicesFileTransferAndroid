package com.afa.devicesfiletransfer.view.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.model.Device;

import java.util.List;
import java.util.Objects;

public class SendFileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_file);

        List<Device> devices = Objects.requireNonNull(
                getIntent().getExtras()).getParcelableArrayList("devicesList");
    }
}
