package com.emdad.travalerts.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.emdad.travalerts.R;
import com.emdad.travalerts.models.Place;
import com.emdad.travalerts.views.activities.AuthActivity;
import com.google.firebase.auth.FirebaseAuth;

import static com.emdad.travalerts.utils.StaticValues.KEY_PLACE_OBJECT;

public class FragmentTransactionHelper {
    private static final String TAG = "FragmentTransactionHelp";
    private NavController navController;

    public FragmentTransactionHelper(Fragment parentFragment) {
        if (parentFragment != null && parentFragment.getView() != null)
            navController = Navigation.findNavController(parentFragment.getView());
        else
            Log.d(TAG, "parentFragment or parentFragment.getView() is null");
    }

    public void navigateLoginFragmentToSignUpFragment() {
        Log.d(TAG, "navigateLoginFragmentToSignUpFragment: called");
        navController.navigate(R.id.action_loginFragment_to_signUpFragment);
    }

    public void navigateSignUpFragmentToLoginFragment() {
        Log.d(TAG, "navigateSignUpFragmentToLoginFragment: called");
        navController.popBackStack();
    }

    public void navigateLoginFragmentToHomeFragment(FragmentActivity activity) {
        Log.d(TAG, "navigateLoginFragmentToHomeFragment: called");
        navController.navigate(R.id.action_loginFragment_to_mainActivity);
        activity.finish();
    }

    public void navigateSignUpFragmentToHomeFragment(FragmentActivity activity) {
        navController.navigate(R.id.action_signUpFragment_to_mainActivity2);
        activity.finish();
    }

    public void navigateProfileFragmentToProfileEditFragment() {
        navController.navigate(R.id.action_profileFragment_to_editProfileFragment);
    }

    public void navigateHomeFragmentToTouristAttractionDetailsFragment(Place place) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_PLACE_OBJECT, place);
        navController.navigate(R.id.action_homeFragment_to_touristAtrractionDetailsFragment, args);
    }

    public void navigateMyPlacesFragmentToTouristAttractionDetailsFragment(Place place) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_PLACE_OBJECT, place);
        navController.navigate(R.id.action_myPlacesFragment_to_touristAtrractionDetailsFragment, args);
    }

    public void navigateAddTouristAttractionFragmentToTouristAttractionDetailsFragment() {
        navController.navigate(R.id.action_addTouristAttractionPageFragment_to_touristAtrractionDetailsFragment);
    }

    public void logout(Activity activity) {
        FirebaseAuth.getInstance().signOut();
        activity.startActivity(new Intent(activity, AuthActivity.class));
        activity.finish();
    }

    public void navigateProfileFragmentToEditProfileActivity() {
        navController.navigate(R.id.action_profileFragment_to_editProfileActivity);
    }
}
