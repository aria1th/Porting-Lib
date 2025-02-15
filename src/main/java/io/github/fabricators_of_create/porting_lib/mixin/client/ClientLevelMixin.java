package io.github.fabricators_of_create.porting_lib.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.fabricators_of_create.porting_lib.event.ClientWorldEvents;
import io.github.fabricators_of_create.porting_lib.util.MixinHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@Environment(EnvType.CLIENT)
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void port_lib$init(CallbackInfo ci) {
		ClientWorldEvents.LOAD.invoker().onWorldLoad(minecraft, MixinHelper.cast(this));
	}
}
