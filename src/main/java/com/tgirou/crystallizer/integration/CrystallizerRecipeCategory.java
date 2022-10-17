package com.tgirou.crystallizer.integration;

import com.tgirou.crystallizer.api.util.Constants;
import com.tgirou.crystallizer.blocks.ModBlocks;
import com.tgirou.crystallizer.recipes.CrystallizerRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CrystallizerRecipeCategory implements IRecipeCategory<CrystallizerRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(Constants.MOD_ID, "crystallizer");
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(Constants.MOD_ID, "textures/gui/crystallizer.png");
    private final IDrawable background;
    private final IDrawable icon;

    public CrystallizerRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.CRYSTALLIZER.get()));
    }

    @Override
    public @NotNull RecipeType<CrystallizerRecipe> getRecipeType() {
        return JEICrystallizerModPlugin.CRYSTALLIZE_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("block.crystallizer.crystallizer");
    }

    @Override
    public @NotNull IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @NotNull IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CrystallizerRecipe recipe, @NotNull IFocusGroup group) {
        builder.addSlot(RecipeIngredientRole.INPUT, 80, 17).addIngredients(recipe.getIngredients().get(0));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 53).addItemStack(recipe.getResultItem());
    }
}
