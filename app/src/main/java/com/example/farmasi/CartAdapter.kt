package com.example.farmasi

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.farmasi.ProductModel
import com.example.farmasi.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartAdapter(
    private val cartItems: MutableList<ProductModel>,
    private val onQuantityChange: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        val context = holder.itemView.context

        // Load image using Glide
        Glide.with(context)
            .load(item.imageRes)
            .into(holder.image)

        holder.title.text = item.title
        holder.price.text = "Rp ${item.price}"
        holder.quantity.text = "Jumlah: ${item.quantity}"

        holder.itemView.findViewById<TextView>(R.id.btnCartIncrease).setOnClickListener {
            item.quantity++
            updateProductQuantity(item)
            notifyItemChanged(position)
            onQuantityChange()
        }

        holder.itemView.findViewById<TextView>(R.id.btnCartDecrease).setOnClickListener {
            if (item.quantity > 1) {
                item.quantity--
                updateProductQuantity(item)
                notifyItemChanged(position)
                onQuantityChange()
            } else {
                showDeleteConfirmationDialog(context, item, position)
            }
        }
    }

    override fun getItemCount(): Int = cartItems.size

    private fun updateProductQuantity(item: ProductModel) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("cart")
            .whereEqualTo("uid", uid)
            .whereEqualTo("title", item.title)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("quantity", item.quantity)
                }
            }
            .addOnFailureListener { e ->
                Log.e("CartAdapter", "Error updating quantity", e)
            }
    }

    private fun deleteProductFromCart(item: ProductModel) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("cart")
            .whereEqualTo("uid", uid)
            .whereEqualTo("title", item.title)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
            }
            .addOnFailureListener { e ->
                Log.e("CartAdapter", "Error deleting item", e)
            }
    }

    private fun showDeleteConfirmationDialog(context: Context, item: ProductModel, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Hapus Produk")
            .setMessage("Apakah Anda yakin ingin menghapus produk ini dari keranjang?")
            .setPositiveButton("Ya") { _, _ ->
                deleteProductFromCart(item)
                cartItems.removeAt(position)
                notifyItemRemoved(position)
                onQuantityChange()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgCartItem)
        val title: TextView = view.findViewById(R.id.tvCartItemTitle)
        val price: TextView = view.findViewById(R.id.tvCartItemPrice)
        val quantity: TextView = view.findViewById(R.id.tvCartItemQuantity)
    }
}