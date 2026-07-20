import { Box, Circle, Cylinder, Cone, Hexagon, Square, Move, RotateCcw, Maximize2, Camera, Download, Trash2 } from 'lucide-react';
import { useSceneStore } from '../../stores/sceneStore';
import { useSelectionStore } from '../../stores/selectionStore';
import { GeometryType } from '../../types';

const geometryItems: { type: GeometryType; icon: typeof Box; label: string }[] = [
  { type: 'box', icon: Box, label: '立方体' },
  { type: 'sphere', icon: Circle, label: '球体' },
  { type: 'cylinder', icon: Cylinder, label: '圆柱体' },
  { type: 'cone', icon: Cone, label: '圆锥体' },
  { type: 'torus', icon: Hexagon, label: '圆环' },
  { type: 'plane', icon: Square, label: '平面' },
];

export function Toolbar() {
  const addObject = useSceneStore((state) => state.addObject);
  const removeObject = useSceneStore((state) => state.removeObject);
  const clearScene = useSceneStore((state) => state.clearScene);
  const selectedObjectId = useSelectionStore((state) => state.selectedObjectId);
  const selectObject = useSelectionStore((state) => state.selectObject);

  const handleAddGeometry = (type: GeometryType) => {
    addObject(type);
    const objects = useSceneStore.getState().objects;
    const lastObject = objects[objects.length - 1];
    if (lastObject) {
      selectObject(lastObject.id);
    }
  };

  const handleDelete = () => {
    if (selectedObjectId) {
      removeObject(selectedObjectId);
      selectObject(null);
    }
  };

  return (
    <div className="h-14 bg-gray-900 border-b border-gray-800 flex items-center px-4 gap-1">
      <div className="flex items-center gap-2 mr-6">
        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-cyan-500 to-blue-600 flex items-center justify-center">
          <Camera className="w-5 h-5 text-white" />
        </div>
        <span className="text-lg font-bold text-white">WebModeler</span>
      </div>

      <div className="h-6 w-px bg-gray-700 mx-2" />

      <div className="flex items-center gap-1">
        <span className="text-xs text-gray-500 mr-2 px-2">几何体</span>
        {geometryItems.map(({ type, icon: Icon, label }) => (
          <button
            key={type}
            onClick={() => handleAddGeometry(type)}
            title={label}
            className="p-2 rounded hover:bg-gray-800 text-gray-400 hover:text-cyan-400
              transition-colors duration-200"
          >
            <Icon className="w-5 h-5" />
          </button>
        ))}
      </div>

      <div className="h-6 w-px bg-gray-700 mx-2" />

      <div className="flex items-center gap-1">
        <span className="text-xs text-gray-500 mr-2 px-2">变换</span>
        <button
          title="移动"
          className="p-2 rounded hover:bg-gray-800 text-gray-400 hover:text-cyan-400
            transition-colors duration-200"
        >
          <Move className="w-5 h-5" />
        </button>
        <button
          title="旋转"
          className="p-2 rounded hover:bg-gray-800 text-gray-400 hover:text-cyan-400
            transition-colors duration-200"
        >
          <RotateCcw className="w-5 h-5" />
        </button>
        <button
          title="缩放"
          className="p-2 rounded hover:bg-gray-800 text-gray-400 hover:text-cyan-400
            transition-colors duration-200"
        >
          <Maximize2 className="w-5 h-5" />
        </button>
      </div>

      <div className="h-6 w-px bg-gray-700 mx-2" />

      <div className="flex items-center gap-1 ml-auto">
        <button
          onClick={handleDelete}
          disabled={!selectedObjectId}
          title="删除选中对象"
          className="p-2 rounded hover:bg-gray-800 text-gray-400 hover:text-red-400
            transition-colors duration-200 disabled:opacity-30 disabled:cursor-not-allowed"
        >
          <Trash2 className="w-5 h-5" />
        </button>
        <button
          onClick={clearScene}
          title="清空场景"
          className="p-2 rounded hover:bg-gray-800 text-gray-400 hover:text-red-400
            transition-colors duration-200"
        >
          <Trash2 className="w-5 h-5" />
        </button>
        <button
          title="导出模型"
          className="p-2 rounded hover:bg-gray-800 text-gray-400 hover:text-cyan-400
            transition-colors duration-200"
        >
          <Download className="w-5 h-5" />
        </button>
      </div>
    </div>
  );
}