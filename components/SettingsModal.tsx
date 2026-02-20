import React, { useState } from 'react';
import { X, Save, Calculator, Timer, Coffee, Droplets, Palette } from 'lucide-react';
import { AppConfig, TimerPhase } from '../types';
import { Theme, a } from '../themes';

interface SettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  config: AppConfig;
  onSave: (newConfig: AppConfig) => void;
  theme: Theme;
  themes: Theme[];
  onThemeChange: (id: string) => void;
}

const FONT_DISPLAY = "'Bebas Neue', sans-serif";
const FONT_LABEL   = "'Manrope', sans-serif";

// ─── Style helpers (theme-aware) ──────────────────────────────────────────────

const sectionLabel = (color: string): React.CSSProperties => ({
  fontFamily: FONT_LABEL, fontSize: '9px', fontWeight: 700,
  letterSpacing: '0.32em', textTransform: 'uppercase', color,
  display: 'flex', alignItems: 'center', gap: 6, marginBottom: 14,
});

const fieldLabel = (t: Theme): React.CSSProperties => ({
  fontFamily: FONT_LABEL, fontSize: '9px', fontWeight: 600,
  letterSpacing: '0.24em', textTransform: 'uppercase',
  color: a(t.text, 0.65), display: 'block', marginBottom: 7,
});

const inputStyle = (t: Theme): React.CSSProperties => ({
  width: '100%',
  background: a(t.text, 0.04),
  border: `1px solid ${a(t.text, 0.12)}`,
  borderRadius: 10, padding: '12px 14px',
  color: t.text, fontFamily: FONT_DISPLAY,
  fontSize: '26px', fontWeight: 400, letterSpacing: '2px',
  outline: 'none', appearance: 'none', WebkitAppearance: 'none',
  transition: 'border-color 0.2s, box-shadow 0.2s',
});

const hintText = (t: Theme): React.CSSProperties => ({
  fontFamily: FONT_LABEL, fontSize: '10px', fontWeight: 400,
  letterSpacing: '0.04em', color: a(t.text, 0.55), marginTop: 6,
});

// ─── Component ────────────────────────────────────────────────────────────────

