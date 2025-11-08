package com.easeon.ss.copperoxidizer;

import com.easeon.ss.core.helper.CopperHelper;
import com.easeon.ss.core.util.system.EaseonLogger;
import com.easeon.ss.core.wrapper.EaseonPlayer;
import com.easeon.ss.core.wrapper.EaseonWorld;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class EaseonBlockUseHandler {
    private static final EaseonLogger logger = EaseonLogger.of();

    public static ActionResult onUseBlock(ServerPlayerEntity playerEntity, World mcWorld, Hand hand, BlockHitResult hitResult) {
        var player = new EaseonPlayer(playerEntity);
        var heldItem = player.getStackInHand(hand);

        if (!heldItem.isWaterBottle()) {
            return ActionResult.PASS;
        }

        var world = new EaseonWorld(mcWorld);
        var pos = hitResult.getBlockPos();
        var block = world.getBlockState(pos);
        var item = block.easeonItem();

        // 산화 가능한 블록인지 확인
        if (CopperHelper.isSneakingRequired(item) && !player.isSneaking())
            return ActionResult.PASS;

        var oxidizer = CopperHelper.oxidize(item);
        if (oxidizer.isEmpty())
            return ActionResult.PASS;

        var newState = oxidizer.get().get();
        newState = block.copyAllProperties(newState);
        world.setBlockState(pos, newState);

        player.removeItem(heldItem, 1);
        if (player.isSurvival()) {
            player.giveOrDropItem(Items.GLASS_BOTTLE, 1);
        }

        world.particles(ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.1);
        world.playSound(player.getPos(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f);

        return ActionResult.SUCCESS;
    }
}