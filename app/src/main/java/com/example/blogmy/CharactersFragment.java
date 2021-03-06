package com.example.blogmy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.blogmy.cards.SliderAdapter;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

public class CharactersFragment extends Fragment {
    private String anime_id;
    private ListView characters_list_layout;
    private ArrayList<Characters> characters_data_list;
    private Map<Integer,JSONObject> characters_data_map = null;
    private JSONArray character_array;
    private CharactersAdapter adapter;
    public ProgressBar progbar;

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

    private AsyncHttpClient client;
    private Context classContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            // Get the anime Id that is currently toggled.
            anime_id = bundle.getString("anime_id");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.characters_fragment_tab, container, false);
    }

    // Views are fully created
    // using context in fragment https://stackoverflow.com/questions/8215308/using-context-in-a-fragment
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        characters_list_layout = (ListView) view.findViewById(R.id.character_list_layout);
        progbar = (ProgressBar) getActivity().findViewById(R.id.toolbarprogress);


        // when using listview inside scrollview , use this hack to override the scrollview and scroll the listview instead
        characters_list_layout.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        characters_list_layout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String charName = adapter.getItem(position).getName();
                String charId = adapter.getItem(position).getMal_id();
                String charURL = adapter.getItem(position).getImage_url();

                Intent fullscreenIntent = new Intent(classContext, FullscreenCharActivity.class);
                fullscreenIntent.putExtra("charName", charName);
                fullscreenIntent.putExtra("charId", charId);
                fullscreenIntent.putExtra("charURL", charURL);
                startActivity(fullscreenIntent);
            }
        });
        initGetCharacter();


    }




    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        classContext = context;
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
               // System.out.println("CHARACTER: " +response.toString());
                characters_data_map = new LinkedHashMap<Integer, JSONObject>();
                characters_data_list = new ArrayList<Characters>();
                character_array = null;
                try {
                    character_array = (JSONArray)response.getJSONArray("characters");
                    characters_data_list= Characters.fromJson(character_array);
                    adapter = new CharactersAdapter(classContext, R.layout.characters_lists, characters_data_list);
                    characters_list_layout.setAdapter(adapter);
                   // adapter.addAll(characters_data_list);
                    progbar.setVisibility(View.GONE);


                } catch (JSONException e) {
                    e.printStackTrace();
                    progbar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response){
                Log.e("CharactersFragment", "Fail" + e.toString());
                Log.e("CharactersFragment", "Status code: " + statusCode);
                //Toast.makeText(classContext, "Fail to characters data, please try again", Toast.LENGTH_SHORT).show();
                progbar.setVisibility(View.GONE);
                initGetCharacter();

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
