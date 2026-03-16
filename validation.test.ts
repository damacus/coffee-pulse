import test from 'node:test';
import assert from 'node:assert';
import { validateConfig, MAX_BLOOM_DURATION, MAX_PULSE_INTERVAL, MAX_COFFEE_WEIGHT, MAX_WATER_RATIO } from './services/validation.ts';

const baseConfig: any = {
  bloomDuration: 30,
  pulseInterval: 5,
  isMuted: false,
  coffeeWeight: 15,
  waterRatio: 15.5,
  themeId: 'default',
};

test('validateConfig returns null for valid config', () => {
  assert.strictEqual(validateConfig(baseConfig), null);
});

test('validateConfig checks bloomDuration lower bound', () => {
  const config = { ...baseConfig, bloomDuration: 0 };
  assert.ok(validateConfig(config)?.includes('Bloom duration must be between 1'));
});

test('validateConfig checks bloomDuration upper bound', () => {
  const config = { ...baseConfig, bloomDuration: MAX_BLOOM_DURATION + 1 };
  assert.ok(validateConfig(config)?.includes(`and ${MAX_BLOOM_DURATION} seconds`));
});

test('validateConfig checks pulseInterval lower bound', () => {
  const config = { ...baseConfig, pulseInterval: 0 };
  assert.ok(validateConfig(config)?.includes('Pulse interval must be between 1'));
});

test('validateConfig checks pulseInterval upper bound', () => {
  const config = { ...baseConfig, pulseInterval: MAX_PULSE_INTERVAL + 1 };
  assert.ok(validateConfig(config)?.includes(`and ${MAX_PULSE_INTERVAL} seconds`));
});

test('validateConfig checks coffeeWeight lower bound', () => {
  const config = { ...baseConfig, coffeeWeight: 0 };
  assert.ok(validateConfig(config)?.includes('Coffee weight must be between 0.1'));
});

test('validateConfig checks coffeeWeight upper bound', () => {
  const config = { ...baseConfig, coffeeWeight: MAX_COFFEE_WEIGHT + 1 };
  assert.ok(validateConfig(config)?.includes(`and ${MAX_COFFEE_WEIGHT}g`));
});

test('validateConfig checks waterRatio lower bound', () => {
  const config = { ...baseConfig, waterRatio: 0 };
  assert.ok(validateConfig(config)?.includes('Water ratio must be between 0.1'));
});

test('validateConfig checks waterRatio upper bound', () => {
  const config = { ...baseConfig, waterRatio: MAX_WATER_RATIO + 1 };
  assert.ok(validateConfig(config)?.includes(`and ${MAX_WATER_RATIO}`));
});
