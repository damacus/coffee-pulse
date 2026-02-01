import React from 'react';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  children: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({ 
  variant = 'primary', 
  children, 
  className = '', 
  ...props 
}) => {
  const baseStyles = "flex items-center justify-center font-bold tracking-wide rounded-full transition-all duration-200 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed";
  
  const variants = {
    primary: "bg-white text-black hover:bg-gray-100 shadow-lg",
    secondary: "bg-white/20 text-white hover:bg-white/30 backdrop-blur-md border border-white/30",
    danger: "bg-red-500/80 text-white hover:bg-red-600/90 shadow-lg",
    ghost: "bg-transparent text-white/70 hover:text-white hover:bg-white/10",
  };

  const sizeStyles = "h-16 px-8 text-lg";

  return (
    <button 
      className={`${baseStyles} ${variants[variant]} ${sizeStyles} ${className}`} 
      {...props}
    >
      {children}
    </button>
  );
};
