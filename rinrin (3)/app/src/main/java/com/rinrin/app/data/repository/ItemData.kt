package com.rinrin.app.data.repository

data class ItemData(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dailyRate: Int = 0,
    val category: String = "",
    val securityDeposit: Int? = null,
    val imageUrl: String? = null,
    val ownerId: String = "",
    val ownerName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
