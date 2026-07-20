import { useSceneStore } from '../../stores/sceneStore';
import { Light } from '../../types';

export function Lights() {
  const ambientIntensity = useSceneStore((state) => state.ambientIntensity);
  const lights = useSceneStore((state) => state.lights);

  return (
    <>
      <ambientLight intensity={ambientIntensity} />
      
      {lights.map((light: Light) => {
        if (!light.visible) return null;
        
        const color: [number, number, number] = [light.color.r, light.color.g, light.color.b];
        const position: [number, number, number] = [light.position.x, light.position.y, light.position.z];
        
        switch (light.type) {
          case 'point':
            return (
              <pointLight
                key={light.id}
                position={position}
                color={color}
                intensity={light.intensity}
                castShadow
              />
            );
          case 'directional':
            return (
              <directionalLight
                key={light.id}
                position={position}
                color={color}
                intensity={light.intensity}
                castShadow
              />
            );
          case 'spot':
            return (
              <spotLight
                key={light.id}
                position={position}
                color={color}
                intensity={light.intensity}
                angle={Math.PI / 4}
                penumbra={0.1}
                castShadow
              />
            );
          default:
            return null;
        }
      })}
    </>
  );
}