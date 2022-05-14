package com.emdad.travalerts.views.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.emdad.travalerts.R;
import com.emdad.travalerts.adapter.ExperiencePostAdapter;
import com.emdad.travalerts.databinding.ActivityPlaceDetailsBinding;
import com.emdad.travalerts.models.Place;
import com.emdad.travalerts.models.Post;
import com.emdad.travalerts.models.User;
import com.emdad.travalerts.utils.DateTimeHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_PLACE_ID;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_POST_CREATED_AT;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_POST_PLACE_ID;
import static com.emdad.travalerts.utils.StaticValues.DB_FIELD_POST_USER_ID;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_EMAIL;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_PLACE;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_POST;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_USER;
import static com.emdad.travalerts.utils.StaticValues.KEY_GEO_POINT_LATITUDE;
import static com.emdad.travalerts.utils.StaticValues.KEY_GEO_POINT_LONGITUDE;
import static com.emdad.travalerts.utils.StaticValues.KEY_PLACE_ID;
import static com.emdad.travalerts.utils.StaticValues.KEY_PLACE_OBJECT;

public class PlaceDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "PlaceDetailsActivity";
    private Place place;
    private String placeId;
    private GeoPoint geoPoint;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private ActivityPlaceDetailsBinding binding;
    private GoogleMap mMap;
    private ExperiencePostAdapter experiencePostAdapter;

    // Uri indicates, where the image will be picked from
    private Uri filePath;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;

    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    private Post post;
    private User user;
    private ArrayList<Post> postList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlaceDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        place = getIntent().getParcelableExtra(KEY_PLACE_OBJECT);
        double lat = getIntent().getDoubleExtra(KEY_GEO_POINT_LATITUDE, 0.0);
        double lng = getIntent().getDoubleExtra(KEY_GEO_POINT_LONGITUDE, 0.0);

        placeId = getIntent().getStringExtra(KEY_PLACE_ID);

        if (place == null && !TextUtils.isEmpty(placeId)) {
            init();
            Log.d(TAG, "onCreate:  placeId");
            getPlaceInfById();
        } else {
            Log.d(TAG, "onCreate: Place");
            geoPoint = new GeoPoint(lat, lng);

            init();

            initializeClickListeners();

            getMyInfo();
        }


    }

    private void getPlaceInfById() {
        Log.d(TAG, "getPlaceInfById: ");
        firestore = FirebaseFirestore.getInstance();
        firestore.collection(DB_PATH_PLACE)
                .document(placeId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.exists()) {
                place = queryDocumentSnapshots.toObject(Place.class);
                Log.d(TAG, "getData: " + place.toString());

                geoPoint = new GeoPoint(place.getGeo_location().getLatitude(), place.getGeo_location().getLongitude());

                init();

                initializeClickListeners();

                getMyInfo();

            } else {
                Toast.makeText(this, "No Place found by ID ("+placeId+")", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getMyInfo() {
        firestore.collection(DB_PATH_USER).whereEqualTo(DB_KEY_EMAIL, mAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                Log.d(TAG, "getData: " + user.toString());

                binding.mySharedExperience.tvName.setText(user.getFirst_name() + " " + user.getLast_name());

                if (!TextUtils.isEmpty(user.getProfile_pic())) {
                    Glide.with(this)
                            .load(user.getProfile_pic())
                            .transform(new CircleCrop())
                            .into(binding.mySharedExperience.ivProfilePic);
                }

            }
        });
    }

    public void init() {
        Log.d(TAG, "init: ");
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (place != null) {
            Log.d(TAG, "place =>> init: " + place.toString());
            getSupportActionBar().setTitle(place.getName());

            binding.progressBar.setVisibility(View.GONE);
            binding.nsvPlaceDetails.setVisibility(View.VISIBLE);

            binding.tvPlaceName.setText(place.getName());
            binding.tvAddress.setText(place.getAddress());
            binding.tvDescription.setText(place.getDescription());
            binding.rating.setRating(place.getRating());

            Glide.with(this)
                    .load(place.getImage())
                    .transform(new CenterInside())
                    .into(binding.ivPlaceImage);

            // Get the SupportMapFragment and request notification when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


            getMyRating();

            postList = new ArrayList<>();
            experiencePostAdapter = new ExperiencePostAdapter(this, postList);
            binding.rvSharedExperiences.setAdapter(experiencePostAdapter);
            binding.rvSharedExperiences.setLayoutManager(new LinearLayoutManager(this));

            getSharedExperiences();
        }
    }

    private void getSharedExperiences() {
        Log.d(TAG, "getSharedExperiences: " + place.getId());
        binding.tvSharedExperiences.setText("Shared Experiences: loading...");
        firestore.collection(DB_PATH_POST)
                .whereEqualTo(DB_FIELD_POST_PLACE_ID, place.getId())
                .orderBy(DB_FIELD_POST_CREATED_AT, Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.tvSharedExperiences.setText("Shared Experiences");
                    if (!queryDocumentSnapshots.isEmpty()) {
                        postList.clear();
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                        for (DocumentSnapshot d : list) {

                            Post p = d.toObject(Post.class);
                            p.setId(d.getId());
                            postList.add(p);

                        }

                        experiencePostAdapter.notifyDataSetChanged();

                        if (postList.size() == 0) {
                            binding.tvSharedExperiencesEmptyListMsg.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvSharedExperiencesEmptyListMsg.setVisibility(View.GONE);
                        }


                    }
                }).addOnFailureListener(e -> {
            Log.d(TAG, "getHomeFeed: ERROR: " + e.getLocalizedMessage());
            binding.tvSharedExperiences.setText("Shared Experiences");
            Toast.makeText(this, getResources().getString(R.string.toast_message_something_went_wrong), Toast.LENGTH_SHORT).show();
        });
    }

    private void getMyRating() {
        firestore.collection(DB_PATH_POST)
                .whereEqualTo(DB_FIELD_POST_PLACE_ID, place.getId())
                .whereEqualTo(DB_FIELD_POST_USER_ID, place.getUserId())
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            Log.d(TAG, "getMyRating: " + queryDocumentSnapshots.getDocuments());
            if (!queryDocumentSnapshots.isEmpty()) {
                // Already Commented my experience
                Post myPost = queryDocumentSnapshots.getDocuments().get(0).toObject(Post.class);
                post = myPost;

                binding.tvShareYourExperience.setText("Your Shared Experience");
                binding.cvShareExperience.setVisibility(View.GONE);
                binding.mySharedExperience.mySharedExperience.setVisibility(View.VISIBLE);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.BELOW, R.id.my_shared_experience);
                binding.tvSharedExperiences.setLayoutParams(params);


                binding.mySharedExperience.tvComment.setText(myPost.getComment());
                binding.mySharedExperience.myRatingExperience.setRating(myPost.getRating());
                binding.mySharedExperience.tvCreatedAt.setText(DateTimeHelper.getTimeAgo(myPost.getCreated_at(), Calendar.getInstance().getTimeInMillis()));
                if (myPost.getImage() != null) {

                    Glide.with(this)
                            .load(myPost.getImage())
                            .transform(new CenterInside())
                            .into(binding.mySharedExperience.ivPostImage);
                } else {
                    binding.mySharedExperience.ivPostImage.setVisibility(View.GONE);
                }


            } else {
                Log.d(TAG, "getMyRating: place is not rated yet by this user");
                binding.cvShareExperience.setVisibility(View.VISIBLE);
                binding.mySharedExperience.mySharedExperience.setVisibility(View.GONE);
            }
        });
    }

    private void initializeClickListeners() {

        binding.llUploadImage.setOnClickListener(view -> selectImage());

        binding.ratingExperience.setOnRatingBarChangeListener((ratingBar, v, b) -> {
            if (post == null) post = new Post();
            post.setRating(ratingBar.getRating());
        });

        binding.btnSubmit.setOnClickListener(view -> {
            String comment = binding.etExperience.getText().toString().trim();
            float rating = binding.ratingExperience.getRating();
            if (TextUtils.isEmpty(comment)) {
                Toast.makeText(this, "Write your experience.", Toast.LENGTH_SHORT).show();
                binding.etExperience.requestFocus();
            } else if (rating == 0.0) {
                Toast.makeText(this, "Rating required.", Toast.LENGTH_SHORT).show();
                binding.ratingExperience.requestFocus();
            } else {
                if (post == null) {
                    post = new Post();
                    post.setId(UUID.randomUUID().toString());
                }
                ;
                post.setPlace_id(place.getId());
                post.setComment(comment);
                post.setRating(rating);
                post.setVisitor(Objects.requireNonNull(mAuth.getCurrentUser()).isAnonymous());
                if (!post.isVisitor()) {
                    post.setUser_id(mAuth.getCurrentUser().getUid());
                }
                post.setCreated_at(Calendar.getInstance().getTimeInMillis());
                uploadImageAndSavePost();
            }
        });

        binding.mySharedExperience.ivEditPost.setOnClickListener(view -> {
            if (post != null) {
                binding.etExperience.setText(post.getComment());
                binding.ratingExperience.setRating(post.getRating());
                binding.cvShareExperience.setVisibility(View.VISIBLE);
                binding.mySharedExperience.mySharedExperience.setVisibility(View.GONE);
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
    private void uploadImageAndSavePost() {
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
                                    post.setImage(uri.toString());
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(PlaceDetailsActivity.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    savePost();
                                });
                            })

                    .addOnFailureListener(e -> {

                        // Error, Image not uploaded
                        progressDialog.dismiss();
                        Toast
                                .makeText(PlaceDetailsActivity.this,
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
            savePost();
        }
    }


    private void savePost() {
        Log.d(TAG, "savePlace: " + place.toString());

        firestore.collection(DB_PATH_POST).add(post).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "savePlace: success. " + documentReference.getId());
            Toast.makeText(this, "Submitted", Toast.LENGTH_SHORT).show();
            updateUI();
        });

    }

    private void updateUI() {
        binding.cvShareExperience.setVisibility(View.GONE);
        binding.mySharedExperience.mySharedExperience.setVisibility(View.VISIBLE);
        binding.mySharedExperience.tvComment.setText(post.getComment());
        binding.mySharedExperience.myRatingExperience.setRating(post.getRating());
        binding.mySharedExperience.tvCreatedAt.setText(DateTimeHelper.getTimeAgo(post.getCreated_at(), Calendar.getInstance().getTimeInMillis()));

        if (mAuth.getCurrentUser().isAnonymous()) {
            binding.mySharedExperience.tvName.setText("Anonymous User");
        } else if (user != null) {
            binding.mySharedExperience.tvName.setText(user.getFirst_name() + " " + user.getLast_name());
        }

        if (post.isVisitor()) {
            binding.mySharedExperience.ivVisitorType.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_guest_user));
            binding.mySharedExperience.tvVisitorType.setText("Visitor");
        } else {
            binding.mySharedExperience.ivVisitorType.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_logged_in_user));
            binding.mySharedExperience.tvVisitorType.setText("Non-Visitor");
        }
        if (post.getImage() != null) {
            Glide.with(this)
                    .load(post.getImage())
                    .transform(new CenterInside())
                    .into(binding.mySharedExperience.ivPostImage);
        } else {
            binding.mySharedExperience.ivPostImage.setVisibility(View.GONE);
        }
    }

    private void drawCircle(LatLng point) {

        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();
        // Specifying the center of the circle
        circleOptions.center(point);
        // Radius of the circle
        circleOptions.radius(place.getRange());
        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);
        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);
        // Border width of the circle
        circleOptions.strokeWidth(2);
        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (geoPoint != null && place != null) {
            LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(place.getName()));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            drawCircle(latLng);


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