package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fish_records",
    foreignKeys = [
        ForeignKey(
            entity = FishSpecies::class,
            parentColumns = ["id"],
            childColumns = ["speciesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("speciesId"), Index("catchTime")]
)
data class FishRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val speciesId: Long,
    val photoUri: String? = null,
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val catchTime: Long = System.currentTimeMillis(),
    val weight: Double? = null,
    val length: Double? = null,
    val notes: String = "",
    val weather: String = "",
    val baitUsed: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
