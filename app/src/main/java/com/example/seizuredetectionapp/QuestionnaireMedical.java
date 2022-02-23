package com.example.seizuredetectionapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;


public class QuestionnaireMedical extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
    NumberPicker seizureDurationMinutes, seizureDurationSeconds, heightFeet, heightInches;
    EditText seizureFrequency, weightInput;
    Spinner seizureType, sexInput;
    Button submitQuestionnaireMedical, openDatePicker;
    String seizureStartD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire_medical);

        seizureDurationMinutes = findViewById(R.id.seizureDurationMinutes);
        seizureDurationSeconds = findViewById(R.id.seizureDurationSeconds);
        heightFeet = findViewById(R.id.heightInputFeet);
        heightInches = findViewById(R.id.heightInputInches);
        weightInput = findViewById(R.id.weightInput);
        seizureFrequency = findViewById(R.id.seizureFrequency);
        seizureType = findViewById(R.id.seizureType);
        sexInput = findViewById(R.id.sexInput);
        seizureStartD = "";

        openDatePicker = findViewById(R.id.openDatePickerDialog);
        submitQuestionnaireMedical = findViewById(R.id.submitQuestionnaireMedical);

        seizureDurationMinutes.setMinValue(0);
        seizureDurationMinutes.setMaxValue(60);
        //seizureDurationMinutes.setTextColor(Integer.parseInt("black"));

        seizureDurationSeconds.setMinValue(0);
        seizureDurationSeconds.setMaxValue(59);
        //seizureDurationSeconds.setTextColor(Integer.parseInt("black"));

        heightFeet.setMinValue(0);
        heightFeet.setMaxValue(12);
        //heightFeet.setTextColor(Integer.parseInt("black"));

        heightInches.setMinValue(0);
        heightInches.setMaxValue(11);
        //heightInches.setTextColor(Integer.parseInt("black"));

        openDatePicker.setOnClickListener(this);
        submitQuestionnaireMedical.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openDatePickerDialog: {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        this,
                        0,
                        this,
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();

                break;
            }

            case R.id.submitQuestionnaireMedical: {
                //Toast.makeText(QuestionnaireMedical.this, seizureStartD, Toast.LENGTH_LONG).show();
                String seizureDuration = String.valueOf(seizureDurationSeconds.getValue() + (seizureDurationMinutes.getValue() * 60));
                String height = String.valueOf(heightInches.getValue() + (heightFeet.getValue() * 12));
                String weight = weightInput.getText().toString().trim();
                String seizureFrequencyPerMonth = seizureFrequency.getText().toString().trim();
                String seizureT = seizureType.getSelectedItem().toString().trim();
                String sex = sexInput.getSelectedItem().toString().trim();

                //checks to see if any inputs are empty and alerts user.
                if (seizureDuration.equals("0")) {
                    seizureDurationSeconds.requestFocus();
                    return;
                }

                if (height.equals("0")) {
                    heightFeet.requestFocus();
                    return;
                }

                if (weight.equals("0")) {
                    weightInput.requestFocus();
                    return;
                }

                if (seizureT.equals("0")) {
                    seizureType.requestFocus();
                    return;
                }

                if (seizureStartD == "") {
                    openDatePicker.requestFocus();
                    openDatePicker.setError("A seizure start date is required!");
                    return;
                }



                // grab data from last questionnaire
                Intent i = getIntent();
                Questionnaire personalObject = (Questionnaire)i.getSerializableExtra("contactListObject");

                Questionnaire personal = new Questionnaire(personalObject.name, personalObject.countdownTimer,
                        personalObject.dateOfBirth, personalObject.contactMethod, seizureDuration, height,
                        weight, seizureFrequencyPerMonth, seizureStartD, seizureT, sex);

                Log.d("confirmation", "completed list: " + personal.toString());

                // Push to firebase
                String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("Users").child(currentUserUID).child("Settings");

                myRef.setValue(personal).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(QuestionnaireMedical.this, "Questionnaire saved.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(QuestionnaireMedical.this, Datatable.class));
                    }
                    else {
                        Toast.makeText(QuestionnaireMedical.this, "Questionnaire save failed.", Toast.LENGTH_LONG).show();
                    }
                });
                break;
            }
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        seizureStartD = (month + 1) + "/" + dayOfMonth + "/" + year;
    }
}