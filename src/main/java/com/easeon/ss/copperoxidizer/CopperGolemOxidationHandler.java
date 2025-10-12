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
    private static final long COOLDOWN_TICKS = 2;

    public static ActionResult onUseEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        // Easeon.LOGGER.info("=== onUseEntity called ===");

        if (!Easeon.CONFIG.isEnabled()) {
            return ActionResult.PASS;
        }

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

        UUID entityId = entity.getUuid();
        long currentTime = world.getTime();

        if (COOLDOWN_MAP.containsKey(entityId)) {
            long lastUseTime = COOLDOWN_MAP.get(entityId);
            if (currentTime - lastUseTime < COOLDOWN_TICKS) {
                return ActionResult.PASS;
            }
        }

        COOLDOWN_MAP.put(entityId, currentTime);

        // Easeon.LOGGER.info("=== Starting oxidation process (SERVER SIDE) ===");

        try {
            if (OXIDATION_LEVEL_FIELD == null) {
                // Easeon.LOGGER.info("Searching for OXIDATION_LEVEL field...");
                // Easeon.LOGGER.info("Entity class: {}", entity.getClass().getName());

                for (Field field : entity.getClass().getDeclaredFields()) {
                    field.setAccessible(true);

                    // Easeon.LOGGER.info("Checking field: {} (type: {})", field.getName(), field.getType().getName());

                    if (TrackedData.class.isAssignableFrom(field.getType())
                            && java.lang.reflect.Modifier.isStatic(field.getModifiers())) {

                        try {
                            Object fieldValue = field.get(null);
                            // Easeon.LOGGER.info("Found TrackedData field: {}, value: {}", field.getName(), fieldValue);

                            if (fieldValue instanceof TrackedData<?>) {
                                try {
                                    Object value = entity.getDataTracker().get((TrackedData<?>) fieldValue);
                                    // Easeon.LOGGER.info("Field {} contains value of type: {}", field.getName(), value.getClass().getName());

                                    if (value instanceof Oxidizable.OxidationLevel) {
                                        OXIDATION_LEVEL_FIELD = field;
                                        // Easeon.LOGGER.info("SUCCESS! Found correct OXIDATION_LEVEL field: {}", field.getName());
                                        break;
                                    }
                                } catch (Exception e) {
                                    // Easeon.LOGGER.debug("Field {} is not the oxidation level field", field.getName());
                                }
                            }
                        } catch (Exception e) {
                            // Easeon.LOGGER.debug("Could not access field: {}", field.getName());
                        }
                    }
                }

                // if (OXIDATION_LEVEL_FIELD == null) {
                //     Easeon.LOGGER.error("OXIDATION_LEVEL field not found!");
                // }
            }

            if (OXIDATION_LEVEL_FIELD != null) {
                // Easeon.LOGGER.info("Getting oxidation data from field: {}", OXIDATION_LEVEL_FIELD.getName());

                @SuppressWarnings("unchecked")
                TrackedData<Oxidizable.OxidationLevel> oxidationData =
                        (TrackedData<Oxidizable.OxidationLevel>) OXIDATION_LEVEL_FIELD.get(null);

                Oxidizable.OxidationLevel currentLevel = entity.getDataTracker().get(oxidationData);
                // Easeon.LOGGER.info("Current oxidation level: {}", currentLevel);

                if (currentLevel != Oxidizable.OxidationLevel.OXIDIZED) {
                    Oxidizable.OxidationLevel nextLevel = getNextOxidationLevel(currentLevel);
                    // Easeon.LOGGER.info("Setting new level: {} -> {}", currentLevel, nextLevel);

                    entity.getDataTracker().set(oxidationData, nextLevel);

                    // Oxidizable.OxidationLevel verifyLevel = entity.getDataTracker().get(oxidationData);
                    // Easeon.LOGGER.info("Verification - New level is: {}", verifyLevel);

                    // if (verifyLevel == nextLevel) {
                    //     Easeon.LOGGER.info("Oxidation SUCCESS!");
                    // } else {
                    //     Easeon.LOGGER.error("Oxidation FAILED - level did not change!");
                    // }
                } else {
                    // Easeon.LOGGER.info("Already fully oxidized");
                    return ActionResult.PASS;
                }
            } else {
                // Easeon.LOGGER.error("OXIDATION_LEVEL_FIELD is null, cannot proceed");
                return ActionResult.FAIL;
            }

        } catch (Exception e) {
            // Easeon.LOGGER.error("Failed to modify oxidation level", e);
            // e.printStackTrace();
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

        COOLDOWN_MAP.entrySet().removeIf(entry -> currentTime - entry.getValue() > 100);

        // Easeon.LOGGER.info("=== Oxidation process completed ===");
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