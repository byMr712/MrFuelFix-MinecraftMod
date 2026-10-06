> **Language:** [Русский](README.md) · English

# [MR] Fuel Fix

![Java 21](https://img.shields.io/badge/Java-21-blue.svg)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21-blue.svg)
![Fabric](https://img.shields.io/badge/Loader-Fabric-blue.svg)
![ModMenu](https://img.shields.io/badge/ModMenu-Supported-blue.svg)
![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)

A lightweight mod for **Minecraft 1.21 (Fabric)** that fixes the annoying vanilla issue where quick-moving fuel (**Shift + Left Click**) into furnaces fails when the smelting slot is occupied.

| Vanilla game | Game with mod |
| :---: | :---: |
| ![Full_vanilla](images/Full_vanilla.webp) | ![This_mod](images/This_mod.webp) |

## What Does This Mod Do? (Player Friendly)

Have you ever had ores or food smelting in a furnace, tried to **Shift-click planks, logs, or sticks** to add them as fuel, but **nothing happens**? The item refuses to enter the furnace, forcing you to drag and drop it manually!

### Why does this happen?
In vanilla Minecraft, wood items, sticks, and logs are **both smeltable and fuel** (e.g. logs smelt into charcoal). Because of a logic oversight, Minecraft always tries to put them into the top smelting slot first. If that top slot is occupied with something else (like raw iron), the game gives up immediately and never tries to put them into the bottom fuel slot!

### How this mod fixes it:
- **Smart slot redirection:** Shift-click any fuel (planks, logs, sticks, blaze rods, etc.) — if the top slot is occupied or full, the items **automatically go straight into the bottom fuel slot**!
- No more manual dragging and dropping of fuel.
- Works across all furnace types: **Furnace**, **Blast Furnace**, and **Smoker**.

## Compatibility

- Works with all furnace types: **Furnace**, **Blast Furnace**, and **Smoker**.
- Fully compatible with custom recipe mods (including **[[MR] Recipe Editor](https://www.curseforge.com/minecraft/mc-mods/mr-recipe-editor)**), where fuel items might also have smelting recipes.
- Works in Singleplayer, LAN, and dedicated servers (Client & Server).
- Bilingual localization (`en_us`, `ru_ru`).

## Installation

1. Download the latest release from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mr-fuel-fix) or [GitHub Releases](https://github.com/byMr712/MrFuelFix-MinecraftMod/releases).
2. Requires:
   - [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
3. Place the `.jar` file into your `mods` folder.
4. Launch the game.

## Technical Details (For Developers)

### Vanilla Minecraft Issue

In vanilla Minecraft, `AbstractFurnaceScreenHandler.quickMove` is structured as follows:
```java
if (this.isSmeltable(itemStack)) {
    if (!this.insertItem(itemStack, 0, 1, false)) {
        return ItemStack.EMPTY; // Blocked!
    }
} else if (this.isFuel(itemStack)) {
    if (!this.insertItem(itemStack, 1, 2, false)) {
        return ItemStack.EMPTY;
    }
}
```

If an item is **both smeltable and fuel** (logs, wood, planks, sticks, blaze rods, or coal/charcoal when custom smelting recipes exist):
1. The game checks `isSmeltable` first, which evaluates to `true`.
2. It tries to insert into input slot 0.
3. If input slot 0 **already contains a different item** (or is full), `insertItem` returns `false`.
4. The method immediately returns `ItemStack.EMPTY`.
5. Due to the `else if`, the `isFuel` check is **never reached**!
6. Fuel gets stuck in the player inventory and cannot be shift-clicked into the fuel slot.

### Mod Solution
- If the item is smeltable, it attempts to insert into input slot 0.
- If the item is also a fuel, and slot 0 could not accept all or any items, the remainder is redirected into fuel slot 1.
- If both slots are full, items cycle between inventory and hotbar as standard.

## License

This project is licensed under the **Apache-2.0 License**.
