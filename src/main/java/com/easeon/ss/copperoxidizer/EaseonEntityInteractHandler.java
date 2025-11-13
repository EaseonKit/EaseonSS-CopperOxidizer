package com.easeon.ss.copperoxidizer;

import com.easeon.ss.core.util.system.EaseonLogger;
import com.easeon.ss.core.wrapper.EaseonEntity;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import com.easeon.ss.core.wrapper.EaseonWorld;
import net.minecraft.block.Oxidizable.OxidationLevel;
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

    @SuppressWarnings("unchecked")
    public static ActionResult onUseEntity(EaseonWorld world, EaseonPlayer player, Hand hand, EaseonEntity entity) {
        if (entity.not(EntityType.COPPER_GOLEM))
            return ActionResult.PASS;

        final var heldItem = player.getStackInHand(hand);
        if (!heldItem.isWaterBottle())
            return ActionResult.PASS;

        try {
            if (OXIDATION_LEVEL_FIELD == null)
                findOxidationLevelField(entity);

            if (OXIDATION_LEVEL_FIELD != null) {
                final var oxidationData = (TrackedData<OxidationLevel>) OXIDATION_LEVEL_FIELD.get(null);
                final var currentLevel = entity.get().getDataTracker().get(oxidationData);
                if (currentLevel == OxidationLevel.OXIDIZED)
                    return ActionResult.PASS;

                final var nextLevel = getNextOxidationLevel(currentLevel);
                entity.get().getDataTracker().set(oxidationData, nextLevel);

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

    private static void findOxidationLevelField(EaseonEntity entity) {
        for (var field : entity.get().getClass().getDeclaredFields()) {
            field.setAccessible(true);

            if (TrackedData.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())) {
                try {
                    var fieldValue = field.get(null);
                    if (fieldValue instanceof TrackedData<?>) {
                        var value = entity.get().getDataTracker().get((TrackedData<?>) fieldValue);
                        if (value instanceof OxidationLevel) {
                            OXIDATION_LEVEL_FIELD = field;
                            return;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Could not access field: {}", field.getName());
                }
            }
        }
    }

    private static OxidationLevel getNextOxidationLevel(OxidationLevel current) {
        return switch (current) {
            case UNAFFECTED -> OxidationLevel.EXPOSED;
            case EXPOSED -> OxidationLevel.WEATHERED;
            case WEATHERED, OXIDIZED -> OxidationLevel.OXIDIZED;
        };
    }
}