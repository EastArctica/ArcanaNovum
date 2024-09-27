package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.EnergyItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.recipes.arcana.GenericArcanaIngredient;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class Planeshifter extends EnergyItem {
	public static final String ID = "planeshifter";
   
   public static final String NETHER_UNLOCK_TAG = "netherUnlocked";
   public static final String END_UNLOCK_TAG = "endUnlocked";
   public static final String HEAT_TAG = "heat";
   
   public static final int[] cdReduction = {0,60,120,240,360,480};
   private static final String TXT_NONE = "item/planeshifter_none";
   private static final String TXT_OVERWORLD = "item/planeshifter_overworld";
   private static final String TXT_NETHER = "item/planeshifter_nether";
   private static final String TXT_END = "item/planeshifter_end";
   
   public Planeshifter(){
      id = ID;
      name = "Planeshifter";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.ITEMS};
      itemVersion = 0;
      initEnergy = 600;
      vanillaItem = Items.RECOVERY_COMPASS;
      item = new PlaneshifterItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.DARK_PURPLE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,TXT_NONE));
      models.add(new Pair<>(vanillaItem,TXT_OVERWORLD));
      models.add(new Pair<>(vanillaItem,TXT_NETHER));
      models.add(new Pair<>(vanillaItem,TXT_END));
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_TEMPORAL_MOMENT,ResearchTasks.DIMENSION_TRAVEL, ResearchTasks.OBTAIN_EYE_OF_ENDER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,NETHER_UNLOCK_TAG,false);
      putProperty(stack,END_UNLOCK_TAG,false);
      putProperty(stack,MODE_TAG,-1); // -1 disabled, 0 nether, 1 end
      putProperty(stack,HEAT_TAG,0);
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("A handheld ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("End").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" and ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Nether ").formatted(Formatting.RED))
            .append(Text.literal("Portal").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("shifter ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("must be taken to the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Nether ").formatted(Formatting.RED))
            .append(Text.literal("and ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("End ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("to unlock their ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("modes").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("When in ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("Nether mode").formatted(Formatting.RED))
            .append(Text.literal(", the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("shifter ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("tries to find the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("nearest portal").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("When in ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("End mode").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(", the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("shifter teleports").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" between ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("world spawns").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("It requires the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("flowing of time").formatted(Formatting.BLUE))
            .append(Text.literal(" to ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("recharge it.").formatted(Formatting.DARK_AQUA)));
      lore.add(Text.literal("")
            .append(Text.literal("Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("activate ").formatted(Formatting.BLUE))
            .append(Text.literal("the ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("shifter").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal("")
            .append(Text.literal("Sneak Right Click").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to ").formatted(Formatting.DARK_PURPLE))
            .append(Text.literal("switch modes").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_PURPLE)));
      lore.add(Text.literal(""));
      
      if(itemStack == null){
         lore.add(Text.literal("")
               .append(Text.literal("Charged - ").formatted(Formatting.DARK_PURPLE))
               .append(Text.literal("100%").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE)));
      }else{
         int charge = (getEnergy(itemStack)*100/getMaxEnergy(itemStack));
         String charging = charge == 100 ? "Charged" : "Charging";
         lore.add(Text.literal("")
               .append(Text.literal(charging+" - ").formatted(Formatting.DARK_PURPLE))
               .append(Text.literal(charge+"%").formatted(Formatting.BOLD,Formatting.LIGHT_PURPLE)));
      }

     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      boolean hasNether = getBooleanProperty(stack,NETHER_UNLOCK_TAG);
      boolean hasEnd = getBooleanProperty(stack,END_UNLOCK_TAG);
      int mode = getIntProperty(stack,MODE_TAG);
      int heat = getIntProperty(stack,HEAT_TAG);
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,NETHER_UNLOCK_TAG,hasNether);
      putProperty(newStack,END_UNLOCK_TAG,hasEnd);
      putProperty(newStack,MODE_TAG,mode);
      putProperty(newStack,HEAT_TAG,heat);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public boolean blocksHandInteractions(ItemStack item){
      return true;
   }
   
   @Override
   public int getMaxEnergy(ItemStack item){ // 10 minute recharge time
      int cdLvl = Math.max(0, ArcanaAugments.getAugmentOnItem(item,ArcanaAugments.PLANAR_FLOW.id));
      return 600 - cdReduction[cdLvl];
   }
   
   private void findPortalAndTeleport(ServerPlayerEntity player, ServerWorld destWorld, boolean destIsNether){
      double scale = DimensionType.getCoordinateScaleFactor(player.getServerWorld().getDimension(), destWorld.getDimension());
      WorldBorder worldBorder = destWorld.getWorldBorder();
      // This is jank, don't do this. Write it correctly. I just don't know how!
      Vec3d destPos3d = worldBorder.clamp(player.getX() * scale, player.getY(), player.getZ() * scale);
      BlockPos destPos = new BlockPos((int) destPos3d.x, (int) destPos3d.y, (int) destPos3d.z);
      Optional<BlockPos> portalRect = destWorld.getPortalForcer().getPortalPos(destPos, destIsNether, worldBorder);
      if(portalRect.isPresent()){
         player.teleportTo(new TeleportTarget(destWorld,portalRect.get().toCenterPos(),player.getVelocity(),player.getYaw(),player.getPitch(),TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(entityx -> entityx.addPortalChunkTicketAt(portalRect.get()))));
         player.resetPortalCooldown();
         player.sendMessage(Text.literal("The Planeshifter syncs up with a Nether Portal").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
      }else{
         player.teleport(destWorld,destPos.getX(),destPos.getY(),destPos.getZ(),new HashSet(),player.getYaw(),player.getPitch(),false);
         player.sendMessage(Text.literal("The Planeshifter could not find a Nether Portal").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
         
         for(int y = player.getBlockY(); y >= player.getBlockY()-destWorld.getHeight(); y--){
            BlockPos blockPos = new BlockPos(player.getBlockX(),y,player.getBlockZ());
            BlockState state = destWorld.getBlockState(blockPos);
            if(state.isOf(Blocks.LAVA)){
               ArcanaAchievements.grant(player,ArcanaAchievements.UNFORTUNATE_MATERIALIZATION.id);
               break;
            }else if(!(state.isAir() || state.getCollisionShape(destWorld,blockPos).isEmpty())){
               break;
            }
         }
      }
   }
   
   private void teleport(ItemStack stack, ServerPlayerEntity player){
      int mode = getIntProperty(stack,MODE_TAG);
      ServerWorld world = player.getServerWorld();
      
      boolean inOverworld = world.getRegistryKey().getValue().equals(ServerWorld.OVERWORLD.getValue());
      boolean inEnd = world.getRegistryKey().getValue().equals(ServerWorld.END.getValue());
      boolean inNether = world.getRegistryKey().getValue().equals(ServerWorld.NETHER.getValue());
      if(inNether) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id, "From The Nether",true);
      if(inEnd) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id,"From The End",true);
      if(inOverworld) ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id,"From The Overworld",true);
      
      if(mode == 0) { // nether mode
         if(inNether){
            world = world.getServer().getWorld(World.OVERWORLD);
            findPortalAndTeleport(player,world,false);
            ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id, "To The Overworld",true);
         }else{
            world = world.getServer().getWorld(World.NETHER);
            findPortalAndTeleport(player,world,true);
            ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id, "To The Nether",true);
         }
      }else if(mode == 1){ // end mode
         EndPortalBlock endPortalBlock = (EndPortalBlock) Blocks.END_PORTAL;
         
         player.teleportTo(endPortalBlock.createTeleportTarget(player.getServerWorld(),player,player.getBlockPos()));
         if(inEnd){
            ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id, "To The Overworld",true);
         }else{
            ArcanaAchievements.setCondition(player,ArcanaAchievements.PLANE_RIDER.id, "To The End",true);
         }
      }
      
      PLAYER_DATA.get(player).addXP(1000); // Add xp
      setEnergy(stack,0);
      SoundUtils.playSongToPlayer(player, SoundEvents.BLOCK_PORTAL_TRAVEL,1,2f);
      ParticleEffectUtils.recallTeleport(world,player.getPos());
   }
   
   private RegistryKey<World> getTargetWorld(RegistryKey<World> currentWorld, int mode){
       if(mode == 0){
          if(!currentWorld.getValue().equals(ServerWorld.NETHER.getValue())){
             return ServerWorld.NETHER;
          }
       }else if(mode == 1){
          if(!currentWorld.getValue().equals(ServerWorld.END.getValue())){
             return ServerWorld.END;
          }
       }
       return ServerWorld.OVERWORLD;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,12);
      ArcanaIngredient c = new ArcanaIngredient(Items.ENDER_EYE,8);
      ArcanaIngredient g = new ArcanaIngredient(Items.NETHER_STAR,1);
      ArcanaIngredient h = new ArcanaIngredient(Items.END_CRYSTAL,8);
      GenericArcanaIngredient m = new GenericArcanaIngredient(ArcanaRegistry.TEMPORAL_MOMENT,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("      Planeshifter\n\nRarity: Exotic\n\nPortals are nice, they create a stable connection between worlds. But they take too much setup, and safety isn't a concern for an Arcanist of my caliber. I can just make an unstable rift long enough to slip in.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("      Planeshifter\n\nHowever, the Shifter needs some dimensional energy of the destination before it can tear through the dimensional fabric.\nFor some fraction of safety, if the shifter finds a Portal near the destination, it should sync my rift to its location.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class PlaneshifterItem extends ArcanaPolymerItem {
      public PlaneshifterItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(player == null) return ArcanaRegistry.getModelData(TXT_NONE).value();
         int mode = getIntProperty(itemStack,MODE_TAG); // 0 nether - 1 end
         ServerWorld world = player.getServerWorld();
         String worldString = world.getRegistryKey().getValue().toString();
         boolean inEnd = worldString.equals("minecraft:the_end");
         boolean inNether = worldString.equals("minecraft:the_nether");
         
         if(getEnergy(itemStack) < getMaxEnergy(itemStack)){
            return ArcanaRegistry.getModelData(TXT_NONE).value();
         }else if(mode == 0){
            return inNether ? ArcanaRegistry.getModelData(TXT_OVERWORLD).value() : ArcanaRegistry.getModelData(TXT_NETHER).value();
         }else if(mode == 1){
            return inEnd ? ArcanaRegistry.getModelData(TXT_OVERWORLD).value() : ArcanaRegistry.getModelData(TXT_END).value();
         }
         
         return ArcanaRegistry.getModelData(TXT_NONE).value();
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity playerEntity, Hand hand) {
         ItemStack stack = playerEntity.getStackInHand(hand);
         if(!ArcanaItemUtils.isArcane(stack)) return ActionResult.PASS;
         
         int mode = getIntProperty(stack,MODE_TAG);
         boolean nether = getBooleanProperty(stack,NETHER_UNLOCK_TAG);
         boolean end = getBooleanProperty(stack,END_UNLOCK_TAG);
         
         if(playerEntity.isSneaking()){
            if(!end && !nether){
               playerEntity.sendMessage(Text.literal("The Planeshifter has not unlocked any dimensions").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            }else if(!end){
               playerEntity.sendMessage(Text.literal("The Planeshifter only has Nether mode").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            }else if(!nether){
               playerEntity.sendMessage(Text.literal("The Planeshifter only has End mode").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            }else{
               if(mode != 0){
                  playerEntity.sendMessage(Text.literal("The Planeshifter set to Nether mode").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
                  putProperty(stack,MODE_TAG,0);
               }else{
                  playerEntity.sendMessage(Text.literal("The Planeshifter set to End mode").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
                  putProperty(stack,MODE_TAG,1);
               }
            }
         }else{
            int curEnergy = getEnergy(stack);
            RegistryKey<World> targetWorld = getTargetWorld(playerEntity.getWorld().getRegistryKey(),mode);
            
            if(mode == -1){
               playerEntity.sendMessage(Text.literal("The Planeshifter has not unlocked any dimensions").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
            }else if(!playerEntity.getServer().isWorldAllowed(playerEntity.getServer().getWorld(targetWorld))){
               playerEntity.sendMessage(Text.literal("The targeted world is not enabled on this Server").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
            }else if(curEnergy >= getMaxEnergy(stack)){
               putProperty(stack,HEAT_TAG,1); // Starts the heat up process
               SoundUtils.playSound(playerEntity.getWorld(), playerEntity.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 1, 1);
            }else{
               playerEntity.sendMessage(Text.literal("Planeshifter Recharging: " + (curEnergy * 100 / getMaxEnergy(stack)) + "%").formatted(Formatting.DARK_AQUA), true);
               SoundUtils.playSongToPlayer((ServerPlayerEntity) playerEntity, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1, .5f);
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected){
         if(!ArcanaItemUtils.isArcane(stack)) return;
         if(!(world instanceof ServerWorld serverWorld && entity instanceof ServerPlayerEntity player)) return;
         int heat = getIntProperty(stack,HEAT_TAG);
         
         if(!getBooleanProperty(stack,NETHER_UNLOCK_TAG) && player.getServerWorld().getRegistryKey().equals(World.NETHER)){
            putProperty(stack,NETHER_UNLOCK_TAG,true);
            putProperty(stack,MODE_TAG,0);
            player.sendMessage(Text.literal("The Planeshifter has Unlocked The Nether").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.3f,2f);
         }
         if(!getBooleanProperty(stack,END_UNLOCK_TAG) && player.getServerWorld().getRegistryKey().equals(World.END)){
            putProperty(stack,END_UNLOCK_TAG,true);
            putProperty(stack,MODE_TAG,1);
            player.sendMessage(Text.literal("The Planeshifter has Unlocked The End").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
            SoundUtils.playSongToPlayer(player, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.3f,2f);
         }
         
         if(heat == 100){
            teleport(stack,player);
            putProperty(stack,HEAT_TAG,0);
         }else if(heat > 0){
            putProperty(stack,HEAT_TAG,heat+1);
            ParticleEffectUtils.recallTeleportCharge(serverWorld,player.getPos());
         }else if(heat == -1){
            // Teleport was cancelled by damage
            ParticleEffectUtils.recallTeleportCancel(serverWorld,player.getPos());
            SoundUtils.playSound(player.getServerWorld(), player.getBlockPos(), SoundEvents.ENTITY_ENDERMAN_HURT, SoundCategory.PLAYERS, 8,0.8f);
            putProperty(stack,HEAT_TAG,0);
            setEnergy(stack,(int)(getMaxEnergy(stack)*0.75));
         }
         
         if(world.getServer().getTicks() % 20 == 0){
            addEnergy(stack, 1); // Recharge
            buildItemLore(stack,world.getServer());
         }
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
}

