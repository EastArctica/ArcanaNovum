package net.borisshoes.arcananovum.mixins;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin {
 
   @Redirect(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)V", at=@At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"))
   private ItemStack arcananovum_removeQuiverData(ItemStack instance){
      ItemStack stack = instance.copy();
      
      if(stack.hasNbt()){
         stack.removeSubNbt("QuiverId");
         stack.removeSubNbt("QuiverSlot");
      }
      return stack;
   }
}
