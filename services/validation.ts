export const MAX_BLOOM_DURATION = 120;
export const MAX_PULSE_INTERVAL = 60;
export const MAX_COFFEE_WEIGHT = 1000;
export const MAX_WATER_RATIO = 100;

export function validateConfig(config: any): string | null {
  if (config.bloomDuration < 1 || config.bloomDuration > MAX_BLOOM_DURATION) {
    return `Bloom duration must be between 1 and ${MAX_BLOOM_DURATION} seconds.`;
  }
  if (config.pulseInterval < 1 || config.pulseInterval > MAX_PULSE_INTERVAL) {
    return `Pulse interval must be between 1 and ${MAX_PULSE_INTERVAL} seconds.`;
  }
  if (config.coffeeWeight <= 0 || config.coffeeWeight > MAX_COFFEE_WEIGHT) {
    return `Coffee weight must be between 0.1 and ${MAX_COFFEE_WEIGHT}g.`;
  }
  if (config.waterRatio <= 0 || config.waterRatio > MAX_WATER_RATIO) {
    return `Water ratio must be between 0.1 and ${MAX_WATER_RATIO}.`;
  }
  return null;
}
