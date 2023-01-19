package com.example.newsaggregator;

import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class NewsArticleRunnable implements Runnable{

    private static final String TAG = "NewsArticleRunnable";

    private final MainActivity mainActivity;
    private final String sourceID;


    private static final String ARTICLE_URL = "https://newsapi.org/v2/top-headlines?";
    //private static final String APIKEY = "81c74e991683469c85352b513f81083a"; // 1
    //private static final String APIKEY = "3cc3cae9341341cc9234838d5230c712"; // 2
    private static final String APIKEY = "42317f9831fa4d58bb7803094f606b96"; //3

    public NewsArticleRunnable(MainActivity mainActivity, String sourceID){
        this.mainActivity = mainActivity;
        this.sourceID = sourceID;
    }

    @Override
    public void run() {
        Uri.Builder buildURL = Uri.parse(ARTICLE_URL).buildUpon();

        buildURL.appendQueryParameter("sources", sourceID);
        buildURL.appendQueryParameter("apiKey", APIKEY);
        String urlToUse = buildURL.build().toString();
        Log.d(TAG, "doInBackground: " + urlToUse);

        StringBuilder sb = new StringBuilder();

        try {

            URL url = new URL(urlToUse);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + connection.getResponseCode());
                handleResults(null);

                return;
            }

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "doInBackground: " + sb.toString());


        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            handleResults(null);
            return;
        }
        handleResults(sb.toString());
    }

    public void handleResults(final String s){
        if(s == null){
            Log.d(TAG, "handleResults: Failure in data download");
            mainActivity.runOnUiThread(mainActivity::downloadFailed);
            return;
        }

        final ArrayList<Article> articleList = parseJsonArticle(s);
        if(articleList == null){
            mainActivity.runOnUiThread(mainActivity::downloadFailed);
            return;
        }
        mainActivity.runOnUiThread(
                () -> mainActivity.updateArticleData(articleList));
    }

    private ArrayList<Article> parseJsonArticle(String s){

        ArrayList<Article> articleList = new ArrayList<>();

        try{
            JSONObject jObjMain = new JSONObject(s);

            JSONArray articles = jObjMain.getJSONArray("articles");

            if(articles.length() > 10){
                for(int i=0; i<10;i++){
                    JSONObject jArticle = (JSONObject) articles.get(i);
                    String author = jArticle.getString("author");
                    String title = jArticle.getString("title");
                    String desc = jArticle.getString("description");
                    String url = jArticle.getString("url");
                    String urlToImage = jArticle.getString("urlToImage");
                    String publishedAt = jArticle.getString("publishedAt");
                    articleList.add(new Article(author, title, desc, url, urlToImage, publishedAt));
                }
            } else {
                for (int i = 0; i < articles.length(); i++) {
                    JSONObject jArticle = (JSONObject) articles.get(i);
                    String author = jArticle.getString("author");
                    String title = jArticle.getString("title");
                    String desc = jArticle.getString("description");
                    String url = jArticle.getString("url");
                    String urlToImage = jArticle.getString("urlToImage");
                    String publishedAt = jArticle.getString("publishedAt");
                    articleList.add(new Article(author, title, desc, url, urlToImage, publishedAt));
                }
            }
            return articleList;

        }catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
