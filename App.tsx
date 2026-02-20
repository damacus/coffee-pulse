import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Settings, Volume2, VolumeX, Play, RotateCcw, Coffee, Droplets, PauseCircle, Square } from 'lucide-react';
import { TimerPhase, AppConfig, TimerState } from './types';
import { THEMES, PHASE_SEMANTIC, DEFAULT_THEME_ID, a } from './themes';
import { audioService } from './services/audioService';
import { wakeLockService } from './services/wakeLockService';
import { SettingsModal } from './components/SettingsModal';

// ─── SVG ring constants ────────────────────────────────────────────────────────

const RING_RADIUS  = 108;
const CIRCUMFERENCE = 2 * Math.PI * RING_RADIUS;
const SVG_SIZE     = 280;
const CX = SVG_SIZE / 2;
const CY = SVG_SIZE / 2;

// ─── Tick marks ───────────────────────────────────────────────────────────────

function buildTicks(ringColor: string, textColor: string, isIdle: boolean) {
  return Array.from({ length: 60 }, (_, i) => {
    const angle   = (i * 6 - 90) * (Math.PI / 180);
    const isMajor = i % 5 === 0;
    const outerR  = 131;
    const innerR  = isMajor ? 121 : 127;
    const stroke  = isIdle
      ? (isMajor ? a(textColor, 0.22) : a(textColor, 0.07))
      : (isMajor ? a(ringColor, 0.8)  : a(ringColor, 0.28));
    return (
      <line
        key={i}
        x1={CX + outerR * Math.cos(angle)} y1={CY + outerR * Math.sin(angle)}
        x2={CX + innerR * Math.cos(angle)} y2={CY + innerR * Math.sin(angle)}
        stroke={stroke}
        strokeWidth={isMajor ? 1.5 : 1}
        strokeLinecap="round"
        style={{ transition: 'stroke 0.8s ease' }}
      />
    );
  });
}

// ─── App ──────────────────────────────────────────────────────────────────────

