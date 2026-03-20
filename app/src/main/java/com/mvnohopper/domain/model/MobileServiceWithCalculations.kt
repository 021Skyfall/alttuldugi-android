package com.mvnohopper.domain.model

import com.mvnohopper.data.entity.MobileService
import java.time.LocalDate

data class MobileServiceWithCalculations(
    val mobileService: MobileService,
    val promotionEndDate: LocalDate,
    val minContractEndDate: LocalDate?,
    val recommendedTerminationDate: LocalDate,
    val recommendedReminderDate: LocalDate,
    val elapsedMonths: Long,
    val remainingPromotionDays: Long
)
