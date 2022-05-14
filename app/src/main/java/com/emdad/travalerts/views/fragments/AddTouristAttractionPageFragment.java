package com.emdad.travalerts.views.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;

import com.emdad.travalerts.databinding.FragmentAddTouristAttractionPageBinding;
import com.emdad.travalerts.utils.FragmentTransactionHelper;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.emdad.travalerts.utils.StaticValues.DB_PATH_PLACE;
import static com.emdad.travalerts.utils.StaticValues.GOOGLE_API_KEY;

public class AddTouristAttractionPageFragment extends Fragment {
    private static final String TAG = "AddTouristAttractionPag";
    private FragmentAddTouristAttractionPageBinding binding;
    private com.emdad.travalerts.models.Place place;
    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FragmentTransactionHelper fragmentTransactionHelper;
    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    private PlacesClient placesClient;


    public AddTouristAttractionPageFragment() {
        // Required empty public constructor
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    // Handle the Intent
                    Log.d(TAG, " on activity result on fragment... RESULT OK" + intent.getData().toString());
                }
                Log.d(TAG, " on activity result on fragment...");
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddTouristAttractionPageBinding.inflate(inflater, container, false);
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
            binding.guestUI.llGuestUI.setOnClickListener(view -> fragmentTransactionHelper.logout(getActivity()));
        } else {
            binding.guestUI.llGuestUI.setVisibility(View.GONE);
        }
    }

    private void init(View view) {
        place = new com.emdad.travalerts.models.Place();
        context = view.getContext();
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        fragmentTransactionHelper = new FragmentTransactionHelper(getParentFragment());


        // Initialize Places.
        Places.initialize(getActivity().getApplicationContext(), GOOGLE_API_KEY);

        // Create a new Places client instance.
        placesClient = Places.createClient(context);

    }

    private void initializeClickListeners() {

        binding.btnChooseLocation.setOnClickListener(view -> {
            // Set the fields to specify which types of place data to return.
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(getActivity());
//            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
            mStartForResult.launch(intent);

        });

        binding.seekBarRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                place.setRange(seekBar.getProgress());
                binding.tvSeekBarValue.setText(String.format("Range: %d km", seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                place.setRange(seekBar.getProgress());
                binding.tvSeekBarValue.setText(String.format("Range: %d km", seekBar.getProgress()));
            }
        });

        binding.btnAddPlace.setOnClickListener(view -> {
            String name = binding.etPlaceName.getText().toString().trim();
            String description = binding.etPlaceDescription.getText().toString().trim();
            String address = binding.etAddress.getText().toString().trim();
            String image = binding.etImageLink.getText().toString().trim();

            if (TextUtils.isEmpty(name)
                    && TextUtils.isEmpty(description)
                    && TextUtils.isEmpty(address)
                    && TextUtils.isEmpty(image)) {
                Toast.makeText(context, "Please input all fields", Toast.LENGTH_SHORT).show();
            } else {
                place.setName(name);
                place.setDescription(description);
                place.setAddress(address);
                place.setImage(image);
                place.setUserId(mAuth.getUid());

                savePlace();
            }

        });

    }

    private void savePlace() {
        Log.d(TAG, "savePlace: " + place.toString());

        firestore.collection(DB_PATH_PLACE).add(place).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "savePlace: success. " + documentReference.getId());
            Toast.makeText(context, "Place added", Toast.LENGTH_SHORT).show();
            fragmentTransactionHelper.navigateAddTouristAttractionFragmentToTouristAttractionDetailsFragment();
        });

    }


}