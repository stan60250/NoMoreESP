package tw.mics.spigot.plugin.nomoreesp.schedule;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import tw.mics.spigot.plugin.nomoreesp.Config;
import tw.mics.spigot.plugin.nomoreesp.EntityHider;
import tw.mics.spigot.plugin.nomoreesp.NoMoreESP;
import tw.mics.spigot.plugin.nomoreesp.runnable.CheckHideEntityRunnable;

public class CheckSchedule {
    NoMoreESP plugin;
    Runnable runnable;
    public EntityHider hider;
    boolean keep_check;
    private HashSet<EntityType> hide_list;

    public CheckSchedule(NoMoreESP i) {
        plugin = i;
        keep_check = true;
        hider = new EntityHider(plugin);
        
        //load config
        hide_list = new HashSet<EntityType>();
        for(String type : Config.HIDE_ENTITY_HIDE_LIST.getStringList()){
            hide_list.add(EntityType.valueOf(type));
        }
        
        setupRunnable();
    }

    private void setupRunnable() {
        check();
    }

    protected void check() {
        Bukkit.getScheduler().runTaskAsynchronously(NoMoreESP.getInstance(), 
            new Runnable(){
                @Override
                public void run() {
                    try{
                        while(keep_check){
                            Iterator<? extends Player> iter_online_player = plugin.getServer().getOnlinePlayers().iterator();
                            while (iter_online_player.hasNext()) {
                                Player player = iter_online_player.next();
                                if(!player.isOnline() || !player.isValid())
                                    continue;
                                
                                //check bypass
                                if(
                                		(Config.HIDE_ENTITY_BYPASS_OP.getBoolean() && player.isOp()) || //bypass op
                                		(Config.HIDE_ENTITY_BYPASS_SPECTATOR.getBoolean() && player.getGameMode().equals(GameMode.SPECTATOR)) || //bypass spectator
                                		(Config.HIDE_ENTITY_BYPASS_BY_PERMISSION.getBoolean() && player.hasPermission("nomoreesp.hide-entity.bypass")) //bypass by permission
                                )
                                	continue;
                                	
                                //hide entity
                                if(
                                        Config.HIDE_ENTITY_ENABLE.getBoolean() && 
                                        Config.HIDE_ENTITY_ENABLE_WORLDS.getStringList().contains(player.getWorld().getName())
                                ){
                                    //this shouldn't async
                                    List<Entity> nearbyEntities = player.getNearbyEntities(Config.HIDE_ENTITY_HIDE_RANGE.getInt() * 2,
                                            player.getWorld().getMaxHeight(), Config.HIDE_ENTITY_HIDE_RANGE.getInt() * 2);
                                    
                                    nearbyEntities.remove(player);
                                    nearbyEntities.forEach(target -> {
                                    if(
                                    		//target in hide list
                                    		hide_list.contains(target.getType()) &&
                                    		
                                    		//target without glowing
                                    		!target.isGlowing() &&
                                    		
                                    		//target without glowing PotionEffect
                                    		!(
                                    			target instanceof LivingEntity &&
                                    			((LivingEntity)target).hasPotionEffect(PotionEffectType.GLOWING)
                                    		)
                                    ) 
                                        Bukkit.getScheduler().runTaskAsynchronously(NoMoreESP.getInstance(), 
                                                new CheckHideEntityRunnable(hider, player, target));
                                    });
                                }
                                
                            }
                            Thread.sleep(200);
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                        try { Thread.sleep(200); } catch (InterruptedException ee) {}
                        if(keep_check) check();
                    }
                }
            }
        );
    }

    public void removeRunnable() {
        keep_check = false;
    }
}
