import { create } from 'zustand';

interface SelectionStore {
  selectedObjectId: string | null;
  selectedLightId: string | null;
  
  selectObject: (id: string | null) => void;
  selectLight: (id: string | null) => void;
  clearSelection: () => void;
}

export const useSelectionStore = create<SelectionStore>((set) => ({
  selectedObjectId: null,
  selectedLightId: null,
  
  selectObject: (id) => set({ selectedObjectId: id, selectedLightId: null }),
  
  selectLight: (id) => set({ selectedLightId: id, selectedObjectId: null }),
  
  clearSelection: () => set({ selectedObjectId: null, selectedLightId: null }),
}));