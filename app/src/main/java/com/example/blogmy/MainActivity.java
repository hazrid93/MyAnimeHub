package com.example.blogmy;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.paging.DatabasePagingOptions;
import com.firebase.ui.database.paging.FirebaseRecyclerPagingAdapter;
import com.firebase.ui.database.paging.LoadingState;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

// https://stackoverflow.com/questions/55894959/firebase-recyclerview-image-not-showing-rest-of-the-information-is
public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef, postRef, likesRef;
    private FloatingActionButton addNewPost;
    // private FirebaseRecyclerOptions<Posts> options;
    // private FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerPagingAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    boolean likeChecker = false;

    // nav profile part
    private CircleImageView navProfileImage;
    private TextView navProfileName;

    String currentUserId;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-5012041161594376~1643148417");
        mAdView = findViewById(R.id.mainactivity_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                firebaseRecyclerAdapter.refresh();
            }
        });
        addNewPost = (FloatingActionButton) findViewById(R.id.add_new_post_button);

        // Find view by id must be done inside OnCreate and not before this during class instantiation because the UI has not been inflated.
        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //all user post view
        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        // Storing nav header into View
        // Set the header manually in the code but can also do by the xml attribute  app:headerLayout="@layout/navigation_header"
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        navProfileName = (TextView) navView.findViewById(R.id.nav_user_full_name);
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("fullname")) {
                        String fullName = dataSnapshot.child("fullname").getValue().toString();
                        navProfileName.setText(fullName);
                    }

                    if(dataSnapshot.hasChild("profileimage")){
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(navProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Pass current selected item
                UserMenuSelector(menuItem);
                return false;
            }
        });

        addNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPostActivity();
            }
        });


    }

    public void updateUserStatus(String state){
        String saveCurrentDate, saveCurrentTime;
        Calendar calForDate =  Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime =  Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        userRef.child(currentUserId).child("userState")
                .updateChildren(currentStateMap);

    }

    // using firebaseUI library
    private void displayAllUsersPost(){
        // The "base query" is a query with no startAt/endAt/limit clauses that the adapter can use
        // to form smaller queries for each page.
       // Query sortPostsInDescendingOrder = postRef.orderByChild("counter");
        Query query = postRef.orderByChild("counter");
        // https://developer.android.com/reference/android/arch/paging/PagedList.Config.html
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPrefetchDistance(0)
                .setPageSize(15)
                .build();

        // The options for the adapter combine the paging configuration with query information
        // and application-specific options for lifecycle, etc.
        DatabasePagingOptions<Posts> optionsPage = new DatabasePagingOptions.Builder<Posts>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Posts.class)
                .build();

        // options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(query, Posts.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerPagingAdapter<Posts, PostsViewHolder>(optionsPage) {

                    // call PostsViewHolder static class
                   @Override
                   protected void onBindViewHolder(@NonNull final PostsViewHolder postsViewHolder, final int i, @NonNull final Posts posts) {
                       // get the key of the particular snapshot data at this index
                       final String postKey = getRef(i).getKey();
                       postsViewHolder.setData(posts);
                       postsViewHolder.setLikeButtonStatus(postKey);
                       postsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               Intent clickPostIntent = new Intent(MainActivity.this,ClickPostActivity.class);
                               clickPostIntent.putExtra("PostKey", postKey);
                               startActivity(clickPostIntent);
                           }
                       });

                       // open user profile
                       postsViewHolder.username.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               String visit_user_id = posts.getUid();

                               if(!TextUtils.isEmpty(visit_user_id)){

                                   Intent profileIntent = new Intent(MainActivity.this, PersonProfileActivity.class);
                                   profileIntent.putExtra("visit_user_id", visit_user_id);
                                   startActivity(profileIntent);
                               }
                           }
                       });

                       postsViewHolder.likePostButton.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               likeChecker = true;
                               likesRef.addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                       if(likeChecker == true){
                                           if(dataSnapshot.child(postKey).hasChild(currentUserId)){
                                               likesRef.child(postKey).child(currentUserId).removeValue();
                                               likeChecker = false;
                                           } else {

                                               // add data into 'Likes' database using the Post key and add the userUid in there,
                                               // any duplicated userUid will not add another one.
                                               likesRef.child(postKey).child(currentUserId).setValue(true);
                                               likeChecker = false;
                                           }
                                       }
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError databaseError) {

                                   }
                               });
                           }
                       });

                       postsViewHolder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View v) {
                               Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                               commentsIntent.putExtra("PostKey", postKey);
                               startActivity(commentsIntent);

                           }
                       });
                   }

                    @Override
                    protected void onLoadingStateChanged(@NonNull LoadingState state) {
                        switch (state) {
                            case LOADING_INITIAL:
                            case LOADING_MORE:
                                // Do your loading animation
                                mSwipeRefreshLayout.setRefreshing(true);
                                break;

                            case LOADED:
                                // Stop Animation
                                mSwipeRefreshLayout.setRefreshing(false);
                                break;

                            case FINISHED:
                                //Reached end of Data set
                                mSwipeRefreshLayout.setRefreshing(false);
                                break;

                            case ERROR:
                                retry();
                                break;
                        }
                    }


                   @NonNull
                   @Override
                   public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_post_layout,parent,false);
                       PostsViewHolder viewHolder = new PostsViewHolder(view);
                       return viewHolder;
                   }

                    @Override
                    protected void onError(@NonNull DatabaseError databaseError) {
                        retry();
                    }
            };
        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();


    }
    // has to use same method name as Posts model because FirebaseRecyclerAdapter/onBindViewHolder will call PostsViewHolder every scroll
    public class PostsViewHolder extends RecyclerView.ViewHolder{
        TextView username,date,time,summary;
        CircleImageView profileimage;
        ImageView postimage;
        DatabaseReference LikesRef;

        int countLikes;
        String currentUserId;

        // post profile image
        String postUid, posterImageUrl;

        // likes,comments section
        ImageButton likePostButton, commentPostButton;
        TextView displayNoOfLikes;

        public PostsViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.post_user_name);
            date = itemView.findViewById(R.id.post_date);
            time = itemView.findViewById(R.id.post_time);
            summary = itemView.findViewById(R.id.click_post_description);
           // postimage = (ImageView) itemView.findViewById(R.id.click_post_image);
            profileimage = (CircleImageView) itemView.findViewById(R.id.post_profile_image);

            likePostButton = (ImageButton) itemView.findViewById(R.id.like_button);
            commentPostButton = (ImageButton) itemView.findViewById(R.id.comment_button);
            displayNoOfLikes = itemView.findViewById(R.id.display_no_of_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setData(Posts postViewHolderData){
            username.setText(postViewHolderData.getFullname());
            time.setText(" " + postViewHolderData.getTime());
            date.setText(" "+ postViewHolderData.getDate());
            summary.setText(postViewHolderData.getSummary());
            postUid = postViewHolderData.getUid();

            // https://github.com/square/picasso/issues/1291 seems like require runtime permission.
            userRef.child(postUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        posterImageUrl = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(getApplicationContext()).load(posterImageUrl).into(profileimage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
          //  Picasso.with(getApplicationContext()).load(postViewHolderData.getPostimage()).into(postimage);
        }

        public void setLikeButtonStatus(final String postKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKey).hasChild(currentUserId)){
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        displayNoOfLikes.setText((Integer.toString(countLikes)) + (" Likes"));
                    } else {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        displayNoOfLikes.setText((Integer.toString(countLikes)) + (" Likes"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseRecyclerAdapter.stopListening();
        updateUserStatus("offline");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check user authentication
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){
            sendToLogin();
        } else {
            updateUserStatus("online");
            checkUserExistence();
            displayAllUsersPost();
        }

    }

    private void checkUserExistence(){
        final String user_id = mAuth.getCurrentUser().getUid();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Users/'user_id' in storage.
                if(!dataSnapshot.hasChild(user_id)){
                    // send user to setup intent
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void sendUserToPostActivity(){
        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(postIntent);
    }

    private void sendUserToSetupActivity(){
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void sendToLogin(){
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    // Method area
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        // return this super if menu item not processed.
        return super.onOptionsItemSelected(item);
    }


    private void UserMenuSelector(MenuItem menuItem){
        switch(menuItem.getItemId()){

            case R.id.nav_anime_search:
                sendUserToSearchAnimeActivity();
                break;

            case R.id.nav_profile:
                sendUserToProfileActivity();
                break;

            case R.id.nav_post:
                sendUserToPostActivity();
                break;

            case R.id.nav_bookmark:
                sendUserToBookmarkActivity();
                break;

            case R.id.nav_friends:
                sendUserToFriendsActivity();
                break;

            case R.id.nav_find_friends:
                sendUserToFindFriends();
                break;

            case R.id.nav_settings:
                sendUserToSettingsActivity();
                break;

            case R.id.nav_logout:
                // Logout from current session
                updateUserStatus("offline");
                mAuth.signOut();
                sendToLogin();
                break;

            default:

        }

    }

    private void sendUserToBookmarkActivity() {
        Intent bookmarkIntent = new Intent(MainActivity.this, BookmarkActivity.class);
        startActivity(bookmarkIntent);
    }

    private void sendUserToSearchAnimeActivity() {
        Intent searchAnimeIntent = new Intent(MainActivity.this, SearchAnime.class);
        startActivity(searchAnimeIntent);
    }

    private void sendUserToFindFriends() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void sendUserToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }

    private void sendUserToSettingsActivity(){
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendUserToFriendsActivity(){
        Intent friendIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(friendIntent);
    }
}
