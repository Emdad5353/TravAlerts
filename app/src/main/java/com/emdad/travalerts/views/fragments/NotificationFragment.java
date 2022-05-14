package com.emdad.travalerts.views.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.emdad.travalerts.R;
import com.emdad.travalerts.adapter.NotificationAdapter;
import com.emdad.travalerts.databinding.FragmentNotificationBinding;
import com.emdad.travalerts.models.Notification;
import com.emdad.travalerts.models.Place;
import com.emdad.travalerts.utils.FragmentTransactionHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_NOTIFICATION_CREATED_AT;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_PLACE_CREATED_AT;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_USER_ID;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_NOTIFICATION;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_PLACE;

public class
NotificationFragment extends Fragment {
    private static final String TAG = "NotificationFragment";
    private FragmentNotificationBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FragmentTransactionHelper fragmentTransactionHelper;
    private ArrayList<Notification> notificationList;
    private NotificationAdapter notificationAdapter;

    public NotificationFragment() {
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
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        getData();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (Objects.requireNonNull(mAuth.getCurrentUser()).isAnonymous()) {
            binding.guestUI.llGuestUI.setVisibility(View.VISIBLE);
            binding.guestUI.btnGoToLogin.setOnClickListener(view -> fragmentTransactionHelper.logout(getActivity()));
        } else {
            binding.guestUI.llGuestUI.setVisibility(View.GONE);
        }
    }

    private void init(View view) {

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        fragmentTransactionHelper = new FragmentTransactionHelper(getParentFragment());
        notificationList = new ArrayList<>();


        notificationAdapter = new NotificationAdapter(getActivity(), notificationList);
        binding.rvNotification.setAdapter(notificationAdapter);
        binding.rvNotification.setLayoutManager(new LinearLayoutManager(view.getContext()));

        binding.swipeRefresh.setOnRefreshListener(() -> {
            // refresh page
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void getData() {
        binding.swipeRefresh.setRefreshing(true);
        firestore.collection(DB_PATH_NOTIFICATION)
                .whereEqualTo(DB_KEY_USER_ID, mAuth.getCurrentUser().getUid())
                .orderBy(DB_FIELD_NOTIFICATION_CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.swipeRefresh.setRefreshing(false);
                    if (!queryDocumentSnapshots.isEmpty()) {
                        notificationList.clear();
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                        for (DocumentSnapshot d : list) {

                            Notification p = d.toObject(Notification.class);
                            p.setId(d.getId());
                            notificationList.add(p);

                        }
                        notificationAdapter.notifyDataSetChanged();

                    }
                }).addOnFailureListener(e -> {
            Log.d(TAG, "getHomeFeed: ERROR: " + e.getLocalizedMessage());
            binding.swipeRefresh.setRefreshing(false);
            Toast.makeText(getContext(), getResources().getString(R.string.toast_message_something_went_wrong), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}