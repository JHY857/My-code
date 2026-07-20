import { Color } from '../../types';

interface ColorPickerProps {
  label: string;
  value: Color;
  onChange: (color: Color) => void;
}

function colorToHex(color: Color): string {
  const r = Math.round(color.r * 255).toString(16).padStart(2, '0');
  const g = Math.round(color.g * 255).toString(16).padStart(2, '0');
  const b = Math.round(color.b * 255).toString(16).padStart(2, '0');
  return `#${r}${g}${b}`;
}

function hexToColor(hex: string): Color {
  const r = parseInt(hex.slice(1, 3), 16) / 255;
  const g = parseInt(hex.slice(3, 5), 16) / 255;
  const b = parseInt(hex.slice(5, 7), 16) / 255;
  return { r, g, b };
}

export function ColorPicker({ label, value, onChange }: ColorPickerProps) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-sm text-gray-400">{label}</span>
      <div className="flex items-center gap-2">
        <input
          type="color"
          value={colorToHex(value)}
          onChange={(e) => onChange(hexToColor(e.target.value))}
          className="w-8 h-8 rounded cursor-pointer border-0 bg-transparent"
        />
        <input
          type="text"
          value={colorToHex(value)}
          onChange={(e) => {
            const hex = e.target.value;
            if (/^#[0-9A-Fa-f]{6}$/.test(hex)) {
              onChange(hexToColor(hex));
            }
          }}
          className="w-20 bg-gray-800 border border-gray-700 rounded px-2 py-1 text-sm
            text-white font-mono focus:outline-none focus:border-cyan-500"
        />
      </div>
    </div>
  );
}