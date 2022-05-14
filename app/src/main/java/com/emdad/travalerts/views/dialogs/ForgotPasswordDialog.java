package com.emdad.travalerts.views.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
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
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordDialog {
    private static final String TAG = "ForgotPasswordDialog";
    private final Context context;
    private final FragmentActivity fragmentActivity;

    public ForgotPasswordDialog(Context context, FragmentActivity fragmentActivity) {
        this.context = context;
        this.fragmentActivity = fragmentActivity;
    }

    public void showDialog() {
        LayoutInflater layoutInflater = fragmentActivity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_forgot_password, null);

        Dialog dialog = new Dialog(context);
//        dialog.setTitle("Forgot Password");
        dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);

        AppCompatButton btnSend = view.findViewById(R.id.btnSend);
        AppCompatEditText etEmail = view.findViewById(R.id.etEmail);

        btnSend.setOnClickListener(view1 -> {
            String email = etEmail.getText().toString().trim();
            if (InputValidations.emailValidation(context, email)) {
                sendForgotPasswordLinkToEmail(email, dialog);
            }
        });

        dialog.show();

    }

    private void sendForgotPasswordLinkToEmail(String email, Dialog dialog) {
        Log.d(TAG, "sendForgotPasswordLinkToEmail: " + email);
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email sent.");
                        Toast.makeText(context, String.format("An e-mail has been sent to '%s', please check..", email), Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }).addOnFailureListener(e -> {
            Log.d(TAG, "sendForgotPasswordLinkToEmail: " + e.getLocalizedMessage());
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        });
    }


}
