package com.example.farmasi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.farmasi.CheckoutActivity
import com.example.farmasi.ProductModel
import com.example.farmasi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnCheckout: Button
    private lateinit var tvTotalPrice: TextView
    private val cartItems = mutableListOf<ProductModel>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        recyclerView = findViewById(R.id.cartRecyclerView)
        btnCheckout = findViewById(R.id.btnCheckout)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)

        // Initialize adapter first
        cartAdapter = CartAdapter(cartItems) {
            updateTotalPrice()
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }

        // Observe cart items after setting up the adapter
        observeCartItems()

        btnCheckout.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Keranjang kosong. Silakan tambahkan barang terlebih dahulu.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedProducts = cartItems.filter { it.quantity > 0 }
            if (selectedProducts.isEmpty()) {
                Toast.makeText(this, "Tidak ada barang di keranjang dengan jumlah lebih dari 0.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putParcelableArrayListExtra("selectedProducts", ArrayList(selectedProducts))
            startActivity(intent)
        }
    }

    private fun calculateTotalPrice(): Int {
        return cartItems.sumOf { it.price * it.quantity }
    }

    private fun updateTotalPrice() {
        val totalPrice = calculateTotalPrice()
        tvTotalPrice.text = "Total: Rp$totalPrice"
    }

    private fun observeCartItems() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("cart")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("CartActivity", "Error listening to cart changes", e)
                    Toast.makeText(this, "Gagal memuat data keranjang", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                cartItems.clear()

                snapshot?.documents?.forEach { doc ->
                    try {
                        val product = ProductModel(
                            productId = doc.id,
                            imageRes = doc.getString("imageRes") ?: "",
                            title = doc.getString("title") ?: "",
                            price = doc.getLong("price")?.toInt() ?: 0,
                            category = doc.getString("category") ?: "",
                            quantity = doc.getLong("quantity")?.toInt() ?: 0
                        )
                        cartItems.add(product)
                    } catch (e: Exception) {
                        Log.e("CartActivity", "Error parsing cart item", e)
                    }
                }

                cartAdapter.notifyDataSetChanged()
                updateTotalPrice()
            }
    }
}