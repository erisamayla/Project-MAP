package com.example.farmasi

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckoutModel(
    val orderId: String,
    val products: List<ProductModel>,
    val totalPrice: Int,
    val shippingMethod: String,
    val paymentMethod: String,
    val orderDate: Long
) : Parcelable