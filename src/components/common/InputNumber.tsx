interface InputNumberProps {
  label: string;
  value: number;
  onChange: (value: number) => void;
  min?: number;
  max?: number;
  step?: number;
}

export function InputNumber({ label, value, onChange, min, max, step = 0.1 }: InputNumberProps) {
  return (
    <div className="flex items-center gap-2">
      <label className="text-sm text-gray-400 w-8">{label}</label>
      <input
        type="number"
        value={value.toFixed(2)}
        onChange={(e) => {
          const num = parseFloat(e.target.value);
          if (!isNaN(num)) {
            onChange(num);
          }
        }}
        min={min}
        max={max}
        step={step}
        className="flex-1 bg-gray-800 border border-gray-700 rounded px-3 py-1.5 text-sm
          text-white focus:outline-none focus:border-cyan-500 focus:ring-1 focus:ring-cyan-500"
      />
    </div>
  );
}