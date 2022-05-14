package com.emdad.travalerts.views.fragments.auth;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.emdad.travalerts.R;
import com.emdad.travalerts.databinding.FragmentLoginBinding;
import com.emdad.travalerts.utils.FragmentTransactionHelper;
import com.emdad.travalerts.utils.InputValidations;
import com.emdad.travalerts.views.dialogs.ForgotPasswordDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    private FragmentLoginBinding binding;
    private FragmentTransactionHelper fragmentTransactionHelper;
    private Context context;
    private ForgotPasswordDialog forgotPasswordDialog;
    // Firebase
    private FirebaseAuth mAuth;


    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        initClickListeners();
    }

    private void init(View view) {
        context = view.getContext();
        fragmentTransactionHelper = new FragmentTransactionHelper(getParentFragment());
        forgotPasswordDialog = new ForgotPasswordDialog(view.getContext(), getActivity());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    private void initClickListeners() {
        binding.tvSkip.setOnClickListener(view -> {
            signInAnonymously();
        });

        binding.btnLogin.setOnClickListener(view -> {
            String email = binding.tiETEmail.getText().toString();
            String password = binding.tiETPassword.getText().toString();
            Log.d(TAG, "initClickListeners: email: " + email);
            Log.d(TAG, "initClickListeners: password: " + password);
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(context, "Please input email.", Toast.LENGTH_SHORT).show();
                binding.tiETEmail.requestFocus();
            } else if (TextUtils.isEmpty(password)) {
                Toast.makeText(context, "Please input password.", Toast.LENGTH_SHORT).show();
                binding.tiETPassword.requestFocus();
            }
            // here the parameter context for showing the Toast msg
            else if (InputValidations.emailValidation(context, email) && InputValidations.isValidPasswordLength(context, password)) {
                firebaseLogin(email, password);
            }
        });

        binding.tvSignUp.setOnClickListener(view -> fragmentTransactionHelper.navigateLoginFragmentToSignUpFragment());

        binding.tvForgotPassword.setOnClickListener(view -> forgotPasswordDialog.showDialog());
    }

    private void signInAnonymously() {
        binding.progressBar.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously()
                .addOnCompleteListener(requireActivity(), task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        fragmentTransactionHelper.navigateLoginFragmentToHomeFragment(requireActivity());
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(requireContext(), "Guest Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void firebaseLogin(String email, String password) {
        binding.progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "firebaseLogin: ");
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        Toast.makeText(context, "Login success", Toast.LENGTH_SHORT).show();
                        fragmentTransactionHelper.navigateLoginFragmentToHomeFragment(requireActivity());
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(context, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}