package com.emdad.travalerts.views.fragments;

import android.content.Context;
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
import com.emdad.travalerts.adapter.TouristAttractionAdapter;
import com.emdad.travalerts.databinding.FragmentHomeBinding;
import com.emdad.travalerts.models.Place;
import com.emdad.travalerts.utils.FragmentTransactionHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import static com.emdad.travalerts.adapter.TouristAttractionAdapter.HOME_FRAGMENT;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_PLACE_CREATED_AT;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_PLACE;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;
    private List<Place> placeList;
    private FirebaseFirestore firestore;
    private Context context;
    private TouristAttractionAdapter touristAttractionAdapter;

    public HomeFragment() {
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
        Log.d(TAG, "onCreateView: ");
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        getData();
    }

    private void init(View view) {
        context = view.getContext();
        firestore = FirebaseFirestore.getInstance();
        placeList = new ArrayList<>();
        FragmentTransactionHelper fragmentTransactionHelper = new FragmentTransactionHelper(getParentFragment());

        touristAttractionAdapter = new TouristAttractionAdapter(HOME_FRAGMENT, getActivity(), context, fragmentTransactionHelper, placeList);

        binding.rvHomeFeed.setAdapter(touristAttractionAdapter);
        binding.rvHomeFeed.setLayoutManager(new LinearLayoutManager(view.getContext()));
        // refresh page
        binding.swipeRefresh.setOnRefreshListener(this::getData);
    }

    private void getData() {
        binding.swipeRefresh.setRefreshing(true);
        firestore.collection(DB_PATH_PLACE)
                .orderBy(DB_FIELD_PLACE_CREATED_AT, Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            binding.swipeRefresh.setRefreshing(false);
            if (!queryDocumentSnapshots.isEmpty()) {
                placeList.clear();
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                for (DocumentSnapshot d : list) {

                    Place p = d.toObject(Place.class);
                    p.setId(d.getId());
                    placeList.add(p);

                }
                touristAttractionAdapter.notifyDataSetChanged();

            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "getHomeFeed: ERROR: " + e.getLocalizedMessage());
            binding.swipeRefresh.setRefreshing(false);
            Toast.makeText(context, getResources().getString(R.string.toast_message_something_went_wrong), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}