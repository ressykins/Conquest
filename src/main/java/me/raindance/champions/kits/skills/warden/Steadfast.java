package me.raindance.champions.kits.skills.warden;

import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.StatusApplyEvent;
import com.podcrash.api.kits.skilltypes.Passive;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.enums.InvType;

import org.bukkit.event.EventHandler;

@SkillMetadata(id = 911, skillType = SkillType.Warden, invType = InvType.SECONDARY_PASSIVE)
public class Steadfast extends Passive {

    // private int radius = 6;
    // private double reductionPerPlayer = 0.75;

    @Override
    public String getName() {
        return "Steadfast";
    }


    @EventHandler
    public void onStatusApply(StatusApplyEvent event) {
        if (event.getEntity() != getPlayer()) return;
        if (event.getStatus().isNegative()) {
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.RESISTANCE, 5, 0, true, true);
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, 5, 0, true, true);
        }
    }

    // @EventHandler
    // public void onDamage(DamageApplyEvent e) {
    //     if(e.getVictim().equals(getPlayer()) && (e.getCause().equals(Cause.MELEE) || e.getCause().equals(Cause.MELEESKILL))) {
    //         double numEnemies = 0;
    //         for(Player p : BlockUtil.getPlayersInArea(getPlayer().getLocation(), radius, getPlayers())) {
    //             if(!isAlly(p)) numEnemies++;
    //         }

    //         e.setDamage(Math.max(e.getDamage() - (numEnemies * reductionPerPlayer), 0));
    //         e.setModified(true);
    //     }
    // }
}