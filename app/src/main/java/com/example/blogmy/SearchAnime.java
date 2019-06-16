package com.example.blogmy;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.blogmy.cards.SliderAdapter;
import com.example.blogmy.cards.CardSliderLayoutManager;
import com.example.blogmy.cards.CardSnapHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.helper.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

public class SearchAnime extends AppCompatActivity {

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

    // season constants
    private static final String JIKAN_SORTBY_SEASON = "season";
    private static final String JIKAN_SUBTYPE_SPRING = "spring";
    private static final String JIKAN_SUBTYPE_SUMMER = "summer";
    private static final String JIKAN_SUBTYPE_FALL = "fall";
    private static final String JIKAN_SUBTYPE_WINTER = "winter";
    private static final String JIKAN_SEASON_YEAR_DEFAULT = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));


    private static final String JIKAN_TOP_AIRING_DEFAULT = JIKAN_URL + "/" + JIKAN_SORTBY_TOP + "/" + JIKAN_TYPE_ANIME + "/" + JIKAN_PAGE_1 + "/" + JIKAN_SUBTYPE_AIRING;
    private static final String JIKAN_ANIME_DETAILS_DEFAULT = JIKAN_URL + "/" + JIKAN_TYPE_ANIME + "/";
    private static final String JIKAN_ANIME_STATS_DEFAULT= JIKAN_URL + "/" + JIKAN_TYPE_ANIME + "/";

    private Map<Integer,JSONObject> topAiringAnime = null;

    private List<String> animeIdList;
    private List<String> picsList;
    private List<String> typeList;
    private List<String> titleList;
    private List<String> scoreList;
    private List<String> rankList;
    private List<String> startTimeList;
    private List<String> endTimeList;

    private SliderAdapter sliderAdapter = null;

    private CardSliderLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private ImageSwitcher previewImgSwitcher;
    private TextSwitcher rankSwitcher;
    private TextSwitcher scoreSwitcher;
    private TextSwitcher clockSwitcher;
    private TextSwitcher descriptionsSwitcher;

    private TextView title1TextView;
    private TextView title2TextView;
    private int titleOffset1;
    private int titleOffset2;
    private long titleAnimDuration;
    private int currentPosition;

    private Toolbar toolbar;
    private TabLayout tabLayout;

    // INDEX PART
    private Button searchTypeTopButton;
    private Button searchTypeButton;
    private Button searchSubTypeButton;
    private Button searchPageButton;
    private String currentTypeTopButton;
    private String currentTypeButton;
    private String currentSubTypeButton;
    private String currentPageButton;

    // Fragments
    private Fragment summaryFragment;
    private Fragment statsFragment;
    private Fragment charactersFragment;
    private JSONObject animeDetailsObject;
    private JSONObject animeStatsDetailsObject;
    // Summary fragment
    private String summaryData;
    private String summaryTitle;
    // Stats fragment
    private String watching;
    private String completed;
    private String on_hold;
    private String dropped;
    private String plan_to_watch;
    private String total;

    private double score_1_pct;
    private double score_2_pct;
    private double score_3_pct;
    private double score_4_pct;
    private double score_5_pct;
    private double score_6_pct;
    private double score_7_pct;
    private double score_8_pct;
    private double score_9_pct;
    private double score_10_pct;
    private String score_1_vt;
    private String score_2_vt;
    private String score_3_vt;
    private String score_4_vt;
    private String score_5_vt;
    private String score_6_vt;
    private String score_7_vt;
    private String score_8_vt;
    private String score_9_vt;
    private String score_10_vt;

    // Characters fragment
    // Also the current active anime selected
    private String anime_id;
    private String bookmark_image;
    private String bookmark_name;

    // bookmark
    private ImageButton bookmarkBtn;

    private EnhancedWrapContentViewPager viewPager;
    private ViewPagerAdapter adapter;
    private FloatingActionButton floatingActionButton;
    private NestedScrollView scrollView;
    // private HorizontalScrollView anime_titles;

    //Firebase stuff
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserID;
    private final List<Anime> animeLists = new ArrayList<>();

    // progress bar
    public ProgressBar progbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_anime);

        //Firebase stuff
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // anime_titles = (HorizontalScrollView) findViewById(R.id.anime_titles);


        // initializeIndex and buttons TOP/ANIME/1/AIRING
        /*
        currentTypeTopButton = JIKAN_SORTBY_TOP;
        currentTypeButton = JIKAN_TYPE_ANIME;
        currentSubTypeButton = JIKAN_SUBTYPE_AIRING;
        currentPageButton = JIKAN_PAGE_1;
        */

        // progressbar
        progbar = findViewById(R.id.toolbarprogress);
        // progbar.setVisibility(View.VISIBLE);

        toolbar = (Toolbar) findViewById(R.id.search_anime_list_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Anime Explorer");


        searchTypeTopButton = (Button) findViewById(R.id.search_top_button_type);
        searchTypeButton = (Button) findViewById(R.id.search_type_button_type);
        searchSubTypeButton = (Button) findViewById(R.id.search_subtype_button_type);
        searchPageButton = (Button) findViewById(R.id.search_page_button_type);
        bookmarkBtn = (ImageButton) findViewById(R.id.search_anime_bookmark_btn);

        bookmarkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.6F);
                buttonClick.setDuration(300);
                v.startAnimation(buttonClick);

                if (!TextUtils.isEmpty(anime_id) && !TextUtils.isEmpty(bookmark_name)
                        && !TextUtils.isEmpty(bookmark_image)) {

                    usersRef.child(currentUserID).child("bookmarks").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Map currentAnimeMap = new HashMap();
                                // if its the same anime trying to be added then don't change count
                                if(dataSnapshot.exists() && dataSnapshot.hasChild(anime_id)){
                                    // dont update if exist
                                    Toast.makeText(SearchAnime.this, "This anime is already in your bookmarks", Toast.LENGTH_SHORT).show();
                                } else {
                                    // UTC timestamp in seconds
                                    String ts = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                                    currentAnimeMap.put("id", anime_id);
                                    currentAnimeMap.put("name", bookmark_name.trim());
                                    currentAnimeMap.put("image", bookmark_image);
                                    currentAnimeMap.put("counter", ts);

                                    usersRef.child(currentUserID).child("bookmarks").child(anime_id)
                                            .updateChildren(currentAnimeMap).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(SearchAnime.this, "This anime is added into your bookmarks", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        });

        // START: SCROLLING PART AND FLOATING ACTION BUTTON
        scrollView = (NestedScrollView) findViewById(R.id.search_anime_scroll);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.search_anime_floating_button);
        floatingActionButton.setVisibility(View.INVISIBLE);
        // ALTERNATIVE, WARNING viewTreeObserver can cause memory leaks.
        // https://developer.android.com/reference/android/view/ViewTreeObserver.html
        // viewtreeobserver is needed so that we got the data reference when the view is laid out on screen
        // scrollView.getViewTreeObserver().addOnScrollChangedListener
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if(scrollY > 200){
                    floatingActionButton.setVisibility(View.VISIBLE);
                } else {
                    floatingActionButton.setVisibility(View.INVISIBLE);
                }
            }
        });
        // on floatingactionbutton clicked, scroll the scrollview to top view.
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        // END: SCROLL SECTION


        // search button
        searchTypeTopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(SearchAnime.this, v);
                menu.getMenu().add(JIKAN_SORTBY_TOP.toUpperCase());
                menu.getMenu().add(JIKAN_SORTBY_SEASON.toUpperCase());
                menu.show();

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        searchTypeTopButton.setText(item.getTitle());

                        // change search buttons when its 'SEASON'
                        if(item.getTitle().equals(JIKAN_SORTBY_SEASON.toUpperCase())){
                            searchTypeButton.setText(JIKAN_SEASON_YEAR_DEFAULT);
                            searchPageButton.setText(JIKAN_SUBTYPE_SUMMER.toUpperCase());
                            searchSubTypeButton.setVisibility(View.INVISIBLE);
                        } else {
                            searchTypeButton.setText(JIKAN_TYPE_ANIME.toUpperCase());
                            searchPageButton.setText(JIKAN_PAGE_1);
                            searchSubTypeButton.setVisibility(View.VISIBLE);
                        }
                      //  updateTopAiring();
                        return false;
                    }
                });
            }
        });

        searchTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(SearchAnime.this, v);
                if(searchTypeTopButton.getText().toString().equals(JIKAN_SORTBY_SEASON.toUpperCase())){
                    for(int i=2000; i<= Integer.parseInt(JIKAN_SEASON_YEAR_DEFAULT); i++ ){
                        menu.getMenu().add(Integer.toString(i));
                    }
                } else {
                    menu.getMenu().add("ANIME");
                }
                menu.show();

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        searchTypeButton.setText(item.getTitle());
                        //  updateTopAiring();
                        return false;
                    }
                });
            }
        });


        // page number button
        searchPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(SearchAnime.this, v);
                if(searchTypeTopButton.getText().toString().equals(JIKAN_SORTBY_SEASON.toUpperCase())){
                    menu.getMenu().add(JIKAN_SUBTYPE_SUMMER.toUpperCase());
                    menu.getMenu().add(JIKAN_SUBTYPE_SPRING.toUpperCase());
                    menu.getMenu().add(JIKAN_SUBTYPE_FALL.toUpperCase());
                    menu.getMenu().add(JIKAN_SUBTYPE_WINTER.toUpperCase());
                } else {
                    menu.getMenu().add("1");
                    menu.getMenu().add("2");
                    menu.getMenu().add("3");
                    menu.getMenu().add("4");
                }

                menu.show();

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        searchPageButton.setText(item.getTitle());
                        updateTopAiring();
                        return false;
                    }
                });
            }
        });

        // subtype button
        searchSubTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(SearchAnime.this, v);
                menu.getMenu().add("AIRING");
                menu.getMenu().add("UPCOMING");
                menu.getMenu().add("TV");
                menu.getMenu().add("MOVIE");
                menu.getMenu().add("OVA");
                menu.getMenu().add("SPECIAL");
                menu.show();

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        searchSubTypeButton.setText(item.getTitle());
                        updateTopAiring();
                        return false;
                    }
                });
            }
        });

        // Pager fragment initialization
        viewPager = (EnhancedWrapContentViewPager) findViewById(R.id.search_anime_viewpager);
        // Set the ViewPagerAdapter into ViewPager
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        summaryFragment = new SummaryFragment();
        statsFragment = new StatsFragment();
        charactersFragment = new CharactersFragment();
        adapter.addFrag(summaryFragment, "Summary");
        adapter.addFrag(statsFragment, "Stats");
        adapter.addFrag(charactersFragment, "Characters");
        viewPager.setAdapter(adapter);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.search_anime_tab_layout);
        mTabLayout.setupWithViewPager(viewPager);


        /* Listener to viewpager
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewPager.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        */

        // get initial top airing during activity launch
        initGetTopAiring();
    }



    private void updateFragment(){
        // Fragment management
        // Locate the viewpager in activity_main.xml
        // Summary Fragment update
        Bundle bundleSum = new Bundle();
        bundleSum.putString("summaryData", summaryData);
        bundleSum.putString("summaryTitle", summaryTitle);
        summaryFragment.setArguments(bundleSum);

        // Stats Fragment update
        Bundle bundleStat = new Bundle();
        bundleStat.putString("watching", watching);
        bundleStat.putString("completed", completed);
        bundleStat.putString("on_hold", on_hold);
        bundleStat.putString("dropped", dropped);
        bundleStat.putString("plan_to_watch", plan_to_watch);

        // score part
        bundleStat.putDouble("score_1_pct", score_1_pct);
        bundleStat.putDouble("score_2_pct", score_2_pct);
        bundleStat.putDouble("score_3_pct", score_3_pct);
        bundleStat.putDouble("score_4_pct", score_4_pct);
        bundleStat.putDouble("score_5_pct", score_5_pct);
        bundleStat.putDouble("score_6_pct", score_6_pct);
        bundleStat.putDouble("score_7_pct", score_7_pct);
        bundleStat.putDouble("score_8_pct", score_8_pct);
        bundleStat.putDouble("score_9_pct", score_9_pct);
        bundleStat.putDouble("score_10_pct", score_10_pct);

        bundleStat.putString("score_1_vt", score_1_vt);
        bundleStat.putString("score_2_vt", score_2_vt);
        bundleStat.putString("score_3_vt", score_3_vt);
        bundleStat.putString("score_4_vt", score_4_vt);
        bundleStat.putString("score_5_vt", score_5_vt);
        bundleStat.putString("score_6_vt", score_6_vt);
        bundleStat.putString("score_7_vt", score_7_vt);
        bundleStat.putString("score_8_vt", score_8_vt);
        bundleStat.putString("score_9_vt", score_9_vt);
        bundleStat.putString("score_10_vt", score_10_vt);
        statsFragment.setArguments(bundleStat);

        // Character Fragment update
        Bundle bundleChar = new Bundle();
        bundleChar.putString("anime_id", anime_id);
        charactersFragment.setArguments(bundleChar);

        adapter.notifyDataSetChanged();

    }


    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        // set to none to update fragments
        // https://stackoverflow.com/questions/37070137/cant-make-fragmentpageradapter-to-update-fragments-contents
        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    // using JIKAN api
    private void initGetTopAiring() {
        RequestParams params = new RequestParams();
        letsDoSomeNetworkingTopAnime(params, JIKAN_TOP_AIRING_DEFAULT, false);
    }

    private void updateTopAiring(){

        RequestParams params = new RequestParams();
        String url =  JIKAN_URL + "/" + JIKAN_SORTBY_TOP + "/" + JIKAN_TYPE_ANIME + "/" + searchPageButton.getText().toString().toLowerCase() + "/" + searchSubTypeButton.getText().toString().toLowerCase();

        letsDoSomeNetworkingTopAnime(params,
                url,
                true);
    }

    // using JIKAN api
    private void initGetAnimeDetails(String animeId, final UpdateDataCallback updateDataCallback) {
        RequestParams params = new RequestParams();
        letsDoSomeNetworkingAnimeDetails(params, JIKAN_ANIME_DETAILS_DEFAULT + animeId, animeId, updateDataCallback);

    }

    // TODO: Add letsDoSomeNetworkingTopAnime(RequestParams params) here:
    private void letsDoSomeNetworkingAnimeDetails(final RequestParams params, String URL, final String animeId, final UpdateDataCallback updateDataCallback){
        final AsyncHttpClient client = new AsyncHttpClient();
        progbar.setVisibility(View.VISIBLE);
        // 1. Summary fragment
        client.get(URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                Log.d("SearchAnime", "Success");
                // process the response object
                animeDetailsObject = response;
                try {
                    if (animeDetailsObject.has("synopsis")) {
                        summaryData = animeDetailsObject.getString("synopsis");
                        summaryTitle = animeDetailsObject.getString("title");
                    } else {
                        summaryData = "";
                        summaryTitle = "";
                    }
                    //  enable updateDataCallback again later
                    //  updateDataCallback.onCallback("success");
                } catch (JSONException e) {
                    summaryData = "";
                    summaryTitle = "";
                    e.printStackTrace();
                    progbar.setVisibility(View.GONE);
                }

                String statsURL = JIKAN_ANIME_STATS_DEFAULT + animeId + "/" + JIKAN_ANIME_STATS;
                // 2. Stats Fragment
                client.get(statsURL, params, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                        progbar.setVisibility(View.GONE);
                        Log.d("SearchAnime", "Success");
                        // process the response object
                        animeStatsDetailsObject = response;
                        try {

                            if (animeStatsDetailsObject.has("watching")) {
                                watching = animeStatsDetailsObject.getString("watching");
                            } else {
                                watching = "";
                            }

                            if (animeStatsDetailsObject.has("completed")) {
                                completed = animeStatsDetailsObject.getString("completed");
                            } else {
                                completed = "";
                            }

                            if (animeStatsDetailsObject.has("on_hold")) {
                                on_hold = animeStatsDetailsObject.getString("on_hold");
                            } else {
                                on_hold = "";
                            }

                            if (animeStatsDetailsObject.has("dropped")) {
                                dropped = animeStatsDetailsObject.getString("dropped");
                            } else {
                                dropped = "";
                            }

                            if (animeStatsDetailsObject.has("plan_to_watch")) {
                                plan_to_watch = animeStatsDetailsObject.getString("plan_to_watch");
                            } else {
                                plan_to_watch = "";
                            }

                            if (animeStatsDetailsObject.has("total")) {
                                total = animeStatsDetailsObject.getString("total");
                            } else {
                                total = "";
                            }

                            if (animeStatsDetailsObject.has("scores")) {
                                if(animeStatsDetailsObject.getJSONObject("scores").has("1")){
                                    score_1_pct = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("1").getDouble("percentage");
                                    score_1_vt = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("1").getString("votes");
                                } else {
                                    score_1_pct = 0.0;
                                    score_1_vt = "0";
                                }

                                if(animeStatsDetailsObject.getJSONObject("scores").has("2")){
                                    score_2_pct = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("2").getDouble("percentage");
                                    score_2_vt = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("2").getString("votes");
                                } else {
                                    score_2_pct = 0.0;
                                    score_2_vt = "0";
                                }

                                if(animeStatsDetailsObject.getJSONObject("scores").has("3")){
                                    score_3_pct = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("3").getDouble("percentage");
                                    score_3_vt = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("3").getString("votes");
                                } else {
                                    score_3_pct = 0.0;
                                    score_3_vt = "0";
                                }

                                if(animeStatsDetailsObject.getJSONObject("scores").has("4")){
                                    score_4_pct = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("4").getDouble("percentage");
                                    score_4_vt = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("4").getString("votes");
                                } else {
                                    score_4_pct = 0.0;
                                    score_4_vt = "0";
                                }

                                if(animeStatsDetailsObject.getJSONObject("scores").has("5")){
                                    score_5_pct = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("5").getDouble("percentage");
                                    score_5_vt = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("5").getString("votes");
                                } else {
                                    score_5_pct = 0.0;
                                    score_5_vt = "0";
                                }

                                if(animeStatsDetailsObject.getJSONObject("scores").has("6")){
                                    score_6_pct = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("6").getDouble("percentage");
                                    score_6_vt = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("6").getString("votes");
                                } else {
                                    score_6_pct = 0.0;
                                    score_6_vt = "0";
                                }

                                if(animeStatsDetailsObject.getJSONObject("scores").has("7")){
                                    score_7_pct = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("7").getDouble("percentage");
                                    score_7_vt = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("7").getString("votes");
                                } else {
                                    score_3_pct = 0.0;
                                    score_3_vt = "0";
                                }

                                if(animeStatsDetailsObject.getJSONObject("scores").has("8")){
                                    score_8_pct = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("8").getDouble("percentage");
                                    score_8_vt = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("8").getString("votes");
                                } else {
                                    score_8_pct = 0.0;
                                    score_8_vt = "0";
                                }

                                if(animeStatsDetailsObject.getJSONObject("scores").has("9")){
                                    score_9_pct = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("9").getDouble("percentage");
                                    score_9_vt = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("9").getString("votes");
                                } else {
                                    score_9_pct = 0.0;
                                    score_9_vt = "0";
                                }

                                if(animeStatsDetailsObject.getJSONObject("scores").has("10")){
                                    score_10_pct = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("10").getDouble("percentage");
                                    score_10_vt = animeStatsDetailsObject.getJSONObject("scores").getJSONObject("10").getString("votes");
                                } else {
                                    score_10_pct = 0.0;
                                    score_10_vt = "0";
                                }
                            } else {
                                score_1_pct = 0.0;
                                score_1_vt = "0";
                                score_2_pct = 0.0;
                                score_2_vt = "0";
                                score_3_pct = 0.0;
                                score_3_vt = "0";
                                score_4_pct = 0.0;
                                score_4_vt = "0";
                                score_5_pct = 0.0;
                                score_5_vt = "0";
                                score_6_pct = 0.0;
                                score_6_vt = "0";
                                score_7_pct = 0.0;
                                score_7_vt = "0";
                                score_8_pct = 0.0;
                                score_8_vt = "0";
                                score_9_pct = 0.0;
                                score_9_vt = "0";
                                score_10_pct = 0.0;
                                score_10_vt = "0";
                            }

                            //  enable updateDataCallback again later
                            updateDataCallback.onCallback("success");
                        } catch (JSONException e) {
                            watching = "";
                            completed = "";
                            on_hold = "";
                            dropped = "";
                            plan_to_watch = "";
                            total = "";

                            score_1_pct = 0.0;
                            score_1_vt = "0";
                            score_2_pct = 0.0;
                            score_2_vt = "0";
                            score_3_pct = 0.0;
                            score_3_vt = "0";
                            score_4_pct = 0.0;
                            score_4_vt = "0";
                            score_5_pct = 0.0;
                            score_5_vt = "0";
                            score_6_pct = 0.0;
                            score_6_vt = "0";
                            score_7_pct = 0.0;
                            score_7_vt = "0";
                            score_8_pct = 0.0;
                            score_8_vt = "0";
                            score_9_pct = 0.0;
                            score_9_vt = "0";
                            score_10_pct = 0.0;
                            score_10_vt = "0";
                            e.printStackTrace();
                            progbar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response){
                        Log.e("SearchAnime", "Fail" + e.toString());
                        Log.e("SearchAnime", "Status code: " + statusCode);
                        progbar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response){
                Log.e("SearchAnime", "Fail" + e.toString());
                Log.e("SearchAnime", "Status code: " + statusCode);
                progbar.setVisibility(View.GONE);
            }
        });


    }

    public interface UpdateDataCallback {
        void onCallback(String value);
    }


    // TODO: Add letsDoSomeNetworkingTopAnime(RequestParams params) here:
    private void letsDoSomeNetworkingTopAnime(RequestParams params, String URL, final boolean update){
        AsyncHttpClient client = new AsyncHttpClient();
        progbar.setVisibility(View.VISIBLE);
        client.get(URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                progbar.setVisibility(View.GONE);
                Toast.makeText(SearchAnime.this, "Request Successful", Toast.LENGTH_SHORT).show();

                // add top airing anime data into the List topAiringAnime
                topAiringAnime = new LinkedHashMap<Integer, JSONObject>();
                animeIdList = new ArrayList<String>();
                picsList = new ArrayList<String>();
                typeList = new ArrayList<String>();
                titleList = new ArrayList<String>();
                scoreList = new ArrayList<String>();
                rankList = new ArrayList<String>();
                startTimeList = new ArrayList<String>();
                endTimeList = new ArrayList<String>();

                JSONArray jArray = null;
                try {
                    jArray = (JSONArray)response.getJSONArray("top");
                    if (jArray != null) {
                        for (int i=0;i<jArray.length();i++){
                            topAiringAnime.put(i,jArray.getJSONObject(i));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progbar.setVisibility(View.GONE);
                }

                for(int i=0; i < topAiringAnime.size(); i++){
                    try {
                        animeIdList.add(topAiringAnime.get(i).get("mal_id").toString());
                        picsList.add(topAiringAnime.get(i).get("image_url").toString());
                        titleList.add("   " + topAiringAnime.get(i).get("title").toString() + "   ");
                        rankList.add(topAiringAnime.get(i).get("rank").toString());
                        scoreList.add(topAiringAnime.get(i).get("score").toString());
                        typeList.add(topAiringAnime.get(i).get("type").toString());
                        if(topAiringAnime.get(i).get("start_date").toString().equals("null")){
                            startTimeList.add("?");
                        } else {
                            startTimeList.add(topAiringAnime.get(i).get("start_date").toString());
                        }

                        if(topAiringAnime.get(i).get("end_date").toString().equals("null")){
                            endTimeList.add("?");
                        }else{
                            endTimeList.add(topAiringAnime.get(i).get("end_date").toString());
                        }

                    } catch (JSONException e) {
                        progbar.setVisibility(View.GONE);
                        e.printStackTrace();
                    }
                }

                if(update == false){
                    sliderAdapter = new SliderAdapter(SearchAnime.this, picsList, topAiringAnime.size(), new OnCardClickListener());
                    anime_id = animeIdList.get(0);
                    bookmark_image = picsList.get(0);
                    bookmark_name = titleList.get(0);
                    initRecyclerView();
                    initTitleText();
                    initSwitchers();
                } else {
                    anime_id = animeIdList.get(0);
                    bookmark_image = picsList.get(0);
                    bookmark_name = titleList.get(0);
                    sliderAdapter.updateData(picsList, new UpdateDataCallback(){
                        @Override
                        public void onCallback(String value) {
                            sliderAdapter.notifyDataSetChanged();
                            updateTitleText();
                            updateSwitchers();

                            recyclerView.scrollToPosition(RecyclerView.SCROLLBAR_POSITION_DEFAULT);
                           // layoutManager.scrollToPosition(RecyclerView.SCROLLBAR_POSITION_DEFAULT);
                        }
                    });

                    //layoutManager.scrollToPosition(RecyclerView.SCROLLBAR_POSITION_DEFAULT);
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response){
                Log.e("SearchAnime", "Fail" + e.toString());
                Log.e("SearchAnime", "Status code: " + statusCode);
                progbar.setVisibility(View.GONE);
                Toast.makeText(SearchAnime.this, "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(sliderAdapter);
        recyclerView.setHasFixedSize(true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onActiveCardChange();
                }
            }
        });

        layoutManager = (CardSliderLayoutManager) recyclerView.getLayoutManager();

        new CardSnapHelper().attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void updateSwitchers(){
        rankSwitcher.setCurrentText("Rank: " + rankList.get(0) + "/" + topAiringAnime.size()*Integer.parseInt(searchPageButton.getText().toString()));
        scoreSwitcher.setCurrentText("Score: " + scoreList.get(0) + "/10");
        clockSwitcher.setCurrentText(startTimeList.get(0) + " ~ " + endTimeList.get(0));
        descriptionsSwitcher.setCurrentText("Type: " + typeList.get(0));
        initGetAnimeDetails(animeIdList.get(0), new UpdateDataCallback() {
            @Override
            public void onCallback(String value) {
                updateFragment();

            }
        });
    }

    private void initSwitchers() {
        rankSwitcher = (TextSwitcher) findViewById(R.id.ts_rank);
        rankSwitcher.setFactory(new TextViewFactory(R.style.RankTextView, true));
        rankSwitcher.setCurrentText("Rank: " + rankList.get(0) + "/" + topAiringAnime.size()*Integer.parseInt(searchPageButton.getText().toString()));

        scoreSwitcher = (TextSwitcher) findViewById(R.id.ts_score);
        scoreSwitcher.setFactory(new TextViewFactory(R.style.ScoreTextView, false));
        scoreSwitcher.setCurrentText("Score: " + scoreList.get(0) + "/10");

        clockSwitcher = (TextSwitcher) findViewById(R.id.ts_clock);
        clockSwitcher.setFactory(new TextViewFactory(R.style.ClockTextView, false));
        clockSwitcher.setCurrentText(startTimeList.get(0) + " ~ " + endTimeList.get(0));

        descriptionsSwitcher = (TextSwitcher) findViewById(R.id.ts_description);
        descriptionsSwitcher.setInAnimation(this, android.R.anim.fade_in);
        descriptionsSwitcher.setOutAnimation(this, android.R.anim.fade_out);
        descriptionsSwitcher.setFactory(new TextViewFactory(R.style.DescriptionTextView, false));
        descriptionsSwitcher.setCurrentText("Type: " + typeList.get(0));

        /*
        previewImgSwitcher = (ImageSwitcher) findViewById(R.id.ts_preview_image);
        previewImgSwitcher.setInAnimation(this, R.anim.fade_in);
        previewImgSwitcher.setOutAnimation(this, R.anim.fade_out);
        previewImgSwitcher.setFactory(new ImageViewFactory());
        */


        // get current active initial = 0 , anime id here when card switch
        initGetAnimeDetails(animeIdList.get(0), new UpdateDataCallback() {
                @Override
                public void onCallback(String value) {
                    updateFragment();
                }
            });
    }

    private void updateTitleText(){

        title1TextView.setText(titleList.get(0));
        title2TextView.setText(titleList.get(0));
        title1TextView.setSelected(true);
        title2TextView.setSelected(true);
    }
    private void initTitleText() {
        /*
        anime_titles.post(new Runnable() {
            @Override
            public void run() {
                anime_titles.fullScroll(View.FOCUS_RIGHT);
            }
        });
        */

        titleAnimDuration = getResources().getInteger(R.integer.labels_animation_duration);
        titleOffset1 = getResources().getDimensionPixelSize(R.dimen.left_offset);
        titleOffset2 = getResources().getDimensionPixelSize(R.dimen.card_width);

        title1TextView = (TextView) findViewById(R.id.tv_title_1);
        title1TextView.setX(titleOffset1);

        title1TextView.setText(titleList.get(0));
        title1TextView.setSelected(true);
        // making the textview scrollable
        // title1TextView.setMovementMethod(new ScrollingMovementMethod());
        // delay the marquee, by activating it with a delay
        /*
        title1TextView.postDelayed(new Runnable() {
            @Override
            public void run() {
                title1TextView.setSelected(true);
            }
        }, 1000);
        */

        title2TextView = (TextView) findViewById(R.id.tv_title_2);
        title2TextView.setX(titleOffset2);
        title2TextView.setSelected(true);
        title2TextView.setAlpha(0f);

        title1TextView.setTypeface(Typeface.createFromAsset(getAssets(), "open-sans-extrabold.ttf"));
        title2TextView.setTypeface(Typeface.createFromAsset(getAssets(), "open-sans-extrabold.ttf"));

    }


    private void setTitleText(String text, boolean left2right) {
        final TextView invisibleText;
        final TextView visibleText;
        if (title1TextView.getAlpha() > title2TextView.getAlpha()) {
            visibleText = title1TextView;
            invisibleText = title2TextView;
        } else {
            visibleText = title2TextView;
            invisibleText = title1TextView;
        }

        final int vOffset;
        if (left2right) {
            invisibleText.setX(0);
            vOffset = titleOffset2;
        } else {
            invisibleText.setX(titleOffset2);
            vOffset = 0;
        }

        invisibleText.setText(text);

        final ObjectAnimator iAlpha = ObjectAnimator.ofFloat(invisibleText, "alpha", 1f);
        final ObjectAnimator vAlpha = ObjectAnimator.ofFloat(visibleText, "alpha", 0f);
        final ObjectAnimator iX = ObjectAnimator.ofFloat(invisibleText, "x", titleOffset1);
        final ObjectAnimator vX = ObjectAnimator.ofFloat(visibleText, "x", vOffset);

        final AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(iAlpha, vAlpha, iX, vX);
        animSet.setDuration(titleAnimDuration);
       // animSet.setStartDelay(500);
        animSet.start();
    }

    private void onActiveCardChange() {
        // reset the scroll back to initial
        // anime_titles.scrollTo(0,0);
        final int pos = layoutManager.getActiveCardPosition();
        if (pos == RecyclerView.NO_POSITION || pos == currentPosition) {
            return;
        }

        onActiveCardChange(pos);
    }

    private void onActiveCardChange(final int pos) {
        int animH[] = new int[] {R.anim.slide_in_right, R.anim.slide_out_left};
        int animV[] = new int[] {R.anim.slide_in_top, R.anim.slide_out_bottom};

        final boolean left2right = pos < currentPosition;
        if (left2right) {
            animH[0] = R.anim.slide_in_left;
            animH[1] = R.anim.slide_out_right;

            animV[0] = R.anim.slide_in_bottom;
            animV[1] = R.anim.slide_out_top;
        }

        setTitleText(titleList.get(pos), left2right);
        title1TextView.setSelected(true);
        title2TextView.setSelected(true);

        rankSwitcher.setInAnimation(SearchAnime.this, animH[0]);
        rankSwitcher.setOutAnimation(SearchAnime.this, animH[1]);
        rankSwitcher.setText("Rank: " + rankList.get(pos) + "/" + topAiringAnime.size()*Integer.parseInt(searchPageButton.getText().toString()));

        scoreSwitcher.setInAnimation(SearchAnime.this, animV[0]);
        scoreSwitcher.setOutAnimation(SearchAnime.this, animV[1]);
        scoreSwitcher.setText("Score: " + scoreList.get(pos)+ "/10");

        clockSwitcher.setInAnimation(SearchAnime.this, animV[0]);
        clockSwitcher.setOutAnimation(SearchAnime.this, animV[1]);
        clockSwitcher.setText(startTimeList.get(pos) + " ~ " + endTimeList.get(pos));

        descriptionsSwitcher.setText("Type: " + typeList.get(pos));


        currentPosition = pos;
        anime_id = animeIdList.get(pos);
        bookmark_image = picsList.get(pos);
        bookmark_name = titleList.get(pos);

        // get current active anime id here when card switch

        initGetAnimeDetails(animeIdList.get(pos), new UpdateDataCallback() {
            @Override
            public void onCallback(String value) {
                updateFragment();
            }
        });
    }

    private void updateAnimeDetails(String resId) {

        final int w = previewImgSwitcher.getWidth();
        final int h = previewImgSwitcher.getHeight();

        InputStream in = null;
        try {
            in = new java.net.URL(resId).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        previewImgSwitcher.setImageDrawable(new BitmapDrawable(getResources(), bitmap));

    }

    private class TextViewFactory implements  ViewSwitcher.ViewFactory {

        @StyleRes
        final int styleId;
        final boolean center;

        TextViewFactory(@StyleRes int styleId, boolean center) {
            this.styleId = styleId;
            this.center = center;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View makeView() {
            final TextView textView = new TextView(SearchAnime.this);

            if (center) {
                textView.setGravity(Gravity.CENTER);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                textView.setTextAppearance(SearchAnime.this, styleId);
            } else {
                textView.setTextAppearance(styleId);
            }

            return textView;
        }

    }

    private class ImageViewFactory implements ViewSwitcher.ViewFactory {
        @Override
        public View makeView() {
            final ImageView imageView = new ImageView(SearchAnime.this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            final ViewGroup.LayoutParams lp = new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(lp);

            return imageView;
        }
    }


    private class OnCardClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            final CardSliderLayoutManager lm =  (CardSliderLayoutManager) recyclerView.getLayoutManager();

            if (lm.isSmoothScrolling()) {
                return;
            }

            final int activeCardPosition = lm.getActiveCardPosition();
            if (activeCardPosition == RecyclerView.NO_POSITION) {
                return;
            }

            final int clickedPosition = recyclerView.getChildAdapterPosition(view);
           // if (clickedPosition == activeCardPosition) {
              //  final Intent intent = new Intent(SearchAnime.this, DetailsActivity.class);
             //   intent.putExtra(DetailsActivity.BUNDLE_IMAGE_ID, pics[activeCardPosition % pics.length]);
                /*
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent);
                } else {
                    final CardView cardView = (CardView) view;
                    final View sharedView = cardView.getChildAt(cardView.getChildCount() - 1);
                    final ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(SearchAnime.this, sharedView, "shared");
                    startActivity(intent, options.toBundle());
                }
            } else if (clickedPosition > activeCardPosition) {
            */
                if (clickedPosition > activeCardPosition) {
                recyclerView.smoothScrollToPosition(clickedPosition);
                onActiveCardChange(clickedPosition);
                }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        progbar.setVisibility(View.GONE);
    }
}
