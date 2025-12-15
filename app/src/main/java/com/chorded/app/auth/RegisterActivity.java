package com.chorded.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chorded.app.R;
import com.chorded.app.main.MainActivity;
import com.chorded.app.models.User;
import com.chorded.app.session.AppSession;
import com.chorded.app.session.SessionStorage;
import com.chorded.app.session.SessionType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText inputName = findViewById(R.id.inputName);
        EditText inputEmail = findViewById(R.id.inputEmail);
        EditText inputPassword = findViewById(R.id.inputPassword);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {

            String name = inputName.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String pass = inputPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Все поля обязательны", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(result -> {

                        String uid = result.getUser().getUid();

                        // ---------- CREATE USER IN FIRESTORE ----------
                        User user = new User(uid, name, email, new ArrayList<>());
                        user.setRole("user");

                        db.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(v2 -> {

                                    // ---------- 🔑 INIT APP SESSION ----------
                                    AppSession.get().startAuth(this, uid);
                                    new SessionStorage(this).saveAuth(uid);

                                    // ---------- GO TO MAIN ----------
                                    startActivity(
                                            new Intent(this, MainActivity.class)
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    );
                                    finish();
                                });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    this,
                                    "Ошибка регистрации: " + e.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
        });
    }
}
