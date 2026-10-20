package com.example.recipebox.data

data class ShoppingItem(
    val id: Int = 0,
    val name: String,
    val amount: Double?,
    val unit: String,
    val checked: Boolean = false,
)
