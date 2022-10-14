package com.tgirou.crystallizer.events;

import com.tgirou.crystallizer.api.util.Constants;
import com.tgirou.crystallizer.recipes.CrystallizerRecipe;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {
    private ModBusEvents() {}
    @SubscribeEvent
    public static void registerRecipeTypes(final RegistryEvent.Register<RecipeSerializer<?>> event) {
        Registry.register(Registry.RECIPE_TYPE, CrystallizerRecipe.Type.ID, CrystallizerRecipe.Type.INSTANCE);
    }
}
