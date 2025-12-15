package com.chorded.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.chorded.app.R;
import com.chorded.app.main.MainActivity;
import com.chorded.app.session.AppSession;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        EditText email = findViewById(R.id.inputEmail);
        EditText pass = findViewById(R.id.inputPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnGuest = findViewById(R.id.btnGuest);
        TextView tvRegister = findViewById(R.id.tvRegister);

        // ---------- LOGIN ----------
        btnLogin.setOnClickListener(v -> {
            auth.signInWithEmailAndPassword(
                    email.getText().toString().trim(),
                    pass.getText().toString().trim()
            ).addOnSuccessListener(r -> {
                AppSession.get().startAuth(this, r.getUser().getUid());
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }).addOnFailureListener(e ->
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        });

        // ---------- GUEST ----------
        btnGuest.setOnClickListener(v -> {
            FirebaseAuth.getInstance()
                    .signInAnonymously()
                    .addOnSuccessListener(result -> {
                        AppSession.get().startGuest(
                                this,
                                result.getUser().getUid()
                        );
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

    }
}
