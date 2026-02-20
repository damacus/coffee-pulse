import React from 'react';

// Generic button — used where a Theme is not available in context.
// The SettingsModal and App both use inline-styled buttons for theme-awareness.

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  children: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  children,
  style,
  className = '',
  ...props
}) => {
  const variants: Record<string, React.CSSProperties> = {
    primary:   { background: '#f5e6c8', color: '#130b05' },
    secondary: { background: 'rgba(245,220,180,0.08)', color: 'rgba(245,220,180,0.7)', border: '1px solid rgba(245,220,180,0.15)' },
    danger:    { background: 'rgba(175,58,48,0.72)', color: 'rgba(255,218,208,0.94)' },
    ghost:     { background: 'transparent', color: 'rgba(245,220,180,0.5)' },
  };

  return (
    <button
      className={`btn-press ${className}`}
      style={{
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        height: 52, padding: '0 24px', borderRadius: 26, gap: 8,
        fontFamily: "'Manrope', sans-serif", fontSize: '12.5px', fontWeight: 700,
        letterSpacing: '0.16em', textTransform: 'uppercase',
        cursor: 'pointer', outline: 'none', border: 'none',
        ...variants[variant],
        ...style,
      }}
      {...props}
    >
      {children}
    </button>
  );
};
