package com.emdad.travalerts.views.fragments;

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

import com.emdad.travalerts.databinding.FragmentEditProfileBinding;
import com.emdad.travalerts.models.User;
import com.emdad.travalerts.utils.FragmentTransactionHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.emdad.travalerts.utils.StaticValues.DB_KEY_EMAIL;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_FIRST_NAME;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_ID;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_LAST_NAME;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_PHONE_NO;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_USER;

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";
    private FragmentEditProfileBinding binding;
    private User user;
    private FragmentTransactionHelper fragmentTransactionHelper;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private Context context;

    public EditProfileFragment() {
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
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        initClickListeners();

        getData();
    }

    private void init(View view) {
        context = view.getContext();
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void initClickListeners() {
        binding.btnSignUp.setOnClickListener(view -> updateUser());
    }

    private void updateUser() {
        String firstName = binding.tiETFirstName.getEditableText().toString();
        String lastName = binding.tiETLastName.getEditableText().toString();
        String phoneNo = binding.tiETPhoneNumber.getEditableText().toString();
        if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName) && TextUtils.isEmpty(phoneNo)) {
            Toast.makeText(context, "Please input all field.", Toast.LENGTH_SHORT).show();
            binding.tiETFirstName.requestFocus();
        }
        // here the parameter context for showing the Toast msg
        else {
            updateUserToFirebase(firstName, lastName, phoneNo);
        }
    }

    private void updateUserToFirebase(String firstName, String lastName, String phoneNo) {
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put(DB_KEY_FIRST_NAME, firstName);
        userInfo.put(DB_KEY_LAST_NAME, lastName);
        userInfo.put(DB_KEY_PHONE_NO, phoneNo);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(DB_PATH_USER).document(uid)
                .update(userInfo)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                    Toast.makeText(context, "The user info updated",
                            Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "Error writing document", e);
                });
    }

    private void getData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d(TAG, "getData: " + currentUser.getUid());
        firestore.collection(DB_PATH_USER).whereEqualTo(DB_KEY_EMAIL, currentUser.getEmail()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                Log.d(TAG, "getData: " + user.toString());

//                Glide.with(context)
//                        .load(currentUser.getPhotoUrl())
//                        .transform(new CircleCrop())
//                        .into(binding.ivProfilePic);
                binding.tiETFirstName.setText(user.getFirst_name());
                binding.tiETLastName.setText(user.getLast_name());
                binding.tiETPhoneNumber.setText(user.getPhoneNo());
            }
        });
    }
}