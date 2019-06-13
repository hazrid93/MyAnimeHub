package com.example.blogmy;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SummaryFragment extends Fragment {
    private TextView textView, textView_title;
    private String summaryData;
    private String summaryTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            summaryData = bundle.getString("summaryData");
            summaryTitle = bundle.getString("summaryTitle");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.summary_fragment_tab, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textView = (TextView) view.findViewById(R.id.fragment_textView);
        textView_title = (TextView) view.findViewById(R.id.fragment_textView_title);


        textView_title.setText(summaryTitle);
        textView_title.setPaintFlags(textView_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        // set drawable programmatically
       // textView_title.setCompoundDrawablesWithIntrinsicBounds(R.drawable.label_title, 0, 0, 0);
        textView.setText(summaryData);


    }




}
