import React, { useState } from 'react';
import { X, Save, Calculator, Settings, Droplets, Coffee } from 'lucide-react';
import { AppConfig } from '../types';
import { Button } from './Button';

interface SettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  config: AppConfig;
  onSave: (newConfig: AppConfig) => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({ 
  isOpen, 
  onClose, 
  config, 
  onSave 
}) => {
  const [localConfig, setLocalConfig] = useState<AppConfig>(config);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSave = () => {
    if (localConfig.bloomDuration < 1 || localConfig.pulseInterval < 1) {
      setError("Durations must be at least 1 second.");
      return;
    }
    if (localConfig.coffeeWeight <= 0 || localConfig.waterRatio <= 0) {
        setError("Coffee weight and ratio must be positive.");
        return;
    }
    setError(null);
    onSave(localConfig);
    onClose();
  };

  const handleChange = (field: keyof AppConfig, value: string) => {
    const numValue = parseFloat(value);
    setLocalConfig(prev => ({
      ...prev,
      [field]: isNaN(numValue) ? 0 : numValue
    }));
    setError(null);
  };

  const totalWater = Math.round(localConfig.coffeeWeight * localConfig.waterRatio);
  const bloomWater = Math.round(localConfig.coffeeWeight * 2); // Rule of thumb: 2x weight
  const mainPourWater = totalWater - bloomWater;

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-in fade-in duration-200">
      <div className="bg-slate-900 border border-slate-700 w-full max-w-md rounded-2xl p-6 shadow-2xl space-y-6 max-h-[80vh] overflow-y-auto">
        <div className="flex justify-between items-center border-b border-slate-700 pb-4">
          <h2 className="text-xl font-bold text-white flex items-center gap-2">
            <Settings size={20} /> Settings
          </h2>
          <button onClick={onClose} className="p-2 hover:bg-slate-800 rounded-full text-slate-400 hover:text-white transition-colors">
            <X size={24} />
          </button>
        </div>

        {error && (
            <div className="p-3 bg-red-500/20 border border-red-500/50 rounded-lg text-red-200 text-sm">
              {error}
            </div>
        )}

        {/* Brew Calculator Section */}
        <div className="bg-slate-800/50 rounded-xl p-4 space-y-4 border border-slate-700/50">
            <h3 className="text-sm font-semibold text-emerald-400 flex items-center uppercase tracking-wider">
                <Calculator size={16} className="mr-2" /> Brew Calculator
            </h3>
            
            <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                    <label className="block text-xs font-medium text-slate-400">
                    Coffee (g)
                    </label>
                    <input
                    type="number"
                    min="1"
                    step="0.1"
                    value={localConfig.coffeeWeight || ''}
                    onChange={(e) => handleChange('coffeeWeight', e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 text-white text-lg rounded-lg p-3 focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                    />
                </div>
                <div className="space-y-2">
                    <label className="block text-xs font-medium text-slate-400">
                    Ratio (1:?)
                    </label>
                    <input
                    type="number"
                    min="1"
                    step="0.1"
                    value={localConfig.waterRatio || ''}
                    onChange={(e) => handleChange('waterRatio', e.target.value)}
                    className="w-full bg-slate-900 border border-slate-700 text-white text-lg rounded-lg p-3 focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                    />
                </div>
            </div>

            <div className="pt-2">
                <div className="flex justify-between items-baseline border-b border-slate-700/50 pb-2 mb-3">
                    <span className="text-sm text-slate-400 font-medium">Total Water Target</span>
                    <span className="text-3xl font-bold text-white tracking-tight">{totalWater}<span className="text-lg text-slate-500 ml-1">g</span></span>
                </div>

                <div className="grid grid-cols-2 gap-3">
                     {/* Bloom Card */}
                     <div className="bg-blue-500/10 rounded-lg p-3 border border-blue-500/20 relative group">
                        <div className="flex items-center gap-2 mb-1">
                            <Droplets size={14} className="text-blue-400" />
                            <span className="text-[10px] uppercase tracking-widest text-blue-300 font-bold">1. Bloom</span>
                        </div>
                        <div className="text-2xl font-bold text-blue-100">{bloomWater}g</div>
                        <div className="text-[10px] text-blue-300/60 leading-tight mt-1">Start by pouring up to this amount</div>
                     </div>

                     {/* Main Pour Card */}
                     <div className="bg-emerald-500/10 rounded-lg p-3 border border-emerald-500/20 relative group">
                         <div className="flex items-center gap-2 mb-1">
                            <Coffee size={14} className="text-emerald-400" />
                            <span className="text-[10px] uppercase tracking-widest text-emerald-300 font-bold">2. Main</span>
                        </div>
                        <div className="text-2xl font-bold text-emerald-100">{mainPourWater}g</div>
                        <div className="text-[10px] text-emerald-300/60 leading-tight mt-1">Add this remaining amount</div>
                     </div>
                </div>
            </div>
        </div>

        {/* Timer Settings Section */}
        <div className="space-y-4">
          <h3 className="text-sm font-semibold text-blue-400 uppercase tracking-wider">Timer Configuration</h3>

          <div className="space-y-2">
            <label className="block text-sm font-medium text-slate-300">
              Bloom Duration (seconds)
            </label>
            <input
              type="number"
              min="1"
              max="120"
              value={localConfig.bloomDuration || ''}
              onChange={(e) => handleChange('bloomDuration', e.target.value)}
              className="w-full bg-slate-800 border border-slate-700 text-white text-lg rounded-xl p-4 focus:ring-2 focus:ring-blue-500 focus:outline-none"
            />
            <p className="text-xs text-slate-500">Initial phase to release CO2.</p>
          </div>

          <div className="space-y-2">
            <label className="block text-sm font-medium text-slate-300">
              Pulse Interval (seconds)
            </label>
            <input
              type="number"
              min="1"
              max="60"
              value={localConfig.pulseInterval || ''}
              onChange={(e) => handleChange('pulseInterval', e.target.value)}
              className="w-full bg-slate-800 border border-slate-700 text-white text-lg rounded-xl p-4 focus:ring-2 focus:ring-emerald-500 focus:outline-none"
            />
            <p className="text-xs text-slate-500">Duration for both Pour and Wait phases.</p>
          </div>
        </div>

        <div className="pt-2">
          <Button onClick={handleSave} className="w-full">
            <Save size={20} className="mr-2" />
            Save Configuration
          </Button>
        </div>
      </div>
    </div>
  );
};