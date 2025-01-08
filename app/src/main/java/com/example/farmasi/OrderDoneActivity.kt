package com.example.farmasi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class OrderDoneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_done)

        val btnDone: Button = findViewById(R.id.btn_done)

        // Tombol Done
        btnDone.setOnClickListener {
            // Kembali ke MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Tutup activity ini
        }

        val checkoutData = intent.getParcelableExtra<CheckoutModel>("checkoutData")
        checkoutData?.let {
            // Gunakan data checkout di halaman ini
            Log.d("OrderDone", "Pesanan: $it")
        }

    }
}
