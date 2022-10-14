package com.tgirou.crystallizer.integration;

import com.tgirou.crystallizer.api.util.Constants;
import com.tgirou.crystallizer.recipes.CrystallizerRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEICrystallizerModPlugin implements IModPlugin {
    public static RecipeType<CrystallizerRecipe> CRYSTALLIZE_TYPE =
            new RecipeType<>(CrystallizerRecipeCategory.UID, CrystallizerRecipe.class);
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Constants.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new
                CrystallizerRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager rm = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
        List<CrystallizerRecipe> crystallizerRecipes = rm.getAllRecipesFor(CrystallizerRecipe.Type.INSTANCE);
        registration.addRecipes(CRYSTALLIZE_TYPE, crystallizerRecipes);
    }
}
