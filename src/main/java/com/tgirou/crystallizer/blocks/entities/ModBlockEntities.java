package com.tgirou.crystallizer.blocks.entities;

import com.tgirou.crystallizer.api.util.Constants;
import com.tgirou.crystallizer.blocks.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    private ModBlockEntities() {}

    public static final RegistryObject<BlockEntityType<CrystallizerBlockEntity>> CRYSTALLIZER =
            BLOCK_ENTITIES.register("crystallizer_block_entity", () -> BlockEntityType.Builder.of(
                    CrystallizerBlockEntity::new, ModBlocks.CRYSTALLIZER.get())
                    .build(null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
