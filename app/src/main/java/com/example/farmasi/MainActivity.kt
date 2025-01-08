package com.example.farmasi

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.farmasi.CartActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Navigasi ke halaman pesanan
        } else {
            // Arahkan ke halaman login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Tombol Logout
        val btnLogout: Button = findViewById(R.id.btnLogout)

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Logout")
                setMessage("Apakah Anda yakin ingin logout?")
                setPositiveButton("Ya") { _, _ ->
                    logoutUser()
                }
                setNegativeButton("Tidak", null)
            }.show()
        }

        // ImageButton cart
        val imgCartBtn: ImageButton = findViewById(R.id.img_cart_btn)

        imgCartBtn.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        val data = ArrayList<DataViewModel>()
        val adapter = CustomAdapter(data) { selectedCategory ->
            Log.d(TAG, "Selected category: ${selectedCategory.category}")
            val intent = Intent(this, ProductActivity::class.java)
            intent.putExtra("category", selectedCategory.category)
            startActivity(intent)
        }

        val recyclerview = findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.adapter = adapter

        val db = Firebase.firestore
        db.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val image = document.getString("image") ?: ""
                    val category = document.getString("category") ?: ""
                    data.add(DataViewModel(image, category))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    private fun logoutUser() {

        val sharedPreference =  getSharedPreferences("app_preference", Context.MODE_PRIVATE)
        var editor = sharedPreference.edit()
        editor.clear()
        editor.commit()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}