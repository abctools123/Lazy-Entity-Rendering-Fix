# Lazy Entity Rendering Fix

A high-performance Fabric mod for Minecraft 1.21.11 that fixes visual entity rendering desyncs in lazy chunks and adds essential technical TNT visual and debugging utilities.

for both uh singleplayer and dedicated servers if the server has the mod too

## Features

### 1. Lazy Chunk Entity Rendering Fix
fixed  the long-standing Minecraft issue where entities inside lazy chunks (ticket level 32) freeze or desync visually on the client.
- fixed tnt desync in lazy chunks with fast pistons
- doesnt desync after /tick rate or sprint
- works even when using `/gamerule spectatorGenerateChunks false` and `/carpet creativePlayerLoadChunks false`.
- no nonstop packet spams

### 2. TNT Momentum Vector Arrow in F3+B
- toggled via `/carpet tntMotionArrow <true|false>`.
- like shows the tnt motion as a yellow arrow when ur using f3+b

### 3. Disable TNT Flashing & Expansion Scaling
- toggled via `/carpet tntNoFlashScale <true|false>`.
- remove the white flashing and the expanding and shaking right before it explodes

### 4. Remaining Fuse Label with Smart $O(N)$ Batching & TNT Count
- toggled via `/carpet tntFuseLabel <true|false>`.a
- a small pane above the tnt that shows the remaining fuse of the tnt
- **Batch Amount Display**: it even works withbatches of tnt like if theyre close enough and has the same fuse (e.g. `80t (500x)` or `80t` if singular).
- i think it only shows when you use f3+b too
### 5. Hide Entity Facing Arrows in F3+B
- toggled via `/carpet hideFacingArrow <true|false>`.
- hides that big ahh blue arrow on entities when using f3+b

---

## carpet commands

use these commands with carpet for the features on this mod
| carpet rule | default | category | desc |
| :--- | :---: | :---: | :--- |
| `lazyEntityRenderingFix` | `true` | `CLIENT` | fix entity rendering and movement inside lazy chunks. |
| `tntMotionArrow` | `false` | `CLIENT` | show momentum vector arrow for tnt when F3+B is active. |
| `tntNoFlashScale` | `false` | `CLIENT` | Disables tnt white flashing and shaking and expanding visual effect before explosion. |
| `tntFuseLabel` | `false` | `CLIENT` | Displays remaining fuse time and batch count pane (clusters $< 0.05\text{ m}$). |
| `hideFacingArrow` | `false` | `CLIENT` | Hides entity blue facing direction arrows in f3+b. |

---

## requirement

- **minecraft:** `1.21.11`
- **fabric version:** `>= 0.18.4` (or latest recommended)
- **dependencies:**
  - [Carpet](https://github.com/gnembon/fabric-carpet/releases)
  - [Fabric API](https://modrinth.com/mod/fabric-api)
- **Side:** server, works for singleplayer and for dedicated servers make sure to have it on both your client and on the server

## credits

- **[XSY_xiaoqi](https://github.com/XSY-HYH)** - Original author of [Carpet-VisualFix](https://github.com/XSY-HYH/Carpet-VisualFix).
- **[abctools123](https://github.com/abctools123)** - Port to 1.21.11, added features from lazy tnt util and updated carpet visual fix and fixed some stuff
- **[wlm3201](https://github.com/wlm3201)** - Author of [lazy-tnt-utils](https://github.com/wlm3201/lazy-tnt-utils) (inspiration and reference for TNT momentum vectors, flash/scale suppression, and fuse labeling).
- **[gnembon & the Carpet Team](https://github.com/gnembon/fabric-carpet)** - For the wonderful Fabric Carpet mod and extension framework.


This mod is licensed under the [MIT License](LICENSE).
