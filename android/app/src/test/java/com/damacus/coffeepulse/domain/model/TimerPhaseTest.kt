package com.damacus.coffeepulse.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerPhaseTest {
    @Test
    fun pourAndWaitUseExplicitActionLabels() {
        assertEquals("POUR NOW", TimerPhase.POUR.presentation().label)
        assertEquals("STOP POURING", TimerPhase.WAIT.presentation().label)
    }
}
