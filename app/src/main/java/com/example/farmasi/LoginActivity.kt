package com.example.farmasi

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    lateinit var btnLogin : Button
    lateinit var etEmail : EditText
    lateinit var etPassword : EditText
    lateinit var txtRegister : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPreference =  getSharedPreferences(
            "app_preference", Context.MODE_PRIVATE
        )

        var id = sharedPreference.getString("id", "").toString()

        if (!id.isNullOrBlank()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


        btnLogin = findViewById(R.id.btn_login)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)

        txtRegister = findViewById(R.id.text_page_register)

        txtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            this.auth(etEmail.text.toString(), etPassword.text.toString()) { isValid ->
                if (!isValid) {
                    Toast.makeText(
                        applicationContext,
                        "Username atau passworrd salah!",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@auth
                }

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun auth(email: String, password: String, checkResult: (isValid: Boolean) -> Unit) {
        val firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login berhasil, ambil UID pengguna
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val uid = user.uid
                        getUserDataFromFirestore(uid) { isValid ->
                            checkResult(isValid)
                        }
                    } else {
                        checkResult(false)
                    }
                } else {
                    // Login gagal
                    checkResult(false)
                }
            }
    }

    private fun getUserDataFromFirestore(uid: String, checkResult: (isValid: Boolean) -> Unit) {
        val db = Firebase.firestore
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val sharedPreference = getSharedPreferences(
                        "app_preference", Context.MODE_PRIVATE
                    )
                    val editor = sharedPreference.edit()

                    editor.putString("id", uid)
                    editor.putString("name", document.getString("name"))
                    editor.putString("email", document.getString("email"))
                    editor.apply()

                    checkResult(true)
                } else {
                    // Data user tidak ditemukan
                    checkResult(false)
                }
            }
            .addOnFailureListener {
                // Gagal mengambil data
                checkResult(false)
            }
    }
}