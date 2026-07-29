package org.hedgetech.waxeverything.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.hedgetech.waxeverything.Constants;
import org.hedgetech.waxeverything.config.WaxEverythingConfig;
import org.hedgetech.waxeverything.waxtracking.ClientWaxRegistry;
import org.hedgetech.waxeverything.waxtracking.WaxTarget;

import java.awt.*;

public final class WaxOverlayRenderer {
    private static final Identifier KEY_CATEGORY_IDENTIFIER = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "key.categories.waxeverything");
    private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(KEY_CATEGORY_IDENTIFIER);
    private static final String KEY_NAME = "key.waxeverything.show_waxed";

    public static final KeyMapping KEY_SHOW_WAXED = new KeyMapping(
            KEY_NAME,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KEY_CATEGORY
    );

    private static float cachedRed;
    private static float cachedGreen;
    private static float cachedBlue;
    // private static float cachedEdgeWidth;

    public static void init() {
        WaxEverythingConfig.registerReloadListener(WaxOverlayRenderer::updateCache);
        updateCache();
    }

    private static void updateCache() {
        var overlayColor = WaxEverythingConfig.CONFIG.overlayColor();
        cachedRed = overlayColor.getRed() / 255.0F;
        cachedGreen = overlayColor.getGreen() / 255.0F;
        cachedBlue = overlayColor.getBlue() / 255.0F;
//        cachedEdgeWidth = WaxEverythingConfig.CONFIG.edgeWidth;
    }

    public static void renderAll(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPos) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Constants.LOG.trace("Rendering wax overlays for {} targets", ClientWaxRegistry.getAllWaxedTargets().size());

        for (WaxTarget target : ClientWaxRegistry.getAllWaxedTargets()) {
            if (target instanceof WaxTarget.BlockTarget(BlockPos pos)) {
                renderBlockOverlay(poseStack, collector, cameraPos, pos);
            } else if (target instanceof WaxTarget.EntityTarget entityTarget) {
                renderEntityOverlay(poseStack, collector, cameraPos, level, entityTarget);
            }
        }
    }

    private static void renderBlockOverlay(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPos, BlockPos pos) {
        poseStack.pushPose();

        double x = pos.getX() - cameraPos.x;
        double y = pos.getY() - cameraPos.y;
        double z = pos.getZ() - cameraPos.z;
        poseStack.translate(x, y, z);

        collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(), (pose, buffer) ->
                drawFilledBox(
                        pose,
                        buffer,
                        new AABB(0, 0, 0, 1, 1, 1).inflate(0.005),
                        cachedRed,
                        cachedGreen,
                        cachedBlue,
                        0.8F
                )
        );

        poseStack.popPose();
    }

    private static void renderEntityOverlay(PoseStack poseStack, SubmitNodeCollector collector, Vec3 cameraPos, ClientLevel level, WaxTarget.EntityTarget target) {
        Entity entity = null;

        for (Entity loaded : level.entitiesForRendering()) {
            if (loaded.getUUID().equals(target.entityUuid())) {
                entity = loaded;
                break;
            }
        }

        if (entity == null) return;

        var box = entity.getBoundingBox();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(), (pose, buffer) ->
                drawFilledBox(pose, buffer, box, cachedRed, cachedGreen, cachedBlue, 0.8F)
        );
    }

