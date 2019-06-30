package com.example.blogmy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class UserSearchAnimeActivity extends AppCompatActivity {
    // Constants:
    // schema REST for top anime, https://jikan.docs.apiary.io/#reference/0/schedule/top-request-example+schema?console=1
    private static final String JIKAN_URL = "https://api.jikan.moe/v3";
    private static final String JIKAN_SORTBY_TOP = "top";
    private static final String JIKAN_TYPE_ANIME = "anime";
    private static final String JIKAN_PAGE_1 = "1";
    private static final String JIKAN_PAGE_2 = "2";
    private static final String JIKAN_PAGE_3 = "3";
    private static final String JIKAN_PAGE_4 = "4";
    private static final String JIKAN_SUBTYPE_AIRING = "airing";
    private static final String JIKAN_SUBTYPE_UPCOMING = "upcoming";
    private static final String JIKAN_SUBTYPE_MOVIE = "movie";
    private static final String JIKAN_SUBTYPE_TV = "tv";
    private static final String JIKAN_SUBTYPE_OVA = "ova";
    private static final String JIKAN_SUBTYPE_SPECIAL = "special";
    private static final String JIKAN_ANIME_STATS = "stats";
    private static final String JIKAN_ANIME_CHARACTERS = "characters_staff";
    private static final String JIKAN_ANIME_SEARCH = "search";


    private static final String JIKAN_TOP_AIRING_DEFAULT = JIKAN_URL + "/" + JIKAN_SORTBY_TOP + "/" + JIKAN_TYPE_ANIME + "/" + JIKAN_PAGE_1 + "/" + JIKAN_SUBTYPE_AIRING;
    private static final String JIKAN_ANIME_DETAILS_DEFAULT = JIKAN_URL + "/" + JIKAN_TYPE_ANIME + "/";
    private static final String JIKAN_ANIME_STATS_DEFAULT = JIKAN_URL + "/" + JIKAN_TYPE_ANIME + "/";

    // need to add https://api.jikan.moe/v3/search/anime  +  ?q=Naruto&sort=descending
    private static final String JIKAN_ANIME_SEARCH_DEFAULT = JIKAN_URL + "/" + JIKAN_ANIME_SEARCH + "/" + JIKAN_TYPE_ANIME ;

    private Toolbar mToolbar;
    private AsyncHttpClient client;
    public ProgressBar progbar;
    private String anime_id;
    private RecyclerView animeReturnList;
    private LinearLayoutManager linearLayoutManager;
    private Map<Integer,JSONObject> anime_data_map = null;
    // replace this later with a class to represent search result
    // create adapter for this class as well.
    private List<UserSearchAnime> anime_data_list;
    private UserSearchAnimeAdapter animeAdapter;
    private JSONArray anime_array;
    private TextView searchAnimeText;
    private EditText searchInputText;
    private ImageButton searchInputButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            // Get the anime Id that is currently toggled.
            anime_id = bundle.getString("anime_id");
        }
        setContentView(R.layout.activity_user_search_anime);
        mToolbar = (Toolbar) findViewById(R.id.user_search_anime_bar_layout);
        // Do this to get action bar class functionality
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Anime Search");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchAnimeText = (TextView) findViewById(R.id.user_search_result_text);
        searchInputText = (EditText) findViewById(R.id.user_search_anime_text) ;
        searchInputButton = (ImageButton) findViewById(R.id.user_search_anime_button);

        progbar = (ProgressBar) findViewById(R.id.toolbarprogress);
        // fetch the result upon oncreate
        if(!TextUtils.isEmpty(anime_id)){
            searchAnimeText.setText("Search Result For: " + anime_id);
            initGetCharacter();
        }

        searchInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = searchInputText.getText().toString();
                anime_id = inputText;
                searchAnimeText.setText("Search Result For: " + anime_id);
                initGetCharacter();
            }
        });

    }

    // using JIKAN api
    private void initGetCharacter() {
        // example url https://api.jikan.moe/v3/search/anime?q=Naruto&sort=descending
        // escape url with URLEncoder.encode("your URL here", "UTF8")
        RequestParams params = new RequestParams();
        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(JIKAN_ANIME_SEARCH_DEFAULT + "?q=" + anime_id + "&sort=descending","UTF8");
            fetchCharactersResources(params, encodedUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void fetchCharactersResources(RequestParams params, String URL){

            client = new AsyncHttpClient();
            progbar.setVisibility(View.VISIBLE);
            client.get(URL, params, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                progbar.setVisibility(View.GONE);
                anime_array = null;
                anime_data_list = new ArrayList<UserSearchAnime>();
               // populateData();
                try {
                    anime_array = (JSONArray)response.getJSONArray("results");
                 //   System.out.println("AZADSEARCH: " + anime_array.toString());
                    if (anime_array != null) {
                        for (int i=0;i<anime_array.length();i++){
                            //System.out.println("AZADSEARCH: " + anime_array.getJSONObject(i).toString());
                            // String mal_id, String image_url, String title, String airing, String score, String episodes, String start_date, String end_date
                            anime_data_list.add(new UserSearchAnime(anime_array.getJSONObject(i).getString("mal_id"), anime_array.getJSONObject(i).getString("image_url"), anime_array.getJSONObject(i).getString("title")
                                , anime_array.getJSONObject(i).getString("airing"), anime_array.getJSONObject(i).getString("score"), anime_array.getJSONObject(i).getString("episodes")
                                , anime_array.getJSONObject(i).getString("start_date"), anime_array.getJSONObject(i).getString("end_date")));
                        }

                        populateData();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    progbar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response){
                Log.e("UserSearchAnimeActivity", "Fail" + e.toString());
                Log.e("UserSearchAnimeActivity", "Status code: " + statusCode);
                progbar.setVisibility(View.GONE);
            }
        });
    }

    private void populateData(){
        animeAdapter = new UserSearchAnimeAdapter(anime_data_list);
        animeReturnList = (RecyclerView) findViewById(R.id.user_search_anime_result);
        linearLayoutManager = new LinearLayoutManager(this);
        animeReturnList.setHasFixedSize(true);
        animeReturnList.setLayoutManager(linearLayoutManager);
        animeReturnList.setAdapter(animeAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        progbar.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            client.cancelAllRequests(true);
        }catch (Exception e){

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // one inherited from android.support.v4.app.FragmentActivity
        return false;
    }
}
