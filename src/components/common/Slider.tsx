interface SliderProps {
  label: string;
  value: number;
  min: number;
  max: number;
  step?: number;
  onChange: (value: number) => void;
  unit?: string;
}

export function Slider({ label, value, min, max, step = 0.01, onChange, unit }: SliderProps) {
  return (
    <div className="space-y-2">
      <div className="flex justify-between items-center">
        <span className="text-sm text-gray-400">{label}</span>
        <span className="text-sm font-mono text-cyan-400">
          {value.toFixed(2)}{unit ? ` ${unit}` : ''}
        </span>
      </div>
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        onChange={(e) => onChange(parseFloat(e.target.value))}
        className="w-full h-2 bg-gray-700 rounded-lg appearance-none cursor-pointer
          [&::-webkit-slider-thumb]:appearance-none
          [&::-webkit-slider-thumb]:w-4
          [&::-webkit-slider-thumb]:h-4
          [&::-webkit-slider-thumb]:bg-cyan-500
          [&::-webkit-slider-thumb]:rounded-full
          [&::-webkit-slider-thumb]:cursor-pointer
          [&::-webkit-slider-thumb]:shadow-lg
          [&::-webkit-slider-thumb]:shadow-cyan-500/50
          [&::-webkit-slider-thumb]:transition-all
          [&::-webkit-slider-thumb]:hover:scale-110"
      />
    </div>
  );
}