//    public static void render(PoseStack poseStack, SubmitNodeCollector collector) {
//        var box = new AABB(0, 0, 0, 1, 1, 1).inflate(0.005);
//
////        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, buffer) ->
////                drawWireframeBox(pose, buffer, box, cachedRed, cachedGreen, cachedBlue, 1.0F));
//
//        collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(), (pose, buffer) ->
//                drawFilledBox(pose, buffer, box, cachedRed, cachedGreen, cachedBlue, 0.8F));
//    }

    private static void drawWireframeBox(PoseStack.Pose pose, VertexConsumer buffer, AABB box, float r, float g, float b, float a) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // Bottom face (4 edges) - pointing downwards (0, -1, 0)
        drawLine(pose, buffer, minX, minY, minZ, maxX, minY, minZ, r, g, b, a, 0, -1, 0);
        drawLine(pose, buffer, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a, 0, -1, 0);
        drawLine(pose, buffer, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a, 0, -1, 0);
        drawLine(pose, buffer, minX, minY, maxZ, minX, minY, minZ, r, g, b, a, 0, -1, 0);

        // Top face (4 edges) - pointing upwards (0, 1, 0)
        drawLine(pose, buffer, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a, 0, 1, 0);
        drawLine(pose, buffer, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a, 0, 1, 0);
        drawLine(pose, buffer, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a, 0, 1, 0);
        drawLine(pose, buffer, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a, 0, 1, 0);

        // Connecting vertical pillars (4 edges) - pointing outwards along X/Z
        drawLine(pose, buffer, minX, minY, minZ, minX, maxY, minZ, r, g, b, a, -1, 0, 0);
        drawLine(pose, buffer, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a, 1, 0, 0);
        drawLine(pose, buffer, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a, 1, 0, 0);
        drawLine(pose, buffer, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a, -1, 0, 0);
    }

    private static void drawFilledBox(PoseStack.Pose pose, VertexConsumer buffer, AABB box,
                                      float r, float g, float b, float a
    ) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        // Face: Up (+Y, Normal: 0, 1, 0)
        addVertex(pose, buffer, maxX, maxY, minZ, r, g, b, a, 0, 1, 0);
        addVertex(pose, buffer, minX, maxY, minZ, r, g, b, a, 0, 1, 0);
        addVertex(pose, buffer, minX, maxY, maxZ, r, g, b, a, 0, 1, 0);
        addVertex(pose, buffer, maxX, maxY, maxZ, r, g, b, a, 0, 1, 0);

        // Face: Down (-Y, Normal: 0, -1, 0)
        addVertex(pose, buffer, minX, minY, minZ, r, g, b, a, 0, -1, 0);
        addVertex(pose, buffer, maxX, minY, minZ, r, g, b, a, 0, -1, 0);
        addVertex(pose, buffer, maxX, minY, maxZ, r, g, b, a, 0, -1, 0);
        addVertex(pose, buffer, minX, minY, maxZ, r, g, b, a, 0, -1, 0);

        // Face: East (+X, Normal: 1, 0, 0)
        addVertex(pose, buffer, maxX, minY, minZ, r, g, b, a, 1, 0, 0);
        addVertex(pose, buffer, maxX, maxY, minZ, r, g, b, a, 1, 0, 0);
        addVertex(pose, buffer, maxX, maxY, maxZ, r, g, b, a, 1, 0, 0);
        addVertex(pose, buffer, maxX, minY, maxZ, r, g, b, a, 1, 0, 0);

        // Face: West (-X, Normal: -1, 0, 0)
        addVertex(pose, buffer, minX, minY, maxZ, r, g, b, a, -1, 0, 0);
        addVertex(pose, buffer, minX, maxY, maxZ, r, g, b, a, -1, 0, 0);
        addVertex(pose, buffer, minX, maxY, minZ, r, g, b, a, -1, 0, 0);
        addVertex(pose, buffer, minX, minY, minZ, r, g, b, a, -1, 0, 0);

        // Face: South (+Z, Normal: 0, 0, 1)
        addVertex(pose, buffer, minX, minY, maxZ, r, g, b, a, 0, 0, 1);
        addVertex(pose, buffer, minX, maxY, maxZ, r, g, b, a, 0, 0, 1);
        addVertex(pose, buffer, maxX, maxY, maxZ, r, g, b, a, 0, 0, 1);
        addVertex(pose, buffer, maxX, minY, maxZ, r, g, b, a, 0, 0, 1);

        // Face: North (-Z, Normal: 0, 0, -1)
        addVertex(pose, buffer, maxX, minY, minZ, r, g, b, a, 0, 0, -1);
        addVertex(pose, buffer, maxX, maxY, minZ, r, g, b, a, 0, 0, -1);
        addVertex(pose, buffer, minX, maxY, minZ, r, g, b, a, 0, 0, -1);
        addVertex(pose, buffer, minX, minY, minZ, r, g, b, a, 0, 0, -1);
    }

    private static void drawLine(PoseStack.Pose pose, VertexConsumer buffer,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float r, float g, float b, float a,
                                 float nx, float ny, float nz
    ) {
        addVertex(pose, buffer, x1, y1, z1, r, g, b, a, nx, ny, nz);
        addVertex(pose, buffer, x2, y2, z2, r, g, b, a, nx, ny, nz);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer buffer,
                                  float x, float y, float z,
                                  float r, float g, float b, float a,
                                  float nx, float ny, float nz
    ) {
        buffer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz)
                .setLineWidth(2.0F);
    }

    private WaxOverlayRenderer() { }
}
