package com.damacus.coffeepulse.domain.model

enum class TimerPhase {
    IDLE,
    BLOOM,
    POUR,
    WAIT,
}

data class PhasePresentation(
    val label: String,
    val hint: String,
)

fun TimerPhase.presentation(): PhasePresentation = when (this) {
    TimerPhase.IDLE -> PhasePresentation("READY", "Begin your ritual")
    TimerPhase.BLOOM -> PhasePresentation("BLOOM", "Let the coffee degas")
    TimerPhase.POUR -> PhasePresentation("POUR", "Add water slowly and evenly")
    TimerPhase.WAIT -> PhasePresentation("WAIT", "Let it drain through")
}
