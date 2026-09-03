package com.damacus.coffeepulse.domain.model

data class BrewPreset(
    val id: String,
    val name: String,
    val brewerType: String,
    val description: String,
    val grindGuide: String,
    val config: BrewConfig,
) {
    companion object {
        val ALL_BREWER_TYPES = listOf("All", "V60", "Chemex", "AeroPress", "Kalita / Flatbed")

        val DEFAULT_PRESETS = listOf(
            // V60 Profiles
            BrewPreset(
                id = "hoffmann_v60",
                name = "James Hoffmann Better 1-Cup V60",
                brewerType = "V60",
                description = "45s bloom (3x water), gentle swirls, with steady 10s pour pulses.",
                grindGuide = "Medium-Fine",
                config = BrewConfig(
                    bloomSeconds = 45,
                    pulseIntervalSeconds = 10,
                    coffeeGrams = 15.0,
                    waterRatio = 16.6,
                ),
            ),
            BrewPreset(
                id = "kasuya_4_6",
                name = "Tetsu Kasuya 4:6 Method",
                brewerType = "V60",
                description = "Coarse grind, 45s bloom, followed by 4 distinct pours at 45s intervals (structure mapped to 15s pulses).",
                grindGuide = "Coarse",
                config = BrewConfig(
                    bloomSeconds = 45,
                    pulseIntervalSeconds = 15,
                    coffeeGrams = 20.0,
                    waterRatio = 15.0,
                ),
            ),
            BrewPreset(
                id = "v60_rao_single_pour",
                name = "Scott Rao V60 High Extraction",
                brewerType = "V60",
                description = "45s bloom with excavation, followed by continuous gentle center-to-edge pour.",
                grindGuide = "Medium-Fine",
                config = BrewConfig(
                    bloomSeconds = 45,
                    pulseIntervalSeconds = 8,
                    coffeeGrams = 18.0,
                    waterRatio = 16.6,
                ),
            ),
            BrewPreset(
                id = "v60_quick_cup",
                name = "Everyday Single Cup V60",
                brewerType = "V60",
                description = "Brisk 30s bloom with 6s active pour/wait pulses for a vibrant morning cup.",
                grindGuide = "Medium",
                config = BrewConfig(
                    bloomSeconds = 30,
                    pulseIntervalSeconds = 6,
                    coffeeGrams = 15.0,
                    waterRatio = 15.5,
                ),
            ),

            // Chemex Profiles
            BrewPreset(
                id = "chemex_classic_batch",
                name = "Chemex Classic 3-Cup / Batch",
                brewerType = "Chemex",
                description = "Thick bonded filter profile. 45s bloom, followed by wide circular pours to keep the slurry hot.",
                grindGuide = "Medium-Coarse",
                config = BrewConfig(
                    bloomSeconds = 45,
                    pulseIntervalSeconds = 20,
                    coffeeGrams = 30.0,
                    waterRatio = 16.0,
                ),
            ),
            BrewPreset(
                id = "chemex_hoffmann",
                name = "Hoffmann Chemex Technique",
                brewerType = "Chemex",
                description = "Extended 60s bloom to ensure full saturation through thick Chemex paper, followed by two main phases.",
                grindGuide = "Medium-Coarse",
                config = BrewConfig(
                    bloomSeconds = 60,
                    pulseIntervalSeconds = 15,
                    coffeeGrams = 32.0,
                    waterRatio = 16.0,
                ),
            ),
            BrewPreset(
                id = "chemex_single",
                name = "Chemex Single Serving",
                brewerType = "Chemex",
                description = "Lighter Chemex dose (20g) with 45s bloom and controlled 12s pulses.",
                grindGuide = "Medium",
                config = BrewConfig(
                    bloomSeconds = 45,
                    pulseIntervalSeconds = 12,
                    coffeeGrams = 20.0,
                    waterRatio = 15.5,
                ),
            ),

            // AeroPress Profiles
            BrewPreset(
                id = "aeropress_inverted_champion",
                name = "World AeroPress Champion Inverted",
                brewerType = "AeroPress",
                description = "Inverted position: 30s bloom, stir vigorously, steep, then flip and gently press for 30s.",
                grindGuide = "Medium-Coarse",
                config = BrewConfig(
                    bloomSeconds = 30,
                    pulseIntervalSeconds = 30,
                    coffeeGrams = 18.0,
                    waterRatio = 12.0,
                ),
            ),
            BrewPreset(
                id = "aeropress_hoffmann_standard",
                name = "James Hoffmann AeroPress",
                brewerType = "AeroPress",
                description = "Standard method: pour all water in 10s, insert plunger to create seal, 2-minute steep, then press.",
                grindGuide = "Fine",
                config = BrewConfig(
                    bloomSeconds = 15,
                    pulseIntervalSeconds = 30,
                    coffeeGrams = 11.0,
                    waterRatio = 18.0,
                ),
            ),
            BrewPreset(
                id = "aeropress_espresso_style",
                name = "AeroPress Concentrate / Americano",
                brewerType = "AeroPress",
                description = "Short, dense espresso-strength brew for lattes, flat whites, or bypassing with hot water.",
                grindGuide = "Fine",
                config = BrewConfig(
                    bloomSeconds = 20,
                    pulseIntervalSeconds = 25,
                    coffeeGrams = 20.0,
                    waterRatio = 5.0,
                ),
            ),

            // Flatbed / Kalita Wave Profiles
            BrewPreset(
                id = "kalita_wave_classic",
                name = "Kalita Wave 3-Pulse",
                brewerType = "Kalita / Flatbed",
                description = "Flat bottom dripper with 30s bloom, followed by 3 structured pulses at 10s intervals.",
                grindGuide = "Medium",
                config = BrewConfig(
                    bloomSeconds = 30,
                    pulseIntervalSeconds = 10,
                    coffeeGrams = 16.0,
                    waterRatio = 15.5,
                ),
            ),
        )
    }
}
