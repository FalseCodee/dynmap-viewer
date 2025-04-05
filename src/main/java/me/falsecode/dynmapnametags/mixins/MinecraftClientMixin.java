package me.falsecode.dynmapnametags.mixins;

import me.falsecode.dynmapnametags.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "<init>(Lnet/minecraft/client/RunArgs;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/SplashOverlay;init(Lnet/minecraft/client/texture/TextureManager;)V", shift = At.Shift.AFTER))
    public void onInit(RunArgs args, CallbackInfo ci) {
        Main.init();
    }
    @Inject(method = "tick", at =@At("TAIL"))
    public void tick(CallbackInfo ci){
        Main.getNametagModule().onTick();
    }
}
