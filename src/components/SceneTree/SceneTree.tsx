import { Box, Circle, Cylinder, Cone, Hexagon, Square, Lightbulb, Eye, EyeOff } from 'lucide-react';
import { useSceneStore } from '../../stores/sceneStore';
import { useSelectionStore } from '../../stores/selectionStore';
import { SceneObject, Light, GeometryType } from '../../types';

const geometryIcons: Record<GeometryType, typeof Box> = {
  box: Box,
  sphere: Circle,
  cylinder: Cylinder,
  cone: Cone,
  torus: Hexagon,
  plane: Square,
};

interface TreeItemProps {
  item: SceneObject | Light;
  isLight: boolean;
  isSelected: boolean;
  onSelect: () => void;
  onToggleVisibility: () => void;
}

function TreeItem({ item, isLight, isSelected, onSelect, onToggleVisibility }: TreeItemProps) {
  const Icon = isLight ? Lightbulb : geometryIcons[item.type as GeometryType];
  
  return (
    <div
      onClick={onSelect}
      className={`flex items-center gap-2 px-3 py-2 cursor-pointer transition-colors
        ${isSelected ? 'bg-gray-800 text-cyan-400' : 'text-gray-400 hover:bg-gray-800/50 hover:text-gray-200'}`}
    >
      <button
        onClick={(e) => {
          e.stopPropagation();
          onToggleVisibility();
        }}
        className="p-0.5 hover:text-gray-200"
      >
        {item.visible ? (
          <Eye className="w-3.5 h-3.5" />
        ) : (
          <EyeOff className="w-3.5 h-3.5" />
        )}
      </button>
      <Icon className="w-4 h-4 flex-shrink-0" />
      <span className="text-sm truncate flex-1">{item.name}</span>
    </div>
  );
}

export function SceneTree() {
  const objects = useSceneStore((state) => state.objects);
  const lights = useSceneStore((state) => state.lights);
  const updateObject = useSceneStore((state) => state.updateObject);
  const updateLight = useSceneStore((state) => state.updateLight);
  
  const selectedObjectId = useSelectionStore((state) => state.selectedObjectId);
  const selectedLightId = useSelectionStore((state) => state.selectedLightId);
  const selectObject = useSelectionStore((state) => state.selectObject);
  const selectLight = useSelectionStore((state) => state.selectLight);

  return (
    <div className="w-56 bg-gray-900 border-r border-gray-800 flex flex-col">
      <div className="p-3 border-b border-gray-700">
        <h2 className="text-sm font-medium text-gray-200">场景树</h2>
        <div className="mt-1 text-xs text-gray-500">
          {objects.length} 个对象 · {lights.length} 个光源
        </div>
      </div>
      
      <div className="flex-1 overflow-y-auto">
        <div className="p-2">
          <div className="text-xs text-gray-600 px-3 py-1">对象</div>
          {objects.length === 0 ? (
            <div className="text-xs text-gray-500 px-3 py-2">暂无对象</div>
          ) : (
            objects.map((obj) => (
              <TreeItem
                key={obj.id}
                item={obj}
                isLight={false}
                isSelected={selectedObjectId === obj.id}
                onSelect={() => selectObject(obj.id)}
                onToggleVisibility={() => updateObject(obj.id, { visible: !obj.visible })}
              />
            ))
          )}
        </div>
        
        <div className="p-2 border-t border-gray-700">
          <div className="text-xs text-gray-600 px-3 py-1">光源</div>
          {lights.length === 0 ? (
            <div className="text-xs text-gray-500 px-3 py-2">暂无光源</div>
          ) : (
            lights.map((light) => (
              <TreeItem
                key={light.id}
                item={light}
                isLight={true}
                isSelected={selectedLightId === light.id}
                onSelect={() => selectLight(light.id)}
                onToggleVisibility={() => updateLight(light.id, { visible: !light.visible })}
              />
            ))
          )}
        </div>
      </div>
    </div>
  );
}