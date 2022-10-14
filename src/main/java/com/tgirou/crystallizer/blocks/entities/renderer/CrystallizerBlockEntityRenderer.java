package com.tgirou.crystallizer.blocks.entities.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.tgirou.crystallizer.blocks.customs.CrystallizerBlock;
import com.tgirou.crystallizer.blocks.entities.CrystallizerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CrystallizerBlockEntityRenderer implements BlockEntityRenderer<CrystallizerBlockEntity> {
    public CrystallizerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(@NotNull CrystallizerBlockEntity blockEntity, float partialTick, @NotNull PoseStack stack, @NotNull MultiBufferSource
            bufferSource, int packedLight, int packedOverlay) {
        renderInputStack(blockEntity, stack, bufferSource);
        renderOutputStack(blockEntity, stack, bufferSource);
    }

    private void renderOutputStack(CrystallizerBlockEntity blockEntity, PoseStack stack, MultiBufferSource
            bufferSource) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack itemStack = blockEntity.getResultStack();
        stack.pushPose();
        stack.translate(0.5f, 0.06f, 0.75f);
        stack.scale(0.25f, 0.25f, 0.25f);
        stack.mulPose(Vector3f.XP.rotationDegrees(90));

        switch (blockEntity.getBlockState().getValue(CrystallizerBlock.FACING)) {
            case NORTH -> {
                stack.mulPose(Vector3f.ZP.rotationDegrees(0));
                stack.translate(0f, -2f, -0.1f);
            }
            case EAST -> {
                stack.mulPose(Vector3f.ZP.rotationDegrees(90));
                stack.translate(-1f, -1f, -0.06f);
            }
            case SOUTH -> stack.mulPose(Vector3f.ZP.rotationDegrees(180));
            case WEST -> {
                stack.mulPose(Vector3f.ZP.rotationDegrees(270));
                stack.translate(1f, -1f, 0f);
            }
        }

        itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.GUI, getLightLevel(Objects.requireNonNull(blockEntity.getLevel()),
                        blockEntity.getBlockPos()),
                OverlayTexture.NO_OVERLAY, stack, bufferSource, 1);
        stack.popPose();
    }

    private void renderInputStack(CrystallizerBlockEntity blockEntity, PoseStack stack, MultiBufferSource
            bufferSource) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack itemStack = blockEntity.getCrystallizingStack();
        stack.pushPose();
        stack.translate(0.5f, 1.25f, 0.82f);
        stack.scale(0.25f, 0.25f, 0.25f);
        stack.mulPose(Vector3f.XP.rotationDegrees(180));

        switch (blockEntity.getBlockState().getValue(CrystallizerBlock.FACING)) {
            case NORTH -> {
                stack.mulPose(Vector3f.XP.rotationDegrees(180));
                stack.mulPose(Vector3f.YP.rotationDegrees(180));
                stack.translate(0f, 0f, 2.5f);
            }
            case EAST -> {
                stack.mulPose(Vector3f.XP.rotationDegrees(180));
                stack.mulPose(Vector3f.YP.rotationDegrees(-90));
                stack.translate(-1.28f, 0f, -1.25f);
            }
            case SOUTH -> stack.mulPose(Vector3f.ZP.rotationDegrees(180));
            case WEST -> {
                stack.mulPose(Vector3f.XP.rotationDegrees(180));
                stack.mulPose(Vector3f.YP.rotationDegrees(90));
                stack.translate(1.25f, 0f, -1.25f);
            }
        }

        itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.GUI, getLightLevel(Objects.requireNonNull(blockEntity.getLevel()),
                        blockEntity.getBlockPos()),
                OverlayTexture.NO_OVERLAY, stack, bufferSource, 1);
        stack.popPose();
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
