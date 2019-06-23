package com.example.blogmy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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


    private static final String JIKAN_TOP_AIRING_DEFAULT = JIKAN_URL + "/" + JIKAN_SORTBY_TOP + "/" + JIKAN_TYPE_ANIME + "/" + JIKAN_PAGE_1 + "/" + JIKAN_SUBTYPE_AIRING;
    private static final String JIKAN_ANIME_DETAILS_DEFAULT = JIKAN_URL + "/" + JIKAN_TYPE_ANIME + "/";
    private static final String JIKAN_ANIME_STATS_DEFAULT= JIKAN_URL + "/" + JIKAN_TYPE_ANIME + "/";

    private Toolbar mToolbar;
    private AsyncHttpClient client;
    public ProgressBar progbar;
    private String anime_id;
    private RecyclerView animeReturnList;
    private LinearLayoutManager linearLayoutManager;
    private final List<Messages> animeList = new ArrayList<>();
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


        progbar = (ProgressBar) findViewById(R.id.toolbarprogress);

    }

    // using JIKAN api
    private void initGetCharacter() {
        RequestParams params = new RequestParams();
        fetchCharactersResources(params, JIKAN_ANIME_DETAILS_DEFAULT + anime_id + "/" + JIKAN_ANIME_CHARACTERS);
    }

    private void fetchCharactersResources(RequestParams params, String URL){
        client = new AsyncHttpClient();
        progbar.setVisibility(View.VISIBLE);
        client.get(URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                progbar.setVisibility(View.GONE);
                characters_data_map = new LinkedHashMap<Integer, JSONObject>();
                characters_data_list = new ArrayList<Characters>();


                character_array = null;
                try {
                    character_array = (JSONArray)response.getJSONArray("characters");
                    characters_data_list= Characters.fromJson(character_array);
                    System.out.println("CharactersFragment data list: " + characters_data_list.size());
                    adapter = new CharactersAdapter(classContext, R.layout.characters_lists, characters_data_list);
                    characters_list_layout.setAdapter(adapter);
                    // adapter.addAll(characters_data_list);


                } catch (JSONException e) {
                    e.printStackTrace();
                    progbar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response){
                Log.e("CharactersFragment", "Fail" + e.toString());
                Log.e("CharactersFragment", "Status code: " + statusCode);
                progbar.setVisibility(View.GONE);
            }
        });
    }

    private void populateData(){
        messagesAdapter = new MessagesAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        client.cancelAllRequests(true);
        progbar.setVisibility(View.GONE);
    }
}
