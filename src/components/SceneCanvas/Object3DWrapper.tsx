import { useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import type * as THREE from 'three';
import { useSelectionStore } from '../../stores/selectionStore';
import { SceneObject, GeometryType } from '../../types';

interface Object3DWrapperProps {
  object: SceneObject;
  isSelected: boolean;
}

const geometryElements: Record<GeometryType, string> = {
  box: 'boxGeometry',
  sphere: 'sphereGeometry',
  cylinder: 'cylinderGeometry',
  cone: 'coneGeometry',
  torus: 'torusGeometry',
  plane: 'planeGeometry',
};

const getGeometryArgs = (type: GeometryType): [number, number] | [] => {
  if (type === 'plane') return [4, 4];
  return [];
};

export function Object3DWrapper({ object, isSelected }: Object3DWrapperProps) {
  const meshRef = useRef<THREE.Mesh>(null);
  const selectObject = useSelectionStore((state) => state.selectObject);

  useFrame((state) => {
    if (meshRef.current && isSelected) {
      const time = state.clock.elapsedTime;
      const material = meshRef.current.material as THREE.MeshStandardMaterial;
      material.emissive.setHSL((time * 0.1) % 1, 1, 0.3);
    }
  });

  const GeometryElement = geometryElements[object.type];
  const geometryArgs = getGeometryArgs(object.type);

  const color: [number, number, number] = [object.material.color.r, object.material.color.g, object.material.color.b];
  const emissive: [number, number, number] = [object.material.emissiveColor.r, object.material.emissiveColor.g, object.material.emissiveColor.b];

  return (
    <mesh
      ref={meshRef}
      position={[object.position.x, object.position.y, object.position.z]}
      rotation={[object.rotation.x, object.rotation.y, object.rotation.z]}
      scale={[object.scale.x, object.scale.y, object.scale.z]}
      visible={object.visible}
      onClick={(e) => {
        e.stopPropagation();
        selectObject(object.id);
      }}
    >
      {GeometryElement === 'planeGeometry' && <planeGeometry args={geometryArgs} />}
      {GeometryElement === 'boxGeometry' && <boxGeometry />}
      {GeometryElement === 'sphereGeometry' && <sphereGeometry />}
      {GeometryElement === 'cylinderGeometry' && <cylinderGeometry />}
      {GeometryElement === 'coneGeometry' && <coneGeometry />}
      {GeometryElement === 'torusGeometry' && <torusGeometry />}
      
      <meshStandardMaterial
        color={color}
        metalness={object.material.metalness}
        roughness={object.material.roughness}
        emissive={emissive}
        emissiveIntensity={object.material.emissiveIntensity}
      />
    </mesh>
  );
}