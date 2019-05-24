package com.example.blogmy;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {
    private TextView userName, userProfileName, userStatus, userCountry, userGender, userRelationship, userDOB;
    private CircleImageView userProfImage;
    private Button sendFriendRequestButton, declineFriendRequestButton;
    private DatabaseReference FriendRequestRef, usersRef, friendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate, saveCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        mAuth = FirebaseAuth.getInstance();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        initializeFields();

        // find the selected friend/person
        usersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationship").getValue().toString();

                    Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);

                    userName.setText("@" + myUserName);
                    userProfileName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userCountry.setText("Country: " + myCountry);
                    userGender.setText("Gender: " + myGender);
                    userRelationship.setText("Relationship: " + myRelationStatus);
                    userDOB.setText("DOB: " + myDOB);

                    MaintainenceOfButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        declineFriendRequestButton.setVisibility(View.INVISIBLE);
        declineFriendRequestButton.setEnabled(false);

        if(!senderUserId.equals(receiverUserId)){
            sendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequestButton.setEnabled(false);
                    if(CURRENT_STATE.equals("not_friends")){
                        sendFriendRequestToAPerson();
                    } else if(CURRENT_STATE.equals("request_sent")){
                        cancelFriendRequest();
                    } else if(CURRENT_STATE.equals("request_received")){
                        acceptFriendRequest();
                    } else if(CURRENT_STATE.equals("friends")){
                        unfriendAnExistingFriends();
                    }
                }
            });
        } else {
            declineFriendRequestButton.setVisibility(View.INVISIBLE);
            sendFriendRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void unfriendAnExistingFriends() {
        friendsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendRequestButton.setText("Send Friend Request");

                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                declineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptFriendRequest() {
        Calendar calForDate =  Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime =  Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        friendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            friendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                //from cancelfriendrequest method
                                                FriendRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){

                                                                    //from cancelfriendrequest method
                                                                    FriendRequestRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        sendFriendRequestButton.setEnabled(true);
                                                                                        CURRENT_STATE = "friends";
                                                                                        sendFriendRequestButton.setText("Unfriend this person");

                                                                                        declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                                        declineFriendRequestButton.setEnabled(false);
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelFriendRequest() {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                sendFriendRequestButton.setText("Send Friend Request");

                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                declineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void MaintainenceOfButtons() {

        FriendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserId)){
                            String request_type = dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if(request_type.equals("sent")){
                                CURRENT_STATE = "request_sent";
                                sendFriendRequestButton.setText("Cancel Friend Request");
                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                declineFriendRequestButton.setEnabled(false);
                            }
                            else if(request_type.equals("received")) {
                                CURRENT_STATE = "request_received";
                                sendFriendRequestButton.setText("Accept Friend Request");
                                declineFriendRequestButton.setVisibility(View.VISIBLE);
                                declineFriendRequestButton.setEnabled(true);

                                declineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelFriendRequest();
                                    }
                                });
                            }
                        } else {
                            friendsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receiverUserId)){
                                                CURRENT_STATE = "friends";
                                                sendFriendRequestButton.setText("Unfriend this person");
                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                declineFriendRequestButton.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void sendFriendRequestToAPerson() {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                sendFriendRequestButton.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                sendFriendRequestButton.setText("Cancel Friend Request");

                                                declineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                declineFriendRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void initializeFields(){
        userName = (TextView) findViewById(R.id.person_username);
        userProfileName = (TextView) findViewById(R.id.person_full_name);
        userStatus = (TextView) findViewById(R.id.person_profile_status);
        userCountry = (TextView) findViewById(R.id.person_country);
        userGender = (TextView) findViewById(R.id.person_gender);
        userRelationship = (TextView) findViewById(R.id.person_relationship_status);
        userDOB = (TextView) findViewById(R.id.person_dob);
        userProfImage = (CircleImageView) findViewById(R.id.person_profile_pic);

        sendFriendRequestButton = (Button) findViewById(R.id.person_send_friend_request_btn);
        declineFriendRequestButton = (Button) findViewById(R.id.person_decline_friend_request_btn);

        CURRENT_STATE = "not_friends";
    }

}
