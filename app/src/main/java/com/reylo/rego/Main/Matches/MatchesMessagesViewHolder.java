package com.reylo.rego.Main.Matches;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.reylo.rego.Main.Matches.Chat.ChatActivity;
import com.reylo.rego.R;

public class MatchesMessagesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView mGoneIdHolder;
    public TextView mMatchName;
    public ImageView mProfilePicURL;
    public TextView mGoneProfilePhotoUrl;

    public MatchesMessagesViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mGoneIdHolder = (TextView) itemView.findViewById(R.id.matches_messages_view_holder_gone_id_holder);
        mMatchName = (TextView) itemView.findViewById(R.id.matches_messages_view_holder_name);
        mProfilePicURL = (ImageView) itemView.findViewById(R.id.matches_messages_view_holder_profile_picture);
        mGoneProfilePhotoUrl = (TextView) itemView.findViewById(R.id.matches_messages_view_holder_gone_photo_url_holder);

    }

    @Override
    public void onClick(View view) {

        Intent intent =  new Intent(view.getContext(), ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("otherUserId", mGoneIdHolder.getText().toString());
        bundle.putString("otherUserName", mMatchName.getText().toString());
        bundle.putString("otherUserProfilePhotoUrl", mGoneProfilePhotoUrl.getText().toString());
        intent.putExtras(bundle);
        view.getContext().startActivity(intent);

    }
}
