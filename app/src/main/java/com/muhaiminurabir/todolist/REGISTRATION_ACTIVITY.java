package com.muhaiminurabir.todolist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class REGISTRATION_ACTIVITY extends AppCompatActivity {
    @BindView(R.id.registration_name)
    EditText registrationName;
    @BindView(R.id.registration_email)
    EditText registrationEmail;
    @BindView(R.id.registration_password)
    EditText registrationPassword;
    @BindView(R.id.registration_confirm_password)
    EditText registrationConfirmPassword;
    @BindView(R.id.registration_complete)
    Button registrationComplete;
    @BindView(R.id.registration_login)
    Button registrationLogin;

    Context context;
    private FirebaseAuth mAuth;
    public static String TAG = "TO DO LIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration__activity);
        //action bar hide for full screen
        getSupportActionBar().hide();
        //this is for boilerplate
        ButterKnife.bind(this);
        context = REGISTRATION_ACTIVITY.this;
        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
    }

    @OnClick({R.id.registration_complete, R.id.registration_login})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.registration_complete:
                //checking input validation
                if (TextUtils.isEmpty(registrationEmail.getText().toString()) || TextUtils.isEmpty(registrationPassword.getText().toString())) {
                    Toast.makeText(context, "CHECK YOUR INPUT", Toast.LENGTH_SHORT).show();
                } else if (!registrationEmail.getText().toString().contains("@")) {
                    Toast.makeText(context, "Invalid email address", Toast.LENGTH_SHORT).show();
                } else if (!registrationPassword.getText().toString().equals(registrationConfirmPassword.getText().toString())) {
                    Toast.makeText(context, "Password and Confirm Password Not Matched", Toast.LENGTH_SHORT).show();
                } else {
                    setRegistrationEmail(registrationEmail.getText().toString(), registrationPassword.getText().toString());
                }
                break;
            case R.id.registration_login:
                //go to registration service
                startActivity(new Intent(context, MainActivity.class));
                break;
        }
    }

    //registration work
    public void setRegistrationEmail(String email, String pass) {
        //show progressdialouge
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("Login, please wait.");
        dialog.show();

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(context, "Registration Created", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(context, MainActivity.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
