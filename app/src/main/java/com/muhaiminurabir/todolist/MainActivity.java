package com.muhaiminurabir.todolist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.login_email)
    EditText loginEmail;
    @BindView(R.id.login_password)
    EditText loginPassword;
    @BindView(R.id.login_remember_me)
    AppCompatCheckBox loginRememberMe;
    @BindView(R.id.login_forgot)
    TextView loginForgot;
    @BindView(R.id.login)
    Button login;
    @BindView(R.id.login_registration)
    Button loginRegistration;

    Context context;
    private FirebaseAuth mAuth;
    public static String TAG = "TO DO LIST";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        context=MainActivity.this;
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
    }

    @OnClick({R.id.login_remember_me, R.id.login_forgot, R.id.login, R.id.login_registration})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.login_remember_me:
                break;
            case R.id.login_forgot:
                break;
            case R.id.login:
                if (TextUtils.isEmpty(loginEmail.getText().toString()) || TextUtils.isEmpty(loginPassword.getText().toString())) {
                    Toast.makeText(context, "CHECK YOUR INPUT", Toast.LENGTH_SHORT).show();
                } else if (!loginEmail.getText().toString().contains("@")) {
                    Toast.makeText(context, "Invalid email address", Toast.LENGTH_SHORT).show();
                } else {
                    setLoginEmail(loginEmail.getText().toString(),loginPassword.getText().toString());
                }
                break;
            case R.id.login_registration:
                startActivity(new Intent(context,REGISTRATION_ACTIVITY.class));
                break;
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //(currentUser);
    }

    public void setLoginEmail(String email,String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(context,TODO_LIST_PAGE.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
