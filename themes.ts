import { TimerPhase } from './types';

// ─── Types ────────────────────────────────────────────────────────────────────

export interface ThemePhaseColors {
  ring: string;
  glow: string;
}

export interface Theme {
  id: string;
  name: string;
  bg: string;      // full-screen background
  surface: string; // modal / card background
  text: string;    // primary text (full-opacity hex)
  phases: Record<TimerPhase, ThemePhaseColors>;
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

/** Convert a hex colour + opacity to an rgba() string. */
export function a(hex: string, opacity: number): string {
  const r = parseInt(hex.slice(1, 3), 16);
  const g = parseInt(hex.slice(3, 5), 16);
  const b = parseInt(hex.slice(5, 7), 16);
  return `rgba(${r}, ${g}, ${b}, ${opacity})`;
}

function phase(ring: string, glowAlpha = 0.18): ThemePhaseColors {
  return { ring, glow: a(ring, glowAlpha) };
}

// ─── Phase semantics (labels/hints/animations — theme-independent) ────────────

export const PHASE_SEMANTIC: Record<TimerPhase, { label: string; hint: string; iconClass: string }> = {
  [TimerPhase.IDLE]:  { label: 'READY', hint: 'Begin your ritual',          iconClass: 'anim-hover'   },
  [TimerPhase.BLOOM]: { label: 'BLOOM', hint: 'Let the coffee degas',       iconClass: 'anim-breathe' },
  [TimerPhase.POUR]:  { label: 'POUR',  hint: 'Add water slowly & evenly',  iconClass: 'anim-drip'    },
  [TimerPhase.WAIT]:  { label: 'WAIT',  hint: 'Let it drain through',       iconClass: 'anim-breathe' },
};

// ─── Themes ───────────────────────────────────────────────────────────────────

export const THEMES: Theme[] = [
  {
    id: 'espresso',
    name: 'Espresso',
    bg: '#130b05',
    surface: '#1c0f07',
    text: '#f5e6c8',
    phases: {
      [TimerPhase.IDLE]:  phase('#b09070', 0.14),
      [TimerPhase.BLOOM]: phase('#88b4e0', 0.18),
      [TimerPhase.POUR]:  phase('#e8943a', 0.22),
      [TimerPhase.WAIT]:  phase('#6aae7e', 0.18),
    },
  },
  {
    id: 'slate',
    name: 'Slate',
    bg: '#0d1117',
    surface: '#161b22',
    text: '#c9d1d9',
    phases: {
      [TimerPhase.IDLE]:  phase('#8b9dc3', 0.14),
      [TimerPhase.BLOOM]: phase('#58a6ff', 0.2),
      [TimerPhase.POUR]:  phase('#3fb950', 0.2),
      [TimerPhase.WAIT]:  phase('#e3b341', 0.2),
    },
  },
  {
    id: 'matcha',
    name: 'Matcha',
    bg: '#0a0f0a',
    surface: '#111811',
    text: '#d4e8c4',
    phases: {
      [TimerPhase.IDLE]:  phase('#7aad7a', 0.14),
      [TimerPhase.BLOOM]: phase('#a8d4a8', 0.18),
      [TimerPhase.POUR]:  phase('#d4c850', 0.22),
      [TimerPhase.WAIT]:  phase('#5aaa8a', 0.18),
    },
  },
  {
    id: 'nightsky',
    name: 'Night Sky',
    bg: '#050810',
    surface: '#0c1020',
    text: '#e0e8f8',
    phases: {
      [TimerPhase.IDLE]:  phase('#7b8fc4', 0.14),
      [TimerPhase.BLOOM]: phase('#c084fc', 0.2),
      [TimerPhase.POUR]:  phase('#f472b6', 0.22),
      [TimerPhase.WAIT]:  phase('#38bdf8', 0.2),
    },
  },
];

export const DEFAULT_THEME_ID = 'espresso';
