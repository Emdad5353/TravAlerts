package com.emdad.travalerts.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.emdad.travalerts.R;
import com.emdad.travalerts.databinding.FragmentProfileBinding;
import com.emdad.travalerts.models.User;
import com.emdad.travalerts.utils.FragmentTransactionHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

import static com.emdad.travalerts.utils.StaticValues.DB_KEY_EMAIL;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_USER_ID;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_USER;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private Context context;
    private FragmentTransactionHelper fragmentTransactionHelper;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    public ProfileFragment() {
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
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        initializeClickListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Objects.requireNonNull(mAuth.getCurrentUser()).isAnonymous()) {
            binding.guestUI.llGuestUI.setVisibility(View.VISIBLE);
            binding.guestUI.btnGoToLogin.setOnClickListener(view -> fragmentTransactionHelper.logout(getActivity()));
        } else {
            binding.guestUI.llGuestUI.setVisibility(View.GONE);
            getData();
        }
    }

    private void init(View view) {
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        context = view.getContext();
        fragmentTransactionHelper = new FragmentTransactionHelper(getParentFragment());
        binding.swipeRefresh.setOnRefreshListener(() -> getData());

        getData();
    }

    private void getData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d(TAG, "getData: " + currentUser.getUid());
        binding.swipeRefresh.setRefreshing(true);
        firestore.collection(DB_PATH_USER).whereEqualTo(DB_KEY_EMAIL, currentUser.getEmail()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            binding.swipeRefresh.setRefreshing(false);
            if (!queryDocumentSnapshots.isEmpty()) {
                User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                Log.d(TAG, "getData: " + user.toString());

                if (!TextUtils.isEmpty(user.getProfile_pic())) {
                    Glide.with(context)
                            .load(user.getProfile_pic())
                            .transform(new CircleCrop())
                            .into(binding.ivProfilePic);
                }
                binding.tvName.setText(String.format("%s %s", user.getFirst_name(), user.getLast_name()));
                binding.tvPhone.setText(user.getPhoneNo());
            }
        });

        binding.tvEmail.setText(currentUser.getEmail());
    }

    private void initializeClickListeners() {
        binding.tvProfileInfo.setOnClickListener(view -> {
            fragmentTransactionHelper.navigateProfileFragmentToEditProfileActivity();
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}