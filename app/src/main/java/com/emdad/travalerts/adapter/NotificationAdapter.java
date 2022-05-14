package com.emdad.travalerts.adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.emdad.travalerts.R;
import com.emdad.travalerts.databinding.RecyclerViewNotificationSingleItemBinding;
import com.emdad.travalerts.models.Notification;
import com.emdad.travalerts.utils.DateTimeHelper;
import com.google.api.Context;

import java.util.Calendar;
import java.util.List;

import static com.emdad.travalerts.utils.StaticValues.KEY_GEO_POINT_LATITUDE;
import static com.emdad.travalerts.utils.StaticValues.KEY_GEO_POINT_LONGITUDE;
import static com.emdad.travalerts.utils.StaticValues.KEY_PLACE_ID;
import static com.emdad.travalerts.utils.StaticValues.KEY_PLACE_OBJECT;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> items;
    private Activity activity;

    public NotificationAdapter(Activity activity, List<Notification> items) {
        this.items = items;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_notification_single_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (items.get(position).getImage() != null) {
            Glide.with(activity)
                    .load(items.get(position).getImage())
                    .transform(new CenterInside())
                    .into(holder.ivNotificationThumbnail);
        }

        holder.tvPlaceName.setText(items.get(position).getMessage());

        holder.tvCreatedAt.setText(DateTimeHelper.getTimeAgo(Long.parseLong(items.get(position).getCreatedAt()), Calendar.getInstance().getTimeInMillis()));

        holder.btnShareYourExperience.setOnClickListener(view -> {
            Bundle args = new Bundle();
            args.putString(KEY_PLACE_ID, items.get(position).getPlaceId());
            Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(R.id.placeDetailsActivity, args);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView ivNotificationThumbnail;
        AppCompatTextView tvPlaceName;
        AppCompatButton btnShareYourExperience;
        AppCompatTextView tvCreatedAt;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivNotificationThumbnail = itemView.findViewById(R.id.ivNotificationThumbnail);
            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            btnShareYourExperience = itemView.findViewById(R.id.btnShareYourExperience);


        }
    }
}
