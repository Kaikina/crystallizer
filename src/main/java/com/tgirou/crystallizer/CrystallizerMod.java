package com.tgirou.crystallizer;

import com.mojang.logging.LogUtils;
import com.tgirou.crystallizer.api.util.Constants;
import com.tgirou.crystallizer.blocks.ModBlocks;
import com.tgirou.crystallizer.blocks.entities.ModBlockEntities;
import com.tgirou.crystallizer.client.gui.CrystallizerScreen;
import com.tgirou.crystallizer.client.gui.ModMenuTypes;
import com.tgirou.crystallizer.items.ModItems;
import com.tgirou.crystallizer.networking.Messages;
import com.tgirou.crystallizer.recipes.ModRecipes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CrystallizerMod.MOD_ID)
public class CrystallizerMod
{
    public static final String MOD_ID = Constants.MOD_ID;

    public CrystallizerMod()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipes.register(modEventBus);

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(ModMenuTypes.CRYSTALLIZER_MENU.get(), CrystallizerScreen::new);
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.CRYSTALLIZER.get(), RenderType.translucent());
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(Messages::register);
    }
}
