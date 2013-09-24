package ru.akbashev.instacollage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;


public class ImageAdapter extends BaseAdapter {

    private ArrayList<InstaImages> mInstaImages;
    private Context mContext;
    private DisplayImageOptions mOptions;

    public ImageAdapter(Context context, ArrayList<InstaImages> instaImages, DisplayImageOptions options){
        this.mInstaImages = instaImages;
        this.mContext = context;
        this.mOptions = options;
    }

    @Override
    public int getCount() {
        return 12;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        final TextView textView;
        if (convertView == null) {
            LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            imageView = (ImageView) li.inflate(R.layout.grid_view_item, parent, false);
        } else {
            imageView = (ImageView) convertView;
        }

        ImageLoader.getInstance().displayImage(mInstaImages.get(position).url, imageView, mOptions);

        return imageView;
    }
}