package com.easeon.ss.copperoxidizer;

import com.easeon.ss.core.game.EaseonItem;
import com.easeon.ss.core.game.EaseonParticle;
import com.easeon.ss.core.game.EaseonSound;
import com.easeon.ss.core.helper.BlockHelper;
import com.easeon.ss.core.helper.ItemHelper;
import com.easeon.ss.core.util.system.EaseonLogger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import static com.easeon.ss.copperoxidizer.common.CopperInfo.OXIDATION_MAP;

public class EaseonBlockUseHandler {
    private static final EaseonLogger logger = EaseonLogger.of();

    public static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        var heldItem = player.getStackInHand(hand);

        if (!ItemHelper.isWaterBottle(heldItem)) {
            return ActionResult.PASS;
        }

        var pos = hitResult.getBlockPos();
        var state = world.getBlockState(pos);
        var block = state.getBlock();

        // 왁스 칠한 블록은 산화 불가
        if (ItemHelper.isWaxed(block)) {
            return ActionResult.PASS;
        }

        // 산화 가능한 블록인지 확인
        var copperInfo = OXIDATION_MAP.get(block);
        if (copperInfo == null || copperInfo.sneaking != player.isSneaking()) {
            return ActionResult.PASS;
        }

        // 클라이언트: 이벤트 완전 소비 (다른 상호작용 차단)
        if (world.isClient()) {
            return ActionResult.CONSUME;
        }

        var newState = copperInfo.nextStage.getDefaultState();
        newState = BlockHelper.copyAllProperties(state, newState);
        world.setBlockState(pos, newState);

        EaseonItem.removeItem(player, heldItem);
        if (!player.isCreative()) {
            EaseonItem.giveOrDropItem(player, Items.GLASS_BOTTLE);
        }

        EaseonParticle.spawn(world, ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.1);
        EaseonSound.playAll(world, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS);

        return ActionResult.SUCCESS;
    }
}