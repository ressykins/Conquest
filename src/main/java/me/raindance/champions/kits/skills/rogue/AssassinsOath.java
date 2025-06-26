package me.raindance.champions.kits.skills.rogue;


import com.podcrash.api.damage.Cause;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.time.resources.TimeResource;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@SkillMetadata(id = 603, skillType = SkillType.Rogue, invType = InvType.INNATE)
public class AssassinsOath extends Passive implements IPassiveTimer, TimeResource {
    private boolean cancel = false;

    @EventHandler
    public void hit(DamageApplyEvent event) {
        //no knockback clause
        if(event.getAttacker() != getPlayer() || event.getCause() != Cause.MELEE) return;

        event.setDoKnockback(false);
        Player victim = (Player) event.getVictim();
        KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(victim);
        double trueCurrentHP = (victim.getHealth() / victim.getMaxHealth()) * victimKitPlayer.getHP();

        if(trueCurrentHP - event.getDamage() > (victimKitPlayer.getHP() * 0.05)) return;

        event.setModified(true);
        event.setDamage(trueCurrentHP);
    }

    @Override
    public void start() {
        cancel = false;
        runAsync(1,0);
    }




    @Override
    public void task() {
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, Integer.MAX_VALUE, 0, true, true);
    }

    @Override
    public boolean cancel() {
        return cancel;
    }

    @Override
    public void cleanup() {
    }
    

    @EventHandler(priority = EventPriority.LOW)
    public void fall(EntityDamageEvent e) {
        if(getPlayer() == e.getEntity() && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            double totalDamage = e.getDamage() - 4;
            if(totalDamage <= 0) {
                e.setDamage(0);
                e.setCancelled(true);
            }
            e.setDamage(totalDamage);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnchant(PlayerInteractEvent e) {
        if (e.getPlayer() == getPlayer()) {
            cancel = true;
        }
    }
    // @EventHandler (priority = EventPriority.LOW)
    // public void onStart(GameStartEvent e) {
    //     TimeHandler.delayTime(30, () -> {
    //         StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, Integer.MAX_VALUE, 1, true);
    //     });
    // }

    // @Override
    // public void afterConstruction() {
    //     StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, Integer.MAX_VALUE, 1, true);
    // }

    // @Override
    // public void afterRespawn() {
    //     // System.out.println("afterRespawn pass");
    //     // StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, Integer.MAX_VALUE, 1, true,true);
    //     StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, Integer.MAX_VALUE, 1, true);
    //     PodcrashSpigot.debugLog("lightweight called!");
    //     TimeHandler.delayTime(3, () -> {
    //         StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, Integer.MAX_VALUE, 1, true);
    //     });
    // }

    @Override
    public String getName() {
        return "Assassin's Oath";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }
}
