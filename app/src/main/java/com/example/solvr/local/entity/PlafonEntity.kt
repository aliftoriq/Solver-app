package com.example.solvr.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plafon")
data class PlafonEntity(
    @PrimaryKey val id: Int,
    val interestRate: Double?,
    val amount: Int?,
    val level: Int?,
    val name: String?,
    val maxTenorMonths: Int?
)
