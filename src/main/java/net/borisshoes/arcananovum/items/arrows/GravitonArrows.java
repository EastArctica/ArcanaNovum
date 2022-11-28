package net.borisshoes.arcananovum.items.arrows;

import net.borisshoes.arcananovum.Arcananovum;
import net.borisshoes.arcananovum.items.ArcaneTome;
import net.borisshoes.arcananovum.items.core.MagicItem;
import net.borisshoes.arcananovum.items.core.RunicArrow;
import net.borisshoes.arcananovum.recipes.MagicItemRecipe;
import net.borisshoes.arcananovum.utils.GenericTimer;
import net.borisshoes.arcananovum.utils.MagicRarity;
import net.borisshoes.arcananovum.utils.ParticleEffectUtils;
import net.borisshoes.arcananovum.utils.SoundUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import static net.borisshoes.arcananovum.Arcananovum.log;

public class GravitonArrows extends MagicItem implements RunicArrow {
   
   public GravitonArrows(){
      id = "graviton_arrows";
      name = "Graviton Arrows";
      rarity = MagicRarity.EXOTIC;
      categories = new ArcaneTome.TomeFilter[]{ArcaneTome.TomeFilter.EXOTIC, ArcaneTome.TomeFilter.ARROWS};
      
      ItemStack item = new ItemStack(Items.TIPPED_ARROW);
      NbtCompound tag = item.getOrCreateNbt();
      NbtCompound display = new NbtCompound();
      NbtList loreList = new NbtList();
      NbtList enchants = new NbtList();
      enchants.add(new NbtCompound()); // Gives enchant glow with no enchants
      display.putString("Name","[{\"text\":\"Runic Arrows - Graviton\",\"italic\":false,\"color\":\"dark_blue\",\"bold\":true}]");
      addRunicArrowLore(loreList);
      loreList.add(NbtString.of("[{\"text\":\"Graviton Arrows:\",\"italic\":false,\"color\":\"dark_blue\",\"bold\":true},{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\",\"bold\":false}]"));
      loreList.add(NbtString.of("[{\"text\":\"These \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"Runic Arrows\",\"color\":\"light_purple\"},{\"text\":\" \"},{\"text\":\"attract\",\"color\":\"dark_aqua\"},{\"text\":\" \"},{\"text\":\"entities\",\"color\":\"aqua\"},{\"text\":\" near the area of \"},{\"text\":\"impact\",\"color\":\"aqua\"},{\"text\":\".\",\"color\":\"blue\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"A hit \",\"italic\":false,\"color\":\"blue\"},{\"text\":\"entity\",\"color\":\"aqua\"},{\"text\":\" is \"},{\"text\":\"not affected\",\"color\":\"dark_aqua\"},{\"text\":\".\"},{\"text\":\"\",\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"\",\"italic\":false,\"color\":\"dark_purple\"}]"));
      loreList.add(NbtString.of("[{\"text\":\"Exotic \",\"italic\":false,\"color\":\"aqua\",\"bold\":true},{\"text\":\"Magic Item\",\"color\":\"dark_purple\",\"bold\":false}]"));
      display.put("Lore",loreList);
      tag.put("display",display);
      tag.put("Enchantments",enchants);
      tag.putInt("CustomPotionColor",869887);
      tag.putInt("HideFlags",127);
      item.setCount(64);
      
      setBookLore(makeLore());
      //setRecipe(makeRecipe());
      prefNBT = addMagicNbt(tag);
      
      item.setNbt(prefNBT);
      prefItem = item;
   }
   
   @Override
   public void entityHit(PersistentProjectileEntity arrow, EntityHitResult entityHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         int duration = (int) MathHelper.clamp(arrow.getVelocity().length()*7,2,20);// Measured in quarter seconds
         gravitonPulse(serverWorld,null,entityHitResult.getEntity(),duration,0);
      }
   }
   
   @Override
   public void blockHit(PersistentProjectileEntity arrow, BlockHitResult blockHitResult){
      if(arrow.getEntityWorld() instanceof ServerWorld serverWorld){
         int duration = (int) MathHelper.clamp(arrow.getVelocity().length()*7,2,20); // Measured in quarter seconds
         gravitonPulse(serverWorld,blockHitResult.getPos(),null,duration,0);
      }
   }
   
   private void gravitonPulse(ServerWorld world, @Nullable Vec3d start, @Nullable Entity entity, int duration, int calls){
      if(start == null && entity == null) return;
      Vec3d pos = entity == null ? start : entity.getPos();
      double range = 3;
      
      Box rangeBox = new Box(pos.x+8,pos.y+8,pos.z+8,pos.x-8,pos.y-8,pos.z-8);
      List<Entity> entities = world.getOtherEntities(entity,rangeBox, e -> !e.isSpectator() && e.squaredDistanceTo(pos) < 2*range*range && !(e instanceof PersistentProjectileEntity));
      for(Entity entity1 : entities){
         Vec3d diff = entity1.getPos().subtract(pos);
         double multiplier = MathHelper.clamp(diff.length()*.2,.03,2);
         Vec3d motion = diff.add(0,0,0).normalize().multiply(-multiplier);
         entity1.setVelocity(motion.x,motion.y,motion.z);
         if(entity1 instanceof ServerPlayerEntity player){
            player.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(player));
         }
   
         if(entity1 instanceof LivingEntity e){
            int amp = (int) (5-diff.length());
            StatusEffectInstance slowness = new StatusEffectInstance(StatusEffects.SLOWNESS, 20, amp, false, false, true);
            e.addStatusEffect(slowness);
         }
      }
      
      ParticleEffectUtils.gravitonArrowEmit(world,pos,entities);
      if(calls % 10 == 1){
         SoundUtils.playSound(world,new BlockPos(pos), SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.PLAYERS,.5f,1.6f);
      }
      
      if(calls < duration){
         Arcananovum.addTickTimerCallback(world, new GenericTimer(5, new TimerTask() {
            @Override
            public void run(){
               gravitonPulse(world, pos, entity,duration,calls + 1);
            }
         }));
      }
   }
   
   //TODO: Make Recipe
   private MagicItemRecipe makeRecipe(){
      return null;
   }
   
   //TODO: Make Lore
   private List<String> makeLore(){
      ArrayList<String> list = new ArrayList<>();
      list.add("{\"text\":\"TODO\"}");
      return list;
   }
}
