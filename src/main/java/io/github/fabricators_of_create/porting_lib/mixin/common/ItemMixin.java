package io.github.fabricators_of_create.porting_lib.mixin.common;

import io.github.fabricators_of_create.porting_lib.extensions.ItemExtensions;

import io.github.fabricators_of_create.porting_lib.util.ToolAction;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.fabricators_of_create.porting_lib.extensions.RegistryNameProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@Mixin(Item.class)
public abstract class ItemMixin implements RegistryNameProvider, ItemExtensions {

	@Unique
	private ResourceLocation port_lib$registryName = null;

	@Override
	public ResourceLocation getRegistryName() {
		if (port_lib$registryName == null) {
			port_lib$registryName = Registry.ITEM.getKey((Item) (Object) this);
		}
		return port_lib$registryName;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !oldStack.equals(newStack);
	}

	@Override
	public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
		return false;
	}
}
