package com.easeon.ss.copperoxidizer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.Map;

public class OxidationHandler {

    // 구리 블록 산화 단계 매핑 (4단계는 절대 포함 안 함!)
    private static final Map<Block, Block> OXIDATION_MAP = new HashMap<>();

    static {
        // === 기존 바닐라 구리 블록들 ===

        // 기본 구리 블록 (3단계까지만!)
        OXIDATION_MAP.put(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER);
        OXIDATION_MAP.put(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER);
        OXIDATION_MAP.put(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER);

        // Cut Copper (3단계까지만!)
        OXIDATION_MAP.put(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER);
        OXIDATION_MAP.put(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER);
        OXIDATION_MAP.put(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER);

        // Cut Copper Stairs (3단계까지만!)
        OXIDATION_MAP.put(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS);
        OXIDATION_MAP.put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS);
        OXIDATION_MAP.put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS);

        // Cut Copper Slabs (3단계까지만!)
        OXIDATION_MAP.put(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB);
        OXIDATION_MAP.put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB);
        OXIDATION_MAP.put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB);

        // Copper Bulb (3단계까지만!)
        OXIDATION_MAP.put(Blocks.COPPER_BULB, Blocks.EXPOSED_COPPER_BULB);
        OXIDATION_MAP.put(Blocks.EXPOSED_COPPER_BULB, Blocks.WEATHERED_COPPER_BULB);
        OXIDATION_MAP.put(Blocks.WEATHERED_COPPER_BULB, Blocks.OXIDIZED_COPPER_BULB);

        // Copper Door (3단계까지만!)
        OXIDATION_MAP.put(Blocks.COPPER_DOOR, Blocks.EXPOSED_COPPER_DOOR);
        OXIDATION_MAP.put(Blocks.EXPOSED_COPPER_DOOR, Blocks.WEATHERED_COPPER_DOOR);
        OXIDATION_MAP.put(Blocks.WEATHERED_COPPER_DOOR, Blocks.OXIDIZED_COPPER_DOOR);

        // Copper Trapdoor (3단계까지만!)
        OXIDATION_MAP.put(Blocks.COPPER_TRAPDOOR, Blocks.EXPOSED_COPPER_TRAPDOOR);
        OXIDATION_MAP.put(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WEATHERED_COPPER_TRAPDOOR);
        OXIDATION_MAP.put(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.OXIDIZED_COPPER_TRAPDOOR);

        // Copper Grate (3단계까지만!)
        OXIDATION_MAP.put(Blocks.COPPER_GRATE, Blocks.EXPOSED_COPPER_GRATE);
        OXIDATION_MAP.put(Blocks.EXPOSED_COPPER_GRATE, Blocks.WEATHERED_COPPER_GRATE);
        OXIDATION_MAP.put(Blocks.WEATHERED_COPPER_GRATE, Blocks.OXIDIZED_COPPER_GRATE);

        // Chiseled Copper (3단계까지만!)
        OXIDATION_MAP.put(Blocks.CHISELED_COPPER, Blocks.EXPOSED_CHISELED_COPPER);
        OXIDATION_MAP.put(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WEATHERED_CHISELED_COPPER);
        OXIDATION_MAP.put(Blocks.WEATHERED_CHISELED_COPPER, Blocks.OXIDIZED_CHISELED_COPPER);

        // === Copper Age 업데이트 새 블록들 (Registry 사용) ===

        // Copper Chest (3단계까지만!)
        addCopperBlockMapping("copper_chest", "exposed_copper_chest", "weathered_copper_chest", "oxidized_copper_chest");

        // Copper Bars (3단계까지만!)
        addCopperBlockMapping("copper_bars", "exposed_copper_bars", "weathered_copper_bars", "oxidized_copper_bars");

        // Copper Chain (3단계까지만!)
        addCopperBlockMapping("copper_chain", "exposed_copper_chain", "weathered_copper_chain", "oxidized_copper_chain");

        // Copper Lantern (3단계까지만!)
        addCopperBlockMapping("copper_lantern", "exposed_copper_lantern", "weathered_copper_lantern", "oxidized_copper_lantern");

        // Copper Golem Statue (3단계까지만!)
        addCopperBlockMapping("copper_golem_statue", "exposed_copper_golem_statue", "weathered_copper_golem_statue", "oxidized_copper_golem_statue");

        // Lightning Rod (3단계까지만!)
        addCopperBlockMapping("lightning_rod", "exposed_lightning_rod", "weathered_lightning_rod", "oxidized_lightning_rod");

        Easeon.LOGGER.info("Copper Oxidizer: {} oxidation mappings loaded", OXIDATION_MAP.size());
    }

    // Registry를 사용해 블록 매핑 추가 (4단계는 절대 추가 안 함!)
    private static void addCopperBlockMapping(String base, String exposed, String weathered, String oxidized) {
        try {
            Block baseBlock = Registries.BLOCK.get(Identifier.ofVanilla(base));
            Block exposedBlock = Registries.BLOCK.get(Identifier.ofVanilla(exposed));
            Block weatheredBlock = Registries.BLOCK.get(Identifier.ofVanilla(weathered));
            Block oxidizedBlock = Registries.BLOCK.get(Identifier.ofVanilla(oxidized));

            // 에어 블록이 아닌 경우에만 3단계까지 매핑 추가
            if (!baseBlock.equals(Blocks.AIR) && !exposedBlock.equals(Blocks.AIR)) {
                OXIDATION_MAP.put(baseBlock, exposedBlock);
                OXIDATION_MAP.put(exposedBlock, weatheredBlock);
                OXIDATION_MAP.put(weatheredBlock, oxidizedBlock);
                // oxidizedBlock는 키로 추가 안 함! (더 이상 산화 안 됨)
                Easeon.LOGGER.info("Added oxidation mapping for: {}", base);
            }
        } catch (Exception e) {
            Easeon.LOGGER.debug("Block not found: {} (probably not in this version)", base);
        }
    }

    public static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (!Easeon.CONFIG.isEnabled()) {
            return ActionResult.PASS;
        }

        ItemStack heldItem = player.getStackInHand(hand);

        // 물병이 아니면 패스
        if (!heldItem.isOf(Items.POTION) || !isWaterBottle(heldItem)) {
            return ActionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // 왁스 칠한 블록은 산화 불가
        if (isWaxed(block)) {
            return ActionResult.PASS;
        }

        // 산화 가능한 블록인지 확인
        Block nextBlock = getNextOxidationStage(block);

        if (nextBlock == null) {
            return ActionResult.PASS;
        }

        // 클라이언트: 이벤트 완전 소비 (다른 상호작용 차단)
        if (world.isClient()) {
            return ActionResult.CONSUME;
        }

        // ===== 서버에서만 실제 산화 처리 =====

        BlockState newState = nextBlock.getDefaultState();

        // 기존 블록의 속성 복사
        for (var property : state.getProperties()) {
            if (newState.contains(property)) {
                newState = copyProperty(state, newState, property);
            }
        }

        world.setBlockState(pos, newState);

        // 물 소모
        if (Easeon.CONFIG.shouldConsumeWater() && !player.getAbilities().creativeMode) {
            heldItem.decrement(1);

            ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().insertStack(emptyBottle)) {
                player.dropItem(emptyBottle, false);
            }
        }

        // 파티클 효과
        if (Easeon.CONFIG.shouldShowParticles() && world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    ParticleTypes.WAX_ON,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    10,
                    0.3, 0.3, 0.3,
                    0.1
            );
        }

        // 사운드 재생
        if (Easeon.CONFIG.shouldPlaySound()) {
            world.playSound(
                    null,
                    pos,
                    SoundEvents.ITEM_BOTTLE_EMPTY,
                    SoundCategory.BLOCKS,
                    1.0F,
                    1.0F
            );
        }

        return ActionResult.CONSUME;
    }

    private static boolean isWaterBottle(ItemStack stack) {
        if (!stack.isOf(Items.POTION)) {
            return false;
        }

        var potionContents = stack.get(net.minecraft.component.DataComponentTypes.POTION_CONTENTS);

        if (potionContents == null) {
            return false;
        }

        // 효과가 하나도 없으면 물병
        boolean hasEffects = false;
        for (var effect : potionContents.getEffects()) {
            hasEffects = true;
            break;
        }

        return !hasEffects;
    }

    private static boolean isWaxed(Block block) {
        String blockName = block.getTranslationKey();
        return blockName.contains("waxed");
    }

    private static Block getNextOxidationStage(Block block) {
        // 매핑에 있으면 반환, 없으면 null (4단계는 매핑에 없으므로 무조건 null!)
        return OXIDATION_MAP.get(block);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, net.minecraft.state.property.Property<?> property) {
        return to.with((net.minecraft.state.property.Property<T>) property, (T) from.get(property));
    }
}