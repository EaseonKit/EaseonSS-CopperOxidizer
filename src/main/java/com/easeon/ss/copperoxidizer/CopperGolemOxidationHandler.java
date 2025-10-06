package com.easeon.ss.copperoxidizer;

import net.minecraft.block.Oxidizable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CopperGolemOxidationHandler {

    private static Field OXIDATION_LEVEL_FIELD = null;
    private static final Map<UUID, Long> COOLDOWN_MAP = new HashMap<>();
    private static final long COOLDOWN_TICKS = 2; // 2틱 쿨다운

    public static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (!Easeon.CONFIG.isEnabled()) {
            return ActionResult.PASS;
        }

        // 메인 핸드만 처리
        if (hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }

        String entityType = entity.getType().toString();

        if (!entityType.contains("copper_golem")) {
            return ActionResult.PASS;
        }

        ItemStack heldItem = player.getStackInHand(hand);

        if (!heldItem.isOf(Items.POTION) || !isWaterBottle(heldItem)) {
            return ActionResult.PASS;
        }

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        // 쿨다운 체크
        UUID entityId = entity.getUuid();
        long currentTime = world.getTime();

        if (COOLDOWN_MAP.containsKey(entityId)) {
            long lastUseTime = COOLDOWN_MAP.get(entityId);
            if (currentTime - lastUseTime < COOLDOWN_TICKS) {
                return ActionResult.PASS;
            }
        }

        // 쿨다운 설정
        COOLDOWN_MAP.put(entityId, currentTime);

        Easeon.LOGGER.info("Hand called: {}, Client: {}", hand, world.isClient());

        try {
            if (OXIDATION_LEVEL_FIELD == null) {
                for (Field field : entity.getClass().getDeclaredFields()) {
                    if (field.getName().equals("OXIDATION_LEVEL")) {
                        OXIDATION_LEVEL_FIELD = field;
                        OXIDATION_LEVEL_FIELD.setAccessible(true);
                        break;
                    }
                }
            }

            if (OXIDATION_LEVEL_FIELD != null) {
                @SuppressWarnings("unchecked")
                TrackedData<Oxidizable.OxidationLevel> oxidationData = (TrackedData<Oxidizable.OxidationLevel>) OXIDATION_LEVEL_FIELD.get(entity);

                Oxidizable.OxidationLevel currentLevel = entity.getDataTracker().get(oxidationData);

                if (currentLevel != Oxidizable.OxidationLevel.OXIDIZED) {
                    Oxidizable.OxidationLevel nextLevel = getNextOxidationLevel(currentLevel);
                    entity.getDataTracker().set(oxidationData, nextLevel);
                    Easeon.LOGGER.info("Oxidation: {} -> {}", currentLevel, nextLevel);
                } else {
                    return ActionResult.PASS;
                }
            }

        } catch (Exception e) {
            Easeon.LOGGER.error("Failed to modify oxidation level", e);
            return ActionResult.FAIL;
        }

        if (Easeon.CONFIG.shouldConsumeWater() && !player.getAbilities().creativeMode) {
            heldItem.decrement(1);
            ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().insertStack(emptyBottle)) {
                player.dropItem(emptyBottle, false);
            }
        }

        if (Easeon.CONFIG.shouldShowParticles() && world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                    ParticleTypes.WAX_ON,
                    entity.getX(),
                    entity.getY() + entity.getHeight() / 2,
                    entity.getZ(),
                    10,
                    0.3, 0.3, 0.3,
                    0.1
            );
        }

        if (Easeon.CONFIG.shouldPlaySound()) {
            world.playSound(
                    null,
                    entity.getBlockPos(),
                    SoundEvents.ITEM_BOTTLE_EMPTY,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1.0F
            );
        }

        // 오래된 쿨다운 데이터 정리 (100틱 이상 지난 것들)
        COOLDOWN_MAP.entrySet().removeIf(entry -> currentTime - entry.getValue() > 100);

        return ActionResult.SUCCESS;
    }

    private static Oxidizable.OxidationLevel getNextOxidationLevel(Oxidizable.OxidationLevel current) {
        return switch (current) {
            case UNAFFECTED -> Oxidizable.OxidationLevel.EXPOSED;
            case EXPOSED -> Oxidizable.OxidationLevel.WEATHERED;
            case WEATHERED -> Oxidizable.OxidationLevel.OXIDIZED;
            case OXIDIZED -> Oxidizable.OxidationLevel.OXIDIZED;
        };
    }

    private static boolean isWaterBottle(ItemStack stack) {
        if (!stack.isOf(Items.POTION)) {
            return false;
        }

        var potionContents = stack.get(net.minecraft.component.DataComponentTypes.POTION_CONTENTS);
        if (potionContents == null) {
            return false;
        }

        for (var effect : potionContents.getEffects()) {
            return false;
        }

        return true;
    }
}