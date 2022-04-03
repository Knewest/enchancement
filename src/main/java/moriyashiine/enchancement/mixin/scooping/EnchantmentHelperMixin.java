package moriyashiine.enchancement.mixin.scooping;

import moriyashiine.enchancement.common.registry.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
	@Inject(method = "getAttackDamage", at = @At("HEAD"), cancellable = true)
	private static void enchancement$scooping(ItemStack stack, EntityGroup group, CallbackInfoReturnable<Float> cir) {
		if (EnchantmentHelper.getLevel(ModEnchantments.SCOOPING, stack) > 0) {
			cir.setReturnValue(1F);
		}
	}

	@Inject(method = "getLooting", at = @At("HEAD"), cancellable = true)
	private static void enchancement$scooping(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
		int level = EnchantmentHelper.getEquipmentLevel(ModEnchantments.SCOOPING, entity);
		if (level > 0) {
			cir.setReturnValue(Enchantments.LOOTING.getMaxLevel() + 2);
		}
	}
}
