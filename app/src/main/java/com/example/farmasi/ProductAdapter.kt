package com.example.farmasi

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.farmasi.ProductModel
import com.example.farmasi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ProductAdapter(
    private val context: Context,
    private val productList: ArrayList<ProductModel>
) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        holder.apply {
            title.text = product.title
            price.text = "Rp${product.price}"
            quantity.text = "0" // Reset quantity display

            // Load image using Glide
            Glide.with(context)
                .load(product.imageRes)
                .into(image)

            // Update stock display if needed
            if (product.stock <= 0) {
                addToCart.isEnabled = false
                addToCart.text = "Stok Habis"
            } else {
                addToCart.isEnabled = true
                addToCart.text = "Tambah ke Keranjang"
            }

            var currentQuantity = 0

            btnIncrease.setOnClickListener {
                if (currentQuantity < product.stock) {
                    currentQuantity++
                    quantity.text = currentQuantity.toString()
                    updateAddToCartButton(currentQuantity, holder.addToCart)
                } else {
                    Toast.makeText(context, "Stok tidak cukup!", Toast.LENGTH_SHORT).show()
                }
            }

            btnDecrease.setOnClickListener {
                if (currentQuantity > 0) {
                    currentQuantity--
                    quantity.text = currentQuantity.toString()
                    updateAddToCartButton(currentQuantity, holder.addToCart)
                }
            }

            addToCart.setOnClickListener {
                if (currentQuantity > 0) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    if (uid == null) {
                        Toast.makeText(context, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    addToCart(product, currentQuantity, uid)
                    // Reset after adding to cart
                    currentQuantity = 0
                    quantity.text = "0"
                    updateAddToCartButton(currentQuantity, holder.addToCart)
                } else {
                    Toast.makeText(context, "Pilih jumlah produk terlebih dahulu", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateAddToCartButton(quantity: Int, button: Button) {
        button.isEnabled = quantity > 0
    }

    private fun addToCart(product: ProductModel, quantity: Int, uid: String) {
        val db = FirebaseFirestore.getInstance()

        // Check existing cart item
        db.collection("cart")
            .whereEqualTo("uid", uid)
            .whereEqualTo("title", product.title)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Add new cart item
                    val cartItem = hashMapOf(
                        "uid" to uid,
                        "title" to product.title,
                        "price" to product.price,
                        "category" to product.category,
                        "quantity" to quantity,
                        "imageRes" to product.imageRes
                    )

                    db.collection("cart")
                        .add(cartItem)
                        .addOnSuccessListener {
                            updateProductStock(product, quantity)
                            Toast.makeText(context, "Berhasil ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ProductAdapter", "Error adding to cart", e)
                            Toast.makeText(context, "Gagal menambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Update existing cart item
                    val doc = documents.first()
                    val existingQuantity = doc.getLong("quantity")?.toInt() ?: 0
                    val newQuantity = existingQuantity + quantity

                    doc.reference
                        .update("quantity", newQuantity)
                        .addOnSuccessListener {
                            updateProductStock(product, quantity)
                            Toast.makeText(context, "Keranjang diperbarui", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ProductAdapter", "Error updating cart", e)
                            Toast.makeText(context, "Gagal memperbarui keranjang", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProductAdapter", "Error checking cart", e)
                Toast.makeText(context, "Gagal memeriksa keranjang", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProductStock(product: ProductModel, quantity: Int) {
        val db = FirebaseFirestore.getInstance()
        db.collection("product")
            .document(product.productId)
            .update("stock", FieldValue.increment(-quantity.toLong()))
            .addOnFailureListener { e ->
                Log.e("ProductAdapter", "Error updating stock", e)
            }
    }

    override fun getItemCount(): Int = productList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.txt_title)
        val price: TextView = itemView.findViewById(R.id.txt_harga)
        val image: ImageView = itemView.findViewById(R.id.imageview)
        val btnIncrease: TextView = itemView.findViewById(R.id.btn_increase)
        val btnDecrease: TextView = itemView.findViewById(R.id.btn_decrease)
        val quantity: TextView = itemView.findViewById(R.id.txt_quantity)
        val addToCart: Button = itemView.findViewById(R.id.btn_add)
    }
}