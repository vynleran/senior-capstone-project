package com.example.seizuredetectionapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Random;

public class RealtimeFragment extends Fragment implements View.OnClickListener {
    Button btnEDA;
    Button btnMM;
    Button btnHR;
    private Dialog dialog;
    TextView reading;
    long lineCount;

    LineChart lineChart;
    LineData lineData;
    LineDataSet lineDataSet;
    ArrayList lineEntries;

    // SET TO FALSE WHEN BACKEND IS READY
    static boolean useDummyData = true;

    enum GraphType {
        GraphType_EDA,
        GraphType_HR,
        GraphType_MM
    }

    private GraphType graphType;

    public RealtimeFragment() {
        graphType = GraphType.GraphType_EDA;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.custom_newuser_dialog);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dialog_bg));
//        }
//        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        dialog.setCancelable(false); //Optional
//        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_realtime, container, false);

        // Buttons
        btnEDA = root.findViewById(R.id.btnshowEDA);
        btnHR = root.findViewById(R.id.btnshowHR);
        btnMM = root.findViewById(R.id.btnshowMM);
        btnEDA.setOnClickListener(this);
        btnHR.setOnClickListener(this);
        btnMM.setOnClickListener(this);

        reading = root.findViewById(R.id.txtCircle);

        lineCount = 2;
        lineChart = root.findViewById(R.id.lineChart);
        lineEntries = new ArrayList<>();
        getEntries();
        createChart("Electrodermal Activity");

        graphType = GraphType.GraphType_EDA;
        updateGraph(true);

        return root;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnshowEDA:
                if (graphType == GraphType.GraphType_EDA) {
                    return;
                }
                graphType = GraphType.GraphType_EDA;
                break;

            case R.id.btnshowHR:
                if (graphType == GraphType.GraphType_HR) {
                    return;
                }
                graphType = GraphType.GraphType_HR;
                break;

            case R.id.btnshowMM:
                if (graphType == GraphType.GraphType_MM) {
                    return;
                }
                graphType = GraphType.GraphType_MM;
                break;
        }
        updateGraph(false);
    }

    private void updateGraph(boolean rfrsh) {
        String s = "Something broke";
        switch (graphType) {
            case GraphType_EDA:
                s = "Electrodermal Activity";
                break;
            case GraphType_HR:
                s = "Heart Rate";
                break;
            case GraphType_MM:
                s = "Movement Magnitude";
                break;
        }

        getEntries();
        createChart(s);

        lineChart.setData(lineData);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

        Entry e = (Entry) lineEntries.get(29);
        float y = e.getY();
        y = Math.round(y  * 10.f) / 10.f;
        reading.setText(String.valueOf(y));

        if (rfrsh) {
            refresh(1000);
        }
    }

    private void refresh(int milliseconds) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateGraph(true);
            }
        };

        handler.postDelayed(runnable, milliseconds);
    }

    // TODO; retrieve real-time data (need to refactor backend first) -John
    private void getEntries() {
        if (useDummyData) {
            Random r = new Random();
            if (lineEntries.isEmpty()) {
                for (int i = 1; i <= 30; ++i) {
                    Entry e = new Entry(i, r.nextFloat() * 30.f);
                    lineEntries.add(e);
                    lineCount++;
                }
            } else {
                while (lineEntries.size() < 30) {
                    Entry e = new Entry(lineEntries.size()+1, r.nextFloat() * 30.f);
                    lineEntries.add(e);
                    lineCount++;
                }

                lineDataSet.removeFirst();
                lineDataSet.addEntry(new Entry(lineCount-1, r.nextFloat() * 30.f));
                lineCount++;
            }
        }
    }
    private void createChart(String graphStr) {
        if (lineChart.getLineData() != null) {
            lineChart.clearValues();
        }
        lineDataSet = new LineDataSet(lineEntries, "Vitals");
        lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        lineDataSet.setValueTextColor(Color.BLACK);
        lineDataSet.setValueTextSize(18f);
        lineDataSet.setDrawValues(false);
        lineDataSet.setLineWidth(3.f);
        XAxis x = lineChart.getXAxis();
        x.setDrawLabels(false);

        Description desc = new Description();
        desc.setText(graphStr);
        desc.setTextSize(21.f);
        lineChart.setDescription(desc);
    }
}