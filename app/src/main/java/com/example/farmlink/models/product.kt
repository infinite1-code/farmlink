package com.example.farmlink.models

data class Product(
    val id: String = "",
    val name: String = "",
    val price: String = "",
    val category: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val expiryDate: String = "",
    val postedDate: String = "",
    val sellerUsername: String = "",
    val sellerUid: String = "",
    val quantity: String = "",       // ✅ new
    val unit: String = "",           // ✅ new
    val location: String = "",       // ✅ new
    val isAvailable: Boolean = true  // ✅ new
)