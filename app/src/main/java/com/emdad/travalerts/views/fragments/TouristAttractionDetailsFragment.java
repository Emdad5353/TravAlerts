package com.emdad.travalerts.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.emdad.travalerts.R;
import com.emdad.travalerts.databinding.FragmentTouristAttractionDetailsBinding;
import com.emdad.travalerts.models.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.emdad.travalerts.utils.StaticValues.KEY_PLACE_OBJECT;

public class TouristAttractionDetailsFragment extends Fragment implements OnMapReadyCallback {
    private FragmentTouristAttractionDetailsBinding binding;
    private Place place;
    private GoogleMap mMap;

    public TouristAttractionDetailsFragment() {
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

        binding = FragmentTouristAttractionDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        place = getArguments() != null ? getArguments().getParcelable(KEY_PLACE_OBJECT) : null;

        init(view);
    }

    private void init(View view) {
        if (place != null) {
            binding.tvPlaceName.setText(place.getName());
            binding.tvAddress.setText(place.getAddress());
            binding.tvDescription.setText(place.getDescription());
            binding.tvLocationFull.setText(place.getName());
            binding.tvPlaceName.setText(place.getName());
            binding.rating.setRating(place.getRating());


            Glide.with(view.getContext())
                    .load(place.getImage())
                    .transform(new CenterInside())
                    .into(binding.ivPlaceImage);

            // Get the SupportMapFragment and request notification when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(TouristAttractionDetailsFragment.this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (place != null) {
            LatLng latLng = new LatLng(place.getGeo_location().getLatitude(), place.getGeo_location().getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(place.getName()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }
}