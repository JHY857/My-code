import { Toolbar } from './components/Toolbar/Toolbar';
import { SceneTree } from './components/SceneTree/SceneTree';
import { SceneCanvas } from './components/SceneCanvas/SceneCanvas';
import { PropertyPanel } from './components/PropertyPanel/PropertyPanel';
import { RenderSettings } from './components/RenderSettings/RenderSettings';

function App() {
  return (
    <div className="h-screen w-screen flex flex-col bg-gray-950">
      <Toolbar />
      <div className="flex-1 flex overflow-hidden">
        <SceneTree />
        <div className="flex-1 flex flex-col overflow-hidden">
          <SceneCanvas />
          <RenderSettings />
        </div>
        <PropertyPanel />
      </div>
    </div>
  );
}

export default App;