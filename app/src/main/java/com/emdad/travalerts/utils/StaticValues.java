package com.emdad.travalerts.utils;

import com.google.firebase.firestore.GeoPoint;

public class StaticValues {
    public static final String URL_TERM_AND_SERVICE = "https://travalerts.com/terms_and_conditions.html";
    public static final String URL_PRIVACY_POLICY = "https://travalerts.com/privacy_policy.html";
    public static final String APP_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.emdad.travalerts";
    public static final String EMAIL_ADDRESS_OF_HELP_AND_SUPPORT = "contact@travalerts.com";

    // API KEYS
    public static final String GOOGLE_API_KEY = "AIzaSyDFFaxAPlRHL3YIEhasN5A-U9lmawy6e6Q";


    // Shared Preference Keys
    public static final String SHARED_PREF_NAME = "TravalertSharedPref";
    public static final String KEY_PLACE_ID = "key_place_id";

    // Parcelable bundle keys
    public static final String KEY_PLACE_OBJECT = "key_place_object";
    public static final String KEY_GEO_POINT_LATITUDE = "key_geo_point_latitude";
    public static final String KEY_GEO_POINT_LONGITUDE = "key_geo_point_longitude";
    // DB path
    public static final String DB_PATH_USER = "user";
    public static final String DB_PATH_PLACE = "places";
    public static final String DB_PATH_NOTIFICATION = "notification";
    public static final String DB_PATH_POST = "posts";


    // DB fields name
    public static final String DB_KEY_ID = "id";
    public static final String DB_KEY_FIRST_NAME = "first_name";
    public static final String DB_KEY_LAST_NAME = "last_name";
    public static final String DB_KEY_PHONE_NO = "phoneNo";
    public static final String DB_KEY_PROFILE_PIC = "profile_pic";
    public static final String DB_KEY_EMAIL = "email";
    public static final String DB_KEY_CREATED_AT = "createdAt";
    public static final String DB_KEY_USER_ID = "userId";


    // COLLECTION Place fields
    public static final String DB_FIELD_PLACE_ID = "id";
    public static final String DB_FIELD_PLACE_NAME = "name";
    public static final String DB_FIELD_PLACE_DESCRIPTION = "description";
    public static final String DB_FIELD_PLACE_IMAGE = "image";
    public static final String DB_FIELD_PLACE_ADDRESS = "address";
    public static final String DB_FIELD_PLACE_GEO_LOCATION = "geo_location";
    public static final String DB_FIELD_PLACE_RANGE = "range";
    public static final String DB_FIELD_PLACE_RATING = "rating";
    public static final String DB_FIELD_PLACE_USER_ID = "userId";
    public static final String DB_FIELD_PLACE_CREATED_AT = "crated_at";
    public static final String DB_FIELD_PLACE_UPDATED_AT = "updated_at";

    public static final String DB_FIELD_POST_PLACE_ID = "place_id";
    public static final String DB_FIELD_POST_USER_ID = "user_id";
    public static final String DB_FIELD_POST_CREATED_AT = "created_at";


    public static final String DB_FIELD_NOTIFICATION_CREATED_AT = "createdAt";

}
