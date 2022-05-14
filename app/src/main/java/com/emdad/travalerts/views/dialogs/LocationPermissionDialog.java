package com.emdad.travalerts.views.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.FragmentActivity;

import com.emdad.travalerts.R;
import com.emdad.travalerts.utils.InputValidations;
import com.emdad.travalerts.views.activities.AuthActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LocationPermissionDialog {
    private static final String TAG = "ForgotPasswordDialog";
    private final Context context;
    private Activity activity;

    public LocationPermissionDialog(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void showDialog() {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_location_permission_request, null);

        Dialog dialog = new Dialog(context);
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setContentView(view);

        AppCompatButton btnCloseApp = view.findViewById(R.id.btnCloseApp);
        AppCompatButton btnRequestPermission = view.findViewById(R.id.btnRequestPermission);

        btnCloseApp.setOnClickListener(view1 -> activity.finish());

        btnRequestPermission.setOnClickListener(view1 -> {
            AuthActivity authActivity = (AuthActivity) activity;
            authActivity.requestAccessFineLocation();
            dialog.dismiss();
        });

        dialog.show();

    }




}
