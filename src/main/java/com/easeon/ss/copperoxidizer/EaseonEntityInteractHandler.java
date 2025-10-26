package com.easeon.ss.copperoxidizer;

import com.easeon.ss.copperoxidizer.common.Helper;
import com.easeon.ss.core.util.system.EaseonLogger;
import net.minecraft.block.Oxidizable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EaseonEntityInteractHandler {
    private static final EaseonLogger logger = EaseonLogger.of();
    private static Field OXIDATION_LEVEL_FIELD = null;
    private static final Map<UUID, Long> COOLDOWN_MAP = new HashMap<>();
    private static final long COOLDOWN_TICKS = 2;

    public static ActionResult onUseEntity(ServerPlayerEntity player, World world, Entity entity, Hand hand) {
//        logger.info("onUseEntity: hand-{}, client-{}", hand.toString(), world.isClient());

        if (hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }

        String entityType = entity.getType().toString();

        if (!entityType.contains("copper_golem")) {
            return ActionResult.PASS;
        }

        ItemStack heldItem = player.getStackInHand(hand);

        if (!heldItem.isOf(Items.POTION) || !Helper.isWaterBottle(heldItem)) {
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

        try {
            if (OXIDATION_LEVEL_FIELD == null) {
                for (Field field : entity.getClass().getDeclaredFields()) {
                    field.setAccessible(true);

                    if (TrackedData.class.isAssignableFrom(field.getType()) && java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        try {
                            Object fieldValue = field.get(null);

                            if (fieldValue instanceof TrackedData<?>) {
                                try {
                                    Object value = entity.getDataTracker().get((TrackedData<?>) fieldValue);

                                    if (value instanceof Oxidizable.OxidationLevel) {
                                        OXIDATION_LEVEL_FIELD = field;
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
            }

            if (OXIDATION_LEVEL_FIELD != null) {
                @SuppressWarnings("unchecked")
                TrackedData<Oxidizable.OxidationLevel> oxidationData =
                        (TrackedData<Oxidizable.OxidationLevel>) OXIDATION_LEVEL_FIELD.get(null);

                Oxidizable.OxidationLevel currentLevel = entity.getDataTracker().get(oxidationData);

                if (currentLevel != Oxidizable.OxidationLevel.OXIDIZED) {
                    Oxidizable.OxidationLevel nextLevel = getNextOxidationLevel(currentLevel);

                    entity.getDataTracker().set(oxidationData, nextLevel);

                } else {
                    return ActionResult.PASS;
                }
            } else {
                return ActionResult.FAIL;
            }

        } catch (Exception e) {
            return ActionResult.FAIL;
        }

        if (!player.getAbilities().creativeMode) {
            heldItem.decrement(1);
            ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().insertStack(emptyBottle)) {
                player.dropItem(emptyBottle, false);
            }
        }

        if (world instanceof ServerWorld serverWorld) {
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

        world.playSound(null, entity.getBlockPos(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.NEUTRAL, 1.0F, 1.0F);

        COOLDOWN_MAP.entrySet().removeIf(entry -> currentTime - entry.getValue() > 100);

//        logger.info("=== Oxidation process completed ===");
        return ActionResult.FAIL;
    }

    private static Oxidizable.OxidationLevel getNextOxidationLevel(Oxidizable.OxidationLevel current) {
        return switch (current) {
            case UNAFFECTED -> Oxidizable.OxidationLevel.EXPOSED;
            case EXPOSED -> Oxidizable.OxidationLevel.WEATHERED;
            case WEATHERED, OXIDIZED -> Oxidizable.OxidationLevel.OXIDIZED;
        };
    }
}