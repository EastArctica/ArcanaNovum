package net.borisshoes.arcananovum.blocks.altars;

import net.borisshoes.arcananovum.ArcanaRegistry;
import net.borisshoes.arcananovum.core.ArcanaBlock;
import net.borisshoes.arcananovum.core.Multiblock;
import net.borisshoes.arcananovum.core.MultiblockCore;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockEntity;
import net.borisshoes.arcananovum.core.polymer.ArcanaPolymerBlockItem;
import net.borisshoes.arcananovum.gui.arcanetome.TomeGui;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaIngredient;
import net.borisshoes.arcananovum.recipes.arcana.ArcanaRecipe;
import net.borisshoes.arcananovum.recipes.arcana.ForgeRequirement;
import net.borisshoes.arcananovum.research.ResearchTasks;
import net.borisshoes.arcananovum.utils.ArcanaRarity;
import net.borisshoes.arcananovum.utils.TextUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static net.borisshoes.arcananovum.ArcanaNovum.MOD_ID;

public class CelestialAltar extends ArcanaBlock implements MultiblockCore {
	public static final String ID = "celestial_altar";
   
   private Multiblock multiblock;
   
   public CelestialAltar(){
      id = ID;
      name = "Celestial Altar";
      rarity = ArcanaRarity.EXOTIC;
      categories = new TomeGui.TomeFilter[]{TomeGui.TomeFilter.EXOTIC, TomeGui.TomeFilter.BLOCKS, TomeGui.TomeFilter.ALTARS};
      itemVersion = 0;
      vanillaItem = Items.PEARLESCENT_FROGLIGHT;
      block = new CelestialAltarBlock(AbstractBlock.Settings.create().mapColor(MapColor.PINK).strength(.3f,1200.0f).luminance(state -> 15).sounds(BlockSoundGroup.FROGLIGHT));
      item = new CelestialAltarItem(this.block,new Item.Settings().maxCount(1).fireproof()
            .component(DataComponentTypes.ITEM_NAME, Text.translatable("item."+MOD_ID+"."+ID).formatted(Formatting.BOLD,Formatting.BLUE))
            .component(DataComponentTypes.LORE, new LoreComponent(getItemLore(null)))
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
      );
      researchTasks = new RegistryKey[]{ResearchTasks.OBTAIN_STARDUST,ResearchTasks.OBTAIN_NETHER_STAR,ResearchTasks.ADVANCEMENT_OBTAIN_CRYING_OBSIDIAN};
      
      ItemStack stack = new ItemStack(item);
      initializeArcanaTag(stack);
      stack.setCount(item.getMaxCount());
      setPrefStack(stack);
   }
   
