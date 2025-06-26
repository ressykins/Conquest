package me.raindance.champions.kits.skills.marksman;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.*;

@SkillMetadata(id = 504, skillType = SkillType.Marksman, invType = InvType.PRIMARY_PASSIVE)
public class Frostbite extends Passive {
    private final HashMap<LivingEntity, Integer> frostbiteStacks = new HashMap<>();

    @Override
    public String getName() {
        return "Frostbite";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.BOW;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void shoot(DamageApplyEvent e) {
        if (!isAlly(e.getVictim()) && e.getAttacker() == getPlayer() && e.getArrow() != null && e.getCause() == Cause.PROJECTILE) {
            if(!(e.getVictim() instanceof Player)) return;
            Player player = (Player) e.getVictim();
            e.addSource(this);
            // player.setSprinting(false);

            int currentStacks = frostbiteStacks.getOrDefault(player, 0);
    
            // Increase stacks up to a maximum of 3
            if (currentStacks < 3) {
                currentStacks++;
                frostbiteStacks.put(player, currentStacks);
            }
            else {
                
            }

            StatusApplier.getOrNew(player).applyStatus(Status.SLOW, 4, currentStacks - 1, true, true);


            Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                int updatedStacks = frostbiteStacks.getOrDefault(player, 0);
                if (updatedStacks > 1) {
                    frostbiteStacks.put(player, updatedStacks - 1);
                } else {
                    frostbiteStacks.remove(player);
                }
            }, 60L);
        }
    }

    // @EventHandler(priority = EventPriority.HIGHEST)
    // public void sprint(PlayerToggleSprintEvent event) {
    //     if(affected.containsKey(event.getPlayer().getName()) && event.isSprinting())
    //         event.setCancelled(true);
    // }
}
