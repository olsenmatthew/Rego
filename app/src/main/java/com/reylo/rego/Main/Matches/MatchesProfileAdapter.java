package com.reylo.rego.Main.Matches;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.reylo.rego.R;

import java.util.List;



public class MatchesProfileAdapter extends RecyclerView.Adapter<MatchesProfileViewHolder> {

    private List<MatchesObject> matchesList;
    private Context context;

    public MatchesProfileAdapter(List<MatchesObject> matchesList, Context context){
        this.matchesList = matchesList;
        this.context = context;
    }

    @Override
    public MatchesProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.matches_profile_cycle_view_holder, null, false);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(layoutParams);
        MatchesProfileViewHolder profileRCV  = new MatchesProfileViewHolder(layoutView);

        return profileRCV;

    }

    @Override
    public void onBindViewHolder(MatchesProfileViewHolder holder, int position) {

        holder.mGoneIdHolder.setText(matchesList.get(position).getUserId());
        holder.mMatchProfilePicRecyclerName.setText(matchesList.get(position).getfirstName());
        Glide.with(context)
                .load(matchesList.get(position).getprofilePicURL())
                .centerCrop()
                .transform(new MatchesProfileAdapter.CircleTransform(context))
                .into(holder.mMatchProfilePicRecyclerPicture);
        holder.mGoneProfilePhotoUrl.setText(matchesList.get(position).getprofilePicURL());

    }

    public static class CircleTransform extends BitmapTransformation {
        public CircleTransform(Context context) {
            super(context);
        }

        @Override protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return circleCrop(pool, toTransform);
        }

        private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null) return null;

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            // TODO this could be acquired from the pool too
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override public String getId() {
            return getClass().getName();
        }
    }

    @Override
    public int getItemCount() {
        return this.matchesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
