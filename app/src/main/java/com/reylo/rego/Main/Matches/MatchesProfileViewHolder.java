package com.reylo.rego.Main.Matches;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.reylo.rego.Main.Matches.Chat.ChatActivity;
import com.reylo.rego.R;

public class MatchesProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView mGoneIdHolder;
    public TextView mMatchProfilePicRecyclerName;
    public ImageView mMatchProfilePicRecyclerPicture;
    public TextView mGoneProfilePhotoUrl;

    // hold references to views used to create matches name and profile picture
    public MatchesProfileViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mGoneIdHolder = (TextView) itemView.findViewById(R.id.matches_profile_pic_cycle_view_holder_gone_id);
        mMatchProfilePicRecyclerName = (TextView) itemView.findViewById(R.id.matches_profile_pic_cycle_view_holder_text_view);
        mMatchProfilePicRecyclerPicture = (ImageView) itemView.findViewById(R.id.matches_profile_pic_cycle_view_holder_image_view);
        mGoneProfilePhotoUrl = (TextView) itemView.findViewById(R.id.matches_profile_pic_cycle_view_holder_gone_photo_url);

    }

    @Override
    public void onClick(View view) {

        Intent intent =  new Intent(view.getContext(), ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("otherUserId", mGoneIdHolder.getText().toString());
        bundle.putString("otherUserName", mMatchProfilePicRecyclerName.getText().toString());
        bundle.putString("otherUserProfilePhotoUrl", mGoneProfilePhotoUrl.getText().toString());
        intent.putExtras(bundle);
        view.getContext().startActivity(intent);

    }
}
