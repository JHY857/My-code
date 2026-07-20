import { ChevronDown, ChevronRight, Eye, EyeOff, Settings } from 'lucide-react';
import { useState } from 'react';
import { useSceneStore } from '../../stores/sceneStore';
import { useSelectionStore } from '../../stores/selectionStore';
import { InputNumber } from '../common/InputNumber';
import { ColorPicker } from '../common/ColorPicker';
import { Slider } from '../common/Slider';
import { Vector3, Euler } from '../../types';

interface PanelSectionProps {
  title: string;
  icon?: typeof Settings;
  children: React.ReactNode;
  defaultOpen?: boolean;
}

function PanelSection({ title, icon: Icon, children, defaultOpen = true }: PanelSectionProps) {
  const [isOpen, setIsOpen] = useState(defaultOpen);

  return (
    <div className="border-b border-gray-700">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="w-full flex items-center justify-between p-3 hover:bg-gray-800/50
          transition-colors text-left"
      >
        <div className="flex items-center gap-2">
          {Icon && <Icon className="w-4 h-4 text-cyan-400" />}
          <span className="text-sm font-medium text-gray-200">{title}</span>
        </div>
        {isOpen ? (
          <ChevronDown className="w-4 h-4 text-gray-500" />
        ) : (
          <ChevronRight className="w-4 h-4 text-gray-500" />
        )}
      </button>
      {isOpen && (
        <div className="px-3 pb-4 space-y-3">
          {children}
        </div>
      )}
    </div>
  );
}

export function PropertyPanel() {
  const selectedObjectId = useSelectionStore((state) => state.selectedObjectId);
  const objects = useSceneStore((state) => state.objects);
  
  const updateObjectPosition = useSceneStore((state) => state.updateObjectPosition);
  const updateObjectRotation = useSceneStore((state) => state.updateObjectRotation);
  const updateObjectScale = useSceneStore((state) => state.updateObjectScale);
  const updateObjectMaterial = useSceneStore((state) => state.updateObjectMaterial);
  const updateObject = useSceneStore((state) => state.updateObject);

  const selectedObject = objects.find((o) => o.id === selectedObjectId);

  if (!selectedObject) {
    return (
      <div className="w-72 bg-gray-900 border-l border-gray-800 flex flex-col">
        <div className="p-4 border-b border-gray-700">
          <h2 className="text-sm font-medium text-gray-200">属性</h2>
        </div>
        <div className="flex-1 flex items-center justify-center text-gray-500 text-sm">
          请选择一个对象
        </div>
      </div>
    );
  }

  const handlePositionChange = (axis: keyof Vector3, value: number) => {
    updateObjectPosition(selectedObjectId, {
      ...selectedObject.position,
      [axis]: value,
    });
  };

  const handleRotationChange = (axis: keyof Euler, value: number) => {
    updateObjectRotation(selectedObjectId, {
      ...selectedObject.rotation,
      [axis]: value,
    });
  };

  const handleScaleChange = (axis: keyof Vector3, value: number) => {
    updateObjectScale(selectedObjectId, {
      ...selectedObject.scale,
      [axis]: value,
    });
  };

  return (
    <div className="w-72 bg-gray-900 border-l border-gray-800 flex flex-col">
      <div className="p-4 border-b border-gray-700">
        <h2 className="text-sm font-medium text-gray-200">属性</h2>
        <input
          type="text"
          value={selectedObject.name}
          onChange={(e) => updateObject(selectedObjectId, { name: e.target.value })}
          className="mt-2 w-full bg-gray-800 border border-gray-700 rounded px-3 py-2 text-sm
            text-white focus:outline-none focus:border-cyan-500"
        />
      </div>

      <div className="flex-1 overflow-y-auto">
        <PanelSection title="对象" icon={Settings}>
          <button
            onClick={() => updateObject(selectedObjectId, { visible: !selectedObject.visible })}
            className="flex items-center gap-2 text-sm text-gray-400 hover:text-white transition-colors"
          >
            {selectedObject.visible ? (
              <Eye className="w-4 h-4" />
            ) : (
              <EyeOff className="w-4 h-4" />
            )}
            <span>{selectedObject.visible ? '可见' : '隐藏'}</span>
          </button>
          <div className="text-xs text-gray-500">
            类型: <span className="text-gray-400">{selectedObject.type}</span>
          </div>
          <div className="text-xs text-gray-500">
            ID: <span className="text-gray-400 font-mono">{selectedObject.id}</span>
          </div>
        </PanelSection>

        <PanelSection title="位置">
          <InputNumber
            label="X"
            value={selectedObject.position.x}
            onChange={(v) => handlePositionChange('x', v)}
          />
          <InputNumber
            label="Y"
            value={selectedObject.position.y}
            onChange={(v) => handlePositionChange('y', v)}
          />
          <InputNumber
            label="Z"
            value={selectedObject.position.z}
            onChange={(v) => handlePositionChange('z', v)}
          />
        </PanelSection>

        <PanelSection title="旋转">
          <InputNumber
            label="X"
            value={selectedObject.rotation.x}
            onChange={(v) => handleRotationChange('x', v)}
          />
          <InputNumber
            label="Y"
            value={selectedObject.rotation.y}
            onChange={(v) => handleRotationChange('y', v)}
          />
          <InputNumber
            label="Z"
            value={selectedObject.rotation.z}
            onChange={(v) => handleRotationChange('z', v)}
          />
        </PanelSection>

        <PanelSection title="缩放">
          <InputNumber
            label="X"
            value={selectedObject.scale.x}
            onChange={(v) => handleScaleChange('x', v)}
            min={0.1}
          />
          <InputNumber
            label="Y"
            value={selectedObject.scale.y}
            onChange={(v) => handleScaleChange('y', v)}
            min={0.1}
          />
          <InputNumber
            label="Z"
            value={selectedObject.scale.z}
            onChange={(v) => handleScaleChange('z', v)}
            min={0.1}
          />
        </PanelSection>

        <PanelSection title="材质">
          <ColorPicker
            label="颜色"
            value={selectedObject.material.color}
            onChange={(color) => updateObjectMaterial(selectedObjectId, { color })}
          />
          <Slider
            label="金属度"
            value={selectedObject.material.metalness}
            min={0}
            max={1}
            onChange={(v) => updateObjectMaterial(selectedObjectId, { metalness: v })}
          />
          <Slider
            label="粗糙度"
            value={selectedObject.material.roughness}
            min={0}
            max={1}
            onChange={(v) => updateObjectMaterial(selectedObjectId, { roughness: v })}
          />
          <Slider
            label="发光强度"
            value={selectedObject.material.emissiveIntensity}
            min={0}
            max={2}
            onChange={(v) => updateObjectMaterial(selectedObjectId, { emissiveIntensity: v })}
          />
          <ColorPicker
            label="发光颜色"
            value={selectedObject.material.emissiveColor}
            onChange={(color) => updateObjectMaterial(selectedObjectId, { emissiveColor: color })}
          />
        </PanelSection>
      </div>
    </div>
  );
}