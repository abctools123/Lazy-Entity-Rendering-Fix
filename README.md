# Lazy Entity Rendering Fix

A high-performance Fabric mod for Minecraft 1.21.11 that fixes visual entity rendering desyncs in lazy chunks and adds essential technical TNT visual and debugging utilities.

Compatible with both **dedicated servers** and **integrated singleplayer**, and seamlessly integrates into **[Fabric Carpet](https://github.com/gnembon/fabric-carpet)** commands.

---

## ✨ Features

### 1. Lazy Chunk Entity Rendering Fix
Fixes the long-standing Minecraft issue where entities inside lazy chunks (ticket level 32) freeze or desync visually on the client.
- Fixes pistons pushing entities in lazy chunks without visual desyncs.
- Fully compatible with `/tick sprint` and high `/tick rate`.
- Works properly even when using `/gamerule spectatorGenerateChunks false` and `/carpet creativePlayerLoadChunks false`.
- High-efficiency delta updates prevent packet flooding when large entity piles are stationary.

### 2. TNT Momentum Vector Arrow in F3+B
- Toggled via `/carpet tntMotionArrow <true|false>`.
- Displays a crisp yellow velocity vector arrow pointing in the exact movement direction of primed TNT when F3+B hitboxes are active.
- Preserves velocity even when TNT flies into or rests inside lazy chunks on dedicated servers.

### 3. Disable TNT Flashing & Expansion Scaling
- Toggled via `/carpet tntNoFlashScale <true|false>`.
- Removes the white flashing and pulsing/growing scale visual effect before primed TNT explodes.
- Keeps TNT visually stable without camera or sub-tick jitter in technical contraptions and cannons.

### 4. Remaining Fuse Label with Smart $O(N)$ Batching & TNT Count
- Toggled via `/carpet tntFuseLabel <true|false>`.
- Displays a clean floating text billboard pane above primed TNT showing the remaining game ticks before detonation (e.g. `80t`).
- **Batch Amount Display**: Shows the exact number of TNTs next to the remaining ticks for clustered groups (e.g. `80t (500x)` or `80t` if singular).
- **Urgency Color Coding**:
  - White: $\ge 40\text{t}$ (2+ seconds)
  - Orange: $20\text{t} - 39\text{t}$ (1 to 2 seconds)
  - Red: $< 20\text{t}$ (under 1 second / critical)
- **$O(N)$ Spatial Clustering (< 0.05 blocks)**: Automatically clusters TNTs sharing the same fuse within $0.05\text{ m}$, collapsing thousands of TNTs into a single network entry to eliminate packet lag.
- **Vertical Label Stacking**: Multiple TNT batches sharing the same block alignment (such as propellant at `40t` and shot at `20t`) are automatically stacked vertically ($+0.35\text{ m}$ per line), preventing unreadable overlapping text.
- **Selective Network Streaming**: Fuse updates are only transmitted to players who have F3+B enabled. When F3+B is off, zero fuse packets are streamed.
- **Lazy Chunk Delta Sync**: In lazy chunks, TNT fuses do not tick down. The server caches lazy clusters and only transmits updates when the frozen state changes, keeping labels visible without packet spam.

### 5. Hide Entity Facing Arrows in F3+B
- Toggled via `/carpet hideFacingArrow <true|false>`.
- Hides the intrusive blue entity facing direction vector lines in F3+B hitbox view, drastically reducing screen clutter when inspecting large cannons and mob contraptions.

---

## ⚙️ Carpet Rules Reference

All features can be dynamically controlled in-game via Carpet commands:

| Carpet Rule | Default | Category | Description |
| :--- | :---: | :---: | :--- |
| `lazyEntityRenderingFix` | `true` | `CLIENT` | Fixes entity rendering and movement inside lazy chunks. |
| `tntMotionArrow` | `false` | `CLIENT` | Draws momentum vector arrow for primed TNT when F3+B is active. |
| `tntNoFlashScale` | `false` | `CLIENT` | Disables TNT white flashing and scaling visual effect before explosion. |
| `tntFuseLabel` | `false` | `CLIENT` | Displays remaining fuse ticks and batch count pane (clusters $< 0.05\text{ m}$). |
| `hideFacingArrow` | `false` | `CLIENT` | Hides entity blue facing direction arrows in F3+B hitbox view. |

---

## 📦 Requirements & Installation

- **Minecraft:** `1.21.11`
- **Fabric Loader:** `>= 0.18.4` (or latest recommended)
- **Required Dependencies:**
  - [Fabric Carpet](https://github.com/gnembon/fabric-carpet/releases)
  - [Fabric API](https://modrinth.com/mod/fabric-api)
- **Side:** Universal (Both Server and Client recommended for full network batching and visual sync; also works in singleplayer).

### Installation:
1. Ensure [Fabric Loader](https://fabricmc.net/) is installed for Minecraft 1.21.11.
2. Download [Fabric Carpet](https://modrinth.com/mod/carpet) and [Fabric API](https://modrinth.com/mod/fabric-api) and place them into your `.minecraft/mods` directory.
3. Place `lazy-entity-rendering-fix-1.2.0.jar` into your `mods` folder.
4. Launch the game and configure rules using `/carpet <ruleName> <value>`.

---

## 🤝 Credits & Acknowledgements

- **[XSY_xiaoqi](https://github.com/XSY-HYH)** - Original author of [Carpet-VisualFix](https://github.com/XSY-HYH/Carpet-VisualFix).
- **[abctools123](https://github.com/abctools123)** - Port to 1.21.11, spectator/ticket level fixes, $O(N)$ spatial clustering, multi-batch vertical stacking, and dedicated server synchronization.
- **[wlm3201](https://github.com/wlm3201)** - Author of [lazy-tnt-utils](https://github.com/wlm3201/lazy-tnt-utils) (inspiration and reference for TNT momentum vectors, flash/scale suppression, and fuse labeling).
- **[gnembon & the Carpet Team](https://github.com/gnembon/fabric-carpet)** - For the wonderful Fabric Carpet mod and extension framework.

---

## 📄 License

This mod is licensed under the [MIT License](LICENSE).
