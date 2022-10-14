package com.tgirou.crystallizer.blocks.entities;

import com.tgirou.crystallizer.blocks.customs.CrystallizerBlock;
import com.tgirou.crystallizer.client.gui.CrystallizerMenu;
import com.tgirou.crystallizer.networking.Messages;
import com.tgirou.crystallizer.networking.packet.ItemStackSyncPacket;
import com.tgirou.crystallizer.recipes.CrystallizerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CrystallizerBlockEntity extends BlockEntity implements MenuProvider, WorldlyContainer {
    private LazyOptional<IItemHandler> lazyOptionalItemHandler = LazyOptional.empty();
    private static final int[] SLOTS_FOR_UP = new int[]{2};
    private static final int[] SLOTS_FOR_DOWN = new int[]{1};
    private static final int[] SLOTS_FOR_SIDES = new int[]{2};

    private final ItemStackHandler itemStackHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            assert level != null;
            if (!level.isClientSide()) {
                Messages.sendToClients(new ItemStackSyncPacket(this, worldPosition));
            }
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 1 -> false;
                case 0, 2 -> !stack.sameItem(itemStackHandler.getStackInSlot(2)) && canCrystallizeHeldItem(stack);
                default -> super.isItemValid(slot, stack);
            };
        }
    };

    private final Map<Direction, LazyOptional<WrappedHandler>> directionWrappedHandlerMap =
            Map.of(Direction.DOWN, LazyOptional.of(() -> new WrappedHandler(itemStackHandler, i -> i == 1,
                            (i, s) -> false)),
                    Direction.NORTH, LazyOptional.of(() -> new WrappedHandler(itemStackHandler, index -> index == 0,
                            (index, stack) -> itemStackHandler.isItemValid(2, stack))),
                    Direction.SOUTH, LazyOptional.of(() -> new WrappedHandler(itemStackHandler, i -> i == 1,
                            (i, s) -> false)),
                    Direction.EAST, LazyOptional.of(() -> new WrappedHandler(itemStackHandler, i -> i == 0,
                            (index, stack) -> itemStackHandler.isItemValid(2, stack))),
                    Direction.WEST, LazyOptional.of(() -> new WrappedHandler(itemStackHandler, index -> index == 2 || index == 0,
                            (index, stack) -> itemStackHandler.isItemValid(2, stack) || itemStackHandler.isItemValid(0, stack))));

    int crystallizingProgress;
    int crystallizingTotalTime;
    protected final ContainerData data;

    public CrystallizerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.CRYSTALLIZER.get(), blockPos, blockState);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> CrystallizerBlockEntity.this.crystallizingProgress;
                    case 1 -> CrystallizerBlockEntity.this.crystallizingTotalTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    CrystallizerBlockEntity.this.crystallizingProgress = value;
                } else {
                    CrystallizerBlockEntity.this.crystallizingTotalTime = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public ItemStack getResultStack() {
        ItemStack stack = new ItemStack(Items.AIR);

        if (!itemStackHandler.getStackInSlot(1).isEmpty()) {
            stack = itemStackHandler.getStackInSlot(1);
        }

        return stack;
    }

    public ItemStack getCrystallizingStack() {
        ItemStack stack = new ItemStack(Items.AIR);

        if (!itemStackHandler.getStackInSlot(2).isEmpty()) {
            stack = itemStackHandler.getStackInSlot(2);
        }

        return stack;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return new TranslatableComponent("block.crystallizer.crystallizer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, @NotNull Inventory inventory, @NotNull Player player) {
        return new CrystallizerMenu(id, inventory, this, this.data);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyOptionalItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag compoundTag) {
        compoundTag.putInt("CrystallizeTime", this.crystallizingProgress);
        compoundTag.putInt("CrystallizeTotalTime", this.crystallizingTotalTime);
        compoundTag.put("inventory", itemStackHandler.serializeNBT());
        super.saveAdditional(compoundTag);
    }

    @Override
    public void load(@NotNull CompoundTag compoundTag) {
        super.load(compoundTag);
        itemStackHandler.deserializeNBT(compoundTag.getCompound("inventory"));
        this.crystallizingProgress = compoundTag.getInt("CrystallizeTime");
        this.crystallizingTotalTime = compoundTag.getInt("CrystallizeTimeTotal");
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 0);
        }
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        inventory.setItem(0, itemStackHandler.getStackInSlot(1));
        inventory.setItem(1, itemStackHandler.getStackInSlot(0));

        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @NotNull
    @Override
    public IModelData getModelData() {
        return super.getModelData();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == null) {
                return lazyOptionalItemHandler.cast();
            }

            if (directionWrappedHandlerMap.containsKey(side)) {
                Direction localDir = this.getBlockState().getValue(CrystallizerBlock.FACING);

                if (side == Direction.UP || side == Direction.DOWN) {
                    return directionWrappedHandlerMap.get(side).cast();
                }

                return switch (localDir) {
                    case EAST -> directionWrappedHandlerMap.get(side.getClockWise()).cast();
                    case SOUTH -> directionWrappedHandlerMap.get(side).cast();
                    case WEST -> directionWrappedHandlerMap.get(side.getCounterClockWise()).cast();
                    default -> directionWrappedHandlerMap.get(side.getOpposite()).cast();
                };
            }
        }
        return super.getCapability(cap, side);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrystallizerBlockEntity entity) {
        boolean flag1 = false;
        SimpleContainer inventory = new SimpleContainer(entity.itemStackHandler.getSlots());
        for (int i = 0; i < entity.itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, entity.itemStackHandler.getStackInSlot(i));
        }
        if (Boolean.TRUE.equals(state.getValue(CrystallizerBlock.POWERED)) && (!inventory.getItem(0).isEmpty() ||
                !inventory.getItem(2).isEmpty())) {
            Recipe<?> recipe = level.getRecipeManager().getRecipeFor(CrystallizerRecipe.Type.INSTANCE, inventory, level)
                    .orElse(null);
            // Max Stack Size
            int i = 64;
            if (entity.canCrystallize(recipe, entity.itemStackHandler, i)) {
                // Save crystallizingItem to slot 2, and remove from input slot (if input has item)
                if (!inventory.getItem(0).isEmpty()) {
                    entity.itemStackHandler.setStackInSlot(2, new ItemStack(entity.itemStackHandler
                            .getStackInSlot(0).getItem()));
                    entity.itemStackHandler.getStackInSlot(0).shrink(1);
                    entity.crystallizingProgress = 0;
                    entity.crystallizingTotalTime = getTotalCrystallizingTime(level, entity.itemStackHandler);
                    level.sendBlockUpdated(entity.getBlockPos(),
                            entity.getBlockState(),
                            entity.getBlockState(),
                            0
                    );
                }
                ++entity.crystallizingProgress;
                entity.crystallizingTotalTime = getTotalCrystallizingTime(level, entity.itemStackHandler);
                flag1 = true;
                state = state.setValue(CrystallizerBlock.LIT, Boolean.TRUE);
                level.setBlock(pos, state, 3);
                if (entity.crystallizingProgress >= entity.crystallizingTotalTime) {
                    entity.crystallizingProgress = 0;
                    entity.crystallize(recipe, entity.itemStackHandler, i);
                }
            } else {
                entity.crystallizingProgress = 0;
                state = state.setValue(CrystallizerBlock.LIT, Boolean.FALSE);
                level.setBlock(pos, state, 3);
                flag1 = true;
            }
        }

        if (flag1) {
            setChanged(level, pos, state);
        }
    }

    private void crystallize(@javax.annotation.Nullable Recipe<?> recipe, ItemStackHandler handler, int maxStack) {
        if (recipe != null && this.canCrystallize(recipe, handler, maxStack)) {
            ItemStack outputStack = handler.getStackInSlot(1);
            if (outputStack.isEmpty()) {
                handler.setStackInSlot(1, new ItemStack(handler.getStackInSlot(2).getItem(), 1));
            } else if (outputStack.is(handler.getStackInSlot(2).getItem())) {
                outputStack.grow(1);
            }
        }
    }

    public void startCrystallizeHeldItem(ItemStack heldItems) {
        itemStackHandler.setStackInSlot(2, heldItems);
        crystallizingProgress = 0;
        crystallizingTotalTime = getTotalCrystallizingTime(level, itemStackHandler);
        assert level != null;
        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 0);
    }

    private static int getTotalCrystallizingTime(Level level, ItemStackHandler itemStackHandler) {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        }
        return level.getRecipeManager().getRecipeFor(CrystallizerRecipe.Type.INSTANCE, inventory, level)
                .map(CrystallizerRecipe::getCrystallizingTime).orElse(200);
    }

    private boolean canCrystallize(@Nullable Recipe<?> recipe, ItemStackHandler handler, int maxStackSize) {
        if ((!handler.getStackInSlot(2).isEmpty() || !handler.getStackInSlot(0).isEmpty()) && recipe != null) {
            ItemStack outputStack = handler.getStackInSlot(1);
            ItemStack inputStack = handler.getStackInSlot(0);
            ItemStack crystallizingStack = handler.getStackInSlot(2);
            if (outputStack.isEmpty()) {
                return true;
            } else if (!inputStack.isEmpty() && !outputStack.sameItem(inputStack)
                    || !outputStack.sameItem(crystallizingStack)) {
                return false;
            } else {
                return outputStack.getCount() + 1 <= maxStackSize;
            }
        } else {
            return false;
        }
    }

    public boolean canCrystallizeHeldItem(ItemStack heldItems) {
        if (level != null) {
            SimpleContainer inventory = new SimpleContainer(heldItems);
            Recipe<?> recipe = level.getRecipeManager().getRecipeFor(CrystallizerRecipe.Type.INSTANCE, inventory, level)
                    .orElse(null);
            if (recipe != null) {
                ItemStack crystallizingStack = itemStackHandler.getStackInSlot(2);
                return !crystallizingStack.sameItem(heldItems);
            }
        }
        return false;
    }


    @Override
    public void onLoad() {
        super.onLoad();
        lazyOptionalItemHandler = LazyOptional.of(() -> itemStackHandler);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("inventory", itemStackHandler.serializeNBT());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ItemStackHandler getItemStackHandler() {
        return itemStackHandler;
    }

    // WORDLY CONTAINER METHODS

    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction pSide) {
        if (pSide == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        } else {
            return pSide == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int pIndex, @NotNull ItemStack pItemStack, @Nullable Direction pDirection) {
        return this.canPlaceItem(pIndex, pItemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int pIndex, @NotNull ItemStack pStack, @NotNull Direction pDirection) {
        return pDirection != Direction.DOWN && pIndex == 1;
    }

    @Override
    public int getContainerSize() {
        return itemStackHandler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            if (!itemStackHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int pIndex) {
        return itemStackHandler.getStackInSlot(pIndex);
    }

    @Override
    public @NotNull ItemStack removeItem(int pIndex, int pCount) {
        return itemStackHandler.extractItem(pIndex, pCount, false);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int pIndex) {
        return itemStackHandler.extractItem(pIndex, 64, false);
    }

    @Override
    public void setItem(int pIndex, ItemStack pStack) {
        ItemStack itemstack = itemStackHandler.getStackInSlot(pIndex);
        boolean flag = !pStack.isEmpty() && pStack.sameItem(itemstack) && ItemStack.tagMatches(pStack, itemstack);
        itemStackHandler.setStackInSlot(pIndex, pStack);
        if (pStack.getCount() > 64) {
            pStack.setCount(64);
        }

        if (pIndex == 0 && !flag) {
            this.crystallizingTotalTime = getTotalCrystallizingTime(level, itemStackHandler);
            this.crystallizingProgress = 0;
            this.setChanged();
        }
    }

    @Override
    public boolean stillValid(@NotNull Player pPlayer) {
        if (level != null && level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        else {
            return pPlayer.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
     * guis use Slot.isItemValid
     */
    @Override
    public boolean canPlaceItem(int pIndex, @NotNull ItemStack pStack) {
        return pIndex != 1;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            itemStackHandler.setStackInSlot(i, new ItemStack(Items.AIR));
        }
    }

    public void setHandler(ItemStackHandler itemStackHandler) {
        for (int i = 0; i < itemStackHandler.getSlots(); i++) {
            this.itemStackHandler.setStackInSlot(i, itemStackHandler.getStackInSlot(i));
        }
    }
}
