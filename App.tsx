import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Settings, Volume2, VolumeX, Play, RotateCcw, Coffee, Droplets, PauseCircle, Square } from 'lucide-react';
import { TimerPhase, AppConfig, TimerState } from './types';
import { audioService } from './services/audioService';
import { wakeLockService } from './services/wakeLockService';
import { Button } from './components/Button';
import { SettingsModal } from './components/SettingsModal';

const App: React.FC = () => {
  // --- Configuration State ---
  const [config, setConfig] = useState<AppConfig>({
    bloomDuration: 30,
    pulseInterval: 5,
    isMuted: false,
    coffeeWeight: 20,
    waterRatio: 15.5,
  });

  const [isSettingsOpen, setIsSettingsOpen] = useState(false);

  // --- Timer State ---
  const [timerState, setTimerState] = useState<TimerState>({
    phase: TimerPhase.IDLE,
    totalTime: 0,
    phaseTimeRemaining: 0,
    isActive: false,
  });

  // Refs for interval management
  const intervalRef = useRef<number | null>(null);

  // --- Helper: Format Time ---
  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const getPhaseTargetDuration = () => {
    if (timerState.phase === TimerPhase.BLOOM) return config.bloomDuration;
    if (timerState.phase === TimerPhase.POUR || timerState.phase === TimerPhase.WAIT) return config.pulseInterval;
    return 0;
  };

  // --- Audio Toggle ---
  const toggleMute = () => {
    const newMuted = !config.isMuted;
    setConfig(prev => ({ ...prev, isMuted: newMuted }));
    audioService.setMute(newMuted);
  };

  // --- Haptic Helper ---
  const triggerHaptic = (pattern: number | number[]) => {
    if (!config.isMuted && typeof navigator !== 'undefined' && navigator.vibrate) {
      try {
        navigator.vibrate(pattern);
      } catch (e) {
        console.warn('Vibration failed', e);
      }
    }
  };

  // --- Timer Logic Actions ---

  const resetTimer = useCallback(() => {
    if (intervalRef.current) {
      window.clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    setTimerState({
      phase: TimerPhase.IDLE,
      totalTime: 0,
      phaseTimeRemaining: config.bloomDuration,
      isActive: false,
    });
    wakeLockService.release();
  }, [config.bloomDuration]);

  const startTimer = useCallback(async () => {
    // Initialize Audio Context on first user interaction
    await audioService.initialize();
    
    // Request Wake Lock
    await wakeLockService.request();

    // Initial Start Haptic
    triggerHaptic(50);

    setTimerState(prev => {
        // If we are starting from IDLE, setup bloom
        if (prev.phase === TimerPhase.IDLE) {
            return {
                ...prev,
                isActive: true,
                phase: TimerPhase.BLOOM,
                phaseTimeRemaining: config.bloomDuration
            };
        }
        // Resuming from paused
        return { ...prev, isActive: true };
    });
  }, [config.bloomDuration]);

  const stopTimer = useCallback(() => {
    setTimerState(prev => ({ ...prev, isActive: false }));
    wakeLockService.release();
  }, []);

  const handleConfigSave = (newConfig: AppConfig) => {
    setConfig(newConfig);
    // If IDLE, update the starting time immediately
    if (timerState.phase === TimerPhase.IDLE) {
      setTimerState(prev => ({ ...prev, phaseTimeRemaining: newConfig.bloomDuration }));
    }
  };

  // --- The Heartbeat (Effect) ---
  useEffect(() => {
    if (timerState.isActive) {
      intervalRef.current = window.setInterval(() => {
        setTimerState(currentState => {
          let { phase, phaseTimeRemaining, totalTime } = currentState;

          // Decrement phase time
          phaseTimeRemaining -= 1;
          // Increment total time
          totalTime += 1;

          // --- State Transitions ---
          // Switch when hitting 0 (skipping 0 display, goes 1 -> NextPhaseStart)
          if (phaseTimeRemaining <= 0) {
            switch (phase) {
              case TimerPhase.BLOOM:
                // Bloom Finished -> Start Pouring
                phase = TimerPhase.POUR;
                phaseTimeRemaining = config.pulseInterval;
                audioService.playArpeggio();
                triggerHaptic([300, 100, 300, 100, 300]); 
                break;
              
              case TimerPhase.POUR:
                // Pour Finished -> Wait (Lower tone)
                phase = TimerPhase.WAIT;
                phaseTimeRemaining = config.pulseInterval;
                audioService.playLowPing();
                triggerHaptic(70); 
                break;
              
              case TimerPhase.WAIT:
                // Wait Finished -> Pour (Higher tone)
                phase = TimerPhase.POUR;
                phaseTimeRemaining = config.pulseInterval;
                audioService.playHighPing();
                triggerHaptic([150, 50, 150]); 
                break;
                
              default:
                break;
            }
          }

          return {
            ...currentState,
            phase,
            phaseTimeRemaining,
            totalTime
          };
        });
      }, 1000);
    } else if (intervalRef.current) {
      window.clearInterval(intervalRef.current);
    }

    return () => {
      if (intervalRef.current) {
        window.clearInterval(intervalRef.current);
      }
    };
  }, [timerState.isActive, config.pulseInterval, config.isMuted]);


  // --- Dynamic Background Logic ---
  const getBackgroundClass = () => {
    switch (timerState.phase) {
      case TimerPhase.BLOOM: return "bg-blue-600";
      case TimerPhase.POUR: return "bg-emerald-600";
      case TimerPhase.WAIT: return "bg-amber-600";
      default: return "bg-slate-900";
    }
  };

  const getPhaseLabel = () => {
    switch (timerState.phase) {
      case TimerPhase.BLOOM: return "BLOOMING";
      case TimerPhase.POUR: return "POUR";
      case TimerPhase.WAIT: return "WAIT";
      default: return "READY";
    }
  };

  const getPhaseIcon = () => {
    switch (timerState.phase) {
      case TimerPhase.BLOOM: return <Coffee size={48} className="animate-pulse" />;
      case TimerPhase.POUR: return <Droplets size={48} className="animate-bounce" />;
      case TimerPhase.WAIT: return <PauseCircle size={48} className="animate-pulse" />;
      default: return <Coffee size={48} />;
    }
  };

  return (
    <div className={`fixed inset-0 transition-colors duration-700 ease-in-out ${getBackgroundClass()} flex flex-col`}>
      
      {/* Header / Top Bar */}
      <div className="flex justify-between items-center p-6 z-10">
        <div className="flex items-center space-x-2">
            <Coffee size={24} className="text-white/80" />
            <h1 className="text-xl font-bold tracking-widest text-white/90">PULSE</h1>
        </div>
        <div className="flex space-x-2">
            <button 
                onClick={toggleMute} 
                className="p-3 bg-black/20 hover:bg-black/30 backdrop-blur rounded-full text-white transition-all"
                aria-label={config.isMuted ? "Unmute" : "Mute"}
            >
                {config.isMuted ? <VolumeX size={20} /> : <Volume2 size={20} />}
            </button>
            <button 
                onClick={() => setIsSettingsOpen(true)}
                className="p-3 bg-black/20 hover:bg-black/30 backdrop-blur rounded-full text-white transition-all"
                aria-label="Settings"
            >
                <Settings size={20} />
            </button>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col items-center justify-center p-6 relative">
        
        {/* Phase Indicator (Large) */}
        <div className="text-center space-y-2 mb-12">
            <div className="text-white/80 flex justify-center mb-6">
                {getPhaseIcon()}
            </div>
            <h2 className="text-6xl sm:text-7xl font-black text-white tracking-tight uppercase drop-shadow-lg">
                {getPhaseLabel()}
            </h2>
            <p className="text-white/70 text-lg font-medium tracking-widest">
                {timerState.phase === TimerPhase.IDLE 
                    ? "Start brewing" 
                    : timerState.phase === TimerPhase.BLOOM 
                        ? "Let it degas" 
                        : timerState.phase === TimerPhase.POUR 
                            ? "Add water slowly" 
                            : "Let it drain"}
            </p>
        </div>

        {/* Phase Timer (Centerpiece) */}
        <div className="relative mb-8">
            <div className="text-[120px] sm:text-[160px] font-bold leading-none text-white tabular-nums tracking-tighter drop-shadow-xl select-none">
                {timerState.phase === TimerPhase.IDLE 
                    ? config.bloomDuration 
                    : timerState.phaseTimeRemaining}
            </div>
            {timerState.phase !== TimerPhase.IDLE && (
                <div className="absolute top-full left-0 right-0 pt-2 flex flex-col items-center gap-1">
                     <div className="text-white/70 font-mono text-base uppercase tracking-[0.2em] font-bold">
                        Target: {getPhaseTargetDuration()}s
                    </div>
                    <div className="text-white/50 font-mono text-xl">
                        TOTAL: {formatTime(timerState.totalTime)}
                    </div>
                </div>
            )}
        </div>

      </div>

      {/* Control Deck */}
      <div className="bg-black/20 backdrop-blur-lg pb-12 pt-8 px-6 rounded-t-3xl border-t border-white/10">
        <div className="flex justify-center items-center space-x-6 max-w-md mx-auto">
            
            {/* Reset Button (Only visible if not idle) */}
            <div className={`transition-all duration-300 ${timerState.phase === TimerPhase.IDLE ? 'w-0 opacity-0 overflow-hidden' : 'w-auto opacity-100'}`}>
                 <Button variant="secondary" onClick={resetTimer} aria-label="Reset Timer">
                    <RotateCcw size={24} />
                 </Button>
            </div>

            {/* Main Action Button */}
            {!timerState.isActive ? (
                <Button variant="primary" onClick={startTimer} className="flex-1" aria-label="Start Timer">
                    <Play size={28} className="ml-1" fill="currentColor" />
                    <span className="ml-3">START BREW</span>
                </Button>
            ) : (
                <Button variant="danger" onClick={stopTimer} className="flex-1" aria-label="Pause Timer">
                    <Square size={24} fill="currentColor" />
                    <span className="ml-3">STOP</span>
                </Button>
            )}
        </div>
      </div>

      {/* Settings Modal */}
      <SettingsModal 
        isOpen={isSettingsOpen} 
        onClose={() => setIsSettingsOpen(false)}
        config={config}
        onSave={handleConfigSave}
      />

    </div>
  );
};

export default App;