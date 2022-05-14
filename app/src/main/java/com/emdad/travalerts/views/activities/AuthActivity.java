package com.emdad.travalerts.views.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.emdad.travalerts.R;
import com.emdad.travalerts.views.dialogs.LocationPermissionDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "AuthActivity";
    private FirebaseAuth mAuth;
    private boolean isLocationPermissionGranted = false;
    private LocationPermissionDialog locationPermissionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        locationPermissionDialog = new LocationPermissionDialog(this, AuthActivity.this);

        requestAccessFineLocation();

    }
    public void requestAccessFineLocation() {

        Dexter.withContext(AuthActivity.this)
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                        , Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport != null) {
                            if (multiplePermissionsReport.areAllPermissionsGranted()) {
//                                Toast.makeText(AuthActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                                isLocationPermissionGranted = true;
                            } else {
                                if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                                    Toast.makeText(AuthActivity.this, "You have permanently denied permissions. Please goto settings and approve those permission.", Toast.LENGTH_LONG).show();
                                    showAlertDialogForGoingToSettings();

                                } else {
//                                    Toast.makeText(AuthActivity.this, "No permission given.", Toast.LENGTH_SHORT).show();
                                    locationPermissionDialog.showDialog();
                                }
                            }
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).withErrorListener(dexterError -> {
            locationPermissionDialog.showDialog();
            Log.d(TAG, "requestAccessFineLocation: " + dexterError.name());

        }).check();

    }

    private void showAlertDialogForGoingToSettings() {
        //If User was asked permission before and denied
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setTitle("Permission needed");
        alertDialogBuilder.setMessage("Location permission needed for notifying places around you.");
        alertDialogBuilder.setPositiveButton("Open Setting", (dialogInterface, i) -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", AuthActivity.this.getPackageName(),
                    null);
            intent.setData(uri);
            AuthActivity.this.startActivity(intent);
        });
        alertDialogBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> Log.d(TAG, "onClick: Cancelling"));

        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    private List<String> createPermissionList1() {
        List<String> permissionList = new ArrayList<String>();
        permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        return permissionList;
    }

    @Override
    public void onBackPressed() {
        if (isLocationPermissionGranted) {
            super.onBackPressed();
        } else {
            locationPermissionDialog.showDialog();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isLocationPermissionGranted) {
            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
            } else {
                Log.d(TAG, "onStart: User is not logged in");
            }
        }
    }
}