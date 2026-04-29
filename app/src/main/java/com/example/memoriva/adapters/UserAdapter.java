package com.example.memoriva.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoriva.R;
import com.example.memoriva.network.UserSearchResponse;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private final Context context;
    private final List<UserSearchResponse> users;

    public UserAdapter(Context context, List<UserSearchResponse> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserSearchResponse user = users.get(position);

        holder.tvHandle.setText("@" + user.getUsername());
        holder.tvName.setText(user.getFullName());

        // Toggle add/added state
        boolean isAdded = user.isFriend();
        updateAddButton(holder.btnAdd, isAdded);

        holder.btnAdd.setOnClickListener(v -> {
            boolean currentState = user.isFriend();
            user.setFriend(!currentState);
            updateAddButton(holder.btnAdd, !currentState);
        });
    }

    private void updateAddButton(MaterialButton btn, boolean isAdded) {
        if (isAdded) {
            btn.setText("Added");
            btn.setStrokeColorResource(R.color.colorPrimary);
            btn.setBackgroundTintList(null);
            btn.setTextColor(context.getResources().getColor(R.color.colorPrimary, context.getTheme()));
        } else {
            btn.setText("Add");
            btn.setBackgroundTintList(
                    context.getResources().getColorStateList(R.color.colorPrimary, context.getTheme()));
            btn.setTextColor(context.getResources().getColor(R.color.white, context.getTheme()));
        }
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        View vOnlineDot;
        TextView tvHandle;
        TextView tvName;
        MaterialButton btnAdd;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            vOnlineDot = itemView.findViewById(R.id.vOnlineDot);
            tvHandle = itemView.findViewById(R.id.tvHandle);
            tvName = itemView.findViewById(R.id.tvName);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}
