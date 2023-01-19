package com.example.newsaggregator;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleViewHolder> {

    private final MainActivity mainAct;
    private final ArrayList<Article> articleList;
    private Picasso picasso;

    public ArticleAdapter(MainActivity mainAct, ArrayList<Article> articleList){
        this.mainAct = mainAct;
        this.articleList = articleList;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        return new ArticleViewHolder(
                LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.article_entry, parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int pos){
        Article a = articleList.get(pos);

        if(a.getAuthor() == "null") {
            holder.author.setVisibility(View.GONE);
        }

        String pAuthor = "";
        if(a.getAuthor().contains("[")){
            pAuthor = parseAuthor(a.getAuthor());
            holder.author.setText(pAuthor);
        } else { holder.author.setText(a.getAuthor()); }

        holder.title.setText(a.getTitle());

        if(a.getTitle() == "null"){
            holder.title.setVisibility(View.GONE);
        }
        holder.title.setOnClickListener(v -> clickSendToBrowser(a.getUrl()));

        holder.desc.setText(a.getDesc());
        if(a.getDesc() == "null"){
            holder.desc.setVisibility(View.GONE);
        }
        holder.desc.setMovementMethod(new ScrollingMovementMethod());
        holder.desc.setOnClickListener(v -> clickSendToBrowser(a.getUrl()));

        if(a.getDate() == "null"){
            holder.date.setVisibility(View.GONE);
        }
        holder.date.setText(newFormatDateTime(a.getDate()));

        picasso = Picasso.get();
        loadRemoteImage(a.getUrlToImage(), holder.image);
        holder.image.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        holder.image.setOnClickListener(v -> clickSendToBrowser(a.getUrl()));

        holder.pageNum.setText(String.format(
                Locale.getDefault(),"%d of %d", (pos+1), articleList.size()));
    }

    @Override
    public int getItemCount() { return articleList.size(); }

    public void clickSendToBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        mainAct.startActivity(intent);
    }

    private void loadRemoteImage(String imageURL, ImageView image){
        picasso.load(imageURL)
                .error(R.drawable.brokenimage)
                .placeholder(R.drawable.loading)
                .into(image);

        if(imageURL == null){
            picasso.load(R.drawable.noimage)
                    .into(image);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String newFormatDateTime(String tzDateTime){
        try {
            DateTimeFormatter parser = DateTimeFormatter.ISO_DATE_TIME;
            Instant instant = parser.parse(tzDateTime, Instant::from);
            LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM d, yyyy hh:mm");
            return ldt.format(dtf);

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String parseAuthor(String s) {
        String sub = s.substring(27);
        String[] split = sub.split("\"");
        return split[0];
    }
}
