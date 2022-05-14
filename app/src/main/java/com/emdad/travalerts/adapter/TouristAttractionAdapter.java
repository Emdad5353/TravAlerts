package com.emdad.travalerts.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.emdad.travalerts.R;
import com.emdad.travalerts.databinding.RecyclerViewHomeFragmentSingleItemBinding;
import com.emdad.travalerts.databinding.RecyclerViewMyPlacesSingleItemBinding;
import com.emdad.travalerts.models.Place;
import com.emdad.travalerts.utils.DateTimeHelper;
import com.emdad.travalerts.utils.FragmentTransactionHelper;
import com.emdad.travalerts.views.activities.PlaceDetailsActivity;
import com.emdad.travalerts.views.activities.SearchActivity;

import java.util.Calendar;
import java.util.List;

import static com.emdad.travalerts.utils.StaticValues.KEY_GEO_POINT_LATITUDE;
import static com.emdad.travalerts.utils.StaticValues.KEY_GEO_POINT_LONGITUDE;
import static com.emdad.travalerts.utils.StaticValues.KEY_PLACE_OBJECT;

public class TouristAttractionAdapter extends RecyclerView.Adapter<TouristAttractionAdapter.ViewHolder> {
    public static final String HOME_FRAGMENT = "HOME_FRAGMENT";
    public static final String SEARCH_ACTIVITY = "SEARCH_ACTIVITY";
    public static final String MY_PLACE_FRAGMENT = "MY_PLACE_FRAGMENT";
    private final List<Place> items;
    private final String adapterUsedFor;
    private final Activity activity;
    private final Context context;
    private final FragmentTransactionHelper fragmentTransactionHelper;

    public TouristAttractionAdapter(String adapterUsedFor, Activity activity, Context context, FragmentTransactionHelper fragmentTransactionHelper, List<Place> items) {
        this.adapterUsedFor = adapterUsedFor;
        this.items = items;
        this.activity = activity;
        this.context = context;
        this.fragmentTransactionHelper = fragmentTransactionHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (adapterUsedFor.equals(MY_PLACE_FRAGMENT)) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_my_places_single_item, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_home_fragment_single_item, parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (adapterUsedFor.equals(MY_PLACE_FRAGMENT)) {
            holder.myPlacesSingleItemBinding.tvPlaceName.setText(items.get(position).getName());
            holder.myPlacesSingleItemBinding.tvAddress.setText(items.get(position).getAddress());
            holder.myPlacesSingleItemBinding.rating.setRating(items.get(position).getRating());
            holder.myPlacesSingleItemBinding.tvCreatedAt.setText(DateTimeHelper.getTimeAgo(Long.parseLong(items.get(position).getCrated_at()), Calendar.getInstance().getTimeInMillis()));

            Glide.with(context)
                    .load(items.get(position).getImage())
                    .transform(new CenterInside())
                    .into(holder.myPlacesSingleItemBinding.ivPlaceImage);

            holder.myPlacesSingleItemBinding.ivEditPlace.setOnClickListener(view -> {
                Bundle args = new Bundle();
                args.putParcelable(KEY_PLACE_OBJECT, items.get(position));
                args.putDouble(KEY_GEO_POINT_LATITUDE, items.get(position).getGeo_location().getLatitude());
                args.putDouble(KEY_GEO_POINT_LONGITUDE, items.get(position).getGeo_location().getLongitude());
                Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(R.id.editPlaceActivity, args);
            });

        } else {
            holder.homeFragmentSingleItemBinding.tvPlaceName.setText(items.get(position).getName());
            holder.homeFragmentSingleItemBinding.tvAddress.setText(items.get(position).getAddress());
            holder.homeFragmentSingleItemBinding.rating.setRating(items.get(position).getRating());
            holder.homeFragmentSingleItemBinding.tvCreatedAt.setText(DateTimeHelper.getTimeAgo(Long.parseLong(items.get(position).getCrated_at()), Calendar.getInstance().getTimeInMillis()));

            Glide.with(context)
                    .load(items.get(position).getImage())
                    .transform(new CenterInside())
                    .into(holder.homeFragmentSingleItemBinding.ivPlaceImage);

        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private RecyclerViewMyPlacesSingleItemBinding myPlacesSingleItemBinding;
        private RecyclerViewHomeFragmentSingleItemBinding homeFragmentSingleItemBinding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            if (adapterUsedFor.equals(MY_PLACE_FRAGMENT)) {
                myPlacesSingleItemBinding = RecyclerViewMyPlacesSingleItemBinding.bind(itemView);
            } else {
                homeFragmentSingleItemBinding = RecyclerViewHomeFragmentSingleItemBinding.bind(itemView);
            }

            itemView.setOnClickListener(view -> {
                if (adapterUsedFor.equals(SEARCH_ACTIVITY)) {
                    Intent intent = new Intent(activity, PlaceDetailsActivity.class);
                    intent.putExtra(KEY_PLACE_OBJECT, items.get(getAdapterPosition()));
                    intent.putExtra(KEY_GEO_POINT_LATITUDE, items.get(getAdapterPosition()).getGeo_location().getLatitude());
                    intent.putExtra(KEY_GEO_POINT_LONGITUDE, items.get(getAdapterPosition()).getGeo_location().getLongitude());
                    activity.startActivity(intent);
                } else {
                    Bundle args = new Bundle();
                    args.putParcelable(KEY_PLACE_OBJECT, items.get(getAdapterPosition()));
                    args.putDouble(KEY_GEO_POINT_LATITUDE, items.get(getAdapterPosition()).getGeo_location().getLatitude());
                    args.putDouble(KEY_GEO_POINT_LONGITUDE, items.get(getAdapterPosition()).getGeo_location().getLongitude());
                    Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(R.id.placeDetailsActivity, args);
                }
            });

        }
    }
}
