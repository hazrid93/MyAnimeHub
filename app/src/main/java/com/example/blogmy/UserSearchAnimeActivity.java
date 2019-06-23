package com.example.blogmy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import cz.msebera.android.httpclient.Header;

public class UserSearchAnimeActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search_anime);

        mToolbar = (Toolbar) findViewById(R.id.user_search_anime_bar_layout);
        // Do this to get action bar class functionality
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Anime Search");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
}
