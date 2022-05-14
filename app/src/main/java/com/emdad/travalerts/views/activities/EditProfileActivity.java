package com.emdad.travalerts.views.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.emdad.travalerts.databinding.ActivityEditProfileBinding;
import com.emdad.travalerts.models.User;
import com.emdad.travalerts.utils.FragmentTransactionHelper;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.emdad.travalerts.utils.StaticValues.DB_KEY_EMAIL;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_FIRST_NAME;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_LAST_NAME;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_PHONE_NO;
import static com.emdad.travalerts.utils.StaticValues.DB_KEY_PROFILE_PIC;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_USER;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private ActivityEditProfileBinding binding;
    private User user;
    private FragmentTransactionHelper fragmentTransactionHelper;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;


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
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        init();

        initializeClickListeners();

        getData();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
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
                binding.ivUserAvatar.setImageBitmap(bitmap);
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    private void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    private void initializeClickListeners() {

        binding.ivUserAvatar.setOnClickListener(view -> selectImage());

        binding.ivEditProfilePic.setOnClickListener(view -> selectImage());

        binding.btnSignUp.setOnClickListener(view -> {
            if (filePath == null) {
                updateUser();
            } else {
                uploadImageAndUpdateUser();
            }
        });

    }

    private void updateUser() {
        String firstName = binding.tiETFirstName.getEditableText().toString();
        String lastName = binding.tiETLastName.getEditableText().toString();
        String phoneNo = binding.tiETPhoneNumber.getEditableText().toString();
        if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName) && TextUtils.isEmpty(phoneNo)) {
            Toast.makeText(this, "Please input all field.", Toast.LENGTH_SHORT).show();
            binding.tiETFirstName.requestFocus();
        }
        // here the parameter context for showing the Toast msg
        else {
            user.setFirst_name(firstName);
            user.setLast_name(lastName);
            user.setPhoneNo(phoneNo);
            updateUserToFirebase();
        }
    }

    private void updateUserToFirebase() {
        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put(DB_KEY_FIRST_NAME, user.getFirst_name());
        userInfo.put(DB_KEY_LAST_NAME, user.getLast_name());
        userInfo.put(DB_KEY_PHONE_NO, user.getPhoneNo());
        userInfo.put(DB_KEY_PROFILE_PIC, user.getProfile_pic());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(DB_PATH_USER).document(uid)
                .update(userInfo)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                    Toast.makeText(this, "The user info updated",
                            Toast.LENGTH_SHORT).show();
                    onBackPressed();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Log.w(TAG, "Error writing document", e);
                });
    }

    private void getData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Log.d(TAG, "getData: " + currentUser.getUid());
        firestore.collection(DB_PATH_USER).whereEqualTo(DB_KEY_EMAIL, currentUser.getEmail()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                Log.d(TAG, "getData: " + user.toString());

                if (!TextUtils.isEmpty(user.getProfile_pic())) {
                    Glide.with(this)
                            .load(user.getProfile_pic())
                            .transform(new CircleCrop())
                            .into(binding.ivUserAvatar);
                }
                binding.tiETFirstName.setText(user.getFirst_name());
                binding.tiETLastName.setText(user.getLast_name());
                binding.tiETPhoneNumber.setText(user.getPhoneNo());
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
    private void uploadImageAndUpdateUser() {
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


                                    // add profile imageurl
                                     user.setProfile_pic(uri.toString());

                                    // Dismiss dialog
                                    progressDialog.dismiss();

                                    Toast
                                            .makeText(EditProfileActivity.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    //
                                    updateUser();
                                });
                            })

                    .addOnFailureListener(e -> {

                        // Error, Image not uploaded
                        progressDialog.dismiss();
                        Toast
                                .makeText(this,
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
            updateUser();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}