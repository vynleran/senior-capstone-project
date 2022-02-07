package com.example.seizuredetectionapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Datatable extends AppCompatActivity{
    Button btnAddJournal, btnSettings;
    ListView journalList;
    ArrayList<String> journalInfo = new ArrayList<>();
    static ArrayAdapter adapter;
    static ArrayAdapter sortedAdapter;
    Journal journal;
    FirebaseDatabase database;
    DatabaseReference myRef;
    LinearLayout sheetBottom;
    private String currentUserUID;
    BottomSheetBehavior bottomSheetBehavior;
    private Button btnHelpRequest;
    private Spinner sortSpinner;
    private String[] sortOptions = new String[1];
    ListView sortedJournalList;
    ArrayList<String> sortedJournalInfo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datatable);

        //firebase
        currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users").child(currentUserUID);

        //ui elements
        btnAddJournal = findViewById(R.id.btnjournaladd);
        btnSettings = findViewById(R.id.settings);
        btnHelpRequest = findViewById(R.id.helpRequest);
        journalList = findViewById(R.id.journalList);
        sortSpinner = findViewById(R.id.sortSpinner);

        // send those journals to listview
        sortedAdapter = new ArrayAdapter<>(Datatable.this, R.layout.listview_textformat, sortedJournalInfo);
        journalList.setAdapter(sortedAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString().trim();
                Log.d("selected Item", "selected Item" + selectedItem);

                // get a list of all the journals in firebase
                //Populate ListView
                myRef.child("Journals").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Journal journal  = snapshot.getValue(Journal.class);
                        sortedJournalInfo.add(journal.dateAndTime);
                    }
                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // sort those journals
                Collections.sort(sortedJournalInfo);
                sortedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //listview set up
        adapter = new ArrayAdapter<>(this, R.layout.listview_textformat, journalInfo);
        journalList.setAdapter(adapter);

        //Bottom Swipe up setup
        sheetBottom = findViewById(R.id.bottom_sheet_header);
        bottomSheetBehavior = BottomSheetBehavior.from(sheetBottom);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        //Peek Height
        bottomSheetBehavior.setPeekHeight(210);

        //Hideable
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        //Populate ListView
        myRef.child("Journals").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Journal journal  = snapshot.getValue(Journal.class);
                journalInfo.add(journal.dateAndTime);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //click row, brings up edit or remove
        journalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                //Dialog popup for choosing edit or remove journal
                AlertDialog.Builder editOrRemove = new AlertDialog.Builder(Datatable.this);
                editOrRemove.setTitle("Do you want to edit or remove this journal?");
                editOrRemove.setMessage("Edit or Remove?");
                editOrRemove.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Toast.makeText(Datatable.this, "Edited", Toast.LENGTH_SHORT).show();

                    }
                });

                editOrRemove.setNegativeButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int j) {
                        //Confirmation on removing journal
                        AlertDialog.Builder confirmRemove = new AlertDialog.Builder(Datatable.this);
                        confirmRemove.setTitle("Are you sure you want to remove this journal?");
                        confirmRemove.setMessage("Yes or No");
                        confirmRemove.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                removeJournal(pos);
                                Toast.makeText(Datatable.this, "Removed.", Toast.LENGTH_SHORT).show();

                            }
                        });
                        confirmRemove.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(Datatable.this, "Canceled.", Toast.LENGTH_SHORT).show();

                            }
                        });
                           confirmRemove.show();

                    }
                });
                editOrRemove.show();

            }
        });

        //button functionality to change to addJournal activity
        btnAddJournal.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //opens up journal activity on button press
                Intent intent = new Intent(Datatable.this, AddJournal.class);
                startActivity(intent);

            }
        });

        //button functionality to change to settings activity
        btnSettings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //opens up journal activity on button press
                Intent intent = new Intent(Datatable.this, MainSettings.class);
                startActivity(intent);

            }
        });

        //button functionality to change to AlertPage activity
        btnHelpRequest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //opens up journal activity on button press
                Intent intent = new Intent(Datatable.this, AlertPage.class);
                startActivity(intent);

            }
        });

    }

    //remove single journal from firebase
    public void removeJournal(int pos){

        //gets key id for chosen journal
        Query query = myRef.child("Journals").orderByChild("dateAndTime").equalTo(journalInfo.get(pos));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Delete Operation", "onCancelled", databaseError.toException());
            }
        });
        journalInfo.remove(pos);
        adapter.notifyDataSetChanged();
    }

    private void sortJournals(){
        String selectedSortOption = sortSpinner.getSelectedItem().toString().trim();
        ArrayList<String> journalInfo = new ArrayList<>();

        //Populate ListView
        myRef.child("Journals").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Journal journal  = snapshot.getValue(Journal.class);
                journalInfo.add(journal.dateAndTime);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //TODO
    public void editJournal(int pos){
        return;
    }

}
