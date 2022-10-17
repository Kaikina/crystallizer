package com.tgirou.crystallizer.client.gui;

import com.tgirou.crystallizer.api.util.Constants;
import com.tgirou.crystallizer.blocks.ModBlocks;
import com.tgirou.crystallizer.blocks.entities.CrystallizerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CrystallizerMenu extends AbstractContainerMenu {
    public final CrystallizerBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 2;

    public CrystallizerMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, inventory.player.level.getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(3));
    }

    public CrystallizerMenu(int id, Inventory inventory, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.CRYSTALLIZER_MENU.get(), id);
        checkContainerSize(inventory, 3);
        blockEntity = ((CrystallizerBlockEntity) entity);
        this.level = inventory.player.level;
        this.data = data;

        // Inventory slots
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Hotbar slots
        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
        }

        // GUI slots
        this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, 0, 80, 17));
            this.addSlot(new CrystallizerResultSlot(handler, 1, 80, 53));
        });

        addDataSlots(data);
    }

    public ResourceLocation getGUI() {
        String itemRegistryName = blockEntity.getItemStackHandler().getStackInSlot(2).getItem().getDescriptionId();
        if (itemRegistryName != null ) {
            if (itemRegistryName.equals("item.minecraft.iron_ingot")) {
                return new ResourceLocation(Constants.MOD_ID, "textures/gui/crystallizer_iron.png");
            } else if (itemRegistryName.equals("item.minecraft.copper_ingot")) {
                return new ResourceLocation(Constants.MOD_ID, "textures/gui/crystallizer_copper.png");
            } else if (itemRegistryName.equals("item.minecraft.diamond")) {
                return new ResourceLocation(Constants.MOD_ID, "textures/gui/crystallizer_diamond.png");
            } else if (itemRegistryName.equals("item.minecraft.emerald")) {
                return new ResourceLocation(Constants.MOD_ID, "textures/gui/crystallizer_emerald.png");
            } else if (itemRegistryName.equals("item.minecraft.gold_ingot")) {
                return new ResourceLocation(Constants.MOD_ID, "textures/gui/crystallizer_gold.png");
            } else if (itemRegistryName.equals("item.minecraft.quartz")) {
                return new ResourceLocation(Constants.MOD_ID, "textures/gui/crystallizer_quartz.png");
            }
        }

        return new ResourceLocation(Constants.MOD_ID, "textures/gui/crystallizer.png");
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;  //EMPTY_ITEM
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots
        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;  // EMPTY_ITEM
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX,
                    VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null
        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player,
                ModBlocks.CRYSTALLIZER.get());
    }

    public int getCrystallizeProgress() {
        int i = this.data.get(0); // Crystallizing Time
        int j = this.data.get(1); // Crystallizing Total Time
        return j != 0 && i != 0 ? i * 16 / j : 0;
    }
}
