package net.borisshoes.arcananovum.items;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.borisshoes.arcananovum.augments.ArcanaAugments;
import net.borisshoes.arcananovum.core.ArcanaItem;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerCrossbowItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.MiscUtils;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Unit;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class AlchemicalArbalest extends ArcanaItem {
   public static final String ID = "alchemical_arbalest";
   
   public AlchemicalArbalest(){
      id = ID;
      name = "Alchemical Arbalest";
      rarity = ArcanaRarity.SOVEREIGN;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.SOVEREIGN, TomeGui.TomeFilter.EQUIPMENT};
      itemVersion = 0;
      vanillaItem = Items.CROSSBOW;
      item = new AlchemicalArbalestItem(new Item.Settings().maxCount(1).fireproof().maxDamage(1024)
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.DARK_AQUA))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
            .component(DataComponentTypes.UNBREAKABLE,new UnbreakableComponent(false))
            .component(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
      );
      researchTasks = new RegistryKey[]{ResearchTasks.UNLOCK_RADIANT_FLETCHERY,ResearchTasks.UNLOCK_STELLAR_CORE,ResearchTasks.UNLOCK_MIDNIGHT_ENCHANTER,ResearchTasks.ADVANCEMENT_OL_BETSY,ResearchTasks.ADVANCEMENT_WHOS_THE_PILLAGER_NOW,ResearchTasks.ADVANCEMENT_ARBALISTIC,ResearchTasks.OBTAIN_NETHERITE_INGOT,ResearchTasks.OBTAIN_TIPPED_ARROW,ResearchTasks.ADVANCEMENT_BREW_POTION,ResearchTasks.ADVANCEMENT_DRAGON_BREATH};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public void finalizePrefItem(MinecraftServer server){
      super.finalizePrefItem(server);
      ItemStack curPrefItem = this.getPrefItem();
      curPrefItem.set(DataComponentTypes.ENCHANTMENTS, MiscUtils.makeEnchantComponent(new EnchantmentLevelEntry(MiscUtils.getEnchantment(server.getRegistryManager(),Enchantments.MULTISHOT),1)).withShowInTooltip(false));
      this.prefItem = buildItemLore(curPrefItem, server);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      lore.add(Text.literal("")
            .append(Text.literal("This ").formatted(Formatting.YELLOW))
            .append(Text.literal("Crossbow ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("is outfitted with ").formatted(Formatting.YELLOW))
            .append(Text.literal("enchanted ").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal("clockwork mechanisms").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.YELLOW)));
      lore.add(Text.literal("")
            .append(Text.literal("Tipped Arrows").formatted(Formatting.BLUE))
            .append(Text.literal(" fired from the ").formatted(Formatting.YELLOW))
            .append(Text.literal("bow ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("create a ").formatted(Formatting.YELLOW))
            .append(Text.literal("lingering ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("field").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.YELLOW)));
      lore.add(Text.literal("")
            .append(Text.literal("Spectral Arrows").formatted(Formatting.BLUE))
            .append(Text.literal(" create a zone of ").formatted(Formatting.YELLOW))
            .append(Text.literal("damage ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("amplification").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.YELLOW)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.YELLOW))
            .append(Text.literal("Crossbow ").formatted(Formatting.DARK_AQUA))
            .append(Text.literal("is ").formatted(Formatting.YELLOW))
            .append(Text.literal("Unbreakable ").formatted(Formatting.BLUE))
            .append(Text.literal("and comes with ").formatted(Formatting.YELLOW))
            .append(Text.literal("Multishot").formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(".").formatted(Formatting.YELLOW)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public ItemStack updateItem(ItemStack stack, MinecraftServer server){
      ItemStack newStack = super.updateItem(stack,server);
      return buildItemLore(newStack,server);
   }
   
   @Override
   public ItemStack forgeItem(Inventory inv){
      ItemStack bowStack = inv.getStack(12); // Should be the Crossbow
      ItemStack newArcanaItem = getNewItem();
      if(bowStack.hasEnchantments()){
         ItemEnchantmentsComponent.Builder enchantBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
         ItemEnchantmentsComponent comp = bowStack.getEnchantments();
         Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchants = new Object2IntOpenHashMap<>();
         comp.getEnchantmentEntries().forEach(entry -> enchants.addTo(entry.getKey(),entry.getIntValue()));
         
         enchants.forEach((e,num) -> {
            if(!e.value().effects().contains(EnchantmentEffectComponentTypes.PROJECTILE_PIERCING)){
               enchantBuilder.add(e,num);
            }
         });
         EnchantmentHelper.set(newArcanaItem,enchantBuilder.build());
      }
      newArcanaItem.addEnchantment(MiscUtils.getEnchantment(Enchantments.MULTISHOT),1);
      return newArcanaItem;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient a = new ArcanaIngredient(Items.GLOWSTONE_DUST,32);
      ArcanaIngredient b = new ArcanaIngredient(Items.NETHER_WART,16);
      ArcanaIngredient c = new ArcanaIngredient(Items.DRAGON_BREATH,32);
      ArcanaIngredient d = new ArcanaIngredient(Items.BLAZE_POWDER,24);
      ArcanaIngredient g = new ArcanaIngredient(Items.ENCHANTED_BOOK,1).withEnchantments(new EnchantmentLevelEntry(MiscUtils.getEnchantment(Enchantments.MULTISHOT),1));
      ArcanaIngredient h = new ArcanaIngredient(Items.NETHERITE_INGOT,2);
      ArcanaIngredient k = new ArcanaIngredient(Items.FERMENTED_SPIDER_EYE,32);
      ArcanaIngredient l = new ArcanaIngredient(Items.NETHER_STAR,4);
      ArcanaIngredient m = new ArcanaIngredient(Items.CROSSBOW,1, true);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,c,d,a},
            {b,g,h,g,d},
            {k,l,m,l,k},
            {d,g,h,g,b},
            {a,d,c,b,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement().withAnvil().withCore().withFletchery().withEnchanter());
      
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("  Alchemical Arbalest\n\nRarity: Sovereign\n\nWhile bows are excellent for sustained damage, crossbows have always been good at bursts of damage, and area suppression. \nI believe I can enhance this niche further...\n").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Alchemical Arbalest\n\nThe Arbalest overcharges Tipped Arrows so that their effects cover a wide space.\nIt also comes with multishot pre-installed\nSpectral Arrows are where things get interesting. They carry no discrete effect but cause ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("  Alchemical Arbalest\n\ncreatures to glow.\nTweaking this ability a bit when used in the Arbalest, Spectral Arrows now create a zone that makes weakpoints on enemies easier to see, causing them to take increased damage from all sources.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class AlchemicalArbalestItem extends ArcanaPolymerCrossbowItem {
      
      public AlchemicalArbalestItem(Item.Settings settings){
         super(getThis(),settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
      
      public void verifyEnchantments(ItemStack stack){
         boolean hasMulti = EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PROJECTILE_COUNT);
         boolean hasPierce = EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PROJECTILE_PIERCING);
         boolean hasScatter = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.SCATTERSHOT.id) > 0;
         boolean hasRunic = ArcanaAugments.getAugmentOnItem(stack,ArcanaAugments.RUNIC_ARBALEST.id) > 0;
         
         ItemEnchantmentsComponent.Builder enchantBuilder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
         ItemEnchantmentsComponent comp = stack.getEnchantments();
         Object2IntOpenHashMap<RegistryEntry<Enchantment>> enchants = new Object2IntOpenHashMap<>();
         comp.getEnchantmentEntries().forEach(entry -> enchants.addTo(entry.getKey(),entry.getIntValue()));
         
         if(hasRunic && hasMulti){ // Remove multishot
            enchants.forEach((e,num) -> {
               if(!e.value().effects().contains(EnchantmentEffectComponentTypes.PROJECTILE_COUNT)){
                  enchantBuilder.add(e,num);
               }
            });
            EnchantmentHelper.set(stack,enchantBuilder.build());

            verifyEnchantments(stack);
         }
         if(hasScatter && hasPierce){ // Remove pierce
            enchants.forEach((e,num) -> {
               if(!e.value().effects().contains(EnchantmentEffectComponentTypes.PROJECTILE_PIERCING)){
                  enchantBuilder.add(e,num);
               }
            });
            EnchantmentHelper.set(stack,enchantBuilder.build());
            
            verifyEnchantments(stack);
         }
         if(hasScatter && !hasMulti){ // Re-add multishot
            stack.addEnchantment(MiscUtils.getEnchantment(Enchantments.MULTISHOT),1);
            verifyEnchantments(stack);
         }
      }
      
      @Override
      public ActionResult use(World world, PlayerEntity user, Hand hand) {
         ItemStack itemStack = user.getStackInHand(hand);
         verifyEnchantments(itemStack);
         return super.use(world,user,hand);
      }
   }
}

