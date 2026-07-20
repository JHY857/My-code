import { create } from 'zustand';
import { SceneObject, Light, Color, Vector3, Euler, GeometryType, Material } from '../types';

function generateId(): string {
  return Math.random().toString(36).substring(2, 9);
}

const defaultColor: Color = { r: 0.6, g: 0.8, b: 1 };
const defaultMaterial: Material = {
  id: generateId(),
  color: defaultColor,
  metalness: 0.3,
  roughness: 0.5,
  emissiveIntensity: 0,
  emissiveColor: { r: 0, g: 0, b: 0 },
};

interface SceneStore {
  objects: SceneObject[];
  lights: Light[];
  backgroundColor: Color;
  ambientIntensity: number;
  
  addObject: (type: GeometryType, position?: Vector3) => void;
  removeObject: (id: string) => void;
  updateObject: (id: string, updates: Partial<SceneObject>) => void;
  updateObjectPosition: (id: string, position: Vector3) => void;
  updateObjectRotation: (id: string, rotation: Euler) => void;
  updateObjectScale: (id: string, scale: Vector3) => void;
  updateObjectMaterial: (id: string, material: Partial<Material>) => void;
  
  addLight: (type: 'point' | 'directional' | 'spot') => void;
  removeLight: (id: string) => void;
  updateLight: (id: string, updates: Partial<Light>) => void;
  
  setBackgroundColor: (color: Color) => void;
  setAmbientIntensity: (intensity: number) => void;
  
  clearScene: () => void;
}

export const useSceneStore = create<SceneStore>((set, get) => ({
  objects: [],
  lights: [
    {
      id: 'dir-light-1',
      type: 'directional',
      name: '主光源',
      position: { x: 5, y: 5, z: 5 },
      color: { r: 1, g: 1, b: 1 },
      intensity: 1,
      visible: true,
    },
  ],
  backgroundColor: { r: 0.05, g: 0.08, b: 0.12 },
  ambientIntensity: 0.3,
  
  addObject: (type, position = { x: 0, y: 0, z: 0 }) => {
    const { objects } = get();
    const count = objects.filter(o => o.type === type).length + 1;
    const newObject: SceneObject = {
      id: generateId(),
      name: `${type}_${count}`,
      type,
      position,
      rotation: { x: 0, y: 0, z: 0 },
      scale: { x: 1, y: 1, z: 1 },
      visible: true,
      material: { ...defaultMaterial, id: generateId() },
    };
    set({ objects: [...objects, newObject] });
    return newObject.id;
  },
  
  removeObject: (id) => {
    set(state => ({ objects: state.objects.filter(o => o.id !== id) }));
  },
  
  updateObject: (id, updates) => {
    set(state => ({
      objects: state.objects.map(o => o.id === id ? { ...o, ...updates } : o),
    }));
  },
  
  updateObjectPosition: (id, position) => {
    set(state => ({
      objects: state.objects.map(o => o.id === id ? { ...o, position } : o),
    }));
  },
  
  updateObjectRotation: (id, rotation) => {
    set(state => ({
      objects: state.objects.map(o => o.id === id ? { ...o, rotation } : o),
    }));
  },
  
  updateObjectScale: (id, scale) => {
    set(state => ({
      objects: state.objects.map(o => o.id === id ? { ...o, scale } : o),
    }));
  },
  
  updateObjectMaterial: (id, material) => {
    set(state => ({
      objects: state.objects.map(o => 
        o.id === id ? { ...o, material: { ...o.material, ...material } } : o
      ),
    }));
  },
  
  addLight: (type) => {
    const { lights } = get();
    const count = lights.filter(l => l.type === type).length + 1;
    const newLight: Light = {
      id: generateId(),
      type,
      name: `${type}_${count}`,
      position: { x: 0, y: 5, z: 0 },
      color: { r: 1, g: 1, b: 1 },
      intensity: 1,
      visible: true,
    };
    set({ lights: [...lights, newLight] });
  },
  
  removeLight: (id) => {
    set(state => ({ lights: state.lights.filter(l => l.id !== id) }));
  },
  
  updateLight: (id, updates) => {
    set(state => ({
      lights: state.lights.map(l => l.id === id ? { ...l, ...updates } : l),
    }));
  },
  
  setBackgroundColor: (color) => set({ backgroundColor: color }),
  
  setAmbientIntensity: (intensity) => set({ ambientIntensity: intensity }),
  
  clearScene: () => set({ objects: [], lights: [] }),
}));