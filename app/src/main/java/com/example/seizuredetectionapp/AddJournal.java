package com.example.seizuredetectionapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AddJournal extends Activity implements View.OnClickListener {
    //class variables
    static EditText dateAndTime, mood, typeOfSeizure, duration, triggers, description, postDescription;
    Button btnClose, btnSave;
    Journal journal;
    private FirebaseAuth mAuth;
    Boolean edit;
    String ID;
    DatabaseReference myRef;
    public static FirebaseDatabase database;
    public static DatabaseReference userTable;
    private static String currentUserUID;
    public static String updateDateTime;
    public static String updateMood;
    public static String updateTypeOfSeizure;
    public static String updateDuration;
    public static String updateTriggers;
    public static String updateDescription;
    public static String updatePostDescription;
    private Journal editJournal;
    private String journalKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addjournal);

        //firebase DB
        currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        userTable = database.getReference("Users").child(currentUserUID);

        //get ui elements
        dateAndTime = findViewById(R.id.datetime);
        mood = findViewById(R.id.mood);
        typeOfSeizure = findViewById(R.id.typeofseizure);
        duration = findViewById(R.id.duration);
        triggers = findViewById(R.id.triggers);
        description = findViewById(R.id.description);
        postDescription = findViewById(R.id.postdescription);
        btnSave =  findViewById(R.id.btnsave);
        btnClose =  findViewById(R.id.btnclose);

        //if user pressed edit
        Bundle extras = getIntent().getExtras();
        edit = false;
        if(extras != null){
            edit = extras.getBoolean("key");
            ID = extras.getString("id");
            Log.d("journal ID","id" + ID);
        }

        if(edit){
            //Retrieving saved journal information
            popJournalText();
        }


        btnClose.setOnClickListener(this);
        btnSave.setOnClickListener(this);

    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.btnclose:
                finish();
                break;
            case R.id.btnsave:
                if(edit){
                    updateInformation();
                }
                else{
                    saveInformation();
                }
                startActivity(new Intent(AddJournal.this, Datatable.class));
                break;
        }
    }

    //method for retrieving info written and saving to firebase
    public void saveInformation()
    {
        //retrieving text from text boxes
        String datetime = dateAndTime.getText().toString().trim();
        String moodType = mood.getText().toString().trim();
        String seizureType = typeOfSeizure.getText().toString().trim();
        String durationOfSeizure = duration.getText().toString().trim();
        String seizureTrigger = triggers.getText().toString().trim();
        String seizureDescription = description.getText().toString().trim();
        String postSeizureDescription = postDescription.getText().toString().trim();

        Journal journal = new Journal(datetime, moodType, seizureType, durationOfSeizure,
                seizureTrigger, seizureDescription, postSeizureDescription);

        // Sends HashMap of entry to Firebase DB
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users").child(currentUserUID).child("Journals");

        myRef.push().setValue(journal).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(AddJournal.this, "Journal Saved.", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(AddJournal.this, "Journal Save Failed.", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    public void updateInformation(){

        //Retrieving new inputted information
        String dateTime = dateAndTime.getText().toString().trim();
        String moodType = mood.getText().toString().trim();
        String seizureType = typeOfSeizure.getText().toString().trim();
        String durationOfSeizure = duration.getText().toString().trim();
        String seizureTrigger = triggers.getText().toString().trim();
        String seizureDescription = description.getText().toString().trim();
        String postSeizureDescription = postDescription.getText().toString().trim();

        String previousValue = editJournal.dateAndTime;
        if(!previousValue.equals(dateTime)){
            updateFieldInFirebase("dateAndTime", dateTime);
        }
        previousValue = editJournal.mood;
        if(!previousValue.equals(moodType) && previousValue != null){
            updateFieldInFirebase("mood", moodType);
        }
        previousValue = editJournal.typeOfSeizure;
        if(!previousValue.equals(seizureType)){
            updateFieldInFirebase("typeOfSeizure", seizureType);
        }
        previousValue = editJournal.durationOfSeizure;
        if(!previousValue.equals(durationOfSeizure)){
            updateFieldInFirebase("durationOfSeizure", durationOfSeizure);
        }
        previousValue = editJournal.triggers;
        if(!previousValue.equals(seizureTrigger)){
            updateFieldInFirebase("triggers", seizureTrigger);
        }
        previousValue = editJournal.description;
        if(!previousValue.equals(seizureDescription)){
            updateFieldInFirebase("description", seizureDescription);
        }
        previousValue = editJournal.postDescription;
        if(!previousValue.equals(postSeizureDescription)){
            updateFieldInFirebase("postDescription", postSeizureDescription);
        }



    }


    public void popJournalText(){
        //set existing journal to each edittext

        userTable.child("Journals").orderByChild("dateAndTime").equalTo(ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                //Log.d("date1", "date1 = " + snapshot.toString());

                for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                    journalKey = childSnapshot.getKey();
                    Log.d("date1", "date1 = " + journalKey);
                    editJournal = childSnapshot.getValue(Journal.class);
                    Log.d("date", "date = " + editJournal.toString());


                    AddJournal.updateDateTime = editJournal.dateAndTime;
                    AddJournal.updateMood = editJournal.mood;
                    AddJournal.updateTypeOfSeizure = editJournal.typeOfSeizure;
                    AddJournal.updateDuration = editJournal.durationOfSeizure;
                    AddJournal.updateTriggers = editJournal.triggers;
                    AddJournal.updateDescription = editJournal.description;
                    AddJournal.updatePostDescription = editJournal.postDescription;

                    //Set EditText to existing saved values
                    AddJournal.dateAndTime.setText(AddJournal.updateDateTime);
                    AddJournal.mood.setText(updateMood);
                    AddJournal.typeOfSeizure.setText(updateTypeOfSeizure);
                    AddJournal.duration.setText(updateDuration);
                    AddJournal.triggers.setText(updateTriggers);
                    AddJournal.description.setText(updateDescription);
                    AddJournal.postDescription.setText(updatePostDescription);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Setting Data Retrieval", error.getDetails());
            }
        });
    }

    private void updateFieldInFirebase(String field, String newValue){
        DatabaseReference journalTable = userTable.child("Journals");

        journalTable.child(journalKey).child(field).setValue(newValue).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Log.d(field, "Updated");
            }
            else{
                Log.d(field, task.getException().toString());
            }
        });
    }

}
