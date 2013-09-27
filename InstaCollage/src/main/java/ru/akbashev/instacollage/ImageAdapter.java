package ru.akbashev.instacollage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.util.ArrayList;


public class ImageAdapter extends BaseAdapter {

    private ArrayList<InstaImages> mInstaImages;
    private Context mContext;
    private DisplayImageOptions mOptions;
    private LayoutInflater mInflater;


    private class ViewHolderItem {
        ImageView imageView;
        TextView textView;
        ProgressBar progressBar;
    }

    public ImageAdapter(Context context, ArrayList<InstaImages> instaImages, DisplayImageOptions options){
        this.mInstaImages = instaImages;
        this.mContext = context;
        this.mOptions = options;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (mInstaImages.size() > 12)
            return 12;
        else
            return mInstaImages.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolderItem holder;
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutinflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutinflater.inflate(R.layout.grid_view_item, parent, false);
            holder = new ViewHolderItem();
            holder.textView = (TextView) view.findViewById(R.id.textView);
            holder.imageView = (ImageView) view.findViewById(R.id.image);
            holder.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            view.setTag(holder);

        } else {
            holder = (ViewHolderItem) view.getTag();
        }

        holder.textView.setText(String.valueOf(mInstaImages.get(position).count));
        if (mInstaImages.get(position).bitmap == null){
            ImageLoader.getInstance().displayImage(mInstaImages.get(position).url, holder.imageView, mOptions, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    super.onLoadingStarted(imageUri, view);
                    holder.progressBar.setVisibility(View.VISIBLE);
                    holder.imageView.setImageDrawable(null);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    mInstaImages.get(position).bitmap = loadedImage;
                    holder.progressBar.setVisibility(View.GONE);
                    holder.textView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            holder.imageView.setImageBitmap(mInstaImages.get(position).bitmap);
        }

        if (mInstaImages.get(position).getState()) {
            view.setBackgroundColor(mContext.getResources().getColor(R.color.holo_orange_dark));
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
        }
        return view;
    }
}