package com.emdad.travalerts.views.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.emdad.travalerts.R;
import com.emdad.travalerts.databinding.ActivityAddPlaceBinding;
import com.emdad.travalerts.utils.GeoFenceHelper;
import com.emdad.travalerts.utils.Sequence;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import static com.emdad.travalerts.utils.StaticValues.DB_PATH_PLACE;
import static com.emdad.travalerts.utils.StaticValues.GOOGLE_API_KEY;

public class AddPlaceActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "AddPlaceActivity";
    private ActivityAddPlaceBinding binding;
    private com.emdad.travalerts.models.Place place;
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

    // Get Places for adding GeoFence
    private List<com.emdad.travalerts.models.Place> placeList;

    // GeoFencing
    private GeofencingClient geofencingClient;
    private GeoFenceHelper geoFenceHelper;
    private final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;

    // get device location
    private boolean locationPermissionGranted;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // The entry point to the Places API.
    private PlacesClient placesClient;

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPlaceBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        init();

        initializeClickListeners();

        initGeoFencing();
    }

    private void initGeoFencing() {
        geofencingClient = LocationServices.getGeofencingClient(this);
        geoFenceHelper = new GeoFenceHelper(this);
        enableUserLocation();
    }


    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "enableUserLocation:ACCESS_FINE_LOCATION = PERMISSION_GRANTED");
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    private void addGeoFence() {

//        String geoFenceId, LatLng latLng, int radius
        Geofence geoFence;
        geoFence = geoFenceHelper.getGeoFence(
                place.getId(),
                new LatLng(place.getGeo_location().getLatitude(), place.getGeo_location().getLongitude()),
                place.getRange(),
                Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_DWELL
                        | Geofence.GEOFENCE_TRANSITION_EXIT);

        GeofencingRequest geofencingRequest = geoFenceHelper.getGeoFencingRequest(geoFence);
        int requestCode = Sequence.nextValue();
        PendingIntent pendingIntent = geoFenceHelper.getPendingIntent(requestCode);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: Geofence Added..."))
                .addOnFailureListener(e -> {
                    String errorMessage = geoFenceHelper.getErrorString(e);
                    Log.d(TAG, "onFailure: " + errorMessage);
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (Objects.requireNonNull(mAuth.getCurrentUser()).isAnonymous()) {
            binding.guestUI.llGuestUI.setVisibility(View.VISIBLE);
            binding.guestUI.btnGoToLogin.setOnClickListener(view -> {
                // logout
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AddPlaceActivity.this, AuthActivity.class));
                finish();
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

    private void updateUI(String placeName, String address, double latitude, double longitude) {
        binding.etPlaceName.setText(placeName);
        binding.tilPlaceName.setVisibility(View.VISIBLE);
        binding.etAddress.setText(address);
        binding.tilAddress.setVisibility(View.VISIBLE);

        // update map
        addMarkerAndCircleOnMap(placeName, latitude, longitude, place.getRange());
    }

    private void updateModel() {
        place.setGeo_location(new GeoPoint(mPlace.getLatLng().latitude, mPlace.getLatLng().longitude));
        place.setName(mPlace.getName());
        place.setAddress(mPlace.getAddress());
    }

    private void updateModel(double latitude, double longitude, String placeName, String address) {
        place.setGeo_location(new GeoPoint(latitude, longitude));
        place.setName(placeName);
        place.setAddress(address);
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

        getSupportActionBar().setTitle(R.string.add_tourist_attraction);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        place = new com.emdad.travalerts.models.Place();
        place.setRange(100);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize Places.
        Places.initialize(getApplicationContext(), GOOGLE_API_KEY);

        placesClient = Places.createClient(this);


        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


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
                    Toast.makeText(AddPlaceActivity.this, "Place Minimum radius is 50m !", Toast.LENGTH_SHORT).show();
                }
                place.setRange(seekBar.getProgress());
                binding.tvSeekBarValue.setText(String.format("Radius: %d m", seekBar.getProgress()));
                if (mMap != null && mPlace != null) {
                    mMap.clear();
                    addMarkerAndCircleOnMap(mPlace.getName(), mPlace.getLatLng().latitude, mPlace.getLatLng().longitude, place.getRange());
                } else if (mMap != null && place != null && lastKnownLocation != null) {
                    mMap.clear();
                    addMarkerAndCircleOnMap(place.getName(), lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), place.getRange());
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
                if (TextUtils.isEmpty(place.getId())) place.setId(UUID.randomUUID().toString());
                place.setName(name);
                place.setDescription(description);
                place.setAddress(address);
                place.setUserId(mAuth.getUid());
                place.setCrated_at(String.valueOf(Calendar.getInstance().getTimeInMillis()));
                place.setUpdated_at(String.valueOf(Calendar.getInstance().getTimeInMillis()));
                uploadImageAndSavePlace();
            }

        });

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
                                            .makeText(AddPlaceActivity.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    savePlace();
                                });
                            })

                    .addOnFailureListener(e -> {

                        // Error, Image not uploaded
                        progressDialog.dismiss();
                        Toast
                                .makeText(AddPlaceActivity.this,
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
            Toast.makeText(this, "Please choose an image.", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePlace() {
        Log.d(TAG, "savePlace: " + place.toString());

        addGeoFence();

        firestore.collection(DB_PATH_PLACE).add(place).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "savePlace: success. " + documentReference.getId());
            Toast.makeText(this, "Place added", Toast.LENGTH_SHORT).show();
            finish();
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
        if (mPlace != null) {
            addMarkerAndCircleOnMap(mPlace.getName(), mPlace.getLatLng().latitude, mPlace.getLatLng().longitude, place.getRange());
        }

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                // get address and update ui and save info into model
                                getAddress(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
//                                showCurrentPlace();
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;

                }
            }
        }
        updateLocationUI();
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        Toast.makeText(geoFenceHelper, "showCurrentPlace()", Toast.LENGTH_SHORT).show();
        if (mMap == null) {
            return;
        }

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FindCurrentPlaceResponse likelyPlaces = task.getResult();

                    // Set the count, handling cases where less than 5 entries are returned.
                    int count;
                    if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                        count = likelyPlaces.getPlaceLikelihoods().size();
                    } else {
                        count = M_MAX_ENTRIES;
                    }

                    int i = 0;
                    likelyPlaceNames = new String[count];
                    likelyPlaceAddresses = new String[count];
                    likelyPlaceAttributions = new List[count];
                    likelyPlaceLatLngs = new LatLng[count];

                    for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                        // Build a list of likely places to show the user.
                        likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                        likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                        likelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                .getAttributions();
                        likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                        i++;
                        if (i > (count - 1)) {
                            break;
                        }
                    }

                    // Show a dialog offering the user the list of likely places, and add a
                    // marker at the selected place.
                    AddPlaceActivity.this.openPlacesDialog();
                } else {
                    Log.e(TAG, "Exception: %s", task.getException());
                }
            });

        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            mMap.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(defaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getAddress(likelyPlaceLatLngs[which].latitude, likelyPlaceLatLngs[which].longitude);
                /*
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = likelyPlaceLatLngs[which];

                String markerSnippet = likelyPlaceAddresses[which];
                if (likelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + likelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                mMap.addMarker(new MarkerOptions()
                        .title(likelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));*/
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(likelyPlaceNames, listener)
                .show();
    }


    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    public void getAddress(double lat, double lng) {

        Geocoder geocoder = new Geocoder(AddPlaceActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0);
//            add = add + "\n" + obj.getCountryName();
//            add = add + "\n" + obj.getCountryCode();
//            add = add + "\n" + obj.getAdminArea();
//            add = add + "\n" + obj.getPostalCode();
//            add = add + "\n" + obj.getSubAdminArea();
//            add = add + "\n" + obj.getLocality();
//            add = add + "\n" + obj.getSubThoroughfare();

            Log.v("IGA", "Address" + add);

            updateUI(obj.getLocality(), add, lat, lng);
            updateModel(lat, lng, obj.getLocality(), add);
            // Toast.makeText(this, "Address=>" + add,
            // Toast.LENGTH_SHORT).show();

            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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