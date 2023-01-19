package com.example.newsaggregator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final HashMap<String, HashSet<Source>> topicsToSources = new HashMap<>();
    private final HashMap<String, HashSet<Source>> countriesToSources = new HashMap<>();
    private final HashMap<String, HashSet<Source>> langToSources = new HashMap<>();
    private final ArrayList<Article> currentArticleList = new ArrayList<>();
    private final ArrayList<Source> currentSourceList = new ArrayList<>();
    private final ArrayList<Source> fullSourceList = new ArrayList<>();

    private final ArrayList<String> sourceDisplayed = new ArrayList<>();
    private final ArrayList<String> topicsDisplayed = new ArrayList<>();
    private final ArrayList<String> countriesDisplayed = new ArrayList<>();
    private final HashMap<String, String> countryCodes = new HashMap<>();
    private final ArrayList<String> langDisplayed = new ArrayList<>();
    private final HashMap<String, String> langCodes = new HashMap<>();
    private String criteria;

    private Menu menu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArticleAdapter articleAdapter;
    private ArrayAdapter<String> arrayAdapter;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.drawer_list);

        // Set up the drawer item click callback method
        mDrawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectDrawerItem(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
        );

        // Create the drawer toggle
        mDrawerToggle = new ActionBarDrawerToggle(
                this,            /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        currentSourceList.clear();
        currentArticleList.clear();

        articleAdapter = new ArticleAdapter(this, currentArticleList);
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(articleAdapter);
        viewPager.setCurrentItem(0);

        if(hasNetworkConnection()){
            new Thread(new NewsSourceRunnable(this)).start();
        } else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Internet connection needed to find news articles.");
            builder.setTitle("No Internet Connection");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        setTitle("News Gateway");
    }

    public void downloadFailed() {
        Log.d(TAG, "downloadFailed: ");
    }

    //questionable
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        return true;
    }

    // tells us which source was selected so that we can run the article thread
    @SuppressLint("NotifyDataSetChanged")
    private void selectDrawerItem(int position){

        viewPager.setCurrentItem(0);
        currentArticleList.clear();
        viewPager.setBackground(null);

        if(!(sourceDisplayed.get(position) == null)){ }

        Source s = currentSourceList.get(position);

        new Thread(new NewsArticleRunnable(this, s.getId())).start();
        setTitle(s.getName());
        arrayAdapter.notifyDataSetChanged();
        articleAdapter.notifyDataSetChanged();

    }

    // tells us which topic/country/lang was selected so we can decrease the source list
    @SuppressLint("NotifyDataSetChanged")
    //private void selectItem(int position)
    private void selectItem(int parentSubmenu){

        ArrayList<Source> temp1 = new ArrayList<>();
        temp1.addAll(currentSourceList);

        ArrayList<Source> temp2 = new ArrayList();

        if(criteria == "all"){
            for(Source s : fullSourceList)
                temp2.add(s);
            temp1.retainAll(temp2);
            currentSourceList.clear();
            currentSourceList.addAll(temp1);
        } else {

            if (parentSubmenu == 0) {
                for (Source s : topicsToSources.get(criteria))
                    temp2.add(s);
                temp1.retainAll(temp2);
                currentSourceList.clear();
                currentSourceList.addAll(temp1);
            }
            if (parentSubmenu == 1) {
                String temp = getKey(countryCodes, criteria);
                for (Source s : countriesToSources.get(temp.toLowerCase()))
                    temp2.add(s);
                temp1.retainAll(temp2);
                currentSourceList.clear();
                currentSourceList.addAll(temp1);
            }
            if (parentSubmenu == 2) {
                String temp = getKey(langCodes, criteria);
                for (Source s : langToSources.get(temp.toLowerCase()))
                    temp2.add(s);
                temp1.retainAll(temp2);
                currentSourceList.clear();
                currentSourceList.addAll(temp1);
            }
        }

        criteria = "";
        arrayAdapter = new ArrayAdapter<>(this, R.layout.drawer_item, sourceDisplayed);
        mDrawerList.setAdapter(arrayAdapter);

        if(currentSourceList.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("There were no sources found with the chosen criteria.");
            builder.setTitle("No Sources Found");

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        sourceDisplayed.clear();
        arrayAdapter.notifyDataSetChanged();
        setTitle("News Gateway (" + currentSourceList.size() + ")");

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }

        if (item.hasSubMenu())
            return true;

        int parentSubmenu = item.getGroupId();
        criteria = (String) item.getTitle();

        if(parentSubmenu == 0 | parentSubmenu == 1 | parentSubmenu == 2){
            selectItem(parentSubmenu);
        }
        ArrayList<String> lst = new ArrayList<>();
        for(int i = 0; i < currentSourceList.size(); i ++){
            Source s = currentSourceList.get(i);
            String name = s.getName();
            lst.add(name);
        }
        Collections.sort(lst);
        if (lst != null){
            sourceDisplayed.addAll(lst);
        }
        arrayAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void updateSourceData(ArrayList<Source> sList){

        //topics = 0, countries = 1, lang = 2
        SubMenu subMenu1 = menu.addSubMenu(0, 0,0,"Topics");
        SubMenu subMenu2 = menu.addSubMenu(1, 1, 1, "Countries");
        SubMenu subMenu3 = menu.addSubMenu(2, 2, 2, "Languages");

        currentSourceList.addAll(sList);
        fullSourceList.addAll(sList);
        setTitle("News Gateway (" + fullSourceList.size() + ")");

        for (Source s : sList){
            String topic = s.getCategory();
            if(!topicsToSources.containsKey(topic))
                topicsToSources.put(topic, new HashSet<>());
            Objects.requireNonNull(topicsToSources.get(topic)).add(s);

            String country = s.getCountry();
            if(!countriesToSources.containsKey(country))
                countriesToSources.put(country, new HashSet<>());
            Objects.requireNonNull(countriesToSources.get(country)).add(s);

            String lang = s.getLanguage();
            if(!langToSources.containsKey(lang))
                langToSources.put(lang, new HashSet<>());
            Objects.requireNonNull(langToSources.get(lang)).add(s);
        }

        ArrayList<String> tTopics = new ArrayList<>(topicsToSources.keySet());
        Collections.sort(tTopics);
        topicsDisplayed.addAll(tTopics);
        subMenu1.add(0, 0, 0, "all");
        for(int i = 1; i < tTopics.size(); i++)
            subMenu1.add(0, i, i, tTopics.get(i));

        ArrayList<String> tCountries = new ArrayList<>(countriesToSources.keySet());
        Collections.sort(tCountries);
        countriesDisplayed.addAll(tCountries);
        changeCountryDisplay(tCountries);

        subMenu2.add(1, 0, 0, "all");
        for(int i = 1; i < tCountries.size(); i++)
            subMenu2.add(1, i, i, tCountries.get(i));

        ArrayList<String> tLang = new ArrayList<>(langToSources.keySet());
        Collections.sort(tLang);
        langDisplayed.addAll(tLang);
        changeLangDisplay(tLang);
        subMenu3.add(2, 0, 0, "all");
        for(int i = 0; i < tLang.size(); i++)
            subMenu3.add(2, i, i, tLang.get(i));

        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    public void updateArticleData(ArrayList<Article> a){
        currentArticleList.addAll(a);
        articleAdapter.notifyDataSetChanged();

        if(currentArticleList.isEmpty()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("There were no articles found with the chosen source.");
            builder.setTitle("No Articles Found");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public ArrayList<String> changeLangDisplay(ArrayList<String> lst) {

        try {
            InputStream is =
                    this.getResources().openRawResource(R.raw.language_codes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder result = new StringBuilder();
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }

            JSONObject jObjMain = new JSONObject(result.toString());
            JSONArray countries = jObjMain.getJSONArray("languages");

            for (int i = 0; i < countries.length(); i++) {
                JSONObject jsonObject = countries.getJSONObject(i);
                String code = jsonObject.getString("code");
                String name = jsonObject.getString("name");

                langCodes.put(code, name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
         if (langCodes == null) {
                Log.d(TAG, "LangCodes failed");
         } else {
            for(String s : lst){
                int index = lst.indexOf(s);
                Log.d(TAG, "language equivalent: " + langCodes.get(s.toUpperCase()));
                lst.set(index, langCodes.get(s.toUpperCase()));     //retrieves name from code s
            }
        } return lst;
    }

    public ArrayList<String> changeCountryDisplay(ArrayList<String> lst) {

        try {
            InputStream is =
                    this.getResources().openRawResource(R.raw.country_codes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            StringBuilder result = new StringBuilder();
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }

            JSONObject jObjMain = new JSONObject(result.toString());
            JSONArray countries = jObjMain.getJSONArray("countries");

            for (int i = 0; i < countries.length(); i++) {
                JSONObject jsonObject = countries.getJSONObject(i);
                String code = jsonObject.getString("code");
                String name = jsonObject.getString("name");

                countryCodes.put(code, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (countryCodes == null) {
            Log.d(TAG, "CountryCodes failed");
        } else {
            for(String s : lst){
                int index = lst.indexOf(s);
                Log.d(TAG, "country equivalent: " + countryCodes.get(s.toUpperCase()));
                lst.set(index, countryCodes.get(s.toUpperCase()));     //retrieves name from code s
            }
        } return lst;
    }

    public static String getKey(HashMap<String, String> h, String value) {
        for (String key: h.keySet())
            if (value.equals(h.get(key))) { return key; }
        return null;
    }

    private boolean hasNetworkConnection() {
        ConnectivityManager connectivityManager = getSystemService(ConnectivityManager.class);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnectedOrConnecting());
    }

}