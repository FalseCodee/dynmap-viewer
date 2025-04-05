package me.falsecode.dynmapnametags.mixins;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessorMixin {
    @Invoker("getFov")
    float getCurrentFov(Camera camera, float tickDelta, boolean changingFov);

}
