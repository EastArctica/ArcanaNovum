package net.borisshoes.arcananovum.callbacks;

import net.borisshoes.arcananovum.items.*;

import java.util.HashMap;

public class LoginCallbacks {
   public static HashMap<String, LoginCallback> registry = new HashMap<>();
   
   public static final LoginCallback SHIELD_OF_FORTITUDE = LoginCallbacks.register("shield_of_fortitude",new ShieldLoginCallback());
   public static final LoginCallback CONTINUUM_ANCHOR = LoginCallbacks.register("continuum_anchor",new AnchorTimeLoginCallback());
   public static final LoginCallback IGNEOUS_COLLIDER = LoginCallbacks.register("igneous_collider",new ColliderLoginCallback());
   
   private static LoginCallback register(String id, LoginCallback callback){
      registry.put(id,callback);
      return callback;
   }
}
