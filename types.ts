export const TimerPhase = {
  IDLE: 'IDLE',
  BLOOM: 'BLOOM',
  POUR: 'POUR',
  WAIT: 'WAIT',
} as const;

export type TimerPhase = typeof TimerPhase[keyof typeof TimerPhase];

export interface AppConfig {
  bloomDuration: number;
  pulseInterval: number;
  isMuted: boolean;
  coffeeWeight: number; // grams
  waterRatio: number; // water per gram of coffee
  themeId: string;
}

export interface TimerState {
  phase: TimerPhase;
  totalTime: number; // in seconds
  phaseTimeRemaining: number; // in seconds
  isActive: boolean;
}