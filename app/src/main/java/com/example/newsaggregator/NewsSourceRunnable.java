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
import java.util.Collections;

import javax.net.ssl.HttpsURLConnection;

public class NewsSourceRunnable implements Runnable{

    private static String TAG = "NewsSourceRunnable";
    private final MainActivity mainActivity;
    private static final String SOURCE_URL = "https://newsapi.org/v2/sources?";
    //private static final String APIKEY = "81c74e991683469c85352b513f81083a"; //1
    //private static final String APIKEY = "3cc3cae9341341cc9234838d5230c712"; //2
    private static final String APIKEY = "42317f9831fa4d58bb7803094f606b96"; //3

    public NewsSourceRunnable(MainActivity mainActivity){ this.mainActivity = mainActivity; }


    @Override
    public void run() {
        Uri.Builder buildURL = Uri.parse(SOURCE_URL).buildUpon();

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

    private void handleResults(String s){
        if (s == null) {
            Log.d(TAG, "handleResults: Failure in data download");
            mainActivity.runOnUiThread(mainActivity::downloadFailed);
            return;
        }

        final ArrayList<Source> sourceList = parseJSONSource(s);
        if (sourceList == null){
            mainActivity.runOnUiThread(mainActivity::downloadFailed);
            return;
        }
        mainActivity.runOnUiThread(
                () -> mainActivity.updateSourceData(sourceList));
    }

    private ArrayList<Source> parseJSONSource(String s){

        ArrayList<Source> sourceList = new ArrayList<>();

        try{
            JSONObject jObjMain = new JSONObject(s);

            JSONArray sources = jObjMain.getJSONArray("sources");

            for(int i=0; i<sources.length();i++){
                JSONObject jSource = (JSONObject) sources.get(i);
                String id = jSource.getString("id");
                String name = jSource.getString("name");
                String category = jSource.getString("category");
                String language = jSource.getString("language");
                String country = jSource.getString("country");
                sourceList.add(new Source(id,name,category,language,country));
            }
            Collections.sort(sourceList);
            return sourceList;

        }catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}