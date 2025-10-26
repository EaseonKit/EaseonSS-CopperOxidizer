package com.easeon.ss.copperoxidizer.common;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class Helper {
    public static boolean isWaterBottle(ItemStack stack) {
        if (!stack.isOf(Items.POTION)) {
            return false;
        }

        var potionContents = stack.get(net.minecraft.component.DataComponentTypes.POTION_CONTENTS);

        if (potionContents == null) {
            return false;
        }

        // 물병은 potion 타입이 없거나 WATER인 경우
        var potion = potionContents.potion();

        // 포션 타입이 없으면 물병
        return potion.map(potionRegistryEntry -> potionRegistryEntry.equals(net.minecraft.potion.Potions.WATER)).orElse(true);

        // Potions.WATER와 비교
    }
}
