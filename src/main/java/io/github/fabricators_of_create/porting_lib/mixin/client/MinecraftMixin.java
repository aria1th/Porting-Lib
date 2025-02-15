package io.github.fabricators_of_create.porting_lib.mixin.client;

import static net.minecraft.world.InteractionResult.PASS;
import static net.minecraft.world.InteractionResult.SUCCESS;

import io.github.fabricators_of_create.porting_lib.event.MinecraftTailCallback;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import io.github.fabricators_of_create.porting_lib.event.AttackAirCallback;
import io.github.fabricators_of_create.porting_lib.event.ClientWorldEvents;
import io.github.fabricators_of_create.porting_lib.event.InstanceRegistrationCallback;
import io.github.fabricators_of_create.porting_lib.event.OnStartUseItemCallback;
import io.github.fabricators_of_create.porting_lib.event.ParticleManagerRegistrationCallback;
import io.github.fabricators_of_create.porting_lib.event.RenderTickStartCallback;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow
	public LocalPlayer player;

	@Shadow
	public @Nullable ClientLevel level;

	@Inject(
			method = "<init>",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/Minecraft;particleEngine:Lnet/minecraft/client/particle/ParticleEngine;",
					shift = Shift.AFTER
			)
	)
	public void port_lib$registerParticleManagers(GameConfig gameConfiguration, CallbackInfo ci) {
		ParticleManagerRegistrationCallback.EVENT.invoker().onParticleManagerRegistration();
	}

	// should inject to right after the initialization of resourceManager
	@Inject(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/packs/resources/SimpleReloadableResourceManager;<init>(Lnet/minecraft/server/packs/PackType;)V"
			)
	)
	public void port_lib$instanceRegistration(GameConfig args, CallbackInfo ci) {
		InstanceRegistrationCallback.EVENT.invoker().registerInstance();
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	public void port_lib$mcTail(GameConfig gameConfiguration, CallbackInfo ci) {
		MinecraftTailCallback.EVENT.invoker().onMinecraftTail((Minecraft) (Object) this);
	}

	@Inject(method = "setLevel", at = @At("HEAD"))
	public void port_lib$onHeadJoinWorld(ClientLevel world, CallbackInfo ci) {
		if (this.level != null) {
			ClientWorldEvents.UNLOAD.invoker().onWorldUnload((Minecraft) (Object) this, this.level);
		}
	}

	@Inject(
			method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V",
			at = @At(
					value = "JUMP",
					opcode = Opcodes.IFNULL,
					ordinal = 1,
					shift = Shift.AFTER
			)
	)
	public void port_lib$onDisconnect(Screen screen, CallbackInfo ci) {
		ClientWorldEvents.UNLOAD.invoker().onWorldUnload((Minecraft) (Object) this, this.level);
	}

	@Inject(method = "startAttack", at = @At(value = "FIELD", ordinal = 2, target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;"))
	private void port_lib$onClickMouse(CallbackInfo ci) {
		AttackAirCallback.EVENT.invoker().attackAir(player);
	}

	@Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;render(Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = Shift.BEFORE))
	private void port_lib$renderTickStart(CallbackInfo ci) {
		RenderTickStartCallback.EVENT.invoker().tick();
	}

	@Inject(
			method = "startUseItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private void port_lib$onStartUseItem(CallbackInfo ci, InteractionHand[] var1, int var2, int var3, InteractionHand hand) {
		InteractionResult result = OnStartUseItemCallback.EVENT.invoker().onStartUse(hand);
		if (result != PASS) {
			if (result == SUCCESS) {
				player.swing(hand);
			}
			ci.cancel();
		}
	}
}
