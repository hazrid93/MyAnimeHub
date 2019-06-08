package com.example.blogmy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CharactersAdapter extends ArrayAdapter<Characters> {

    public CharactersAdapter(Context context, int resource, ArrayList<Characters> characters) {
        super(context, resource, characters);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Characters character = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.characters_lists, parent, false);
        }
        // Lookup view for data population
        TextView char_name = (TextView) convertView.findViewById(R.id.char_name);
        TextView char_role = (TextView) convertView.findViewById(R.id.char_role);
        CircleImageView char_image_url = (CircleImageView) convertView.findViewById(R.id.char_img_url);

        // Populate the data into the template view using the data object
        char_name.setText(character.name);
        char_role.setText(character.role);
        Picasso.with(getContext()).load(character.image_url).placeholder(R.drawable.profile).into(char_image_url);
        // Return the completed view to render on screen
        return convertView;
    }
}
