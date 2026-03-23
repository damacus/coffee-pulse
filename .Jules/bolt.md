# Performance Journal - Coffee Pulse

## Memory Allocation & Render Performance

### Static Array Extractions (2025-03-14)
- **Problem**: Inline array literals like `[15, 30, 45, 60, 75].map(...)` inside a React component's render body cause a new array to be allocated on every single render.
- **Impact**: While small, frequent re-renders (common in apps with timers or animations) can lead to increased GC pressure and potential micro-stuttering.
- **Solution**: Extracted these arrays to module-level constants (`COFFEE_PRESETS`, `THEME_SWATCH_PHASES`). This ensures the array is created only once when the module is loaded.
- **Learning**: Proactively identifying static data in render loops is a low-effort, high-reward habit for maintaining "buttery smooth" UI performance.
