package com.simonits.adalerts.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.simonits.adalerts.R;
import com.simonits.adalerts.activities.DetailsActivity;
import com.simonits.adalerts.activities.FullscreenActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPagerAdapter extends PagerAdapter {
    private final Context context;
    private final LayoutInflater mLayoutInflater;
    private final ArrayList<String> images;

    public ViewPagerAdapter(Context context, ArrayList<String> images) {
        this.context = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (context instanceof DetailsActivity) {
            ImageView imageView = new ImageView(context);

            Picasso.get().load(images.get(position)).fit().centerCrop().into(imageView);
            container.addView(imageView);

            imageView.setOnClickListener(view -> {
                Intent intent = new Intent(context, FullscreenActivity.class);
                intent.putStringArrayListExtra("images", images);
                intent.putExtra("startPosition", position);
                context.startActivity(intent);
                ((DetailsActivity) context).overridePendingTransition(R.anim.scale_in, R.anim.nothing);
            });

            return imageView;
        } else {
            View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

            PhotoView photoView = itemView.findViewById(R.id.image);
            Glide
                    .with(this.context)
                    .load(images.get(position))
                    .into(photoView);

            container.addView(itemView);

            photoView.setOnClickListener(view -> ((FullscreenActivity) context).toggle());

            return itemView;
        }
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}