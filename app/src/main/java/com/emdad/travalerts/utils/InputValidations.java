package com.emdad.travalerts.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.emdad.travalerts.R;

import java.util.regex.Pattern;

public class InputValidations {

    public static boolean emailValidation(Context context, String emailInput) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (TextUtils.isEmpty(emailInput)) {
            Toast.makeText(context, "Please type email.", Toast.LENGTH_SHORT).show();
            return false;
        }else if (emailInput.matches(emailPattern)) {
            return true;
        } else {
            Toast.makeText(context, R.string.email_is_not_valid, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean isValidPasswordLength(Context context, String password) {
        if (password.length() >= 6) {
            return true;
        } else {
            Toast.makeText(context, "Password is too short.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean passwordValidation(Context context, String passwordInput) {
        // defining password pattern
        Pattern PASSWORD_PATTERN =
                Pattern.compile("^" +
                        "(?=.*[@#$%^&+=])" +     // at least 1 special character
                        "(?=\\S+$)" +            // no white spaces
                        ".{4,}" +                // at least 4 characters
                        "$");
        if (PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            return true;
        } else {
            Toast.makeText(context, R.string.password_is_too_weak, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
