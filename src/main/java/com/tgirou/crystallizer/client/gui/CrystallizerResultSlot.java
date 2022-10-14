package com.tgirou.crystallizer.client.gui;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CrystallizerResultSlot extends SlotItemHandler {

    public CrystallizerResultSlot(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack p_39553_) {
        return false;
    }
}
