package com.saunvid.infilectassignment.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.saunvid.infilectassignment.R;
import com.saunvid.infilectassignment.remote.Api;

import java.util.List;

public class ImgPathAdapter extends RecyclerView.Adapter<ImgPathAdapter.MyViewHolder> {

    private List<String> list;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvPath;

        public MyViewHolder(View view) {
            super(view);
            tvPath = (TextView) view.findViewById(R.id.tv);
        }
    }

    public ImgPathAdapter(List<String> list) {
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_img_path, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.tvPath.setText(Api.IMAGE_URL + list.get(position));


    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}