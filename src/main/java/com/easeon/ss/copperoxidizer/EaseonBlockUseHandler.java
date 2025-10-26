package com.easeon.ss.copperoxidizer;

import com.easeon.ss.copperoxidizer.common.Helper;
import com.easeon.ss.core.util.system.EaseonLogger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.easeon.ss.copperoxidizer.common.CopperInfo.OXIDATION_MAP;

public class EaseonBlockUseHandler {
    private static final EaseonLogger logger = EaseonLogger.of();

    public static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
//        logger.debug("onUseBlock: hand-{}, client-{}", hand.toString(), world.isClient());

        ItemStack heldItem = player.getStackInHand(hand);

        // 물병이 아니면 패스
        if (!heldItem.isOf(Items.POTION) || !Helper.isWaterBottle(heldItem)) {
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
        var copperInfo = OXIDATION_MAP.get(block);

        if (copperInfo == null || copperInfo.requiresSneaking != player.isSneaking()) {
            return ActionResult.PASS;
        }

        // 클라이언트: 이벤트 완전 소비 (다른 상호작용 차단)
        if (world.isClient()) {
            return ActionResult.CONSUME;
        }

        // ===== 서버에서만 실제 산화 처리 =====
        BlockState newState = copperInfo.nextStage.getDefaultState();

        // 기존 블록의 속성 복사
        for (var property : state.getProperties()) {
            if (newState.contains(property)) {
                newState = copyProperty(state, newState, property);
            }
        }

        world.setBlockState(pos, newState);

        // 물 소모
        if (!player.getAbilities().creativeMode) {
            heldItem.decrement(1);

            ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!player.getInventory().insertStack(emptyBottle)) {
                player.dropItem(emptyBottle, false);
            }
        }

        // 파티클 효과
        if (world instanceof ServerWorld serverWorld) {
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
        world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);

        return ActionResult.SUCCESS;
    }

    private static boolean isWaxed(Block block) {
        String blockName = block.getTranslationKey();
        return blockName.contains("waxed");
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, net.minecraft.state.property.Property<?> property) {
        return to.with((net.minecraft.state.property.Property<T>) property, (T) from.get(property));
    }
}