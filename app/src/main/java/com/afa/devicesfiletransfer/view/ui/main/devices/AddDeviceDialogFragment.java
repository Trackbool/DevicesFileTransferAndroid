package com.afa.devicesfiletransfer.view.ui.main.devices;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afa.devicesfiletransfer.R;
import com.afa.devicesfiletransfer.domain.model.Device;
import com.afa.devicesfiletransfer.view.model.ErrorModel;
import com.afa.devicesfiletransfer.view.ui.main.devices.viewmodel.AddDeviceViewModel;

import org.jetbrains.annotations.NotNull;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class AddDeviceDialogFragment extends DialogFragment {
    private AddDeviceViewModel addDeviceViewModel;
    private EditText nameEditText;
    private EditText ipAddressEditText;
    private TextView errorTextView;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_add_device,
                new FrameLayout(requireContext()), false);

        initViewModel();
        nameEditText = view.findViewById(R.id.nameEditText);
        ipAddressEditText = view.findViewById(R.id.ipAddressEditText);
        errorTextView = view.findViewById(R.id.errorTextView);
        Button formSubmitButton = view.findViewById(R.id.formSubmitButton);
        formSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = String.valueOf(nameEditText.getText());
                String ipAddress = String.valueOf(ipAddressEditText.getText());
                addDeviceViewModel.onAddDeviceButtonClicked(name, ipAddress);
            }
        });

        Dialog builder = new Dialog(requireContext());
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setContentView(view);
        return builder;
    }

    private void initViewModel() {
        addDeviceViewModel = new ViewModelProvider(this).get(AddDeviceViewModel.class);
        addDeviceViewModel.getDeviceLiveData().observe(this, new Observer<Device>() {
            @Override
            public void onChanged(Device device) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("device", device);
                Intent intent = new Intent().putExtras(bundle);
                getTargetFragment()
                        .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                dismiss();
            }
        });
        addDeviceViewModel.getOnErrorEvent().observe(this, new Observer<ErrorModel>() {
            @Override
            public void onChanged(ErrorModel errorModel) {
                errorTextView.setText(errorModel.getMessage());
            }
        });
    }
}
