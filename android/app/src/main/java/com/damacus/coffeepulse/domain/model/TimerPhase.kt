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
    TimerPhase.IDLE -> PhasePresentation("READY", "Set up your brewer, then start")
    TimerPhase.BLOOM -> PhasePresentation("BLOOM", "Wet the grounds, then let them bloom")
    TimerPhase.POUR -> PhasePresentation("POUR NOW", "Keep the stream slow and even")
    TimerPhase.WAIT -> PhasePresentation("STOP POURING", "Hands off while the coffee drains")
}
