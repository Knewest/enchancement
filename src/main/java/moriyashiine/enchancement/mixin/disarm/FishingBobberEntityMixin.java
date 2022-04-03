package moriyashiine.enchancement.mixin.disarm;

import moriyashiine.enchancement.common.registry.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends Entity {
	@Unique
	private boolean hasDisarm = false;

	@Shadow
	public abstract @Nullable PlayerEntity getPlayerOwner();

	public FishingBobberEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Inject(method = "<init>(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/World;II)V", at = @At("TAIL"))
	private void enchancment$disarm(PlayerEntity thrower, World world, int luckOfTheSeaLevel, int lureLevel, CallbackInfo ci) {
		hasDisarm = EnchantmentHelper.getEquipmentLevel(ModEnchantments.DISARM, thrower) > 0;
	}

	@Inject(method = "pullHookedEntity", at = @At("HEAD"), cancellable = true)
	private void enchancment$disarm(Entity entity, CallbackInfo ci) {
		if (!world.isClient && hasDisarm && entity instanceof LivingEntity living) {
			ItemStack stack = living.getMainHandStack();
			if (entity instanceof EndermanEntity enderman && enderman.getCarriedBlock() != null) {
				stack = new ItemStack(enderman.getCarriedBlock().getBlock());
			}
			if (!stack.isEmpty()) {
				if (entity instanceof PlayerEntity player) {
					if (!player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
						player.getItemCooldownManager().set(stack.getItem(), 100);
						player.stopUsingItem();
					}
				} else {
					PlayerEntity owner = getPlayerOwner();
					if (owner != null) {
						if (stack.isDamageable()) {
							stack.setDamage(MathHelper.nextInt(living.getRandom(), stack.getDamage(), (int) (stack.getMaxDamage() - (stack.getMaxDamage() * 0.05F))));
						}
						ItemEntity itemEntity = new ItemEntity(world, entity.getX(), entity.getBodyY(0.5), entity.getZ(), stack);
						itemEntity.setToDefaultPickupDelay();
						double deltaX = owner.getX() - getX();
						double deltaY = owner.getY() - getY();
						double deltaZ = owner.getZ() - getZ();
						itemEntity.setVelocity(deltaX * 0.1, deltaY * 0.1 + Math.sqrt(Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)) * 0.08, deltaZ * 0.1);
						world.spawnEntity(itemEntity);
						living.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
						if (entity instanceof MobEntity mob) {
							if (!owner.isCreative()) {
								mob.setTarget(owner);
							}
							if (entity instanceof EndermanEntity enderman) {
								enderman.setCarriedBlock(null);
							}
						}
					}
				}
				ci.cancel();
			}
		}
	}

	@Inject(method = "use", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
	private void enchancment$disarm(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
		if (hasDisarm && cir.getReturnValueI() > 0) {
			cir.setReturnValue(1);
		}
	}
}
