package com.example.blogmy;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.blogmy.cards.SliderAdapter;
import com.example.blogmy.cards.CardSliderLayoutManager;
import com.example.blogmy.cards.CardSnapHelper;
import com.google.android.material.tabs.TabLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private static final String JIKAN_TOP_AIRING_DEFAULT = JIKAN_URL + "/" + JIKAN_SORTBY_TOP + "/" + JIKAN_TYPE_ANIME + "/" + JIKAN_PAGE_1 + "/" + JIKAN_SUBTYPE_AIRING;
    private static final String JIKAN_ANIME_DETAILS_DEFAULT = JIKAN_URL + "/" + JIKAN_TYPE_ANIME + "/";

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

    private JSONObject animeDetailsObject;
    private String summaryData;
    private EnhancedWrapContentViewPager viewPager;
    private ViewPagerAdapter adapter;


    // Fragments
    private Fragment summaryFragment;
    private Fragment statsFragment;
    // private HorizontalScrollView anime_titles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_anime);

        // anime_titles = (HorizontalScrollView) findViewById(R.id.anime_titles);


        // initializeIndex and buttons TOP/ANIME/1/AIRING
        currentTypeTopButton = JIKAN_SORTBY_TOP;
        currentTypeButton = JIKAN_TYPE_ANIME;
        currentSubTypeButton = JIKAN_SUBTYPE_AIRING;
        currentPageButton = JIKAN_PAGE_1;

        toolbar = (Toolbar) findViewById(R.id.search_anime_list_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Anime Explorer");

        searchTypeTopButton = (Button) findViewById(R.id.search_top_button_type);
        searchTypeTopButton.setPressed(true);
        searchTypeTopButton.setClickable(false);

        searchTypeButton = (Button) findViewById(R.id.search_type_button_type);
        searchTypeButton.setPressed(true);
        searchTypeButton.setClickable(false);

        searchSubTypeButton = (Button) findViewById(R.id.search_subtype_button_type);
        searchPageButton = (Button) findViewById(R.id.search_page_button_type);

        // page number button
        searchPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(SearchAnime.this, v);
                menu.getMenu().add("1");
                menu.getMenu().add("2");
                menu.getMenu().add("3");
                menu.getMenu().add("4");
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
                menu.getMenu().add("airing");
                menu.getMenu().add("upcoming");
                menu.getMenu().add("tv");
                menu.getMenu().add("movie");
                menu.getMenu().add("ova");
                menu.getMenu().add("special");
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
        adapter.addFrag(summaryFragment, "Summary");
        adapter.addFrag(statsFragment, "Stats");
        viewPager.setAdapter(adapter);

        TabLayout mTabLayout = (TabLayout) findViewById(R.id.search_anime_tab_layout);
        mTabLayout.setupWithViewPager(viewPager);

        // get initial top airing during activity launch
        initGetTopAiring();
    }

    private void updateFragment(){
        // Fragment management
        // Locate the viewpager in activity_main.xml
        Bundle bundle = new Bundle();
        bundle.putString("summaryData", summaryData);
        summaryFragment.setArguments(bundle);
        adapter.notifyDataSetChanged();

    }


    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

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
        searchTypeTopButton.setPressed(true);
        searchTypeTopButton.setClickable(false);
        searchTypeButton.setPressed(true);
        searchTypeButton.setClickable(false);

        RequestParams params = new RequestParams();
        String url =  JIKAN_URL + "/" + JIKAN_SORTBY_TOP + "/" + JIKAN_TYPE_ANIME + "/" + searchPageButton.getText().toString().toLowerCase() + "/" + searchSubTypeButton.getText().toString().toLowerCase();

        letsDoSomeNetworkingTopAnime(params,
                url,
                true);
    }

    // using JIKAN api
    private void initGetAnimeDetails(String animeId, final UpdateDataCallback updateDataCallback) {
        RequestParams params = new RequestParams();
        letsDoSomeNetworkingAnimeDetails(params, JIKAN_ANIME_DETAILS_DEFAULT + animeId, updateDataCallback);

    }

    // TODO: Add letsDoSomeNetworkingTopAnime(RequestParams params) here:
    private void letsDoSomeNetworkingAnimeDetails(RequestParams params, String URL, final UpdateDataCallback updateDataCallback){
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                Log.d("SearchAnime", "Success");
                // process the response object
                animeDetailsObject = response;
                try {
                    summaryData = animeDetailsObject.getString("synopsis");
                    updateDataCallback.onCallback("success");
                } catch (JSONException e) {
                    summaryData = "";
                    e.printStackTrace();
                }
                Log.d("SearchAnime", "Details:" +summaryData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response){
                Log.e("SearchAnime", "Fail" + e.toString());
                Log.e("SearchAnime", "Status code: " + statusCode);
            }
        });
    }

    public interface UpdateDataCallback {
        void onCallback(String value);
    }


    // TODO: Add letsDoSomeNetworkingTopAnime(RequestParams params) here:
    private void letsDoSomeNetworkingTopAnime(RequestParams params, String URL, final boolean update){
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
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
                        e.printStackTrace();
                    }
                }

                if(update == false){
                    sliderAdapter = new SliderAdapter(SearchAnime.this, picsList, topAiringAnime.size(), new OnCardClickListener());
                    initRecyclerView();
                    initTitleText();
                    initSwitchers();
                } else {
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

        // updateAnimeDetails(animeIdList.get(0));

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

    private void onActiveCardChange(int pos) {
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

       // updateAnimeDetails(animeIdList.get(pos));

        currentPosition = pos;

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
            if (clickedPosition == activeCardPosition) {
                final Intent intent = new Intent(SearchAnime.this, DetailsActivity.class);
             //   intent.putExtra(DetailsActivity.BUNDLE_IMAGE_ID, pics[activeCardPosition % pics.length]);

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
                recyclerView.smoothScrollToPosition(clickedPosition);
                onActiveCardChange(clickedPosition);
            }

        }
    }
}
