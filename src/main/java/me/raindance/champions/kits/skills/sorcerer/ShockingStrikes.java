package me.raindance.champions.kits.skills.sorcerer;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.enums.ItemType;

import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 1017, skillType = SkillType.Sorcerer, invType = InvType.PRIMARY_PASSIVE)
public class ShockingStrikes extends Passive {
    public ShockingStrikes() {
        super();
    }

    @Override
    public String getName() {
        return "Shocking Strikes";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void onHit(DamageApplyEvent event) {
        if (event.isCancelled()) return;
        if(event.getCause() != Cause.MELEE) return;
        if(event.getAttacker() != getPlayer()) return;

        event.setDoKnockback(false);
        event.addSource(this);
        event.setDamage(event.getDamage() + 2);
        StatusApplier.getOrNew(event.getVictim()).applyStatus(Status.SHOCK, 4, 0);


        Location victLoc = event.getVictim().getLocation();
        for (Player player : getPlayers()){

            if (isAlly(player) || event.getVictim() == player || getPlayer() == player || victLoc.distanceSquared(player.getLocation()) > 6D) continue;
            if (!StatusApplier.getOrNew(player).has(Status.SHOCK)) continue;
            DamageApplier.damage(player, getPlayer(), event.getDamage() , this, false);
            // DamageApplier.nativeApplyKnockback(player, getPlayer());
        }
    }
}
