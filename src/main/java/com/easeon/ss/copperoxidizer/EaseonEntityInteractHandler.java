package com.easeon.ss.copperoxidizer;

import com.easeon.ss.core.util.system.EaseonLogger;
import com.easeon.ss.core.wrapper.EaseonEntity;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import com.easeon.ss.core.wrapper.EaseonWorld;
import net.minecraft.block.Oxidizable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class EaseonEntityInteractHandler {
    private static final EaseonLogger logger = EaseonLogger.of();
    private static Field OXIDATION_LEVEL_FIELD = null;

    public static ActionResult onUseEntity(EaseonWorld world, EaseonPlayer player, Hand hand, EaseonEntity entity) {
        if (entity.not(EntityType.COPPER_GOLEM))
            return ActionResult.PASS;

        var heldItem = player.getStackInHand(hand);
        if (heldItem.not(Items.POTION) || !heldItem.isWaterBottle()) {
            return ActionResult.PASS;
        }

        try {
            if (OXIDATION_LEVEL_FIELD == null) {
                for (Field field : entity.get().getClass().getDeclaredFields()) {
                    field.setAccessible(true);

                    if (TrackedData.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())) {
                        try {
                            var fieldValue = field.get(null);
                            if (fieldValue instanceof TrackedData<?>) {
                                try {
                                    var value = entity.get().getDataTracker().get((TrackedData<?>) fieldValue);
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
                //noinspection unchecked
                var oxidationData = (TrackedData<Oxidizable.OxidationLevel>) OXIDATION_LEVEL_FIELD.get(null);
                var currentLevel = entity.get().getDataTracker().get(oxidationData);
                if (currentLevel != Oxidizable.OxidationLevel.OXIDIZED) {
                    Oxidizable.OxidationLevel nextLevel = getNextOxidationLevel(currentLevel);
                    entity.get().getDataTracker().set(oxidationData, nextLevel);
                } else {
                    return ActionResult.PASS;
                }
            } else {
                return ActionResult.FAIL;
            }

        } catch (Exception e) {
            return ActionResult.FAIL;
        }

        if (player.isSurvival()) {
            player.removeItem(heldItem, 1);
            player.giveOrDropItem(Items.GLASS_BOTTLE, 1);
        }

        world.particles(ParticleTypes.WAX_ON, entity.getX(), entity.getY() + entity.getHeight() / 2, entity.getZ(), 10, 0.3, 0.3, 0.3, 0.1);
        world.playSound(entity.getPos(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f);

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