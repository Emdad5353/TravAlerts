package com.emdad.travalerts.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.emdad.travalerts.R;
import com.emdad.travalerts.models.Notification;
import com.emdad.travalerts.models.Post;
import com.emdad.travalerts.utils.DateTimeHelper;

import java.util.Calendar;
import java.util.List;

public class ExperiencePostAdapter extends RecyclerView.Adapter<ExperiencePostAdapter.ViewHolder> {

    private List<Post> items;
    private Activity activity;

    public ExperiencePostAdapter(Activity activity, List<Post> items) {
        this.items = items;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_shared_experience_single_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (items.get(position).getImage() != null) {
            Glide.with(activity)
                    .load(items.get(position).getImage())
                    .transform(new CenterInside())
                    .into(holder.ivPostImage);
        } else {
            holder.ivPostImage.setVisibility(View.GONE);
        }
        holder.tvCreatedAt.setText(DateTimeHelper.getTimeAgo(items.get(position).getCreated_at(), Calendar.getInstance().getTimeInMillis()));
        holder.tvComment.setText(items.get(position).getComment());
        holder.myRatingExperience.setRating(items.get(position).getRating());

        if (items.get(position).isVisitor()) {
            holder.ivVisitorType.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.ic_logged_in_user));
            holder.tvVisitorType.setText("Visitor");
        } else {
            holder.ivVisitorType.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.ic_guest_user));
            holder.tvVisitorType.setText("Non-Visitor");
        }
        holder.tvComment.setText(items.get(position).getComment());
        holder.tvComment.setText(items.get(position).getComment());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView ivProfilePic;
        AppCompatTextView tvName;
        AppCompatTextView tvVisitorType;
        AppCompatImageView ivVisitorType;
        AppCompatTextView tvCreatedAt;
        AppCompatImageView ivPostImage;
        AppCompatTextView tvComment;
        AppCompatRatingBar myRatingExperience;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            tvName = itemView.findViewById(R.id.tvName);
            tvVisitorType = itemView.findViewById(R.id.tvVisitorType);
            ivVisitorType = itemView.findViewById(R.id.ivVisitorType);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            tvComment = itemView.findViewById(R.id.tvComment);
            myRatingExperience = itemView.findViewById(R.id.myRatingExperience);


        }
    }
}
