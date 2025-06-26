package me.raindance.champions.kits.skills.berserker;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;


@SkillMetadata(id = 104, skillType = SkillType.Berserker, invType = InvType.SECONDARY_PASSIVE)
public class Cleave extends Passive {
    private float multiplier = 1.0F;

    @Override
    public String getName() {
        return "Cleave";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void damage(DamageApplyEvent e) {
        if (e.isCancelled()) return;

        if (e.getAttacker() != getPlayer() || e.getCause() != Cause.MELEE) return;

        if (getChampionsPlayer().getEnergyBar().getEnergy() < 4) return;

        Location victLoc = e.getVictim().getLocation();
        for (Player player : getPlayers()){

            if (isAlly(player) || e.getVictim() == player || getPlayer() == player || victLoc.distanceSquared(player.getLocation()) > 4D) continue;
            DamageApplier.damage(player, getPlayer(), e.getDamage() * (double) multiplier, this, true);
            DamageApplier.nativeApplyKnockback(player, getPlayer());
            e.addSource(this);
        }
    }
}

