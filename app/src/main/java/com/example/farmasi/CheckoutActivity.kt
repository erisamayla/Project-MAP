package com.example.farmasi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CheckoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Ambil daftar produk dari Intent
        val selectedProducts = intent.getParcelableArrayListExtra<ProductModel>("selectedProducts")
        Log.d("CheckoutActivity", "Products received: $selectedProducts")

        if (selectedProducts == null || selectedProducts.isEmpty()) {
            Toast.makeText(this, "Tidak ada produk yang dipilih", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Convert ProductModel ke CartModel untuk adapter
        val cartItems = selectedProducts.map { product ->
            CartModel(
                product = product,
                quantity = product.quantity,
                uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            )
        }

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_selected_products)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
            adapter = CheckoutAdapter(cartItems)
        }

        // Hitung dan tampilkan total harga
        val totalPrice = cartItems.sumOf { it.product.price * it.quantity }
        val totalPriceTextView = findViewById<TextView>(R.id.text_total_price)
        totalPriceTextView.text = "Total: Rp $totalPrice"

        // Setup Spinner Pengiriman
        val shippingSpinner = findViewById<Spinner>(R.id.spinner_shipping_options)
        val shippingOptions = listOf("JNE", "J&T", "GO-SEND", "TIKI")
        val shippingAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, shippingOptions)
        shippingSpinner.adapter = shippingAdapter

        // Setup Spinner Pembayaran
        val paymentSpinner = findViewById<Spinner>(R.id.spinner_payment_options)
        val paymentOptions = listOf("Transfer Bank", "COD", "e-Wallet")
        val paymentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paymentOptions)
        paymentSpinner.adapter = paymentAdapter

        // Handle Tombol Buat Pesanan
        val btnPlaceOrder = findViewById<Button>(R.id.btn_place_order)
        btnPlaceOrder.setOnClickListener {
            val selectedShipping = shippingSpinner.selectedItem?.toString()
            val selectedPayment = paymentSpinner.selectedItem?.toString()

            if (selectedShipping.isNullOrEmpty() || selectedPayment.isNullOrEmpty()) {
                Toast.makeText(this, "Silakan pilih metode pengiriman dan pembayaran!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createOrder(cartItems, totalPrice, selectedShipping, selectedPayment)
        }
    }

    private fun createOrder(
        cartItems: List<CartModel>,
        totalPrice: Int,
        shippingMethod: String,
        paymentMethod: String
    ) {
        val orderId = "ORDER-${System.currentTimeMillis()}"
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val checkoutData = CheckoutModel(
            orderId = orderId,
            products = cartItems.map { it.product },
            totalPrice = totalPrice,
            shippingMethod = shippingMethod,
            paymentMethod = paymentMethod,
            orderDate = System.currentTimeMillis()
        )

        val db = Firebase.firestore
        db.collection("orders")
            .document(orderId)
            .set(checkoutData)
            .addOnSuccessListener {
                // Hapus item dari keranjang setelah order berhasil
                clearCart(uid) {
                    // Pindah ke OrderDoneActivity
                    val intent = Intent(this, OrderDoneActivity::class.java)
                    intent.putExtra("checkoutData", checkoutData)
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal membuat pesanan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearCart(uid: String, onSuccess: () -> Unit) {
        val db = Firebase.firestore
        db.collection("cart")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Gagal mengosongkan keranjang: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Gagal mengambil data keranjang: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi")
            .setMessage("Apakah Anda yakin ingin meninggalkan halaman ini? Perubahan Anda mungkin tidak tersimpan.")
            .setPositiveButton("Ya") { _, _ -> super.onBackPressed() }
            .setNegativeButton("Tidak", null)
            .show()
    }
}