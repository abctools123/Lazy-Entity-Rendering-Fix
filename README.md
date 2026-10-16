# Lazy Entity Rendering Fix

Fabric mod for Minecraft 1.21.11 that fixes visual entity rendering desyncs in lazy chunks and adds technical TNT visual and debugging utilities.

Works for both singleplayer and dedicated servers (just make sure both sides have the mod installed).

## Features

### 1. Lazy Chunk Entity Rendering Fix
Fixes the long-standing Minecraft issue where entities inside lazy chunks visually fall or explode because the client falsely predicts movement for entities that isnt ticking (e.g. primed TNT).
- Fixes TNT position desync in lazy chunks with fast pistons.
- Doesn't desync after `/tick rate` or sprint.
- Works even when using `/gamerule spectatorGenerateChunks false` and `/carpet creativePlayerLoadChunks false`.
- No nonstop packet spams.

### 2. TNT Momentum Vector Arrow in F3+B
- Toggled via `/carpet tntMotionArrow <true|false>`.
- Shows TNT motion as a yellow arrow when you're using F3+B.

### 3. Disable TNT Flashing & Expansion Scaling
- Toggled via `/carpet tntNoFlashScale <true|false>`.
- Removes the white flashing, expanding, and shaking right before it explodes.

### 4. Remaining Fuse Label with Smart $O(N)$ Batching & TNT Count
- Toggled via `/carpet tntFuseLabel <true|false>`.
- Small pane above the TNT that shows remaining fuse ticks when using F3+B.
- **Batch Amount Display**: Groups batches of TNT if they are close enough (less than 0.05 blocks) and have the same fuse (e.g. `80t (500x)` or just `80t` if singular).

### 5. Hide Entity Facing Arrows in F3+B
- Toggled via `/carpet hideFacingArrow <true|false>`.
- Hides that big blue direction arrow on entities when using F3+B.

---

## Carpet Commands

Use these commands with Carpet to toggle features:

| Carpet Rule | Default | Category | Description |
| :--- | :---: | :---: | :--- |
| `lazyEntityRenderingFix` | `true` | `CLIENT` | Fix entity rendering and movement inside lazy chunks. |
| `tntMotionArrow` | `false` | `CLIENT` | Show momentum vector arrow for TNT when F3+B is active. |
| `tntNoFlashScale` | `false` | `CLIENT` | Disables TNT white flashing, shaking, and expanding visual effect before explosion. |
| `tntFuseLabel` | `false` | `CLIENT` | Displays remaining fuse time and batch count pane (clusters $< 0.05\text{ m}$). |
| `hideFacingArrow` | `false` | `CLIENT` | Hides entity blue facing direction arrows in F3+B. |

---

## Requirements

- **Minecraft:** `1.21.11`
- **Fabric Loader:** `>= 0.18.4` (or latest recommended)
- **Dependencies:**
  - [Carpet](https://github.com/gnembon/fabric-carpet/releases)
  - [Fabric API](https://modrinth.com/mod/fabric-api)
- **Side:** Universal. Works in singleplayer, but for dedicated servers make sure it's on both client and server.

---

## Credits

- **[XSY_xiaoqi](https://github.com/XSY-HYH)** - Original author of [Carpet-VisualFix](https://github.com/XSY-HYH/Carpet-VisualFix).
- **[abctools123](https://github.com/abctools123)** - Port to 1.21.11, merged lazy TNT utils, updated Carpet visual fixes, and patched bugs.
- **[wlm3201](https://github.com/wlm3201)** - Author of [lazy-tnt-utils](https://github.com/wlm3201/lazy-tnt-utils) (inspiration and reference for TNT momentum vectors, flash/scale suppression, and fuse labeling).
- **[gnembon & the Carpet Team](https://github.com/gnembon/fabric-carpet)** - For the Carpet mod and extension framework.

---

## License

This mod is licensed under the [MIT License](LICENSE).
