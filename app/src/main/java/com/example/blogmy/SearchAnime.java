package com.example.blogmy;

import androidx.annotation.DrawableRes;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.blogmy.cards.SliderAdapter;
import com.example.blogmy.utils.DecodeBitmapTask;
import com.example.blogmy.cards.CardSliderLayoutManager;
import com.example.blogmy.cards.CardSnapHelper;
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
    private String JIKAN_URL = "https://api.jikan.moe/v3";
    private String JIKAN_TYPE = "anime";
    private String JIKAN_PAGE = "1";
    private String JIKAN_AIRING = "airing";

    private Map<Integer,JSONObject> topAiringAnime = null;

    private final List<String> animeIdList = new ArrayList<String>();
    private final List<String> picsList = new ArrayList<String>();
    private final List<String> typeList = new ArrayList<String>();
    private final List<String> titleList = new ArrayList<String>();
    private final List<String> scoreList = new ArrayList<String>();
    private final List<String> rankList = new ArrayList<String>();
    private final List<String> startTimeList = new ArrayList<String>();
    private final List<String> endTimeList = new ArrayList<String>();

    private SliderAdapter sliderAdapter = null;

    private CardSliderLayoutManager layoutManger;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_anime);
        // get initial top airing during activity launch
        initGetTopAiring();
    }

    // using JIKAN api
    private void initGetTopAiring() {
        RequestParams params = new RequestParams();
        String JIKAN_TOP_AIRING_DEFAULT = JIKAN_URL + "/top/anime/" + JIKAN_PAGE + "/" + JIKAN_AIRING;
       // params.put("type", JIKAN_TYPE);
       // params.put("page", JIKAN_PAGE);
       // params.put("subtype", JIKAN_SUBTYPE);
        letsDoSomeNetworking(params, JIKAN_TOP_AIRING_DEFAULT);
    }

    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private void letsDoSomeNetworking(RequestParams params, String URL){
        AsyncHttpClient client = new AsyncHttpClient();

        client.get(URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response){
                Log.d("SearchAnime", "Success: " + response.toString());
                Toast.makeText(SearchAnime.this, "Request Successful", Toast.LENGTH_SHORT).show();

                // add top airing anime data into the List topAiringAnime
                topAiringAnime = new LinkedHashMap<Integer, JSONObject>();
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
                        titleList.add(topAiringAnime.get(i).get("title").toString());
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

                Log.d("SearchAnime", "Size:" +topAiringAnime.size());
                sliderAdapter = new SliderAdapter(SearchAnime.this, picsList, topAiringAnime.size(), new OnCardClickListener());
                initRecyclerView();
                initTitleText();
                initSwitchers();

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

        layoutManger = (CardSliderLayoutManager) recyclerView.getLayoutManager();

        new CardSnapHelper().attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initSwitchers() {
        rankSwitcher = (TextSwitcher) findViewById(R.id.ts_rank);
        rankSwitcher.setFactory(new TextViewFactory(R.style.RankTextView, true));
        rankSwitcher.setCurrentText("Rank: " + rankList.get(0) + "/" + topAiringAnime.size());

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

        updateAnimeDetails(animeIdList.get(0));


    }

    private void initTitleText() {
        titleAnimDuration = getResources().getInteger(R.integer.labels_animation_duration);
        titleOffset1 = getResources().getDimensionPixelSize(R.dimen.left_offset);
        titleOffset2 = getResources().getDimensionPixelSize(R.dimen.card_width);
        title1TextView = (TextView) findViewById(R.id.tv_title_1);
        title2TextView = (TextView) findViewById(R.id.tv_title_2);

        title1TextView.setX(titleOffset1);
        title2TextView.setX(titleOffset2);
        title1TextView.setText(titleList.get(0));
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
        animSet.start();
    }

    private void onActiveCardChange() {
        final int pos = layoutManger.getActiveCardPosition();
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

        rankSwitcher.setInAnimation(SearchAnime.this, animH[0]);
        rankSwitcher.setOutAnimation(SearchAnime.this, animH[1]);
        rankSwitcher.setText("Rank: " + rankList.get(pos) + "/" + topAiringAnime.size());

        scoreSwitcher.setInAnimation(SearchAnime.this, animV[0]);
        scoreSwitcher.setOutAnimation(SearchAnime.this, animV[1]);
        scoreSwitcher.setText("Score: " + scoreList.get(pos)+ "/10");

        clockSwitcher.setInAnimation(SearchAnime.this, animV[0]);
        clockSwitcher.setOutAnimation(SearchAnime.this, animV[1]);
        clockSwitcher.setText(startTimeList.get(pos) + " ~ " + endTimeList.get(pos));

        descriptionsSwitcher.setText("Type: " + typeList.get(pos));

        updateAnimeDetails(animeIdList.get(pos));

        currentPosition = pos;
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
