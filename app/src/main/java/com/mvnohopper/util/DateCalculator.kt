package com.mvnohopper.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object DateCalculator {

    fun calculatePromotionEndDate(
        activationDate: LocalDate,
        promotionMonths: Int
    ): LocalDate {
        return activationDate.plusMonths(promotionMonths.toLong()).minusDays(1)
    }

    fun calculateMinContractEndDate(
        activationDate: LocalDate,
        minContractMonths: Int
    ): LocalDate? {
        if (minContractMonths == 0) {
            return null
        }
        return activationDate.plusMonths(minContractMonths.toLong()).minusDays(1)
    }

    fun calculateRecommendedTerminationDate(
        activationDate: LocalDate,
        promotionMonths: Int,
        minContractMonths: Int
    ): LocalDate {
        val promotionEndDate = calculatePromotionEndDate(activationDate, promotionMonths)
        val minContractEndDate = calculateMinContractEndDate(activationDate, minContractMonths)

        return if (minContractEndDate != null && minContractEndDate.isAfter(promotionEndDate)) {
            minContractEndDate
        } else {
            promotionEndDate
        }
    }

    fun calculateReminderDate(
        recommendedTerminationDate: LocalDate,
        reminderDaysBeforeEnd: Int
    ): LocalDate {
        return recommendedTerminationDate.minusDays(reminderDaysBeforeEnd.toLong())
    }

    fun calculateElapsedMonths(
        activationDate: LocalDate,
        today: LocalDate
    ): Long {
        return ChronoUnit.MONTHS.between(activationDate, today)
    }

    fun calculateRemainingPromotionDays(
        promotionEndDate: LocalDate,
        today: LocalDate
    ): Long {
        return ChronoUnit.DAYS.between(today, promotionEndDate)
    }
}
