package com.mvnohopper.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mobile_services")
data class MobileService(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val operatorName: String,
    val providerName: String,
    val planName: String,
    val activationDate: String,
    val promotionMonths: Int,
    val minContractMonths: Int = 0,
    val earlyTerminationFee: Int = 0,
    val monthlyFee: Int = 0,
    val reminderDaysBeforeEnd: Int = 15,
    val notes: String = "",
    val createdAt: String,
    val updatedAt: String
)
