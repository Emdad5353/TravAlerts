package com.emdad.travalerts.views.fragments.auth;

import android.app.Activity;
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
import com.emdad.travalerts.databinding.FragmentSignupBinding;
import com.emdad.travalerts.utils.FragmentTransactionHelper;
import com.emdad.travalerts.utils.InputValidations;
import com.emdad.travalerts.views.activities.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.emdad.travalerts.utils.StaticValues.DB_KEY_EMAIL;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_FIRST_NAME;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_ID;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_LAST_NAME;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_PHONE_NO;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_USER;

public class SignUpFragment extends Fragment {
    private static final String TAG = "SignUpFragment";
    private FragmentSignupBinding binding;
    private FragmentTransactionHelper fragmentTransactionHelper;
    private Context context;
    private FirebaseAuth mAuth;

    public SignUpFragment() {
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
        binding = FragmentSignupBinding.inflate(inflater, container, false);
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
        mAuth = FirebaseAuth.getInstance();
        fragmentTransactionHelper = new FragmentTransactionHelper(getParentFragment());
    }

    private void initClickListeners() {
        binding.btnSignUp.setOnClickListener(view -> {
            String email = binding.tiETEmail.getEditableText().toString();
            String password = binding.tiETPassword.getEditableText().toString();
            String confPassword = binding.tiETConfirmPassword.getEditableText().toString();
            String firstName = binding.tiETFirstName.getEditableText().toString();
            String lastName = binding.tiETLastName.getEditableText().toString();
            String phoneNo = binding.tiETPhoneNumber.getEditableText().toString();
            if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName) && TextUtils.isEmpty(email) && TextUtils.isEmpty(phoneNo)) {
                Toast.makeText(context, "Please input all field.", Toast.LENGTH_SHORT).show();
                binding.tiETFirstName.requestFocus();
            }
            // here the parameter context for showing the Toast msg
            else if (InputValidations.emailValidation(context, email) && InputValidations.isValidPasswordLength(context, password)) {
                if (password.equals(confPassword)) {
                    createUser(firstName, lastName, email, password, phoneNo);
                } else {
                    Toast.makeText(context, "Confirm password not matched", Toast.LENGTH_SHORT).show();
                    binding.tiETConfirmPassword.requestFocus();
                }
            }
        });
        binding.tvLogin.setOnClickListener(view -> fragmentTransactionHelper.navigateSignUpFragmentToLoginFragment());
    }

    private void createUser(String firstName, String lastName, String email, String password, String phoneNo) {
        binding.progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        saveUserInfoToFirebaseDB(firstName, lastName, email, phoneNo);

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(context, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserInfoToFirebaseDB(String firstName, String lastName, String email, String phoneNo) {
        binding.progressBar.setVisibility(View.VISIBLE);
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put(DB_KEY_ID, uid);
        userInfo.put(DB_KEY_FIRST_NAME, firstName);
        userInfo.put(DB_KEY_LAST_NAME, lastName);
        userInfo.put(DB_KEY_EMAIL, email);
        userInfo.put(DB_KEY_PHONE_NO, phoneNo);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(DB_PATH_USER).document(uid)
                .set(userInfo)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                    Toast.makeText(context, "The user has been registered ",
                            Toast.LENGTH_SHORT).show();
                    fragmentTransactionHelper.navigateSignUpFragmentToHomeFragment(requireActivity());
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "Error writing document", e);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}