export const SettingsModal: React.FC<SettingsModalProps> = ({
  isOpen, onClose, config, onSave, theme, themes, onThemeChange,
}) => {
  const [local, setLocal] = useState<AppConfig>(config);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleChange = (field: keyof AppConfig, value: string) => {
    const num = parseFloat(value);
    setLocal(prev => ({ ...prev, [field]: isNaN(num) ? 0 : num }));
    setError(null);
  };

  const handleSave = () => {
    if (local.bloomDuration < 1 || local.pulseInterval < 1) {
      setError('Durations must be at least 1 second.');
      return;
    }
    if (local.coffeeWeight <= 0 || local.waterRatio <= 0) {
      setError('Coffee weight and ratio must be positive.');
      return;
    }
    setError(null);
    onSave(local);
    onClose();
  };

  const selectTheme = (id: string) => {
    onThemeChange(id);
  };

  const totalWater = Math.round(local.coffeeWeight * local.waterRatio);
  const bloomWater = Math.round(local.coffeeWeight * 2);
  const mainPour   = totalWater - bloomWater;

  const bloomRing = theme.phases[TimerPhase.BLOOM].ring;
  const waitRing  = theme.phases[TimerPhase.WAIT].ring;

  return (
    <div
      className="modal-overlay"
      style={{
        position: 'fixed', inset: 0, zIndex: 50,
        display: 'flex', alignItems: 'flex-end', justifyContent: 'center',
        background: 'rgba(0,0,0,0.82)',
        backdropFilter: 'blur(6px)', WebkitBackdropFilter: 'blur(6px)',
      }}
      onClick={e => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div
        className="modal-sheet modal-scroll"
        style={{
          width: '100%', maxWidth: 480, maxHeight: '88vh', overflowY: 'auto',
          background: theme.surface,
          borderTop: `1px solid ${a(theme.text, 0.08)}`,
          borderRadius: '24px 24px 0 0',
          padding: '0 0 48px',
          transition: 'background 0.4s ease',
        }}
      >
        {/* Drag handle */}
        <div style={{ display: 'flex', justifyContent: 'center', padding: '12px 0 0' }}>
          <div style={{ width: 36, height: 3.5, borderRadius: 2, background: a(theme.text, 0.15) }} />
        </div>

        {/* Header */}
        <div style={{
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
          padding: '16px 24px 18px',
          borderBottom: `1px solid ${a(theme.text, 0.07)}`,
        }}>
          <span style={{ fontFamily: FONT_LABEL, fontSize: '11px', fontWeight: 700, letterSpacing: '0.28em', color: a(theme.text, 0.8), textTransform: 'uppercase' }}>
            Settings
          </span>
          <button onClick={onClose} className="btn-press" aria-label="Close settings" style={{
            width: 34, height: 34, borderRadius: '50%',
            background: a(theme.text, 0.06), border: `1px solid ${a(theme.text, 0.1)}`,
            color: a(theme.text, 0.7), cursor: 'pointer',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            outline: 'none', padding: 0,
          }}>
            <X size={15} />
          </button>
        </div>

        <div style={{ padding: '22px 24px 0' }}>

          {/* Error */}
          {error && (
            <div style={{
              padding: '10px 14px', marginBottom: 18,
              background: 'rgba(180,58,48,0.18)', border: '1px solid rgba(200,80,70,0.3)',
              borderRadius: 10, fontFamily: FONT_LABEL, fontSize: '11px',
              color: 'rgba(255,210,200,0.85)', letterSpacing: '0.04em',
            }}>
              {error}
            </div>
          )}

          {/* ── Appearance ── */}
          <div style={{ marginBottom: 24 }}>
            <div style={sectionLabel(a(theme.text, 0.75))}>
              <Palette size={12} />
              Appearance
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 10 }}>
              {themes.map(t => {
                const active = t.id === config.themeId;
                return (
                  <button
                    key={t.id}
                    onClick={() => selectTheme(t.id)}
                    className="btn-press"
                    style={{
                      display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 8,
                      background: 'transparent', border: 'none', cursor: 'pointer', outline: 'none', padding: 4,
                    }}
                  >
                    {/* Swatch circle */}
                    <div style={{
                      width: 48, height: 48, borderRadius: '50%',
                      background: t.bg,
                      border: `2.5px solid ${active ? t.phases[TimerPhase.POUR].ring : a(theme.text, 0.18)}`,
                      boxShadow: active ? `0 0 14px ${a(t.phases[TimerPhase.POUR].ring, 0.55)}` : 'none',
                      display: 'flex', alignItems: 'center', justifyContent: 'center',
                      transition: 'border-color 0.2s, box-shadow 0.2s',
                      position: 'relative', overflow: 'hidden',
                    }}>
                      {/* Phase colour dots arranged in a 2×2 grid */}
                      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 3 }}>
                        {([TimerPhase.BLOOM, TimerPhase.POUR, TimerPhase.WAIT, TimerPhase.IDLE] as TimerPhase[]).map(p => (
                          <div key={p} style={{
                            width: 7, height: 7, borderRadius: '50%',
                            background: t.phases[p].ring,
                            opacity: 0.9,
                          }} />
                        ))}
                      </div>
                    </div>
                    {/* Label */}
                    <span style={{
                      fontFamily: FONT_LABEL, fontSize: '8.5px',
                      fontWeight: active ? 700 : 400,
                      letterSpacing: '0.1em', textTransform: 'uppercase',
                      color: active ? a(theme.text, 0.9) : a(theme.text, 0.45),
                      transition: 'color 0.2s, font-weight 0.2s',
                    }}>
                      {t.name}
                    </span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* ── Brew Calculator ── */}
          <div style={{
            background: a(theme.text, 0.03),
            border: `1px solid ${a(theme.text, 0.07)}`,
            borderRadius: 16, padding: 18, marginBottom: 22,
          }}>
            <div style={sectionLabel(a(theme.phases[TimerPhase.POUR].ring, 0.9))}>
              <Calculator size={12} />
              Brew Calculator
            </div>

            {/* Coffee presets */}
            <div style={{ marginBottom: 12 }}>
              <label style={fieldLabel(theme)}>Coffee (g)</label>
              <div style={{ display: 'flex', gap: 7, marginBottom: 8 }}>
                {[15, 30, 45, 60, 75].map(g => {
                  const active = local.coffeeWeight === g;
                  return (
                    <button
                      key={g}
                      onClick={() => { setLocal(prev => ({ ...prev, coffeeWeight: g })); setError(null); }}
                      className="btn-press"
                      style={{
                        flex: 1, height: 34, borderRadius: 8,
                        background: active ? a(theme.phases[TimerPhase.POUR].ring, 0.2) : a(theme.text, 0.04),
                        border: `1px solid ${active ? a(theme.phases[TimerPhase.POUR].ring, 0.5) : a(theme.text, 0.1)}`,
                        color: active ? a(theme.phases[TimerPhase.POUR].ring, 1) : a(theme.text, 0.5),
                        fontFamily: FONT_LABEL, fontSize: '11px',
                        fontWeight: active ? 700 : 400,
                        cursor: 'pointer', outline: 'none', transition: 'all 0.18s',
                      }}
                    >
                      {g}g
                    </button>
                  );
                })}
              </div>
              <input
                type="number" min="1" step="0.1"
                value={local.coffeeWeight || ''}
                onChange={e => handleChange('coffeeWeight', e.target.value)}
                style={inputStyle(theme)}
              />
            </div>

            {/* Ratio */}
            <div style={{ marginBottom: 18 }}>
              <label style={fieldLabel(theme)}>Ratio (1:?)</label>
              <input
                type="number" min="1" step="0.1"
                value={local.waterRatio || ''}
                onChange={e => handleChange('waterRatio', e.target.value)}
                style={inputStyle(theme)}
              />
            </div>

            {/* Total water */}
            <div style={{
              display: 'flex', justifyContent: 'space-between', alignItems: 'baseline',
              paddingBottom: 14, marginBottom: 14,
              borderBottom: `1px solid ${a(theme.text, 0.07)}`,
            }}>
              <span style={{ fontFamily: FONT_LABEL, fontSize: '9.5px', fontWeight: 600, letterSpacing: '0.2em', color: a(theme.text, 0.65), textTransform: 'uppercase' }}>
                Total Water
              </span>
              <span>
                <span style={{ fontFamily: FONT_DISPLAY, fontSize: '32px', fontWeight: 400, color: theme.text, letterSpacing: '2px' }}>{totalWater}</span>
                <span style={{ fontFamily: FONT_LABEL, fontSize: '12px', color: a(theme.text, 0.55), marginLeft: 3 }}>g</span>
              </span>
            </div>

            {/* Phase cards */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10 }}>
              {/* Bloom */}
              <div style={{ background: a(bloomRing, 0.08), border: `1px solid ${a(bloomRing, 0.18)}`, borderRadius: 12, padding: '12px 14px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginBottom: 8 }}>
                  <Droplets size={11} color={a(bloomRing, 0.8)} />
                  <span style={{ fontFamily: FONT_LABEL, fontSize: '8px', fontWeight: 700, letterSpacing: '0.28em', color: a(bloomRing, 0.9), textTransform: 'uppercase' }}>1 · Bloom</span>
                </div>
                <div style={{ fontFamily: FONT_DISPLAY, fontSize: '26px', fontWeight: 400, color: a(bloomRing, 0.95), letterSpacing: '1px' }}>
                  {bloomWater}<span style={{ fontFamily: FONT_LABEL, fontSize: '11px', color: a(bloomRing, 0.65), marginLeft: 2 }}>g</span>
                </div>
                <div style={{ fontFamily: FONT_LABEL, fontSize: '9.5px', color: a(bloomRing, 0.6), marginTop: 5, letterSpacing: '0.04em', lineHeight: 1.4 }}>
                  Initial pour to degas
                </div>
              </div>

              {/* Main pour */}
              <div style={{ background: a(waitRing, 0.08), border: `1px solid ${a(waitRing, 0.18)}`, borderRadius: 12, padding: '12px 14px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 5, marginBottom: 8 }}>
                  <Coffee size={11} color={a(waitRing, 0.8)} />
                  <span style={{ fontFamily: FONT_LABEL, fontSize: '8px', fontWeight: 700, letterSpacing: '0.28em', color: a(waitRing, 0.9), textTransform: 'uppercase' }}>2 · Main</span>
                </div>
                <div style={{ fontFamily: FONT_DISPLAY, fontSize: '26px', fontWeight: 400, color: a(waitRing, 0.95), letterSpacing: '1px' }}>
                  {mainPour}<span style={{ fontFamily: FONT_LABEL, fontSize: '11px', color: a(waitRing, 0.65), marginLeft: 2 }}>g</span>
                </div>
                <div style={{ fontFamily: FONT_LABEL, fontSize: '9.5px', color: a(waitRing, 0.6), marginTop: 5, letterSpacing: '0.04em', lineHeight: 1.4 }}>
                  Remaining in pulses
                </div>
              </div>
            </div>
          </div>

          {/* ── Timer Configuration ── */}
          <div style={{ marginBottom: 24 }}>
            <div style={sectionLabel(a(theme.phases[TimerPhase.BLOOM].ring, 0.9))}>
              <Timer size={12} />
              Timer Configuration
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
              <div>
                <label style={fieldLabel(theme)}>Bloom Duration</label>
                <input
                  type="number" min="1" max="120"
                  value={local.bloomDuration || ''}
                  onChange={e => handleChange('bloomDuration', e.target.value)}
                  style={inputStyle(theme)}
                />
                <p style={hintText(theme)}>Seconds to degas before pouring begins</p>
              </div>
              <div>
                <label style={fieldLabel(theme)}>Pulse Interval</label>
                <input
                  type="number" min="1" max="60"
                  value={local.pulseInterval || ''}
                  onChange={e => handleChange('pulseInterval', e.target.value)}
                  style={inputStyle(theme)}
                />
                <p style={hintText(theme)}>Duration of each pour & wait phase</p>
              </div>
            </div>
          </div>

          {/* Save */}
          <button
            onClick={handleSave}
            className="btn-press"
            style={{
              width: '100%', height: 54, borderRadius: 27,
              background: theme.text, color: theme.bg, border: 'none',
              fontFamily: FONT_LABEL, fontSize: '13px', fontWeight: 700,
              letterSpacing: '0.18em', textTransform: 'uppercase',
              cursor: 'pointer', outline: 'none',
              display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
              transition: 'background 0.4s ease, color 0.4s ease',
            }}
          >
            <Save size={16} />
            Save Configuration
          </button>
        </div>
      </div>
    </div>
  );
};
