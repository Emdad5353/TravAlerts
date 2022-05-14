package com.emdad.travalerts.views.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.emdad.travalerts.R;
import com.emdad.travalerts.adapter.TouristAttractionAdapter;
import com.emdad.travalerts.databinding.ActivitySearchBinding;
import com.emdad.travalerts.models.Place;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import static com.emdad.travalerts.adapter.TouristAttractionAdapter.SEARCH_ACTIVITY;
import static com.emdad.travalerts.utils.StaticValues.DB_PATH_PLACE;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private ActivitySearchBinding binding;
    private FirebaseFirestore firestore;
    private List<Place> placeList;
    private TouristAttractionAdapter touristAttractionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        firestore = FirebaseFirestore.getInstance();
        placeList = new ArrayList<>();

        touristAttractionAdapter = new TouristAttractionAdapter(SEARCH_ACTIVITY, (Activity) SearchActivity.this, (Context) this, null, placeList);

        binding.rvSearch.setAdapter(touristAttractionAdapter);
        binding.rvSearch.setLayoutManager(new LinearLayoutManager(this));
    }

    private void searchData(String searchQuery) {
        List<String> queryList = new ArrayList<>();
        queryList.add(searchQuery);
        firestore.collection(DB_PATH_PLACE)
                .whereIn("name", queryList)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                binding.tvSearchMsg.setVisibility(View.GONE);
                binding.rvSearch.setVisibility(View.VISIBLE);
                placeList.clear();
                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                for (DocumentSnapshot d : list) {

                    Place p = d.toObject(Place.class);
                    p.setId(d.getId());
                    placeList.add(p);

                }
                touristAttractionAdapter.notifyDataSetChanged();

            } else {
                binding.tvSearchMsg.setVisibility(View.VISIBLE);
                binding.tvSearchMsg.setText(String.format("No data found with \"%s\"", searchQuery));
                Toast.makeText(this, "No data found with \"" + searchQuery + "\"", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.d(TAG, "getHomeFeed: ERROR: " + e.getLocalizedMessage());
            Toast.makeText(this, getString(R.string.toast_message_something_went_wrong), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);


        inflater.inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        menu.performIdentifierAction(R.id.action_search, 0);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search));
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setSelected(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                Log.d(TAG, "onQueryTextChange: " + searchQuery);
                // Search
                if (searchQuery.length() > 2) {
                    searchData(searchQuery);
                } else {
                    binding.rvSearch.setVisibility(View.GONE);
                    binding.tvSearchMsg.setVisibility(View.VISIBLE);
                    binding.tvSearchMsg.setText("Type place name to search.");
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}