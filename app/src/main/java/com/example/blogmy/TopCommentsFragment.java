package com.example.blogmy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class TopCommentsFragment extends Fragment {
    private String anime_id;
    private ListView  comments_list_layout;
    private ArrayList<TopComments> comments_data_list;
    private Map<Integer,JSONObject> comments_data_map = null;
    private JSONArray comments_array;
    private TopCommentsAdapter adapter;
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
    private EnhancedWrapContentViewPager viewPager;

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
        // the current parent is the viewpager so container = viewpager
        viewPager = (EnhancedWrapContentViewPager) container;
        return inflater.inflate(R.layout.top_comments_fragment_tab, container, false);
    }

    // Views are fully created
    // using context in fragment https://stackoverflow.com/questions/8215308/using-context-in-a-fragment
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        comments_list_layout = (ListView) view.findViewById(R.id.top_comments_lists);
        progbar = (ProgressBar) getActivity().findViewById(R.id.toolbarprogress);

        /*
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                System.out.println("AZADVIEWPAGER2: " + position);
                viewPager.reMeasureCurrentPage(viewPager.getCurrentItem());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        */



        // when using listview inside scrollview , use this hack to override the scrollview and scroll the listview instead

        comments_list_layout.setOnTouchListener(new ListView.OnTouchListener() {
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


        /*
        comments_list_layout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        */
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
        // get only first page of reviews (cached) 20 comments
        fetchCharactersResources(params, JIKAN_ANIME_DETAILS_DEFAULT + anime_id + "/reviews/1");
    }

    private void fetchCharactersResources(RequestParams params, String URL){
        client = new AsyncHttpClient();
        progbar.setVisibility(View.VISIBLE);
        client.get(URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){

                comments_data_map = new LinkedHashMap<Integer, JSONObject>();
                comments_data_list = new ArrayList<TopComments>();
                comments_array = null;
                try {
                    comments_array = (JSONArray)response.getJSONArray("reviews");
                    comments_data_list= TopComments.fromJson(comments_array);
                    adapter = new TopCommentsAdapter(classContext, R.layout.top_comments_lists, comments_data_list);
                    comments_list_layout.setAdapter(adapter);
                   // adapter.addAll(characters_data_list);
                    progbar.setVisibility(View.GONE);
                    viewPager.reMeasureCurrentPage(viewPager.getCurrentItem());

                } catch (JSONException e) {
                    e.printStackTrace();
                    progbar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response){
                Log.e("TopCommentsFragment", "Fail" + e.toString());
                Log.e("TopCommentsFragment", "Status code: " + statusCode);
                progbar.setVisibility(View.GONE);
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
