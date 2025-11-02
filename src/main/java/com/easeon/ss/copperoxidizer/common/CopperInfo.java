package com.easeon.ss.copperoxidizer.common;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.HashMap;
import java.util.Map;

public class CopperInfo {
    public static class CopperRecord {
        public final Block nextStage;
        public final boolean sneaking;

        CopperRecord(Block nextStage, boolean requiresSneaking) {
            this.nextStage = nextStage;
            this.sneaking = requiresSneaking;
        }
    }

    public static final Map<Block, CopperRecord> OXIDATION_MAP = new HashMap<>();

    static {
        // 기본 구리 블록 (3단계까지만!)
        addCopperMapping(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER, false);
        addCopperMapping(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER, false);
        addCopperMapping(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER, false);

        // Cut Copper (3단계까지만!)
        addCopperMapping(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER, false);
        addCopperMapping(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER, false);
        addCopperMapping(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER, false);

        // Cut Copper Stairs (3단계까지만!)
        addCopperMapping(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS, false);
        addCopperMapping(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS, false);
        addCopperMapping(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS, false);

        // Cut Copper Slabs (3단계까지만!)
        addCopperMapping(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB, false);
        addCopperMapping(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB, false);
        addCopperMapping(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB, false);

        // Copper Bulb (3단계까지만!)
        addCopperMapping(Blocks.COPPER_BULB, Blocks.EXPOSED_COPPER_BULB, false);
        addCopperMapping(Blocks.EXPOSED_COPPER_BULB, Blocks.WEATHERED_COPPER_BULB, false);
        addCopperMapping(Blocks.WEATHERED_COPPER_BULB, Blocks.OXIDIZED_COPPER_BULB, false);

        // Copper Door (3단계까지만! - 스니킹 필요)
        addCopperMapping(Blocks.COPPER_DOOR, Blocks.EXPOSED_COPPER_DOOR, true);
        addCopperMapping(Blocks.EXPOSED_COPPER_DOOR, Blocks.WEATHERED_COPPER_DOOR, true);
        addCopperMapping(Blocks.WEATHERED_COPPER_DOOR, Blocks.OXIDIZED_COPPER_DOOR, true);

        // Copper Trapdoor (3단계까지만! - 스니킹 필요)
        addCopperMapping(Blocks.COPPER_TRAPDOOR, Blocks.EXPOSED_COPPER_TRAPDOOR, true);
        addCopperMapping(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WEATHERED_COPPER_TRAPDOOR, true);
        addCopperMapping(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.OXIDIZED_COPPER_TRAPDOOR, true);

        // Copper Grate (3단계까지만!)
        addCopperMapping(Blocks.COPPER_GRATE, Blocks.EXPOSED_COPPER_GRATE, false);
        addCopperMapping(Blocks.EXPOSED_COPPER_GRATE, Blocks.WEATHERED_COPPER_GRATE, false);
        addCopperMapping(Blocks.WEATHERED_COPPER_GRATE, Blocks.OXIDIZED_COPPER_GRATE, false);

        // Chiseled Copper (3단계까지만!)
        addCopperMapping(Blocks.CHISELED_COPPER, Blocks.EXPOSED_CHISELED_COPPER, false);
        addCopperMapping(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WEATHERED_CHISELED_COPPER, false);
        addCopperMapping(Blocks.WEATHERED_CHISELED_COPPER, Blocks.OXIDIZED_CHISELED_COPPER, false);

        // Copper Chest (3단계까지만!)
        addCopperMapping(Blocks.COPPER_CHEST, Blocks.EXPOSED_COPPER_CHEST, true);
        addCopperMapping(Blocks.EXPOSED_COPPER_CHEST, Blocks.WEATHERED_COPPER_CHEST, true);
        addCopperMapping(Blocks.WEATHERED_COPPER_CHEST, Blocks.OXIDIZED_COPPER_CHEST, true);

        // Copper Bars (3단계까지만!)
        addCopperMapping(Blocks.COPPER_BARS.unaffected(), Blocks.COPPER_BARS.exposed(), false);
        addCopperMapping(Blocks.COPPER_BARS.exposed(), Blocks.COPPER_BARS.weathered(), false);
        addCopperMapping(Blocks.COPPER_BARS.weathered(), Blocks.COPPER_BARS.oxidized(), false);

        // Copper Chain (3단계까지만!)
        addCopperMapping(Blocks.COPPER_CHAINS.unaffected(), Blocks.COPPER_CHAINS.exposed(), false);
        addCopperMapping(Blocks.COPPER_CHAINS.exposed(), Blocks.COPPER_CHAINS.weathered(), false);
        addCopperMapping(Blocks.COPPER_CHAINS.weathered(), Blocks.COPPER_CHAINS.oxidized(), false);

        // Copper Lantern (3단계까지만!)
        addCopperMapping(Blocks.COPPER_LANTERNS.unaffected(), Blocks.COPPER_LANTERNS.exposed(), false);
        addCopperMapping(Blocks.COPPER_LANTERNS.exposed(), Blocks.COPPER_LANTERNS.weathered(), false);
        addCopperMapping(Blocks.COPPER_LANTERNS.weathered(), Blocks.COPPER_LANTERNS.oxidized(), false);

        // Copper Golem Statue (3단계까지만! - 스니킹 필요)
        addCopperMapping(Blocks.COPPER_GOLEM_STATUE, Blocks.EXPOSED_COPPER_GOLEM_STATUE, true);
        addCopperMapping(Blocks.EXPOSED_COPPER_GOLEM_STATUE, Blocks.WEATHERED_COPPER_GOLEM_STATUE, true);
        addCopperMapping(Blocks.WEATHERED_COPPER_GOLEM_STATUE, Blocks.OXIDIZED_COPPER_GOLEM_STATUE, true);

        // Lightning Rod (3단계까지만!)
        addCopperMapping(Blocks.LIGHTNING_ROD, Blocks.EXPOSED_LIGHTNING_ROD, false);
        addCopperMapping(Blocks.EXPOSED_LIGHTNING_ROD, Blocks.WEATHERED_LIGHTNING_ROD, false);
        addCopperMapping(Blocks.WEATHERED_LIGHTNING_ROD, Blocks.OXIDIZED_LIGHTNING_ROD, false);
    }

    private static void addCopperMapping(Block current, Block next, boolean requiresSneaking) {
        OXIDATION_MAP.put(current, new CopperRecord(next, requiresSneaking));
    }
}
