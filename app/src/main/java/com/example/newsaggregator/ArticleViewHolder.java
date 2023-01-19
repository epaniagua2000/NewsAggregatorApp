package com.example.newsaggregator;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

public class ArticleViewHolder extends RecyclerView.ViewHolder {

    TextView author;
    TextView title;
    TextView desc;
    TextView date;
    ImageView image;
    TextView pageNum;

    public ArticleViewHolder(@NonNull View v){
        super(v);
        author = v.findViewById(R.id.author);
        title = v.findViewById(R.id.title);
        desc = v.findViewById(R.id.desc);
        date = v.findViewById(R.id.date);
        image = v.findViewById(R.id.image);
        pageNum = v.findViewById(R.id.pageNum);
    }

}
