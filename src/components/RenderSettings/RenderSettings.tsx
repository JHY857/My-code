import { Sun, Plus, X } from 'lucide-react';
import { useSceneStore } from '../../stores/sceneStore';
import { Slider } from '../common/Slider';
import { ColorPicker } from '../common/ColorPicker';
import { InputNumber } from '../common/InputNumber';
import { useState } from 'react';

export function RenderSettings() {
  const backgroundColor = useSceneStore((state) => state.backgroundColor);
  const ambientIntensity = useSceneStore((state) => state.ambientIntensity);
  const lights = useSceneStore((state) => state.lights);
  
  const setBackgroundColor = useSceneStore((state) => state.setBackgroundColor);
  const setAmbientIntensity = useSceneStore((state) => state.setAmbientIntensity);
  const addLight = useSceneStore((state) => state.addLight);
  const removeLight = useSceneStore((state) => state.removeLight);
  const updateLight = useSceneStore((state) => state.updateLight);

  const [expandedLightId, setExpandedLightId] = useState<string | null>(null);

  return (
    <div className="h-48 bg-gray-900 border-t border-gray-800 p-4">
      <div className="flex items-center justify-between mb-3">
        <h2 className="text-sm font-medium text-gray-200">渲染设置</h2>
        <button
          onClick={() => addLight('point')}
          className="flex items-center gap-1 px-3 py-1.5 bg-cyan-600 hover:bg-cyan-500
            text-white text-xs rounded transition-colors"
        >
          <Plus className="w-3 h-3" />
          添加光源
        </button>
      </div>
      
      <div className="grid grid-cols-12 gap-4">
        <div className="col-span-4 space-y-3">
          <ColorPicker
            label="背景颜色"
            value={backgroundColor}
            onChange={setBackgroundColor}
          />
          <Slider
            label="环境光强度"
            value={ambientIntensity}
            min={0}
            max={1}
            onChange={setAmbientIntensity}
          />
        </div>
        
        <div className="col-span-8">
          <div className="flex items-center gap-2 mb-2">
            <Sun className="w-4 h-4 text-yellow-400" />
            <span className="text-xs text-gray-400">光源列表</span>
          </div>
          <div className="space-y-2 max-h-28 overflow-y-auto">
            {lights.map((light) => (
              <div
                key={light.id}
                className="bg-gray-800 rounded p-2"
              >
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-gray-400">{light.name}</span>
                    <span className="text-xs text-gray-500">({light.type})</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => setExpandedLightId(expandedLightId === light.id ? null : light.id)}
                      className="text-xs text-gray-500 hover:text-cyan-400"
                    >
                      {expandedLightId === light.id ? '收起' : '展开'}
                    </button>
                    <button
                      onClick={() => removeLight(light.id)}
                      className="text-xs text-gray-500 hover:text-red-400"
                    >
                      <X className="w-3 h-3" />
                    </button>
                  </div>
                </div>
                {expandedLightId === light.id && (
                  <div className="space-y-2 text-xs">
                    <div className="grid grid-cols-3 gap-2">
                      <InputNumber
                        label="X"
                        value={light.position.x}
                        onChange={(v) => updateLight(light.id, { position: { ...light.position, x: v } })}
                      />
                      <InputNumber
                        label="Y"
                        value={light.position.y}
                        onChange={(v) => updateLight(light.id, { position: { ...light.position, y: v } })}
                      />
                      <InputNumber
                        label="Z"
                        value={light.position.z}
                        onChange={(v) => updateLight(light.id, { position: { ...light.position, z: v } })}
                      />
                    </div>
                    <ColorPicker
                      label="颜色"
                      value={light.color}
                      onChange={(color) => updateLight(light.id, { color })}
                    />
                    <Slider
                      label="强度"
                      value={light.intensity}
                      min={0}
                      max={5}
                      onChange={(v) => updateLight(light.id, { intensity: v })}
                    />
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}