   @Override
   public List<Text> getItemLore(@Nullable ItemStack itemStack){
      List<MutableText> lore = new ArrayList<>();
      addAltarLore(lore);
      lore.add(Text.literal("Celestial Altar:").formatted(Formatting.BOLD,Formatting.BLUE));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Altar ").formatted(Formatting.BLUE))
            .append(Text.literal("glistens ").formatted(Formatting.YELLOW))
            .append(Text.literal("in the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("light ").formatted(Formatting.YELLOW))
            .append(Text.literal("of the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Sun ").formatted(Formatting.YELLOW))
            .append(Text.literal("and ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Moon").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Altar ").formatted(Formatting.BLUE))
            .append(Text.literal("lets you change the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("time of day").formatted(Formatting.YELLOW))
            .append(Text.literal(" and the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("phase").formatted(Formatting.BLUE))
            .append(Text.literal(" of the ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Moon").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
      lore.add(Text.literal("")
            .append(Text.literal("The ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Altar ").formatted(Formatting.BLUE))
            .append(Text.literal("requires a ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("Nether Star").formatted(Formatting.YELLOW))
            .append(Text.literal(" to ").formatted(Formatting.DARK_GRAY))
            .append(Text.literal("activate").formatted(Formatting.BLUE))
            .append(Text.literal(".").formatted(Formatting.DARK_GRAY)));
     return lore.stream().map(TextUtils::removeItalics).collect(Collectors.toCollection(ArrayList::new));
   }
   
   @Override
   public void loadMultiblock(){
      multiblock = Multiblock.loadFromFile(getId());
   }
   
   @Override
   public Vec3i getCheckOffset(){
      return new Vec3i(-5,0,-5);
   }
   
   @Override
   public Multiblock getMultiblock(){
      return multiblock;
   }
   
   @Override
	protected ArcanaRecipe makeRecipe(){
      ArcanaIngredient p = new ArcanaIngredient(Items.SEA_LANTERN,4);
      ArcanaIngredient a = new ArcanaIngredient(Items.CRYING_OBSIDIAN,32);
      ArcanaIngredient b = new ArcanaIngredient(Items.GOLD_BLOCK,4);
      ArcanaIngredient r = new ArcanaIngredient(Items.LAPIS_BLOCK,4);
      ArcanaIngredient d = new ArcanaIngredient(Items.GLOWSTONE,4);
      ArcanaIngredient g = new ArcanaIngredient(Items.OBSIDIAN,32);
      ArcanaIngredient k = new ArcanaIngredient(ArcanaRegistry.STARDUST,12);
      ArcanaIngredient l = new ArcanaIngredient(Items.NETHER_STAR,2);
      ArcanaIngredient m = new ArcanaIngredient(Items.PEARLESCENT_FROGLIGHT,16);
      
      ArcanaIngredient[][] ingredients = {
            {a,b,b,d,d},
            {b,g,b,d,d},
            {k,l,m,l,k},
            {p,p,r,g,r},
            {p,p,r,r,a}};
      return new ArcanaRecipe(ingredients,new ForgeRequirement());
   }
   
   @Override
   public List<List<Text>> getBookLore(){
      List<List<Text>> list = new ArrayList<>();
      list.add(List.of(Text.literal("     Celestial Altar\n\nRarity: Exotic\n\nLeylines across the world have their influence extend into the orbit of the planet. If I can provide a sufficient energy source to the structure, I should be able to accelerate the planet, or the moon! ").formatted(Formatting.BLACK)));
      list.add(List.of(Text.literal("     Celestial Altar\n\nA Nether Star should be sufficient to let this altar change the time of day and the phase of the moon by accelerating the planet and moon's orbital periods for a brief moment before the Star is depleted and the orbits normalize.").formatted(Formatting.BLACK)));
      return list;
   }
   
   public class CelestialAltarItem extends ArcanaPolymerBlockItem {
      public CelestialAltarItem(Block block, Settings settings){
         super(getThis(),block, settings);
      }
      
      @Override
      public ItemStack getDefaultStack(){
         return prefItem;
      }
   }
   
   public class CelestialAltarBlock extends ArcanaPolymerBlockEntity {
      public static final EnumProperty<Direction> HORIZONTAL_FACING = Properties.HORIZONTAL_FACING;
      
      public CelestialAltarBlock(AbstractBlock.Settings settings){
         super(settings);
      }
      
      @Override
      public BlockState getPolymerBlockState(BlockState state){
         return Blocks.PEARLESCENT_FROGLIGHT.getDefaultState();
      }
      
      @Nullable
      @Override
      public BlockState getPlacementState(ItemPlacementContext ctx) {
         return this.getDefaultState().with(HORIZONTAL_FACING,ctx.getHorizontalPlayerFacing().getOpposite());
      }
      
      @Override
      protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
         stateManager.add(HORIZONTAL_FACING);
      }
      
      @Override
      public BlockState rotate(BlockState state, BlockRotation rotation) {
         return state.with(HORIZONTAL_FACING, rotation.rotate(state.get(HORIZONTAL_FACING)));
      }
      
      @Override
      public BlockState mirror(BlockState state, BlockMirror mirror) {
         return state.rotate(mirror.getRotation(state.get(HORIZONTAL_FACING)));
      }
      
      @Nullable
      public static CelestialAltarBlockEntity getEntity(World world, BlockPos pos) {
         BlockState state = world.getBlockState(pos);
         if (!(state.getBlock() instanceof CelestialAltarBlock)) {
            return null;
         }
         return world.getBlockEntity(pos) instanceof CelestialAltarBlockEntity altar ? altar : null;
      }
      
      @Override
      public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
         return new CelestialAltarBlockEntity(pos, state);
      }
      
      @Nullable
      @Override
      public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
         return validateTicker(type, ArcanaRegistry.CELESTIAL_ALTAR_BLOCK_ENTITY, CelestialAltarBlockEntity::ticker);
      }
      
      @Override
      protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerEntity, BlockHitResult hit){
         CelestialAltarBlockEntity altar = (CelestialAltarBlockEntity) world.getBlockEntity(pos);
         if(altar != null){
            if(playerEntity instanceof ServerPlayerEntity player){
               if(altar.isAssembled()){
                  altar.openGui(player);
                  player.getItemCooldownManager().set(playerEntity.getMainHandStack(),1);
               }else{
                  player.sendMessage(Text.literal("Multiblock not constructed."));
                  multiblock.displayStructure(altar.getMultiblockCheck(),player);
               }
            }
         }
         return ActionResult.SUCCESS;
      }
      
      @Override
      public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
         BlockEntity entity = world.getBlockEntity(pos);
         if (placer instanceof ServerPlayerEntity player && entity instanceof CelestialAltarBlockEntity altar) {
            initializeArcanaBlock(stack,altar);
         }
      }
   }
}

