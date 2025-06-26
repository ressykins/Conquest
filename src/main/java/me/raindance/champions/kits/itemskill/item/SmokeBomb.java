package me.raindance.champions.kits.itemskill.item;

import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.ItemCollideEvent;
import com.podcrash.api.game.GameManager;
import com.podcrash.api.item.ItemManipulationManager;
import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.annotation.ItemMetaData;
import me.raindance.champions.kits.itemskill.IItem;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ItemMetaData(mat = Material.FIREWORK_CHARGE, actions = {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK})
public class SmokeBomb implements IItem, Listener {
    private final Map<Integer, String> itemIDs = new HashMap<>();

    @Override
    public void useItem(Player player, Action action) {
        Location location = player.getLocation();
        Vector vector = location.getDirection();
        vector = throwVector(vector);
        Item spawnItem = ItemManipulationManager.regular(Material.FIREWORK_CHARGE, player.getEyeLocation(), vector);
        itemIDs.put(spawnItem.getEntityId(), player.getName());
        ItemManipulationManager.intercept(spawnItem, 1.1,
                (Item item, LivingEntity intercepted, Location land) -> bomb(KitPlayerManager.getInstance().getKitPlayer(player), item, intercepted, land));
    }

    private void bomb(KitPlayer user, Item item, LivingEntity intercepted, Location land) {
        World world = item.getWorld();
        // world.playEffect(item.getLocation(), Effect.EXPLOSION_HUGE, 1);
        world.playSound(item.getLocation(), Sound.FIZZ, 2f, 0.5f);
        List<LivingEntity> entities = world.getLivingEntities();
        for(LivingEntity entity : entities) {
            if(entity.getLocation().distanceSquared(item.getLocation()) > 16D) continue;
            if (entity instanceof Player && GameManager.getGame().isParticipating((Player) entity) && user.isAlly((Player) entity)) continue;
            StatusApplier.getOrNew(entity).applyStatus(Status.BLIND, 3, 0);
            StatusApplier.getOrNew(entity).applyStatus(Status.SLOW, 3, 0);
        }
        item.remove();
    }
    @Override
    public String getName() {
        return "Smoke Bomb";
    }

    @EventHandler
    public void collideItem(ItemCollideEvent e) {
        if(e.isCancelled()) return;
        //identity check + owner of item check = cancel collision
        String ownerName = itemIDs.get(e.getItem().getEntityId());
        if(ownerName == null) return;
        if(!ownerName.equalsIgnoreCase(e.getCollisionVictim().getName())) return;
        e.setCancelled(true);
    }
}
