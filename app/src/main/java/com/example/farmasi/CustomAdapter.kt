package com.example.farmasi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CustomAdapter(
    private val mList: List<DataViewModel>,
    private val onItemClick: (DataViewModel) -> Unit
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.category.text = item.category

        Glide.with(holder.imageview.context)
            .load(item.image)
            .placeholder(R.drawable.pharmacy) // Opsional: Tambahkan placeholder
            .error(R.drawable.pharmacy)      // Opsional: Tambahkan error handling
            .into(holder.imageview) // Tetapkan ke ImageView


        holder.itemView.setOnClickListener {
            if (item.category.isNotEmpty()) {
                onItemClick(item)
            } else {
                Toast.makeText(holder.itemView.context, "Category not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var category: TextView = view.findViewById(R.id.txt_title)
        var imageview: ImageView = view.findViewById(R.id.imageview)
    }
}
