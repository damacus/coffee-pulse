export enum TimerPhase {
  IDLE = 'IDLE',
  BLOOM = 'BLOOM',
  POUR = 'POUR',
  WAIT = 'WAIT',
}

export interface AppConfig {
  bloomDuration: number;
  pulseInterval: number;
  isMuted: boolean;
  coffeeWeight: number; // grams
  waterRatio: number; // water per gram of coffee
}

export interface TimerState {
  phase: TimerPhase;
  totalTime: number; // in seconds
  phaseTimeRemaining: number; // in seconds
  isActive: boolean;
}