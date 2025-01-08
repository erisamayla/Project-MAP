package com.example.farmasi

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartModel(
    var product: ProductModel = ProductModel(
        productId = "",
        imageRes = "",
        title = "",
        price = 0,
        category = "",
        stock = 0
    ), // Konstruktor default untuk ProductModel
    var quantity: Int = 0,
    var uid: String = "",
) : Parcelable