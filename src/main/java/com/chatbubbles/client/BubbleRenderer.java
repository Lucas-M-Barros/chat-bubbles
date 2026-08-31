package com.chatbubbles.client;

import com.chatbubbles.data.BubbleAppearance;
import com.chatbubbles.data.ChatBubble;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public final class BubbleRenderer {
    private BubbleRenderer() {
    }

    public static void render(Player player, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        List<ChatBubble> bubbles = ClientBubbleManager.getBubbles(player.getUUID());
        if (bubbles.isEmpty()) {
            return;
        }

        BubbleAppearance appearance = ClientBubbleManager.getAppearance(player.getUUID());
        Font font = Minecraft.getInstance().font;
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        int padding = ClientBubbleManager.padding;
        int maxLineWidth = ClientBubbleManager.maxLineWidth;
        boolean sneaking = player.isCrouching() || player.isDiscrete();
        float sneakMul = sneaking ? 0.35F : 1.0F;
        int fullBright = LightTexture.FULL_BRIGHT;

        float y = player.getBbHeight() + 0.35F + appearance.offset() * 0.18F;
        if (player.isCrouching()) {
            y -= 0.25F;
        }

        record Layout(List<FormattedCharSequence> lines, int textWidth, int textHeight, int boxWidth, int boxHeight, float worldY, float alpha) {
        }

        ArrayList<Layout> layouts = new ArrayList<>();
        for (ChatBubble bubble : bubbles) {
            float alpha = bubble.alpha() * sneakMul;
            if (alpha <= 0.02F) {
                continue;
            }
            List<FormattedCharSequence> lines = font.split(Component.literal(bubble.message()), maxLineWidth);
            int textWidth = 0;
            for (FormattedCharSequence line : lines) {
                textWidth = Math.max(textWidth, font.width(line));
            }
            int textHeight = Math.max(font.lineHeight, lines.size() * font.lineHeight);
            int boxWidth = textWidth + padding * 2;
            int boxHeight = textHeight + padding * 2;
            layouts.add(new Layout(lines, textWidth, textHeight, boxWidth, boxHeight, y + boxHeight * 0.0125F, alpha));
            y += boxHeight * 0.025F + appearance.spacing() * 0.14F;
        }

        VertexConsumer background = buffer.getBuffer(BubbleRenderType.BACKGROUND);
        for (Layout layout : layouts) {
            poseStack.pushPose();
            poseStack.translate(0.0D, layout.worldY(), 0.0D);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            drawBubble(poseStack, background, layout.boxWidth(), layout.boxHeight(), appearance, layout.alpha());
            poseStack.popPose();
        }

        for (Layout layout : layouts) {
            poseStack.pushPose();
            poseStack.translate(0.0D, layout.worldY(), 0.0D);
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.scale(-0.025F, -0.025F, 0.025F);
            poseStack.translate(0.0D, 0.0D, 0.03D);

            int textX = -layout.textWidth() / 2;
            int textY = -layout.textHeight() / 2;
            int textColor = withAlpha(appearance.textColor(), layout.alpha());
            Matrix4f textMatrix = poseStack.last().pose();
            for (int lineIndex = 0; lineIndex < layout.lines().size(); lineIndex++) {
                float lineY = textY + lineIndex * font.lineHeight;
                if (!sneaking) {
                    font.drawInBatch(
                            layout.lines().get(lineIndex),
                            textX,
                            lineY,
                            textColor,
                            false,
                            textMatrix,
                            buffer,
                            Font.DisplayMode.SEE_THROUGH,
                            0,
                            fullBright
                    );
                }
                font.drawInBatch(
                        layout.lines().get(lineIndex),
                        textX,
                        lineY,
                        textColor,
                        false,
                        textMatrix,
                        buffer,
                        Font.DisplayMode.NORMAL,
                        0,
                        fullBright
                );
            }
            poseStack.popPose();
        }
    }

    public static void drawPreview(PoseStack poseStack, int centerX, int centerY, BubbleAppearance appearance, String sample) {
        Font font = Minecraft.getInstance().font;
        int padding = ClientBubbleManager.padding;
        List<FormattedCharSequence> lines = font.split(Component.literal(sample), ClientBubbleManager.maxLineWidth);
        int textWidth = 0;
        for (FormattedCharSequence line : lines) {
            textWidth = Math.max(textWidth, font.width(line));
        }
        int textHeight = Math.max(font.lineHeight, lines.size() * font.lineHeight);
        int boxWidth = textWidth + padding * 2;
        int boxHeight = textHeight + padding * 2;

        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 0);
        drawPreviewBubble(poseStack, boxWidth, boxHeight, appearance);
        poseStack.translate(0.0D, 0.0D, 1.0D);
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        int textX = -textWidth / 2;
        int textY = -textHeight / 2;
        int textColor = withAlpha(appearance.textColor(), 1.0F);
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            font.drawInBatch(
                    lines.get(lineIndex),
                    textX,
                    textY + lineIndex * font.lineHeight,
                    textColor,
                    false,
                    poseStack.last().pose(),
                    buffer,
                    Font.DisplayMode.NORMAL,
                    0,
                    LightTexture.FULL_BRIGHT
            );
        }
        buffer.endBatch();
        poseStack.popPose();
    }

    private static void drawBubble(PoseStack poseStack, VertexConsumer consumer, int boxWidth, int boxHeight, BubbleAppearance appearance, float alpha) {
        int x1 = -boxWidth / 2;
        int y1 = -boxHeight / 2;
        int x2 = boxWidth / 2;
        int y2 = boxHeight / 2;
        int border = withAlpha(appearance.borderColor(), alpha);
        int fill = withAlpha(appearance.bgColor(), alpha);
        Matrix4f matrix = poseStack.last().pose();
        putRect(consumer, matrix, x1, y1, x2, y2, 0.0F, fill);
        putRect(consumer, matrix, x1 - 1, y1 - 1, x2 + 1, y1, 0.01F, border);
        putRect(consumer, matrix, x1 - 1, y2, x2 + 1, y2 + 1, 0.01F, border);
        putRect(consumer, matrix, x1 - 1, y1, x1, y2, 0.01F, border);
        putRect(consumer, matrix, x2, y1, x2 + 1, y2, 0.01F, border);
    }

    private static void drawPreviewBubble(PoseStack poseStack, int boxWidth, int boxHeight, BubbleAppearance appearance) {
        int x1 = -boxWidth / 2;
        int y1 = -boxHeight / 2;
        int x2 = boxWidth / 2;
        int y2 = boxHeight / 2;
        int border = withAlpha(appearance.borderColor(), 1.0F);
        int fill = withAlpha(appearance.bgColor(), 1.0F);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        Matrix4f matrix = poseStack.last().pose();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        putRect(builder, matrix, x1, y1, x2, y2, 0.0F, fill);
        putRect(builder, matrix, x1 - 1, y1 - 1, x2 + 1, y1, 0.01F, border);
        putRect(builder, matrix, x1 - 1, y2, x2 + 1, y2 + 1, 0.01F, border);
        putRect(builder, matrix, x1 - 1, y1, x1, y2, 0.01F, border);
        putRect(builder, matrix, x2, y1, x2 + 1, y2, 0.01F, border);
        tesselator.end();
    }

    private static void putRect(VertexConsumer consumer, Matrix4f matrix, float x1, float y1, float x2, float y2, float z, int argb) {
        if (x1 == x2 || y1 == y2) {
            return;
        }
        if (x1 > x2) {
            float tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        if (y1 > y2) {
            float tmp = y1;
            y1 = y2;
            y2 = tmp;
        }

        float a = FastColor.ARGB32.alpha(argb) / 255.0F;
        if (a <= 0.01F) {
            return;
        }
        float r = FastColor.ARGB32.red(argb) / 255.0F;
        float g = FastColor.ARGB32.green(argb) / 255.0F;
        float b = FastColor.ARGB32.blue(argb) / 255.0F;
        consumer.vertex(matrix, x1, y1, z).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, x1, y2, z).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, x2, y2, z).color(r, g, b, a).endVertex();
        consumer.vertex(matrix, x2, y1, z).color(r, g, b, a).endVertex();
    }

    private static int withAlpha(int rgb, float alpha) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        return (a << 24) | (rgb & 0xFFFFFF);
    }
}
