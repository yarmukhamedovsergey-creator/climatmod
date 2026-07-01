package com.climatezones.mixin;

import com.climatezones.client.ClimateClientHandler;
import com.climatezones.client.render.ClimateOverlayRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void climatezones$renderClimateOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        float shakeX = ClimateClientHandler.getShakeOffsetX() * width;
        float shakeY = ClimateClientHandler.getShakeOffsetY() * height;

        if (Math.abs(shakeX) > 0.001f || Math.abs(shakeY) > 0.001f) {
            context.getMatrices().push();
            context.getMatrices().translate(shakeX, shakeY, 0);
            ClimateOverlayRenderer.render(context, width, height);
            context.getMatrices().pop();
        } else {
            ClimateOverlayRenderer.render(context, width, height);
        }
    }
}
