// Singleton Audio Service using Web Audio API

class AudioService {
  private context: AudioContext | null = null;
  private gainNode: GainNode | null = null;
  private isMuted: boolean = false;

  constructor() {
    // Context is initialized on user interaction
  }

  public setMute(muted: boolean) {
    this.isMuted = muted;
  }

  public async initialize() {
    if (!this.context) {
      const AudioContextClass = window.AudioContext || (window as any).webkitAudioContext;
      this.context = new AudioContextClass();
      this.gainNode = this.context.createGain();
      this.gainNode.connect(this.context.destination);
    }
    
    if (this.context.state === 'suspended') {
      await this.context.resume();
    }
  }

  private playTone(freq: number, type: OscillatorType, duration: number, startTime: number, volume: number = 0.1) {
    if (this.isMuted || !this.context || !this.gainNode) return;

    const osc = this.context.createOscillator();
    const noteGain = this.context.createGain();

    osc.type = type;
    osc.frequency.setValueAtTime(freq, startTime);

    noteGain.gain.setValueAtTime(0, startTime);
    noteGain.gain.linearRampToValueAtTime(volume, startTime + 0.05);
    noteGain.gain.exponentialRampToValueAtTime(0.001, startTime + duration);

    osc.connect(noteGain);
    noteGain.connect(this.gainNode);

    osc.start(startTime);
    osc.stop(startTime + duration);
  }

  public playArpeggio() {
    if (this.isMuted || !this.context) return;
    const now = this.context.currentTime;
    // C Major Arpeggio: C4, E4, G4, C5 - Crisp and clear
    this.playTone(261.63, 'sine', 0.5, now, 0.2);      // C4
    this.playTone(329.63, 'sine', 0.5, now + 0.12, 0.2); // E4
    this.playTone(392.00, 'sine', 0.5, now + 0.24, 0.2);  // G4
    this.playTone(523.25, 'sine', 1.0, now + 0.36, 0.15); // C5
  }

  public playHighPing() {
    if (this.isMuted || !this.context) return;
    const now = this.context.currentTime;
    // High alert for POUR (A5)
    this.playTone(880, 'sine', 0.6, now, 0.15);
  }

  public playLowPing() {
    if (this.isMuted || !this.context) return;
    const now = this.context.currentTime;
    // Lower tone for WAIT (A4)
    this.playTone(440, 'sine', 0.8, now, 0.15);
  }
}

export const audioService = new AudioService();
