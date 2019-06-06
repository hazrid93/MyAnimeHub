package com.example.blogmy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {
    private TextView watching_textView;
    private TextView completed_textView;
    private TextView stats_scores;
    private String watching;
    private String completed;
    private String on_hold;
    private String dropped;
    private String plan_to_watch;
    private String total;

    private PieChart stat_pie_chart;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            watching = bundle.getString("watching");
            completed = bundle.getString("completed");
            on_hold = bundle.getString("on_hold");
            dropped = bundle.getString("dropped");
            plan_to_watch = bundle.getString("plan_to_watch");
            total = bundle.getString("total");

            // pie chart
            score_1_pct = bundle.getDouble("score_1_pct");
            score_1_vt = bundle.getString("score_1_vt");
            score_2_pct = bundle.getDouble("score_2_pct");
            score_2_vt = bundle.getString("score_2_vt");
            score_3_pct = bundle.getDouble("score_3_pct");
            score_3_vt = bundle.getString("score_3_vt");
            score_4_pct = bundle.getDouble("score_4_pct");
            score_4_vt = bundle.getString("score_4_vt");
            score_5_pct = bundle.getDouble("score_5_pct");
            score_5_vt = bundle.getString("score_5_vt");
            score_6_pct = bundle.getDouble("score_6_pct");
            score_6_vt = bundle.getString("score_6_vt");
            score_7_pct = bundle.getDouble("score_7_pct");
            score_7_vt = bundle.getString("score_7_vt");
            score_8_pct = bundle.getDouble("score_8_pct");
            score_8_vt = bundle.getString("score_8_vt");
            score_9_pct = bundle.getDouble("score_9_pct");
            score_9_vt = bundle.getString("score_9_vt");
            score_10_pct = bundle.getDouble("score_10_pct");
            score_10_vt = bundle.getString("score_10_vt");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.stats_fragment_tab, container, false);
    }

    // Views are fully created
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        watching_textView = (TextView) view.findViewById(R.id.stats_watching);
        completed_textView = (TextView) view.findViewById(R.id.stats_completed);
        stat_pie_chart = (PieChart) view.findViewById(R.id.stat_pie_chart);
        stats_scores = (TextView) view.findViewById(R.id.stats_scores);

        watching_textView.setText("Watching: " + watching);
        completed_textView.setText("Completed Watching: " + completed);
        stats_scores.setText("Scores (from MAL): \n\n"
                + " - 1 Star: " +  score_1_vt + "\n"
                + " - 2 Star: " +  score_2_vt + "\n"
                + " - 3 Star: " +  score_3_vt + "\n"
                + " - 4 Star: " +  score_4_vt + "\n"
                + " - 5 Star: " +  score_5_vt + "\n"
                + " - 6 Star: " +  score_6_vt + "\n"
                + " - 7 Star: " +  score_7_vt + "\n"
                + " - 8 Star: " +  score_8_vt + "\n"
                + " - 9 Star: " +  score_9_vt + "\n"
                + " - 10 Star: " +  score_10_vt + "\n");




        // set data for pie chart
        List<PieEntry> entries = new ArrayList<PieEntry>();
        /*
        for (Integer data : dataObjects) {
            // turn your data into Entry objects
            entries.add(new PieEntry(data, "Green"));
        }
        */

        // set the pie chart
        entries.add(new PieEntry((float)score_1_pct, "1 Star"));
        entries.add(new PieEntry((float)score_2_pct, "2 Star"));
        entries.add(new PieEntry((float)score_3_pct, "3 Star"));
        entries.add(new PieEntry((float)score_4_pct, "4 Star"));
        entries.add(new PieEntry((float)score_5_pct, "5 Star"));
        entries.add(new PieEntry((float)score_6_pct, "6 Star"));
        entries.add(new PieEntry((float)score_7_pct, "7 Star"));
        entries.add(new PieEntry((float)score_8_pct, "8 Star"));
        entries.add(new PieEntry((float)score_9_pct, "9 Star"));
        entries.add(new PieEntry((float)score_10_pct, "10 Star"));

        PieDataSet set = new PieDataSet(entries, "Vote Scores");

        // set color for pie chart
        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());
        set.setColors(colors);
        set.setValueLinePart1OffsetPercentage(80.f);
        set.setValueLinePart1Length(0.2f);
        set.setValueLinePart2Length(0.4f);
        set.setSliceSpace(3f);
        set.setSelectionShift(5f);
        set.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(set);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);

        stat_pie_chart.setDrawEntryLabels(true);
        stat_pie_chart.setUsePercentValues(true);
        stat_pie_chart.getDescription().setEnabled(false);
        stat_pie_chart.setExtraOffsets(5, 10, 5, 5);
        stat_pie_chart.setDrawCenterText(true);
        stat_pie_chart.setRotationAngle(0);
        stat_pie_chart.setDragDecelerationFrictionCoef(0.95f);
        stat_pie_chart.setCenterText("Votes");
        stat_pie_chart.setExtraOffsets(20.f, 0.f, 20.f, 0.f);
        stat_pie_chart.setDrawHoleEnabled(true);
        stat_pie_chart.setHoleColor(Color.WHITE);

        stat_pie_chart.setTransparentCircleColor(Color.WHITE);
        stat_pie_chart.setTransparentCircleAlpha(110);

        stat_pie_chart.setHoleRadius(58f);
        stat_pie_chart.setTransparentCircleRadius(61f);

        stat_pie_chart.setRotationEnabled(true);
        stat_pie_chart.setHighlightPerTapEnabled(true);
        stat_pie_chart.animateY(1400, Easing.EaseInOutQuad);
        // chart.spin(2000, 0, 360);

        Legend l = stat_pie_chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(false);

        stat_pie_chart.setData(data);
        stat_pie_chart.highlightValues(null);
        stat_pie_chart.invalidate(); // refresh
    }




}
