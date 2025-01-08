package com.example.farmasi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CheckoutAdapter(
    private val cartItems: List<CartModel>
) : RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checkout, parent, false)
        return CheckoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        val cartItem = cartItems[position]

        holder.apply {
            title.text = cartItem.product.title
            quantity.text = "Jumlah: ${cartItem.quantity}"
            price.text = "Rp ${cartItem.product.price * cartItem.quantity}"

            // Load image using Glide
            Glide.with(itemView.context)
                .load(cartItem.product.imageRes)
                .into(image)
        }
    }

    override fun getItemCount(): Int = cartItems.size

    class CheckoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.imgCheckoutItem)
        val title: TextView = view.findViewById(R.id.txt_checkout_title)
        val quantity: TextView = view.findViewById(R.id.txt_checkout_quantity)
        val price: TextView = view.findViewById(R.id.txt_checkout_price)
    }
}