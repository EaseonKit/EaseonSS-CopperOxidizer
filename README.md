## 🌿 Easeon - Copper Oxidizer
**Minecraft:** `1.21.10`, `1.21.9`  
**Loader:** `Fabric`  
**Side:** `Server-Side`, `Singleplayer`

## Overview
Copper Oxidizer is a server-side Fabric mod that allows players to accelerate copper oxidation using water bottles. Perfect for builders who want to quickly age copper blocks and golems without waiting for natural oxidation.

## Features
- **Instant Oxidation**: Right-click copper blocks or golems with water bottles to advance oxidation stages
- **Comprehensive Support**: Works with all vanilla copper variants (blocks, stairs, slabs, doors, trapdoors, bulbs, grates, etc.)
- **Waxed Block Protection**: Waxed copper blocks cannot be oxidized
- **Server-Side Only**: No client installation required for players to benefit

## Commands
All commands require OP level 2 permission.  

**View Current Status:**
```
/easeon copperoxidizer
```
**Enable Copper Oxidizing:**
```
/easeon copperoxidizer on
```
**Disable Copper Oxidizing:**
```
/easeon copperoxidizer off
```

## Configuration
```json
{
  "enabled": true,
  "requiredOpLevel": 2, // Requires a server restart to take effect.
  "consumeWater": true,
  "showParticles": true,
  "playSound": true
}
```
`config/easeon/easeon.copperoxidizer.json`

**Configuration Options:**
- enabled: Enable/disable the mod functionality
- requiredOpLevel: Required OP level to change settings (default: 2)
- consumeWater: Whether water bottles are consumed on use
- showParticles: Show particle effects when oxidizing
- playSound: Play sound effects when oxidizing
