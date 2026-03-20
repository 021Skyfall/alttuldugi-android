package com.mvnohopper.domain.usecase

import com.mvnohopper.data.entity.MobileService
import com.mvnohopper.domain.model.MobileServiceWithCalculations
import com.mvnohopper.util.DateCalculator
import java.time.LocalDate

class CalculateDateUseCase {

    operator fun invoke(
        mobileService: MobileService,
        today: LocalDate = LocalDate.now()
    ): MobileServiceWithCalculations {
        val activationDate = LocalDate.parse(mobileService.activationDate)
        val promotionEndDate = DateCalculator.calculatePromotionEndDate(
            activationDate = activationDate,
            promotionMonths = mobileService.promotionMonths
        )
        val minContractEndDate = DateCalculator.calculateMinContractEndDate(
            activationDate = activationDate,
            minContractMonths = mobileService.minContractMonths
        )
        val recommendedTerminationDate = DateCalculator.calculateRecommendedTerminationDate(
            activationDate = activationDate,
            promotionMonths = mobileService.promotionMonths,
            minContractMonths = mobileService.minContractMonths
        )
        val reminderDate = DateCalculator.calculateReminderDate(
            recommendedTerminationDate = recommendedTerminationDate,
            reminderDaysBeforeEnd = mobileService.reminderDaysBeforeEnd
        )

        return MobileServiceWithCalculations(
            mobileService = mobileService,
            promotionEndDate = promotionEndDate,
            minContractEndDate = minContractEndDate,
            recommendedTerminationDate = recommendedTerminationDate,
            recommendedReminderDate = reminderDate,
            elapsedMonths = DateCalculator.calculateElapsedMonths(
                activationDate = activationDate,
                today = today
            ),
            remainingPromotionDays = DateCalculator.calculateRemainingPromotionDays(
                promotionEndDate = promotionEndDate,
                today = today
            )
        )
    }
}
