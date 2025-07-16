package com.example.pawpals

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        val emailField = findViewById<EditText>(R.id.emailInput)
        val passField = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val goToRegister = findViewById<TextView>(R.id.goToRegister)

        loginButton.setOnClickListener{
            val email= emailField.text.toString()
            val password = passField.text.toString()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{task ->
                    if (task.isSuccessful){
                        startActivity(Intent(this , MainActivity::class.java))
                        finish()
                    }else {
                        Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        goToRegister.setOnClickListener { startActivity(Intent(this, Register::class.java )) }

    }


}