package com.example.farmasi

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.content.Intent
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    lateinit var btnRegister : Button
    lateinit var etEmail : EditText
    lateinit var etName : EditText
    lateinit var etPassword : EditText
    lateinit var etPasswordConfirmation : EditText
    lateinit var text_page_register : TextView

    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btnRegister = findViewById(R.id.btn_register)
        etEmail = findViewById(R.id.et_register_email)
        etName = findViewById(R.id.et_register_name)
        etPassword = findViewById(R.id.et_register_password)
        etPasswordConfirmation = findViewById(R.id.et_register_password_confirmation)
        text_page_register = findViewById(R.id.text_page_register)

        text_page_register.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val name = etName.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val passwordConfirmation = etPasswordConfirmation.text.toString().trim()

            if (password != passwordConfirmation) {
                Toast.makeText(this, "Password dan konfirmasi tidak sama!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, name, password)
        }
    }

    private fun registerUser(email: String, name: String, password: String) {
        // Mendaftarkan user ke Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val uid = user.uid
                        saveUserToFirestore(uid, email, name)
                    }
                } else {
                    Toast.makeText(this, "Registrasi gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFirestore(uid: String, email: String, name: String) {
        val userModel = UserModel(
            Email = email,
            Name = name,
            Password = null // Tidak menyimpan password di Firestore
        )

        val db = Firebase.firestore
        db.collection("users").document(uid) // Menyimpan data dengan UID sebagai dokumen ID
            .set(userModel)
            .addOnSuccessListener {
                Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke LoginActivity
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(this, "Gagal menyimpan data pengguna!", Toast.LENGTH_SHORT).show()
            }
    }
}
