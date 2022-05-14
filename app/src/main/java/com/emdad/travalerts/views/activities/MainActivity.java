package com.emdad.travalerts.views.activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.emdad.travalerts.R;
import com.emdad.travalerts.models.Place;
import com.emdad.travalerts.models.User;
import com.emdad.travalerts.utils.ConnectionDetector;
import com.emdad.travalerts.utils.GeoFenceHelper;
import com.emdad.travalerts.utils.Sequence;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.emdad.travalerts.utils.StaticValues.APP_PLAY_STORE_URL;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_EMAIL;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_PLACE;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_USER;
import static com.emdad.travalerts.utils.StaticValues.EMAIL_ADDRESS_OF_HELP_AND_SUPPORT;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MyMainActivity";
    private NavController navController;
    private DrawerLayout drawerLayout;

    // Get Places for adding GeoFence
    private List<Place> placeList;
    private FirebaseFirestore firestore;

    // GeoFencing
    private GeofencingClient geofencingClient;
    private GeoFenceHelper geoFenceHelper;
    private final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: ");

        ConnectionDetector cd = new ConnectionDetector(this);
        if (cd.isConnected()) {
            Log.d(TAG, "onCreate: Connection Established !");
//            Toast.makeText(this, "Connection Established !", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Your phone is Offline, Check your connection !", Toast.LENGTH_LONG).show();
        }

        setUpNavigationController();

        init();

        initGeoFencing();

        fetchAllPlace();

        initializeClickListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
            navigationView.getHeaderView(0).findViewById(R.id.llGuestUI).setVisibility(View.VISIBLE);
        } else {
            navigationView.getHeaderView(0).findViewById(R.id.llGuestUI).setVisibility(View.GONE);
            updateDrawerUI();
        }
    }

    private void updateDrawerUI() {
        AppCompatImageView ivUserAvatar = navigationView.getHeaderView(0).findViewById(R.id.ivUserAvatar);
        AppCompatTextView tvUserName = navigationView.getHeaderView(0).findViewById(R.id.tvUserName);
        AppCompatTextView tvEmail = navigationView.getHeaderView(0).findViewById(R.id.tvEmail);
        AppCompatImageView ivEditProfile = navigationView.getHeaderView(0).findViewById(R.id.ivEditProfile);

        ivUserAvatar.setOnClickListener(view -> gotoProfileFragment());

        tvUserName.setOnClickListener(view -> gotoProfileFragment());

        ivEditProfile.setOnClickListener(view -> {
            if (isValidDestination(R.id.editProfileActivity)) {
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.editProfileActivity);
            }
        });



        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "getData: " + currentUser.getUid());
        firestore.collection(DB_PATH_USER).whereEqualTo(DB_KEY_EMAIL, currentUser.getEmail()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                Log.d(TAG, "getData: " + user.toString());

                if (!TextUtils.isEmpty(user.getProfile_pic())) {
                    Glide.with(this)
                            .load(user.getProfile_pic())
                            .transform(new CircleCrop())
                            .into(ivUserAvatar);
                }
                tvUserName.setText(String.format("%s %s", user.getFirst_name(), user.getLast_name()));
                tvEmail.setText(user.getEmail());
            }
        });
    }

    private void gotoProfileFragment() {
        if (isValidDestination(R.id.profileFragment)) {
            Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.profileFragment);
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void initializeClickListener() {
        navigationView.getHeaderView(0).findViewById(R.id.btnGoToLogin).setOnClickListener(view -> {
            // logout
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
        });

        navigationView.getHeaderView(0).findViewById(R.id.ivEditProfile).setOnClickListener(view -> {
            // go to profile edit page
        });
    }

    private void init() {
        firestore = FirebaseFirestore.getInstance();
        placeList = new ArrayList<>();
    }

    private void fetchAllPlace() {
        Log.d(TAG, "fetchAllPlace: fetching all place");
        firestore.collection(DB_PATH_PLACE).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                placeList.clear();
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                for (DocumentSnapshot d : list) {

                    Place p = d.toObject(Place.class);
                    Log.d(TAG, "fetchAllPlace: p= " + p.getId());
                    Log.d(TAG, "fetchAllPlace: d= " + d.getId());
//                    p.setId(d.getId());
                    placeList.add(p);

                }

                if (placeList.size() > 0) {
                    addGeoFence();
                }
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "getHomeFeed: ERROR: " + e.getLocalizedMessage());
            Toast.makeText(this, getResources().getString(R.string.toast_message_something_went_wrong), Toast.LENGTH_SHORT).show();
        });
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
        List<Geofence> geoFenceList = new ArrayList<>();
        for (Place place : placeList) {
            geoFenceList.add(
                    geoFenceHelper.getGeoFence(
                            place.getId(),
                            new LatLng(place.getGeo_location().getLatitude(), place.getGeo_location().getLongitude()),
                            place.getRange(),
                            Geofence.GEOFENCE_TRANSITION_ENTER
                                    | Geofence.GEOFENCE_TRANSITION_DWELL
                                    | Geofence.GEOFENCE_TRANSITION_EXIT)
            );
        }

        GeofencingRequest geofencingRequest = geoFenceHelper.getGeoFencingRequest(geoFenceList);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Log.d(TAG, "onRequestPermissionsResult: We've the permission. (PERMISSION_GRANTED)");
            } else {
                //We do not have the permission..
                Log.d(TAG, "onRequestPermissionsResult: We've the permission. (PERMISSION_GRANTED)");
                Toast.makeText(this, "You must grant permission", Toast.LENGTH_SHORT).show();


            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show();
            } else {
                //We do not have the permission..
                Toast.makeText(this, "Background location access is neccessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void setUpNavigationController() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        drawerLayout = findViewById(R.id.drawer_layout);
//        drawerLayout.setBackgroundColor(getResources().getColor(R.color.color_fragment_background));

        navigationView = findViewById(R.id.nav_view);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavView);

        NavigationUI.setupWithNavController(bottomNav, navController);
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);


        inflater.inflate(R.menu.main_activity_menu, menu);
        /*MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search));
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);
//                if (categoryContainerAdapter.getFilter() != null)
//                    categoryContainerAdapter.getFilter().filter(newText);
//                else
//                    Log.d(TAG, "categoryContainerAdapter.getFilter() is null.");
                return false;
            }
        });*/

        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            case R.id.action_search:
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.searchActivity);
                return true;

            case android.R.id.home: {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                } else {
                    return false;
                }
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected: item selected");
        switch (item.getItemId()) {

            case R.id.nav_home: {
                if (isValidDestination(R.id.homeFragment)) {
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.homeFragment);
                }
                break;
            }

            case R.id.nav_my_places: {
                if (isValidDestination(R.id.myPlacesFragment)) {
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.myPlacesFragment);
                }
                break;
            }

            case R.id.nav_add_location: {
                if (isValidDestination(R.id.addTouristAttractionPageFragment)) {
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.addPlaceActivity);
                }
                break;
            }

            case R.id.nav_notification: {
                if (isValidDestination(R.id.notificationFragment)) {
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.notificationFragment);
                }
                break;
            }

            case R.id.nav_profile: {
                if (isValidDestination(R.id.profileFragment)) {
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.profileFragment);
                }
                break;
            }
            case R.id.nav_settings: {
                if (isValidDestination(R.id.settingsFragment)) {
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.settingsFragment);
                }
                break;
            }
            case R.id.nav_share: {

                shareApp();

                break;
            }

            case R.id.nav_privacy_policy: {
                if (isValidDestination(R.id.privacyPolicyFragment)) {
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.privacyPolicyFragment);
                }
                break;
            }

            case R.id.nav_help_and_support: {
                helpAndSupport();
                break;
            }
            case R.id.nav_logout: {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, AuthActivity.class));
                finish();
                break;
            }
            default:
                return false;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, drawerLayout);
    }

    private boolean isValidDestination(int destination) {
        return destination != Objects.requireNonNull(Navigation.findNavController(this, R.id.nav_host_fragment).getCurrentDestination()).getId();
    }

    private void shareApp() {
        final Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setAction(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String extraText = "This app is terrific, give it a try.\n" +
                APP_PLAY_STORE_URL;
        sharingIntent.putExtra(Intent.EXTRA_TEXT, extraText);

        startActivity(Intent.createChooser(sharingIntent, "Share Travalerts app via"));
    }

    private void helpAndSupport() {
        String body = null;
        try {
            body = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "Help and support - catch exception ");
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL_ADDRESS_OF_HELP_AND_SUPPORT});
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.email_subject_help_and_support));
        intent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(intent, getString(R.string.choose_email_client)));
    }

}