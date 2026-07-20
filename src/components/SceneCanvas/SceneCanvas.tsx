import { Canvas } from '@react-three/fiber';
import { OrbitControls, Grid } from '@react-three/drei';
import { useSceneStore } from '../../stores/sceneStore';
import { useSelectionStore } from '../../stores/selectionStore';
import { Object3DWrapper } from './Object3DWrapper';
import { Lights } from './Lights';

export function SceneCanvas() {
  const backgroundColor = useSceneStore((state) => state.backgroundColor);
  const objects = useSceneStore((state) => state.objects);
  const selectedObjectId = useSelectionStore((state) => state.selectedObjectId);

  return (
    <div className="flex-1 relative bg-gray-950">
      <Canvas
        camera={{ position: [8, 8, 8], fov: 50 }}
        style={{ background: `rgb(${Math.round(backgroundColor.r * 255)}, ${Math.round(backgroundColor.g * 255)}, ${Math.round(backgroundColor.b * 255)})` }}
      >
        <color attach="background" args={[`rgb(${backgroundColor.r}, ${backgroundColor.g}, ${backgroundColor.b})`]} />
        
        <Lights />
        
        <Grid args={[20, 20]} />
        <axesHelper args={[5]} />
        
        {objects.map((obj) => (
          <Object3DWrapper
            key={obj.id}
            object={obj}
            isSelected={selectedObjectId === obj.id}
          />
        ))}
        
        <OrbitControls
          enableDamping
          dampingFactor={0.05}
          minDistance={2}
          maxDistance={50}
        />
      </Canvas>
      
      <div className="absolute bottom-4 left-4 text-xs text-gray-500 font-mono">
        WebModeler · {objects.length} objects
      </div>
      
      <div className="absolute top-4 right-4 text-xs text-gray-500 font-mono">
        Left click: Rotate · Right click: Pan · Scroll: Zoom
      </div>
    </div>
  );
}