export interface Vector3 {
  x: number;
  y: number;
  z: number;
}

export interface Euler {
  x: number;
  y: number;
  z: number;
}

export interface Color {
  r: number;
  g: number;
  b: number;
}

export interface Material {
  id: string;
  color: Color;
  metalness: number;
  roughness: number;
  emissiveIntensity: number;
  emissiveColor: Color;
}

export type GeometryType = 'box' | 'sphere' | 'cylinder' | 'cone' | 'torus' | 'plane';

export interface SceneObject {
  id: string;
  name: string;
  type: GeometryType;
  position: Vector3;
  rotation: Euler;
  scale: Vector3;
  visible: boolean;
  material: Material;
  parentId?: string;
}

export type LightType = 'ambient' | 'point' | 'directional' | 'spot';

export interface Light {
  id: string;
  type: LightType;
  name: string;
  position: Vector3;
  color: Color;
  intensity: number;
  visible: boolean;
}

export interface SceneState {
  objects: SceneObject[];
  lights: Light[];
  selectedObjectId: string | null;
  backgroundColor: Color;
  ambientIntensity: number;
}