package com.example.blogmy;

import android.media.Image;
import android.os.Bundle;

import com.asura.library.posters.Poster;
import com.asura.library.posters.RemoteImage;
import com.asura.library.views.PosterSlider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class FullscreenCharActivity extends AppCompatActivity {
    private PosterSlider full_image_char;
    private String charName, charId, charURL;
    public ProgressBar progbar;
    // Constants:
    // schema REST for top anime, https://jikan.docs.apiary.io/#reference/0/schedule/top-request-example+schema?console=1
    private static final String JIKAN_URL = "https://api.jikan.moe/v3";
    private static final String JIKAN_SORTBY_TOP = "top";
    private static final String JIKAN_TYPE_ANIME = "anime";
    private static final String JIKAN_ANIME_CHARACTER_PICTURES = "character";
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
    private static final String JIKAN_PICTURES = "pictures";


    private static final String JIKAN_TOP_AIRING_DEFAULT = JIKAN_URL + "/" + JIKAN_SORTBY_TOP + "/" + JIKAN_TYPE_ANIME + "/" + JIKAN_PAGE_1 + "/" + JIKAN_SUBTYPE_AIRING;
    private static final String JIKAN_ANIME_DETAILS_DEFAULT = JIKAN_URL + "/" + JIKAN_TYPE_ANIME + "/";
    private static final String JIKAN_ANIME_STATS_DEFAULT = JIKAN_URL + "/" + JIKAN_TYPE_ANIME + "/";
    private static final String JIKAN_ANIME_CHARACTER_DEFAULT = JIKAN_URL + "/" + JIKAN_ANIME_CHARACTER_PICTURES + "/";

    private AsyncHttpClient client;
    private Map<Integer,JSONObject> characters_data_map = null;
    private JSONArray character_array;
    private List<Poster> imageSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_char);

        charName = getIntent().getExtras().get("charName").toString();
        charId = getIntent().getExtras().get("charId").toString();
        charURL = getIntent().getExtras().get("charURL").toString();

        Toolbar toolbar = findViewById(R.id.fullscreen_char_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(charName);
        //progress bar
        progbar = findViewById(R.id.toolbarprogress);

        full_image_char = (PosterSlider) findViewById(R.id.char_image_full);
        initGetImages();



       // Picasso.with(FullscreenCharActivity.this).load(charURL).placeholder(R.drawable.profile).into(full_image_char);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // one inherited from android.support.v4.app.FragmentActivity
        return false;
    }

    private void initGetImages() {
        RequestParams params = new RequestParams();
        fetchCharactersResources(params, JIKAN_ANIME_CHARACTER_DEFAULT + charId + "/" + JIKAN_PICTURES);
    }

    private void fetchCharactersResources(final RequestParams params, final String URL){
        client = new AsyncHttpClient();
        progbar.setVisibility(View.VISIBLE);
        client.get(URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                progbar.setVisibility(View.GONE);
                characters_data_map = new LinkedHashMap<Integer, JSONObject>();
                character_array = null;
                try {
                    character_array = (JSONArray)response.getJSONArray("pictures");

                    imageSlider = new ArrayList<Poster>();
                    imageSlider.add(new RemoteImage(charURL));
                    if (character_array != null) {
                        for (int i=0;i<character_array.length();i++){
                            imageSlider.add(new RemoteImage(character_array.getJSONObject(i).getString("large")));
                        }
                    }

                    full_image_char.setPosters(imageSlider);
                } catch (JSONException e) {
                    imageSlider = new ArrayList<Poster>();
                    imageSlider.add(new RemoteImage(charURL));
                    full_image_char.setPosters(imageSlider);
                    e.printStackTrace();
                    progbar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response){
                Log.e("FullscreenChar", "Fail" + e.toString());
                Log.e("FullscreenChar", "Status code: " + statusCode);
                progbar.setVisibility(View.GONE);
                fetchCharactersResources( params,  URL);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        progbar.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            client.cancelAllRequests(true);
        }catch (Exception e){

        }
    }


}
