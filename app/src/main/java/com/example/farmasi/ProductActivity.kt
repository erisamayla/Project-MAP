package com.example.farmasi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.farmasi.ProductAdapter
import com.example.farmasi.CartActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class ProductActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private val productList = ArrayList<ProductModel>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        val btnViewCart: Button = findViewById(R.id.btn_view_cart)
        btnViewCart.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, CartActivity::class.java))
        }

        recyclerView = findViewById(R.id.productrecyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with empty list first
        productAdapter = ProductAdapter(this, productList)
        recyclerView.adapter = productAdapter

        val category = intent.getStringExtra("category") ?: ""
        if (category.isNotEmpty()) {
            fetchProductsByCategory(category)
        } else {
            Toast.makeText(this, "Kategori tidak valid", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchProductsByCategory(category: String) {
        firestore.collection("product")
            .whereEqualTo("category", category)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ProductActivity", "Error fetching products", e)
                    Toast.makeText(this, "Gagal memuat produk", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                productList.clear()
                snapshot?.documents?.forEach { document ->
                    try {
                        val product = ProductModel(
                            productId = document.id,
                            imageRes = document.getString("imageRes") ?: "",
                            title = document.getString("title") ?: "",
                            price = document.getLong("price")?.toInt() ?: 0,
                            category = document.getString("category") ?: "",
                            stock = document.getLong("stock")?.toInt() ?: 0,
                            quantity = 0 // Reset quantity for display
                        )
                        productList.add(product)
                    } catch (e: Exception) {
                        Log.e("ProductActivity", "Error parsing product", e)
                    }
                }
                productAdapter.notifyDataSetChanged()
            }
    }
}