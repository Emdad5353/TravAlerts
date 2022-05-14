package com.emdad.travalerts.views.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.emdad.travalerts.R;
import com.emdad.travalerts.databinding.ActivityEditPlaceBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_PLACE_ADDRESS;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_PLACE_DESCRIPTION;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_PLACE_GEO_LOCATION;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_PLACE_ID;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_PLACE_IMAGE;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_PLACE_NAME;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_PLACE_RANGE;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_PLACE_UPDATED_AT;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_POST_PLACE_ID;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_PLACE;
import static com.emdad.travalerts.utils.StaticValues.GOOGLE_API_KEY;
import static com.emdad.travalerts.utils.StaticValues.KEY_GEO_POINT_LATITUDE;
import static com.emdad.travalerts.utils.StaticValues.KEY_GEO_POINT_LONGITUDE;
import static com.emdad.travalerts.utils.StaticValues.KEY_PLACE_OBJECT;

public class EditPlaceActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "EditPlaceActivity";
    private ActivityEditPlaceBinding binding;
    private com.emdad.travalerts.models.Place place;
    private GeoPoint geoPoint;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private final int AUTOCOMPLETE_REQUEST_CODE = 1;
    private Place mPlace;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private CircleOptions mCircleOptions;

    // Uri indicates, where the image will be picked from
    private Uri filePath;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditPlaceBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getSupportActionBar().setTitle("Edit Place");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        place = getIntent().getParcelableExtra(KEY_PLACE_OBJECT);
        double lat = getIntent().getDoubleExtra(KEY_GEO_POINT_LATITUDE, 0.0);
        double lng = getIntent().getDoubleExtra(KEY_GEO_POINT_LONGITUDE, 0.0);
        geoPoint = new GeoPoint(lat, lng);

        if (place != null) {
            init();
            initializeClickListeners();
        } else {
            Toast.makeText(this, "Something went wrong!.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Objects.requireNonNull(mAuth.getCurrentUser()).isAnonymous()) {
            binding.guestUI.llGuestUI.setVisibility(View.VISIBLE);
            binding.guestUI.llGuestUI.setOnClickListener(view -> {
                // logout
            });
        } else {
            binding.guestUI.llGuestUI.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Google Map Place Picker Result
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mPlace = Autocomplete.getPlaceFromIntent(data);
                Log.d(TAG, "onActivityResult: " + mPlace.toString());
                Log.i(TAG, "Place: " + mPlace.getName() + ", " + mPlace.getId());

                updateUI();

                updateModel();

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.d(TAG, "onActivityResult: The user canceled the operation.");
            }
        }
        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        else if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                binding.ivPlaceImage.setImageBitmap(bitmap);
                binding.ivPlaceImage.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    private void updateUI() {
        binding.etPlaceName.setText(mPlace.getName());
        binding.tilPlaceName.setVisibility(View.VISIBLE);
        binding.etAddress.setText(mPlace.getAddress());
        binding.tilAddress.setVisibility(View.VISIBLE);

        // update map
        addMarkerAndCircleOnMap(mPlace.getName(), mPlace.getLatLng().latitude, mPlace.getLatLng().longitude, place.getRange());

    }

    private void updateModel() {
        place.setGeo_location(new GeoPoint(mPlace.getLatLng().latitude, mPlace.getLatLng().longitude));
        place.setName(mPlace.getName());
        place.setAddress(mPlace.getAddress());
    }

    private CircleOptions getCircle(LatLng point, int range) {

        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();
        // Specifying the center of the circle
        circleOptions.center(point);
        // Radius of the circle
        circleOptions.radius(range);
        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);
        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);
        // Border width of the circle
        circleOptions.strokeWidth(2);
        // Adding the circle to the GoogleMap

        return circleOptions;
    }

    private void init() {
        // Set info to UI
        binding.etPlaceName.setText(place.getName());
        binding.etAddress.setText(place.getAddress());
        binding.etPlaceDescription.setText(place.getDescription());
        binding.seekBarRange.setProgress(place.getRange());
        binding.tvSeekBarValue.setText(String.format("Radius: %d km", place.getRange()));
        Glide.with(this)
                .load(place.getImage())
                .transform(new CenterInside())
                .into(binding.ivPlaceImage);


        // firebase initializations
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize Places.
        Places.initialize(getApplicationContext(), GOOGLE_API_KEY);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void initializeClickListeners() {

        // open place picker
        binding.btnChooseLocation.setOnClickListener(view -> {
            // Set the fields to specify which types of place data to return.
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.RATING, Place.Field.USER_RATINGS_TOTAL);

            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(this);
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

        });

        // set radius
        binding.seekBarRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (seekBar.getProgress() < 50) {
                    seekBar.setProgress(50);
                    place.setRange(50);
                    Toast.makeText(EditPlaceActivity.this, "Place Minimum radius is 50m !", Toast.LENGTH_SHORT).show();
                }
                place.setRange(seekBar.getProgress());
                binding.tvSeekBarValue.setText(String.format("Radius: %d km", seekBar.getProgress()));
                if (mMap != null) {
                    mMap.clear();
                    if (mPlace == null) {
                        addMarkerAndCircleOnMap(place.getName(), geoPoint.getLatitude(), geoPoint.getLongitude(), place.getRange());
                    } else {
                        addMarkerAndCircleOnMap(mPlace.getName(), mPlace.getLatLng().latitude, mPlace.getLatLng().longitude, place.getRange());
                    }
                } else
                    Toast.makeText(seekBar.getContext(), "Please choose location first.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                place.setRange(seekBar.getProgress());
                binding.tvSeekBarValue.setText(String.format("Radius: %dm", seekBar.getProgress()));
            }
        });

        binding.btnChooseImage.setOnClickListener(view -> selectImage());

        // save place to firestore and go to home
        binding.btnAddPlace.setOnClickListener(view -> {
            String name = binding.etPlaceName.getText().toString().trim();
            String description = binding.etPlaceDescription.getText().toString().trim();
            String address = binding.etAddress.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                if (mPlace == null) {
                    Toast.makeText(this, "Please choose location", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Please input place name", Toast.LENGTH_SHORT).show();
                }

            } else if (TextUtils.isEmpty(description)) {
                Toast.makeText(this, "Please input description", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(address)) {
                Toast.makeText(this, "Please input address", Toast.LENGTH_SHORT).show();
            } else if (binding.ivPlaceImage.getVisibility() != View.VISIBLE) {
                Toast.makeText(this, "Please choose an image", Toast.LENGTH_SHORT).show();
            } else {
                place.setName(name);
                place.setDescription(description);
                place.setAddress(address);
                place.setUserId(mAuth.getUid());
                uploadImageAndSavePlace();
            }

        });

    }

    private void addMarkerAndCircleOnMap(String placeName, double lat, double lng, int range) {
        if (mMap != null) {
            float zoomLevel = 13.0f; //This goes up to 21
            LatLng latLng = new LatLng(lat, lng);
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
            mMap.addCircle(getCircle(latLng, range));
        }
    }

    // Select Image method
    private void selectImage() {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    // UploadImage method
    private void uploadImageAndSavePlace() {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            // Progress Listener for loading
            // percentage on the dialog box
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            taskSnapshot -> {
                                // Image uploaded successfully
                                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                                task.addOnSuccessListener(uri -> {
                                    Log.d(TAG, "uploadImageToFirebaseStorage: " + uri.toString());
                                    place.setImage(uri.toString());
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(EditPlaceActivity.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    updatePlace();
                                });
                            })

                    .addOnFailureListener(e -> {

                        // Error, Image not uploaded
                        progressDialog.dismiss();
                        Toast
                                .makeText(EditPlaceActivity.this,
                                        "Failed " + e.getMessage(),
                                        Toast.LENGTH_SHORT)
                                .show();
                    })
                    .addOnProgressListener(
                            taskSnapshot -> {
                                double progress
                                        = (100.0
                                        * taskSnapshot.getBytesTransferred()
                                        / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage(
                                        "Uploaded "
                                                + (int) progress + "%");
                            });
        } else {
            updatePlace();
        }
    }

    private void updatePlace() {
        Log.d(TAG, "savePlace: " + place.toString());

        firestore.collection(DB_PATH_PLACE).whereEqualTo(DB_FIELD_PLACE_ID, place.getId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {

                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                String documentId = documentSnapshot.getId();

                // create a map object with updated input
                Map<String, Object> placeMap = new HashMap<>();
                placeMap.put(DB_FIELD_PLACE_NAME, place.getName());
                placeMap.put(DB_FIELD_PLACE_DESCRIPTION, place.getDescription());
                placeMap.put(DB_FIELD_PLACE_IMAGE, place.getImage());
                placeMap.put(DB_FIELD_PLACE_ADDRESS, place.getAddress());
                placeMap.put(DB_FIELD_PLACE_GEO_LOCATION, mPlace == null ? geoPoint : new GeoPoint(mPlace.getLatLng().latitude, mPlace.getLatLng().longitude));
                placeMap.put(DB_FIELD_PLACE_RANGE, place.getRange());
                placeMap.put(DB_FIELD_PLACE_UPDATED_AT, String.valueOf(Calendar.getInstance().getTimeInMillis()));


                firestore.collection(DB_PATH_PLACE).document(documentId).update(placeMap).addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditPlaceActivity.this, "Place Updated", Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> Toast.makeText(EditPlaceActivity.this, "Failed to update", Toast.LENGTH_SHORT).show());


            } else {
                Toast.makeText(EditPlaceActivity.this, "Place not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        if (place != null && geoPoint != null) {
            addMarkerAndCircleOnMap(place.getName(), geoPoint.getLatitude(), geoPoint.getLongitude(), place.getRange());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}