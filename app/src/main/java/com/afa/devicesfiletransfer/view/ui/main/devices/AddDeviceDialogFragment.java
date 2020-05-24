package com.afa.devicesfiletransfer.view.ui.main.devices;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afa.devicesfiletransfer.R;

import org.jetbrains.annotations.NotNull;

import androidx.fragment.app.DialogFragment;

public class AddDeviceDialogFragment extends DialogFragment {

    private TextView formTitleTextView;
    private EditText nameEditText;
    private EditText ipAddressEditText;
    private Button formSubmitButton;

    public AddDeviceDialogFragment() {
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_add_device,
                new FrameLayout(requireContext()), false);

        formTitleTextView = view.findViewById(R.id.formTitleTextView);
        nameEditText = view.findViewById(R.id.nameEditText);
        ipAddressEditText = view.findViewById(R.id.ipAddressEditText);
        formSubmitButton = view.findViewById(R.id.formSubmitButton);
        formSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Add device logic
            }
        });

        Dialog builder = new Dialog(requireContext());
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setContentView(view);
        return builder;
    }
}
