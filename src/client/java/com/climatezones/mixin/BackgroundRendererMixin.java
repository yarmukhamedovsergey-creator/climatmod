package com.climatezones.mixin;

import com.climatezones.client.render.ClimateFogState;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true)
    private static void climatezones$modifyFogColor(Camera camera, float tickDelta, CallbackInfoReturnable<Vector4f> cir) {
        float r = ClimateFogState.getFogRed();
        if (r < 0) {
            return;
        }
        Vector4f color = cir.getReturnValue();
        float g = ClimateFogState.getFogGreen();
        float b = ClimateFogState.getFogBlue();
        float blend = Math.min(1.0f, ClimateFogState.getFogEndMultiplier() > 0.5f ? 0.6f : 0.6f);
        color.x = color.x * (1 - blend) + r * blend;
        color.y = color.y * (1 - blend) + g * blend;
        color.z = color.z * (1 - blend) + b * blend;
        cir.setReturnValue(color);
    }
}
