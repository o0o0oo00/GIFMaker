package com.zhihu.matisse.internal.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zhihu.matisse.R;

import java.util.ArrayList;
import java.util.List;

public class PreviewListAdapter extends RecyclerView.Adapter<PreviewListAdapter.MyViewHolder> {
    private Context mContext;
    private List<Uri> list = new ArrayList<>();

    public PreviewListAdapter(Context context,List<Uri> list) {
        this.mContext = context;
        this.list = list;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.preview_item, null,false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
            Uri uri = list.get(position);
            ImageView imageView = holder.imageView;
            Glide.with(mContext).load(uri).into(imageView);
    }

    public void setData(List<Uri> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView ;
        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }



    }

}
