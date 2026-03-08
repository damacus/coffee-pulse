import test from 'node:test';
import assert from 'node:assert';
import { a } from './colorUtils.ts';

test('a function converts hex to rgba', async (t) => {
  await t.test('converts white', () => {
    assert.strictEqual(a('#ffffff', 1), 'rgba(255, 255, 255, 1)');
  });

  await t.test('converts black', () => {
    assert.strictEqual(a('#000000', 0), 'rgba(0, 0, 0, 0)');
  });

  await t.test('converts a specific color with decimal opacity', () => {
    assert.strictEqual(a('#b09070', 0.14), 'rgba(176, 144, 112, 0.14)');
  });

  await t.test('handles case insensitivity', () => {
    assert.strictEqual(a('#ABCDEF', 0.5), 'rgba(171, 205, 239, 0.5)');
    assert.strictEqual(a('#abcdef', 0.5), 'rgba(171, 205, 239, 0.5)');
  });

  await t.test('documents behavior with shorthand hex (it does NOT support it correctly)', () => {
    // Current implementation:
    // r = parseInt("#fff".slice(1, 3), 16) = parseInt("ff", 16) = 255
    // g = parseInt("#fff".slice(3, 5), 16) = parseInt("f", 16) = 15
    // b = parseInt("#fff".slice(5, 7), 16) = parseInt("", 16) = NaN
    const result = a('#fff', 1);
    assert.strictEqual(result, 'rgba(255, 15, NaN, 1)');
  });
});
