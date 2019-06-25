package com.example.blogmy;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;


public class UserSearchAnimeAdapter extends RecyclerView.Adapter<UserSearchAnimeAdapter.MessageViewHolder> {
    private List<UserSearchAnime> animeList;

    public UserSearchAnimeAdapter(List<UserSearchAnime> animeList){
        this.animeList = animeList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.anime_search_layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        final UserSearchAnime messages = animeList.get(position);

        // get context from a image view
        // https://developer.android.com/reference/android/view/View.html#getContext%28%29
        holder.allLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println("CLICKER: " + messages.getMal_id());
                Intent searchAnimeIntent = new Intent(v.getContext(), ClickSearchAnime.class);
                searchAnimeIntent.putExtra("anime_id", messages.getMal_id());
                v.getContext().startActivity(searchAnimeIntent);
            }
        });
        holder.animeTitle.setText(messages.getTitle());
        holder.animeScore.setText("Score: " + messages.getScore());
        holder.animeEpisodes.setText("Episodes: " + messages.getEpisodes());
        holder.animeStart.setText("Start: " + messages.getStart_date());
        holder.animeEnd.setText("End: " + messages.getEnd_date());
        Picasso.with(holder.animeImage.getContext()).load(messages.getImage_url()).placeholder(R.drawable.profile).into(holder.animeImage);

    }

    // need to be implemented
    @Override
    public int getItemCount() {
        return animeList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public View allLayout;
        public TextView animeTitle, animeScore, animeEpisodes, animeStart, animeEnd;
        public ImageView animeImage;
        public MessageViewHolder(View itemView){
            super(itemView);
            allLayout = (View) itemView.findViewById(R.id.anime_search_all_layout);
            animeTitle = (TextView) itemView.findViewById(R.id.anime_search_title);
            animeScore = (TextView) itemView.findViewById(R.id.anime_search_score);
            animeEpisodes = (TextView) itemView.findViewById(R.id.anime_search_episodes);
            animeStart = (TextView) itemView.findViewById(R.id.anime_search_start);
            animeEnd = (TextView) itemView.findViewById(R.id.anime_search_end);
            animeImage = (ImageView) itemView.findViewById(R.id.anime_search_picture);
          //  Picasso.with().load(postViewHolderData.getPostimage()).into(postimage);

        }
    }

}
