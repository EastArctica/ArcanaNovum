package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.cardinalcomponents.IMagicEntityComponent;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntity;
import net.borisshoes.arcananovum.cardinalcomponents.MagicEntityComponent;
import net.borisshoes.arcananovum.items.MagicItems;
import net.borisshoes.arcananovum.items.RunicArrow;
import net.borisshoes.arcananovum.utils.MagicItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

import java.util.Iterator;
import java.util.List;

import static net.borisshoes.arcananovum.cardinalcomponents.WorldDataComponentInitializer.MAGIC_ENTITY_LIST;

public class EntityLoadCallbacks {
   
   public static void loadEntity(Entity entity, ServerWorld serverWorld){
   
   }
   
   public static void unloadEntity(Entity entity, ServerWorld serverWorld){
      try{
         IMagicEntityComponent entityComponent = MAGIC_ENTITY_LIST.get(serverWorld);
         List<MagicEntity> entities = entityComponent.getEntities();
         Iterator<MagicEntity> iter = entities.iterator();
         while(iter.hasNext()){
            MagicEntity magicEntity = iter.next();
            if(entity.getUuidAsString().equals(magicEntity.getUuid())){
               NbtCompound magicData = magicEntity.getData();
               String id = magicData.getString("id");
               if(id.equals(MagicItems.STASIS_PEARL.getId())){
                  if(entity.getRemovalReason() != Entity.RemovalReason.KILLED && entity.getRemovalReason() != Entity.RemovalReason.DISCARDED){
                     iter.remove();
                     entity.kill();
                  }
               }else if(MagicItemUtils.getItemFromId(id) instanceof RunicArrow){
                  if(entity.getRemovalReason() == Entity.RemovalReason.KILLED || entity.getRemovalReason() == Entity.RemovalReason.DISCARDED || entity.getRemovalReason() == Entity.RemovalReason.CHANGED_DIMENSION){
                     iter.remove();
                  }
               }
         
               //System.out.println("Unloading magic entity ("+id+"): "+entity.getUuidAsString()+" "+entity.getRemovalReason());
            }
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
