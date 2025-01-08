package com.example.farmasi

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductModel(
    val productId: String = "",
    val imageRes: String = "",
    val title: String = "",
    val price: Int = 0,
    val category: String = "",
    var stock: Int = 0,
    var quantity: Int = 0
) : Parcelable