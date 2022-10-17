package com.tgirou.crystallizer.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tgirou.crystallizer.api.util.Constants;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrystallizerRecipe implements Recipe<SimpleContainer> {
    protected final ResourceLocation id;
    protected final Ingredient ingredient;
    protected final ItemStack result;
    protected final int crystallizingTime;

    public CrystallizerRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int crystallizingTime) {
        this.id = id;
        this.ingredient = ingredient;
        this.result = result;
        this.crystallizingTime = crystallizingTime;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, @NotNull Level pLevel) {
        return this.ingredient.test(pContainer.getItem(2)) || this.ingredient.test(pContainer.getItem(0));
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull SimpleContainer pContainer) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> nonNullList = NonNullList.create();
        nonNullList.add(this.ingredient);
        return nonNullList;
    }

    @Override
    public @NotNull ItemStack getResultItem() {
        return this.result;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.id;
    }

    public int getCrystallizingTime() {
        return this.crystallizingTime;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<CrystallizerRecipe> {
        private Type() {}
        public static final Type INSTANCE = new Type();
        public static final String ID = "crystallizer";
    }

    public static class Serializer implements RecipeSerializer<CrystallizerRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(Constants.MOD_ID, "crystallizer");

        @Override
        public @NotNull CrystallizerRecipe fromJson(@NotNull ResourceLocation pRecipeId, @NotNull JsonObject pJson) {
            JsonElement jsonelement = GsonHelper.getAsJsonObject(pJson, "ingredient");
            Ingredient ingredient = Ingredient.fromJson(jsonelement);
            if (!pJson.has("result")) throw new com.google.gson.JsonSyntaxException("Missing result, expected to find a string or object");
            ItemStack itemstack;
            String s1 = GsonHelper.getAsString(pJson, "result");
            ResourceLocation resourcelocation = new ResourceLocation(s1);
            itemstack = new ItemStack(Registry.ITEM.getOptional(resourcelocation).orElseThrow(() -> {
                return new IllegalStateException("Item: " + s1 + " does not exist");
            }), 1);
            int i = GsonHelper.getAsInt(pJson, "crystallizingTime");
            return new CrystallizerRecipe(pRecipeId, ingredient, itemstack, i);
        }

        @Nullable
        @Override
        public CrystallizerRecipe fromNetwork(@NotNull ResourceLocation pRecipeId, @NotNull FriendlyByteBuf pBuffer) {
            Ingredient ingredient = Ingredient.fromNetwork(pBuffer);
            ItemStack itemstack = pBuffer.readItem();
            int crystallizingTime = pBuffer.readVarInt();
            return new CrystallizerRecipe(pRecipeId, ingredient, itemstack, crystallizingTime);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf pBuffer, CrystallizerRecipe pRecipe) {
            pRecipe.ingredient.toNetwork(pBuffer);
            pBuffer.writeItemStack(pRecipe.result, false);
            pBuffer.writeVarInt(pRecipe.crystallizingTime);
        }

        private static <G> Class<G> castClass()
        {
            return (Class<G>) RecipeSerializer.class;
        }
    }
}