const App: React.FC = () => {
  // ── Config & theme ──────────────────────────────────────────────────────────
  const [config, setConfig] = useState<AppConfig>({
    bloomDuration: 30,
    pulseInterval: 5,
    isMuted: false,
    coffeeWeight: 15,
    waterRatio: 15.5,
    themeId: DEFAULT_THEME_ID,
  });

  const [isSettingsOpen, setIsSettingsOpen] = useState(false);

  const activeTheme = THEMES.find(t => t.id === config.themeId) ?? THEMES[0];
  const T = activeTheme.text; // shorthand for a(T, x) calls

  // ── Timer state ─────────────────────────────────────────────────────────────
  const [timerState, setTimerState] = useState<TimerState>({
    phase: TimerPhase.IDLE,
    totalTime: 0,
    phaseTimeRemaining: 0,
    isActive: false,
  });

  const intervalRef = useRef<number | null>(null);

  // ── Per-render derived styles (theme-aware, so must be inside component) ────
  const iconBtnStyle: React.CSSProperties = {
    width: 42, height: 42, borderRadius: '50%',
    background: a(T, 0.07),
    border: `1px solid ${a(T, 0.15)}`,
    color: a(T, 0.75),
    cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center',
    backdropFilter: 'blur(8px)', WebkitBackdropFilter: 'blur(8px)',
    outline: 'none', padding: 0, flexShrink: 0,
  };

  // ── Helpers ──────────────────────────────────────────────────────────────────
  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const toggleMute = () => {
    const newMuted = !config.isMuted;
    setConfig(prev => ({ ...prev, isMuted: newMuted }));
    audioService.setMute(newMuted);
  };

  const triggerHaptic = (pattern: number | number[]) => {
    if (typeof navigator !== 'undefined' && navigator.vibrate) {
      try { navigator.vibrate(pattern); } catch { /* ignore */ }
    }
  };

  // ── Timer actions ────────────────────────────────────────────────────────────
  const resetTimer = useCallback(() => {
    if (intervalRef.current) { window.clearInterval(intervalRef.current); intervalRef.current = null; }
    setTimerState({ phase: TimerPhase.IDLE, totalTime: 0, phaseTimeRemaining: config.bloomDuration, isActive: false });
    wakeLockService.release();
  }, [config.bloomDuration]);

  const startTimer = useCallback(async () => {
    await audioService.initialize();
    await wakeLockService.request();
    triggerHaptic(50);
    setTimerState(prev => prev.phase === TimerPhase.IDLE
      ? { ...prev, isActive: true, phase: TimerPhase.BLOOM, phaseTimeRemaining: config.bloomDuration }
      : { ...prev, isActive: true }
    );
  }, [config.bloomDuration]);

  const stopTimer = useCallback(() => {
    setTimerState(prev => ({ ...prev, isActive: false }));
    wakeLockService.release();
  }, []);

  const handleConfigSave = (newConfig: AppConfig) => {
    setConfig(newConfig);
    if (timerState.phase === TimerPhase.IDLE) {
      setTimerState(prev => ({ ...prev, phaseTimeRemaining: newConfig.bloomDuration }));
    }
  };

  const handleThemeChange = (id: string) => {
    setConfig(prev => ({ ...prev, themeId: id }));
  };

  // ── Heartbeat ────────────────────────────────────────────────────────────────
  useEffect(() => {
    if (timerState.isActive) {
      intervalRef.current = window.setInterval(() => {
        setTimerState(cur => {
          let { phase, phaseTimeRemaining, totalTime } = cur;
          phaseTimeRemaining -= 1;
          totalTime += 1;
          if (phaseTimeRemaining <= 0) {
            switch (phase) {
              case TimerPhase.BLOOM:
                phase = TimerPhase.POUR; phaseTimeRemaining = config.pulseInterval;
                audioService.playArpeggio(); triggerHaptic([300, 100, 300, 100, 300]); break;
              case TimerPhase.POUR:
                phase = TimerPhase.WAIT; phaseTimeRemaining = config.pulseInterval;
                audioService.playLowPing(); triggerHaptic(70); break;
              case TimerPhase.WAIT:
                phase = TimerPhase.POUR; phaseTimeRemaining = config.pulseInterval;
                audioService.playHighPing(); triggerHaptic([150, 50, 150]); break;
            }
          }
          return { ...cur, phase, phaseTimeRemaining, totalTime };
        });
      }, 1000);
    } else if (intervalRef.current) {
      window.clearInterval(intervalRef.current);
    }
    return () => { if (intervalRef.current) window.clearInterval(intervalRef.current); };
  }, [timerState.isActive, config.pulseInterval]);

  // ── Render computations ───────────────────────────────────────────────────────
  const phaseColors  = activeTheme.phases[timerState.phase];
  const phaseInfo    = PHASE_SEMANTIC[timerState.phase];
  const isIdle       = timerState.phase === TimerPhase.IDLE;

  const phaseDuration =
    timerState.phase === TimerPhase.BLOOM ? config.bloomDuration : config.pulseInterval;

  const displayTime = isIdle ? config.bloomDuration : timerState.phaseTimeRemaining;
  const progress    = isIdle ? 1 : timerState.phaseTimeRemaining / phaseDuration;
  const dashoffset  = CIRCUMFERENCE * (1 - progress);
  const totalWater  = Math.round(config.coffeeWeight * config.waterRatio);

  const getPhaseIcon = () => {
    const col = phaseColors.ring;
    switch (timerState.phase) {
      case TimerPhase.BLOOM: return <Coffee     size={20} color={col} />;
      case TimerPhase.POUR:  return <Droplets   size={20} color={col} />;
      case TimerPhase.WAIT:  return <PauseCircle size={20} color={col} />;
      default:               return <Coffee     size={20} color={col} />;
    }
  };

  const ticks = buildTicks(phaseColors.ring, T, isIdle);

  // ── JSX ───────────────────────────────────────────────────────────────────────
  return (
    <div style={{ position: 'fixed', inset: 0, background: activeTheme.bg, overflow: 'hidden', display: 'flex', flexDirection: 'column', transition: 'background 0.8s ease' }}>

      {/* Phase glow layers */}
      {THEMES.find(t => t.id === config.themeId) && (Object.keys(activeTheme.phases) as TimerPhase[]).map(p => (
        <div key={p} style={{
          position: 'fixed', inset: 0, pointerEvents: 'none', zIndex: 0,
          background: `radial-gradient(ellipse 65% 55% at 50% 44%, ${activeTheme.phases[p].glow} 0%, transparent 72%)`,
          opacity: timerState.phase === p ? 1 : 0,
          transition: 'opacity 0.9s ease',
        }} />
      ))}

      {/* Vignette */}
      <div style={{ position: 'fixed', inset: 0, pointerEvents: 'none', zIndex: 1, background: 'radial-gradient(ellipse at center, transparent 40%, rgba(0,0,0,0.65) 100%)' }} />

      {/* ── Header ── */}
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '22px 24px 10px', position: 'relative', zIndex: 20 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 9 }}>
          <Coffee size={15} color={a(T, 0.55)} />
          <span style={{ fontFamily: "'Manrope', sans-serif", fontSize: '11px', fontWeight: 600, letterSpacing: '0.28em', color: a(T, 0.55), textTransform: 'uppercase' }}>
            Coffee Pulse
          </span>
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          <button onClick={toggleMute} style={iconBtnStyle} className="btn-press" aria-label={config.isMuted ? 'Unmute' : 'Mute'}>
            {config.isMuted ? <VolumeX size={15} /> : <Volume2 size={15} />}
          </button>
          <button onClick={() => setIsSettingsOpen(true)} style={iconBtnStyle} className="btn-press" aria-label="Settings">
            <Settings size={15} />
          </button>
        </div>
      </header>

      {/* ── Main ── */}
      <main style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', position: 'relative', zIndex: 10, padding: '0 24px' }}>

        {/* Phase label */}
        <div key={timerState.phase + '-lbl'} className="phase-enter" style={{
          fontFamily: "'Manrope', sans-serif", fontSize: '10.5px', fontWeight: 700,
          letterSpacing: '0.38em', color: phaseColors.ring, textTransform: 'uppercase',
          marginBottom: 18, transition: 'color 0.8s ease',
        }}>
          {phaseInfo.label}
        </div>

        {/* Ring */}
        <div style={{ position: 'relative', width: SVG_SIZE, height: SVG_SIZE, flexShrink: 0 }}>
          <svg viewBox={`0 0 ${SVG_SIZE} ${SVG_SIZE}`} style={{ width: '100%', height: '100%', overflow: 'visible' }} aria-hidden>
            <defs>
              <filter id="arc-glow" x="-30%" y="-30%" width="160%" height="160%">
                <feGaussianBlur stdDeviation="4.5" result="blur" />
                <feMerge><feMergeNode in="blur" /><feMergeNode in="SourceGraphic" /></feMerge>
              </filter>
            </defs>

            {ticks}

            {/* Track */}
            <circle cx={CX} cy={CY} r={RING_RADIUS} fill="none" stroke={a(T, 0.06)} strokeWidth={8} />

            {/* Progress arc */}
            <circle
              cx={CX} cy={CY} r={RING_RADIUS}
              fill="none" stroke={phaseColors.ring} strokeWidth={8} strokeLinecap="round"
              strokeDasharray={CIRCUMFERENCE} strokeDashoffset={dashoffset}
              transform={`rotate(-90 ${CX} ${CY})`} filter="url(#arc-glow)"
              style={{ transition: 'stroke-dashoffset 0.88s linear, stroke 0.8s ease' }}
            />

            {/* Centre dot */}
            <circle cx={CX} cy={CY} r={3.5} fill={phaseColors.ring} style={{ opacity: 0.45, transition: 'fill 0.8s ease' }} />
          </svg>

          {/* Centre overlay */}
          <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
            <div className={phaseInfo.iconClass} style={{ marginBottom: 6 }}>
              {getPhaseIcon()}
            </div>
            <div style={{
              fontFamily: "'Bebas Neue', sans-serif",
              fontSize: '96px', fontWeight: 400, lineHeight: 1, letterSpacing: '4px',
              color: activeTheme.text,
              textShadow: `0 0 45px ${a(phaseColors.ring, 0.5)}`,
              transition: 'text-shadow 0.8s ease, color 0.8s ease',
              userSelect: 'none',
            }}>
              {displayTime}
            </div>
            <div style={{
              fontFamily: "'Manrope', sans-serif", fontSize: '9.5px', fontWeight: 500,
              letterSpacing: '0.26em', color: a(T, 0.55), textTransform: 'uppercase', marginTop: 5,
            }}>
              seconds
            </div>
          </div>
        </div>

        {/* Phase hint */}
        <div key={timerState.phase + '-hint'} className="phase-enter" style={{
          fontFamily: "'Manrope', sans-serif", fontSize: '12px', fontWeight: 400,
          letterSpacing: '0.04em', color: a(T, 0.7), marginTop: 14, textAlign: 'center',
        }}>
          {phaseInfo.hint}
        </div>

        {/* Brew stats */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 28, marginTop: 18 }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontFamily: "'Manrope', sans-serif", fontSize: '9px', fontWeight: 600, letterSpacing: '0.22em', color: a(T, 0.6), textTransform: 'uppercase', marginBottom: 4 }}>Coffee</div>
            <div style={{ fontFamily: "'Bebas Neue', sans-serif", fontSize: '28px', fontWeight: 400, color: activeTheme.text, letterSpacing: '2px', transition: 'color 0.8s ease' }}>
              {config.coffeeWeight}<span style={{ fontFamily: "'Manrope', sans-serif", fontSize: '12px', color: a(T, 0.55), marginLeft: 2, fontWeight: 400 }}>g</span>
            </div>
          </div>
          <div style={{ width: 1, height: 30, background: a(T, 0.15) }} />
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontFamily: "'Manrope', sans-serif", fontSize: '9px', fontWeight: 600, letterSpacing: '0.22em', color: a(T, 0.6), textTransform: 'uppercase', marginBottom: 4 }}>Water</div>
            <div style={{ fontFamily: "'Bebas Neue', sans-serif", fontSize: '28px', fontWeight: 400, color: activeTheme.text, letterSpacing: '2px', transition: 'color 0.8s ease' }}>
              {totalWater}<span style={{ fontFamily: "'Manrope', sans-serif", fontSize: '12px', color: a(T, 0.55), marginLeft: 2, fontWeight: 400 }}>g</span>
            </div>
          </div>
        </div>

        {/* Total elapsed */}
        {!isIdle && (
          <div style={{ fontFamily: "'Manrope', sans-serif", fontSize: '10.5px', fontWeight: 500, letterSpacing: '0.18em', color: a(T, 0.55), marginTop: 10, textTransform: 'uppercase' }}>
            Total — {formatTime(timerState.totalTime)}
          </div>
        )}
      </main>

      {/* ── Control Deck ── */}
      <footer style={{
        position: 'relative', zIndex: 20, padding: '18px 28px 44px',
        background: 'rgba(0,0,0,0.28)', backdropFilter: 'blur(24px)', WebkitBackdropFilter: 'blur(24px)',
        borderTop: `1px solid ${a(T, 0.06)}`,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 14, maxWidth: 380, margin: '0 auto' }}>

          {/* Reset */}
          {!isIdle && (
            <button onClick={resetTimer} aria-label="Reset Timer" className="btn-press" style={{
              width: 50, height: 50, borderRadius: '50%',
              background: a(T, 0.07), border: `1px solid ${a(T, 0.13)}`,
              color: a(T, 0.75), cursor: 'pointer',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              flexShrink: 0, outline: 'none',
            }}>
              <RotateCcw size={18} />
            </button>
          )}

          {/* Start / Stop */}
          {!timerState.isActive ? (
            <button onClick={startTimer} aria-label="Start Timer" className="btn-press" style={{
              flex: 1, height: 58, borderRadius: 29,
              background: activeTheme.text, border: 'none', color: activeTheme.bg,
              cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10,
              fontFamily: "'Manrope', sans-serif", fontSize: '13px', fontWeight: 700,
              letterSpacing: '0.18em', textTransform: 'uppercase',
              boxShadow: `0 4px 28px ${a(activeTheme.text, 0.14)}`,
              outline: 'none', transition: 'background 0.8s ease, color 0.8s ease, box-shadow 0.8s ease',
            }}>
              <Play size={16} fill={activeTheme.bg} />
              {isIdle ? 'Start Brew' : 'Resume'}
            </button>
          ) : (
            <button onClick={stopTimer} aria-label="Pause Timer" className="btn-press" style={{
              flex: 1, height: 58, borderRadius: 29,
              background: 'rgba(175, 58, 48, 0.72)', border: '1px solid rgba(210, 90, 78, 0.38)',
              backdropFilter: 'blur(8px)', WebkitBackdropFilter: 'blur(8px)',
              color: 'rgba(255,218,208,0.94)', cursor: 'pointer',
              display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 10,
              fontFamily: "'Manrope', sans-serif", fontSize: '13px', fontWeight: 700,
              letterSpacing: '0.18em', textTransform: 'uppercase', outline: 'none',
            }}>
              <Square size={14} fill="rgba(255,218,208,0.94)" />
              Pause
            </button>
          )}
        </div>
      </footer>

      {/* ── Settings ── */}
      <SettingsModal
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        config={config}
        onSave={handleConfigSave}
        theme={activeTheme}
        themes={THEMES}
        onThemeChange={handleThemeChange}
      />
    </div>
  );
};

export default App;
