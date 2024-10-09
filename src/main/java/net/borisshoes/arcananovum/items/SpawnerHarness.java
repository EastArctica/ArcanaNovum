package net.borisshoes.arcananovum.items;

import net.borisshoes.arcananovum.ArcanaNovum;
import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.achievements.ArcanaAchievements;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.*;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;
import static net.borisshoes.arcananovum.cardinalcomponents.PlayerComponentInitializer.PLAYER_DATA;

public class SpawnerHarness extends ArcanaItem {
	public static final String ID = "spawner_harness";
   
   public static final String SPAWNER_TAG = "spawner";
   
   private static final String FULL_TXT = "item/spawner_harness";
   private static final String EMPTY_TXT = "item/spawner_harness_empty";
   
   public SpawnerHarness(){
      id = ID;
      name = "Spawner Harness";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.ITEMS, TomeGui.TomeFilter.BLOCKS};
      itemVersion = 1;
      vanillaItem = Items.SPAWNER;
      item = new SpawnerHarnessItem(new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.DARK_AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      models = new ArrayList<>();
      models.add(new Pair<>(vanillaItem,FULL_TXT));
      models.add(new Pair<>(vanillaItem,EMPTY_TXT));
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_SILK_TOUCH,ResearchTasks.BREAK_SPAWNER,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      putProperty(stack,SPAWNER_TAG,new NbtCompound());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("While ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("silk touch").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" fails to provide adequate finesse to obtain ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("spawners").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(",").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("through ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("magical enhancement").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" this harness should suffice.").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal("")
            .append(Text.literal("Right click").formatted(Formatting.AQUA))
            .append(Text.literal(" on a ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("mob spawner").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(" to obtain it as an ").formatted(Formatting.DARK_GREEN))
            .append(Text.literal("item").formatted(Formatting.YELLOW))
            .append(Text.literal(".").formatted(Formatting.DARK_GREEN)));
      lore.add(Text.literal(""));
      
      String type = "Uncaptured";
      if(itemStack != null){
         NbtCompound spawnerTag = getCompoundProperty(itemStack,SPAWNER_TAG);
         boolean hasSpawner = !spawnerTag.isEmpty();
         
         if(hasSpawner){
            type = "Empty Spawner";
            if(spawnerTag.contains("SpawnData")){
               NbtCompound spawnData = spawnerTag.getCompound("SpawnData");
               NbtCompound entity = spawnData.getCompound("entity");
               if(!entity.isEmpty()){
                  String entityTypeId = entity.getString("id");
                  Optional<EntityType<?>> entityType = EntityType.get(entityTypeId);
                  type = entityType.isPresent() ? entityType.get().getName().getString() : "Unknown";
               }
            }
         }
      }
      
      lore.add(Text.literal("")
            .append(Text.literal("Type - ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal(type).formatted(Formatting.DARK_GREEN))
      );
      return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      NbtCompound spawnerNbt = getCompoundProperty(stack,SPAWNER_TAG).copy();
      ItemStack newStack = super.updateItem(stack,server);
      putProperty(newStack,SPAWNER_TAG,spawnerNbt);
      return buildItemLore(newStack,server);
   }
   
   private void giveScrap(PlayerEntity player){
      ItemStack stack = new ItemStack(Items.NETHERITE_SCRAP);
      int reduction = (int) ArcanaNovum.config.getValue("ingredientReduction");
      int scrapCost = (int) Math.ceil(4.0 / reduction);
      stack.setCount(scrapCost/2);
      MiscUtils.giveStacks(player,stack);
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("   Spawner Harness\n\nRarity: Exotic\n\nSpawners have always been one of the few blocks that have beyond the reach of the silk touch enchantment.\nPerhaps I can enhance the enchant a bit further by giving the magic a Harness").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Spawner Harness\n\nto channel additional Arcana to.\n\nThe Harness itself has to be incredibly durable to withstand the Arcana driving the enchant into overdrive, however even with my best efforts the Harness can break after use.").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("   Spawner Harness\n\nRight click on a spawner with the Harness to capture the spawner. \n\nThe Harness can then place the spawner elsewhere in the world with a 15% chance of breaking after use.").formatted(Formatting.BLACK)));
      return list;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,16);
      ArcanaIngredient b = new ArcanaIngredient(Items.OBSIDIAN,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.SILK_TOUCH),1));
      ArcanaIngredient g = new ArcanaIngredient(Items.ENDER_EYE,4);
      ArcanaIngredient h = new ArcanaIngredient(Items.IRON_BARS,16);
      ArcanaIngredient m = new ArcanaIngredient(Items.NETHERITE_INGOT,1);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,b,a},
            {b,g,h,g,b},
            {c,h,m,h,c},
            {b,g,h,g,b},
            {a,b,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withEnchanter().withCore());
   }
   
   public class SpawnerHarnessItem extends ArcanaPolymerItem {
      public SpawnerHarnessItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player){
         if(!ArcanaItemUtils.isArcane(itemStack)) return ArcanaRegistry.getModelData(FULL_TXT).value();
         NbtCompound spawnerData = getCompoundProperty(itemStack,SPAWNER_TAG);
         boolean hasSpawner = spawnerData.contains("SpawnData");
         return hasSpawner ? ArcanaRegistry.getModelData(FULL_TXT).value() : ArcanaRegistry.getModelData(EMPTY_TXT).value();
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      @Override
      public ActionResult useOnBlock(ItemUsageContext context){
         World world = context.getWorld();
         PlayerEntity player = context.getPlayer();
         if(player == null) return ActionResult.PASS;
         try{
            ItemStack stack = context.getStack();
            NbtCompound spawnerTag = getCompoundProperty(stack,SPAWNER_TAG);
            
            if(!spawnerTag.isEmpty()){ // Has spawner, try to place
               Direction side = context.getSide();
               BlockPos placePos = context.getBlockPos().add(side.getVector());
               if(world.getBlockState(placePos).isAir()){
                  BlockEntity blockEntity;
                  world.setBlockState(placePos,Blocks.SPAWNER.getDefaultState(), Block.NOTIFY_ALL);
                  if ((blockEntity = world.getBlockEntity(placePos)) != null) {
                     blockEntity.read(spawnerTag,context.getWorld().getRegistryManager());
                  }
                  
                  int reinforceLvl = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.REINFORCED_CHASSIS.id));
                  double breakChance = new double[]{.15,.13,.11,.09,.07,0}[reinforceLvl];
                  if(Math.random() > breakChance){ // Chance of the harness breaking after use
                     player.sendMessage(Text.literal("The harness successfully places the spawner.").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
                     SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.BLOCK_CHAIN_PLACE, 1,.1f);
                     putProperty(stack,SPAWNER_TAG,new NbtCompound());
                     buildItemLore(stack,player.getServer());
                  }else{
                     boolean scrap = Math.max(0,ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SALVAGEABLE_FRAME.id)) > 0;
                     player.sendMessage(Text.literal("The harness shatters upon placing the spawner.").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
                     SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.ITEM_SHIELD_BREAK, 1,.5f);
                     putProperty(stack,SPAWNER_TAG,new NbtCompound());
                     buildItemLore(stack,player.getServer());
                     stack.decrementUnlessCreative(stack.getCount(),player);
                     if(scrap) giveScrap(player);
                  }
                  PLAYER_DATA.get(player).addXP((int) Math.max(0,20000*breakChance)); // Add xp
                  return ActionResult.SUCCESS;
               }else{
                  player.sendMessage(Text.literal("The harness cannot be placed here.").formatted(Formatting.RED,Formatting.ITALIC),true);
                  SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.BLOCK_FIRE_EXTINGUISH, 1,1);
               }
            }else if(world.getBlockState(context.getBlockPos()).getBlock() == Blocks.SPAWNER && world.getBlockEntity(context.getBlockPos()) instanceof MobSpawnerBlockEntity){
               MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) world.getBlockEntity(context.getBlockPos());
               NbtCompound spawnerNbt = spawner.createNbt(world.getRegistryManager());
               Entity renderedEntity = spawner.getLogic().getRenderedEntity(world,context.getBlockPos());
               if(renderedEntity != null){
                  String entityTypeId = EntityType.getId(renderedEntity.getType()).toString();
                  String entityTypeName = EntityType.get(entityTypeId).get().getName().getString();
                  player.sendMessage(Text.literal("The harness captures the "+entityTypeName+" spawner.").formatted(Formatting.DARK_AQUA,Formatting.ITALIC),true);
                  if(entityTypeId.equals(EntityType.getId(EntityType.SILVERFISH).toString())) ArcanaAchievements.grant((ServerPlayerEntity) player,ArcanaAchievements.FINALLY_USEFUL.id);
               }
               
               putProperty(stack,SPAWNER_TAG,spawnerNbt);
               world.breakBlock(context.getBlockPos(),false);
               
               SoundUtils.playSongToPlayer((ServerPlayerEntity) player, SoundEvents.BLOCK_CHAIN_BREAK, 1,.1f);
               buildItemLore(stack,player.getServer());
               
               return ActionResult.SUCCESS;
            }
         }catch (Exception e){
            e.printStackTrace();
         }
         return ActionResult.PASS;
      }
   }
}

