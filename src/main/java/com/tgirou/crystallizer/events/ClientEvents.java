package com.tgirou.crystallizer.events;

import com.tgirou.crystallizer.api.util.Constants;
import com.tgirou.crystallizer.blocks.entities.ModBlockEntities;
import com.tgirou.crystallizer.blocks.entities.renderer.CrystallizerBlockEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientEvents {

    private ClientEvents() {}

    @Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {

        private ClientModBusEvents() {}

        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.CRYSTALLIZER.get(),
                    CrystallizerBlockEntityRenderer::new);
        }
    }
}
