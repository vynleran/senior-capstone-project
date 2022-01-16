package com.example.seizuredetectionapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class SignupTabFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    Button signup;
    EditText email, username, password, confirmPassword;
    float v = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.signup_tab_fragment, container, false);

        // Initializing Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Getting the views by ID
        email = root.findViewById(R.id.email);
        username = root.findViewById(R.id.username);
        password = root.findViewById(R.id.password);
        confirmPassword = root.findViewById(R.id.confirmPassword);
        signup = root.findViewById(R.id.signup);
        signup.setOnClickListener(this);

        // Adding animations to the views
        email.setTranslationY(800);
        username.setTranslationY(800);
        password.setTranslationY(800);
        confirmPassword.setTranslationY(800);
        signup.setTranslationY(800);

        email.setAlpha(v);
        username.setAlpha(v);
        password.setAlpha(v);
        confirmPassword.setAlpha(v);
        signup.setAlpha(v);

        email.animate().translationY(0).alpha(1).setDuration(800).setStartDelay(300).start();
        username.animate().translationY(0).alpha(1).setDuration(800).setStartDelay(500).start();
        password.animate().translationY(0).alpha(1).setDuration(800).setStartDelay(500).start();
        confirmPassword.animate().translationY(0).alpha(1).setDuration(800).setStartDelay(700).start();
        signup.animate().translationY(0).alpha(1).setDuration(800).setStartDelay(900).start();

        return root;
    }

    // Overriding the onclick function to signup the user and move to the datatable page
    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.signup:
                // TODO: Change this to the questionnaire page later
                // Change to the Datatable page
                signupUser();
                startActivity(new Intent(this.getContext(), Datatable.class));
                break;
        }
    }

    // Checking the user inputs and signing them up if valid
    public void signupUser(){
        String emailText = email.getText().toString().trim();
        String usernameText = username.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String confirmPasswordText = confirmPassword.getText().toString().trim();

        if(emailText.isEmpty()){
            email.setError("Email is required!");
            email.requestFocus();
            return;
        }

        if(usernameText.isEmpty()){
            username.setError("Username is required!");
            username.requestFocus();
            return;
        }

        if(passwordText.isEmpty()){
            password.setError("Password is required!");
            password.requestFocus();
            return;
        }

        if(confirmPasswordText.isEmpty()){
            confirmPassword.setError("Confirm password is required!");
            confirmPassword.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){
            email.setError("Please provide a valid email address");
            email.requestFocus();
            return;
        }

        if(passwordText.length() < 8){
            password.setError("The password needs to be at least 8 characters");
            password.requestFocus();
            return;
        }

        if(passwordText.equals(confirmPasswordText)){
            confirmPassword.setError("The confirm password needs to match the password");
            confirmPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){
                            User user = new User(usernameText, emailText);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getActivity(), "User has signed up successfully", Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        Toast.makeText(getActivity(), "User sign up failed!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(getActivity(), "Failed to signup", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

}
