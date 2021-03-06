package com.example.seizuredetectionapp;


import static java.lang.Math.round;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.print.PrintAttributes;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.Button;
import android.widget.ToggleButton;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import gherkin.lexer.Ca;
import gherkin.lexer.Vi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import com.example.seizuredetectionapp.adapter.DiscoveredBluetoothDevice;
import com.example.seizuredetectionapp.databinding.ActivityBleBinding;
import com.example.seizuredetectionapp.viewmodels.STRappBleViewModel;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.ble.observer.ConnectionObserver;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DatatableFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DatatableFragment extends Fragment implements View.OnClickListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int DAY_OF_WEEK = 7;
    private static final int WEEK_OF_MONTH  = 4;
    private static final int MONTH_OF_YEAR = 2;
    private Dialog dialog;
    private String isQuestionnaireComplete;
    private Set<String> contactList = new HashSet<String>();
    private SharedPreferences sharedPreferences;


    Button btnSettings;
    ListView journalList;
    ArrayList<Journal> journals = new ArrayList<>();
    ArrayList<JournalLayout> journalInfo = new ArrayList<>();
    ArrayAdapter sortedAdapter;
    JournalAdapter adapter;
    Journal journal;
    FirebaseDatabase database;
    DatabaseReference myRef;
    LinearLayout sheetBottom;
    private String currentUserUID, graphDisplaySpinnerValue;
    BottomSheetBehavior bottomSheetBehavior;
    private Button btnHelpRequest;
    private PowerSpinnerView sortDropDown, graphDisplaySpinner;
    private String[] sortOptions = new String[1];
    ListView sortedJournalList;
    ArrayList<String> sortedJournalInfo = new ArrayList<>();
    int pdfHeight = 1080;
    int pdfWidth = 720;
    Bitmap bmp;
    Calendar dateCompare = Calendar.getInstance();
    private LocalSettings localSettings;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private ImageView hintImage;
    private Button startSeizureButton;
    private DiscoveredBluetoothDevice BLEDevice;
    private STRappBleViewModel viewModel;

    BarChart barChart;
    ArrayList<Calendar> journalDates;
    TextView textBox, titleBox;

    public enum ThreadStatus {
        STARTED,
        STOPPED
    }
    private ThreadStatus threadStatus = ThreadStatus.STOPPED;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Object simplyPdfDocument;

    public DatatableFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DatatableFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DatatableFragment newInstance(String param1, String param2) {
        DatatableFragment fragment = new DatatableFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Retrieving the user info from shared preferences
        sharedPreferences = getActivity().getSharedPreferences(localSettings.PREFERENCES, Context.MODE_PRIVATE);
        isQuestionnaireComplete = sharedPreferences.getString("questionnaire bool", localSettings.getQuestionnaireComplete());
        Log.d("boolQ", ""+isQuestionnaireComplete);

        // Checking if the user has completed the questionnaire or not
        //this still crashes my build for some reason

        if(isQuestionnaireComplete != null && isQuestionnaireComplete.equals("0")){
            showNewUserDialog();
        }

//        BLEDevice = loadBLEDevice();
//        Log.d("BLEDevice", String.valueOf(BLEDevice));
//        LiveData<String> AccX = viewModel.getAccxData();
//        Log.d("AccX", String.valueOf(AccX));


        // Logging the personal questionnaire data
        Log.d("seizureTypes", ""+sharedPreferences.getStringSet("SeizureTypes", localSettings.getSeizureTypes()));
        Log.d("firstSeizure", ""+sharedPreferences.getString("firstSeizure", ""));
        Log.d("seizureFreq", ""+sharedPreferences.getString("seizureFrequencyPerMonth", ""));
        Log.d("averageSeizure", ""+sharedPreferences.getString("seizureDuration", ""));
        Log.d("longestSeizure", ""+sharedPreferences.getString("longestSeizure", ""));

        // Initializing Firebase
        currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users").child(currentUserUID);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_datatable, container, false);

        //ui elements
        btnSettings = root.findViewById(R.id.settings);
        btnHelpRequest = root.findViewById(R.id.helpRequest);
        journalList = root.findViewById(R.id.journalList);
        sortDropDown = root.findViewById(R.id.sortDropdown);
        graphDisplaySpinner = root.findViewById(R.id.graphDisplaySpinner);
        hintImage = root.findViewById(R.id.hintDatatable);
        startSeizureButton = root.findViewById(R.id.startSeizureButton);

        if(foregroundServiceRunning()){
            threadStatus = ThreadStatus.STARTED;
            startSeizureButton.setText("Stop Seizure Detection");
            startSeizureButton.setBackground(getResources().getDrawable(R.drawable.addjournal_button_bg));
        }

        //Buttons
        hintImage.setOnClickListener(this);
        startSeizureButton.setOnClickListener(this);

        //listview adapter
        adapter = new JournalAdapter(getContext(), R.layout.journal_item_listview, journalInfo);
        journalList.setAdapter(adapter);

        //Bottom Swipe up setup
        sheetBottom = root.findViewById(R.id.bottom_sheet_header);
        bottomSheetBehavior = BottomSheetBehavior.from(sheetBottom);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        bottomSheetBehavior.setPeekHeight((height/9));
        bottomSheetBehavior.setHideable(false);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        //Populate ListView upon datatable start up
        myRef.child("Journals").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("child added", "child added " + snapshot);
                Journal journal = snapshot.getValue(Journal.class);
                journals.add(journal);
                JournalLayout journalLayout = new JournalLayout(journal.dateAndTime, journal.durationOfSeizure, journal.description, journal.severity);
                journalInfo.add(journalLayout);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Log.d("wehere", "before graph spinner listener");
        graphDisplaySpinner.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int i, @Nullable String s, int i1, String t1) {
                Log.d("wehere", "in graph spinner listener");
                changeGraphView(t1, root);
            }
        });

        sortDropDown.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<String>() {
            @Override public void onItemSelected(int oldIndex, @Nullable String oldItem, int newIndex, String newItem) {
                sortJournals(newItem);
            }
        });

        barChart = root.findViewById(R.id.timeLineDisplayGraph);
        dateCompare = normalizeDates(DAY_OF_WEEK);

        //assign Xaxis values
        Log.d("startcheck", "fuck");
        ArrayList<String> xAxisValues = new ArrayList<String>();
        xAxisValues.add("Sun");
        xAxisValues.add("Mon");
        xAxisValues.add("Tue");
        xAxisValues.add("Wed");
        xAxisValues.add("Thu");
        xAxisValues.add("Fri");
        xAxisValues.add("Sat");
        getDates(dateCompare, root, xAxisValues, DAY_OF_WEEK);
        //Array goes into generateChart

        return root;
    }

    private void changeGraphView(String s, View view){
        ArrayList<String> xAxisValues = new ArrayList<String>();
        Log.d("NameCheck", s);
        switch (s) {
            case ("Year"):
                Log.d("NameCheck", "Year");
                dateCompare = normalizeDates(MONTH_OF_YEAR);

                //assign Xaxis values
                xAxisValues = new ArrayList<String>();
                xAxisValues.add("Jan");
                xAxisValues.add("Feb");
                xAxisValues.add("Mar");
                xAxisValues.add("Apr");
                xAxisValues.add("May");
                xAxisValues.add("Jun");
                xAxisValues.add("Jul");
                xAxisValues.add("Aug");
                xAxisValues.add("Sep");
                xAxisValues.add("Oct");
                xAxisValues.add("Nov");
                xAxisValues.add("Dec");

                getDates(dateCompare, view, xAxisValues, MONTH_OF_YEAR);
                break;

            case ("Month"):
                Log.d("NameCheck", "Month");
                dateCompare = normalizeDates(WEEK_OF_MONTH);

                //assign Xaxis values
                xAxisValues = new ArrayList<String>();
                int timeSpan = dateCompare.getActualMaximum(Calendar.WEEK_OF_MONTH);
                for (int j = 1; j <= timeSpan; j++)
                    xAxisValues.add("Week " + j);

                getDates(dateCompare, view, xAxisValues, WEEK_OF_MONTH);
                break;

            case ("Week"):
                Log.d("NameCheck", "Week");
                dateCompare = normalizeDates(DAY_OF_WEEK);

                //assign Xaxis values
                xAxisValues = new ArrayList<String>();
                xAxisValues.add("Sun");
                xAxisValues.add("Mon");
                xAxisValues.add("Tue");
                xAxisValues.add("Wed");
                xAxisValues.add("Thu");
                xAxisValues.add("Fri");
                xAxisValues.add("Sat");

                getDates(dateCompare, view, xAxisValues, DAY_OF_WEEK);
                break;
        }
    }

    public DiscoveredBluetoothDevice loadBLEDevice(){
        Gson gson = new Gson();
        String deviceJson = sharedPreferences.getString("device", "");
        DiscoveredBluetoothDevice device = gson.fromJson(deviceJson, DiscoveredBluetoothDevice.class);
        return device;
    }

    private void sortJournals(String selectedItem){
        ArrayList<JournalLayout> sortedJournals = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");

        for(Journal journal: journals){
            if(!journal.dateAndTime.equals("")) {
                sortedJournals.add(new JournalLayout(journal.dateAndTime, journal.durationOfSeizure, journal.description, journal.severity));
            }
        }

        if (selectedItem.equals("Date")) {
            Collections.sort(sortedJournals, new Comparator<JournalLayout>() {
                @Override
                public int compare(JournalLayout journalLayout, JournalLayout t1) {
                    Log.d("d1", t1.getDateAndTime());
                    Log.d("d2", journalLayout.getDateAndTime());
                    String d1 = t1.getDateAndTime();
                    String d1Converted = d1.substring(0, Math.min(d1.length(), 15));

                    String d2 = journalLayout.getDateAndTime();
                    String d2Converted = d2.substring(0, Math.min(d2.length(), 15));

                    Date date1 = null;
                    Date date2 = null;

                    try {
                         date1 = sdf.parse(d1Converted);
                         date2 = sdf.parse(d2Converted);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if(date1 != null && date2 != null){
                        return date1.compareTo(date2);
                    }
                    return 0 ;
                }
            });
        }
        else if(selectedItem.equals("Duration")){
            Collections.sort(sortedJournals, new Comparator<JournalLayout>() {
                @Override
                public int compare(JournalLayout journalLayout, JournalLayout t1) {
                    return Float.valueOf(t1.getDuration()).compareTo(Float.valueOf(journalLayout.getDuration()));
                }
            });
        }
        else if(selectedItem.equals("Severity")){
            Collections.sort(sortedJournals, new Comparator<JournalLayout>() {
                @Override
                public int compare(JournalLayout journalLayout, JournalLayout t1) {
                    return t1.getSeverity().compareTo(journalLayout.getSeverity());
                }
            });
        }

        adapter = new JournalAdapter(getContext(), R.layout.journal_item_listview, sortedJournals);
        journalList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private String durationStringToInt(String durationString){
        int durationInt = 0;
        if(durationString.equals("No duration recorded")){
            return String.valueOf(durationInt);
        }
        String[] durations = durationString.split(":");
        int hourInSecs = Integer.parseInt(durations[0]) * 3600;
        int minInSecs = Integer.parseInt(durations[1]) * 60;
        int secs = Integer.parseInt(durations[2]);
        durationInt = hourInSecs + minInSecs + secs;
        return String.valueOf(durationInt);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case(R.id.settings):
                intent = new Intent(getContext(), MainSettings.class);
                startActivity(intent);
                break;
            case(R.id.helpRequest):
                intent = new Intent(getContext(), AlertPage.class);
                startActivity(intent);
                break;
            case R.id.hintDatatable:
                showHint(view.getContext());
                break;
            case R.id.startSeizureButton:
                if(threadStatus == ThreadStatus.STARTED){
                    stopService();
                } else if(threadStatus == ThreadStatus.STOPPED){
                    startService();
                }
                break;
        }
    }
    // TODO: Connect BLE Device
    public void startService() {
        threadStatus = ThreadStatus.STARTED;
        startSeizureButton.setText("Stop Seizure Detection");
        startSeizureButton.setBackground(getResources().getDrawable(R.drawable.addjournal_button_bg));

        if(!foregroundServiceRunning()){
            Intent serviceIntent = new Intent(getContext(), ExampleService.class);
            serviceIntent.putExtra("inputExtra", "Start Service");

            ContextCompat.startForegroundService(getContext(), serviceIntent);
        }
    }

    public void stopService() {
        threadStatus = ThreadStatus.STOPPED;
        startSeizureButton.setText("Start Seizure Detection");
        startSeizureButton.setBackground(getResources().getDrawable(R.drawable.button_bg));

        Intent serviceIntent = new Intent(getContext(), ExampleService.class);
        serviceIntent.putExtra("inputExtra", "Stop Service");

        ContextCompat.startForegroundService(getContext(), serviceIntent);
    }

    public boolean foregroundServiceRunning(){
        Context context = getContext();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(ExampleService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private Calendar normalizeDates(Integer timeValue){
        dateCompare = Calendar.getInstance();
        dateCompare.add(timeValue, -dateCompare.get(timeValue)+1);
        dateCompare.set(Calendar.HOUR_OF_DAY, 0);
        dateCompare.set(Calendar.MINUTE, 0);
        dateCompare.set(Calendar.SECOND, 0);
        dateCompare.set(Calendar.MILLISECOND, 0);

        if(timeValue == MONTH_OF_YEAR)
            dateCompare.set(timeValue, 0);
        return dateCompare;
    }

    /**
     * method for displaying the new user dialog
     */
    private void showNewUserDialog() {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.custom_newuser_dialog);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dialog_bg));
        }
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false); //Optional
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

        Button Okay = dialog.findViewById(R.id.btn_okay);
        Button Cancel = dialog.findViewById(R.id.btn_cancel);

        Okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), QuestionnairePersonal.class));
                dialog.dismiss();
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), LoginPage.class));
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void generateChart(View view, ArrayList<Calendar> dates, ArrayList<String> xAxisValues, int timeSpan){

        int maxValue = 0;
        ArrayList<Double> valueList = new ArrayList<Double>();
        ArrayList<BarEntry> entries = new ArrayList<>();
        String title = " Recorded Seizures";

        for (int k = 0; k < xAxisValues.size(); k++){
            valueList.add(0.0);
        }

        if(timeSpan == MONTH_OF_YEAR) {
            for (int i = 0; i < dates.size(); i++) {
                valueList.set((dates.get(i).get(timeSpan)), valueList.get(dates.get(i).get(timeSpan)) + 1.0);
            }
        }
        else {
            // add 1 to corresponding column for each JournalDate
            for (int i = 0; i < dates.size(); i++) {
                valueList.set((dates.get(i).get(timeSpan) - 1), valueList.get(dates.get(i).get(timeSpan) - 1) + 1.0);
            }
        }

        //fit the data into a bar
        for (int i = 0; i < valueList.size(); i++) {
            BarEntry barEntry = new BarEntry(i, valueList.get(i).floatValue());
            entries.add(barEntry);
            if(valueList.get(i) > maxValue)
                maxValue = (int) valueList.get(i).floatValue();
        }

        BarDataSet barDataSet = new BarDataSet(entries, title);
        barDataSet.setDrawValues(false);
        barDataSet.setColor(Color.parseColor("#473fa2"));
        BarData data = new BarData(barDataSet);

        barChart.setData(data);

        barChart.notifyDataSetChanged();
        barChart.animateXY(2000, 2000);
        barChart.setDrawGridBackground(false);

        barChart.setExtraOffsets(5f,5f,0f,15f);

        XAxis axisX = barChart.getXAxis();
        axisX.setGranularity(1f);
        axisX.setDrawGridLines(false);
        axisX.setLabelCount(25);
        axisX.setValueFormatter(new IndexAxisValueFormatter(xAxisValues));

        YAxis axisY = barChart.getAxisLeft();
        axisY.setAxisMaximum(Math.max(maxValue, 5f));
        axisY.setGranularity(1f);
        axisY.setGridLineWidth(2f);
        axisY.setGridColor(Color.parseColor("#B0C4DE"));
        Log.d("getMax", String.valueOf(maxValue));

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawLabels(false);

        barChart.getDescription().setEnabled(false);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.animateY(1400, Easing.EaseInOutSine);
        barChart.invalidate();
    }

    public void getDates(Calendar dateCompare, View view, ArrayList<String> xAxisValues, int timeSpan){
        journalDates = new ArrayList<>();
        Log.d("getDates START", "ENTRIES BELOW");
        myRef.child("Journals").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm");
                Journal graphJournal = snapshot.getValue(Journal.class);
                try {
                    java.util.Date date = dateFormat.parse(graphJournal.dateAndTime);
                    Calendar cDate = new GregorianCalendar();
                    cDate.setTime(date);

                    if((cDate.getTimeInMillis() >= dateCompare.getTimeInMillis())) {
                        //Log.d("getgraph checker", String.valueOf(cDate));
                        journalDates.add(cDate);
                    }
                } catch (ParseException ex) {
                    Log.v("Exception", ex.getLocalizedMessage());
                }
                generateChart(view, journalDates, xAxisValues, timeSpan);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        Log.d("getDates END", "");
    }

    private void showHint(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.fragment_datatable_hint);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.dialog_bg));
        }
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false); //Optional
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

        Button gotIt = dialog.findViewById(R.id.btn_gotit);

        gotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gotIt.getText() == "Got it!")
                    dialog.dismiss();
                gotIt.setText("Got it!");
                textBox = dialog.getWindow().findViewById(R.id.textView2);
                titleBox = dialog.getWindow().findViewById(R.id.textView);
                textBox.setText("By swiping up on the bottom tab, you can edit and view your journals within a list view. Journals are automatically added when a seizure is detected. If desired, you can add a journal manually by clicking the middle button at the bottom.");
                titleBox.setText("Journal Listview");
            }
        });

        dialog.show();
    }

}
