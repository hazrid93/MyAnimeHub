package com.example.blogmy;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView myFriendList,myFriendReqList;
    private DatabaseReference friendsRef, usersRef, friendReq;
    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private FirebaseRecyclerOptions<Friends> options, optionsReq;
    private FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter,firebaseRecyclerAdapterReq;
    private String online_user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mToolbar = (Toolbar) findViewById(R.id.friends_list_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friend List");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myFriendList = (RecyclerView) findViewById(R.id.friends_lists);
        myFriendList.setHasFixedSize(false);

        // friend request recycler
        myFriendReqList = (RecyclerView) findViewById(R.id.friends_req_lists);
        myFriendReqList.setHasFixedSize(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        linearLayoutManager2.setReverseLayout(true);
        linearLayoutManager2.setStackFromEnd(true);
        myFriendReqList.setLayoutManager(linearLayoutManager2);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        friendReq = FirebaseDatabase.getInstance().getReference().child("FriendRequests");

        // populate recycler view
        displayFriendRequestStatus();
        displayAllFriends();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseRecyclerAdapter.stopListening();
        firebaseRecyclerAdapterReq.stopListening();
    }

    private void displayFriendRequestStatus() {
        Query friendReqQuery = friendReq.child(online_user_id);
        optionsReq = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(friendReqQuery, Friends.class).build();
        firebaseRecyclerAdapterReq = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(optionsReq) {



            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int i, @NonNull Friends friends) {
                friendsViewHolder.setRequestStatus(friends);
                final String usersIDs = getRef(i).getKey();
                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String username = dataSnapshot.child("username").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                            final String fullname = dataSnapshot.child("fullname").getValue().toString();
                            final String type;

                            if(dataSnapshot.hasChild("userState")){
                                type = dataSnapshot.child("userState").child("type").getValue().toString();
                                if(type.equals("online")){
                                    friendsViewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                    friendsViewHolder.setOnlineText("Online");

                                } else {
                                    friendsViewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                    friendsViewHolder.setOnlineText("Offline");
                                }
                            }

                            friendsViewHolder.setFullName(fullname);
                            friendsViewHolder.setUsername("@" + username);
                            friendsViewHolder.setProfileImage(getApplicationContext(), profileImage);

                            friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    /*
                                    Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", usersIDs);
                                    chatIntent.putExtra("userName", fullname);
                                    startActivity(chatIntent);
                                    */


                                    Intent profileIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                    profileIntent.putExtra("visit_user_id", usersIDs);
                                    startActivity(profileIntent);
                                }
                            });
                        } else {
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_friends_display_layout,parent,false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;
            }
        };

        myFriendReqList.setAdapter(firebaseRecyclerAdapterReq);
        firebaseRecyclerAdapterReq.startListening();
    }

    private void displayAllFriends() {
        options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(friendsRef, Friends.class).build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int i, @NonNull Friends friends) {
                friendsViewHolder.setData(friends);
                final String usersIDs = getRef(i).getKey();

                friendsViewHolder.username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!TextUtils.isEmpty(usersIDs)){
                            Intent profileIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                            profileIntent.putExtra("visit_user_id", usersIDs);
                            startActivity(profileIntent);
                        }
                    }
                });

                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String username = dataSnapshot.child("username").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                            final String fullname = dataSnapshot.child("fullname").getValue().toString();
                            final String type;

                            if(dataSnapshot.hasChild("userState")){
                                type = dataSnapshot.child("userState").child("type").getValue().toString();
                                if(type.equals("online")){
                                    friendsViewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                    friendsViewHolder.setOnlineText("Online");

                                } else {
                                    friendsViewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                    friendsViewHolder.setOnlineText("Offline");
                                }
                            }

                            friendsViewHolder.setFullName(fullname);
                            friendsViewHolder.setUsername("@" + username);
                            friendsViewHolder.setProfileImage(getApplicationContext(), profileImage);

                            friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", usersIDs);
                                    chatIntent.putExtra("userName", fullname);
                                    startActivity(chatIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_friends_display_layout,parent,false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;
            }
        };

        myFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView fullname, date, username, onlineText;
        CircleImageView profileimage;
        final ImageView onlineStatusView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            fullname = (TextView) itemView.findViewById(R.id.all_friends_profile_full_name);
            username = (TextView) itemView.findViewById(R.id.all_friends_profile_username);
            date = (TextView) itemView.findViewById(R.id.all_friends_status);
            profileimage = (CircleImageView) itemView.findViewById(R.id.all_friends_profile_image);
            onlineStatusView = (ImageView) itemView.findViewById(R.id.all_friends_online_icon);
            onlineText = (TextView) itemView.findViewById(R.id.all_friends_status_text);
        }

        public void setData(Friends friendsViewHolderData){
            this.date.setText("Friend since: " + friendsViewHolderData.getDate());

        }

        public void setRequestStatus(Friends friendsViewHolderData){
            this.date.setText("Status: " + friendsViewHolderData.getRequest_type());
        }


        public void setOnlineStatusView(int status){
            this.onlineStatusView.setVisibility(status);
        }

        public void setOnlineText(String onlineText) {
            this.onlineText.setText(onlineText);
        }

        public void setUsername(String username) {
            this.username.setText(username);
        }
        public void setFullName(String fullName){
            this.fullname.setText(fullName);
        }
        public void setProfileImage(Context ctx, String profileimage){
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(this.profileimage);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // one inherited from android.support.v4.app.FragmentActivity
        return false;
    }



}
