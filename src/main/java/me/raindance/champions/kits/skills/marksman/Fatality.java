package me.raindance.champions.kits.skills.marksman;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SkillMetadata(id = 501, skillType = SkillType.Marksman, invType = InvType.PRIMARY_PASSIVE)
public class Fatality extends Passive {
    @Override
    public String getName() {
        return "Fatality";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void bowHit(DamageApplyEvent event) {
        if(event.getAttacker() != getPlayer() || event.getCause() != Cause.PROJECTILE || isAlly(event.getVictim())) return;
        
        if(!event.getArrow().isCritical()) return;
        
        Player victim = (Player) event.getVictim();
        KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(victim);
        double trueCurrentHP = (victim.getHealth() / victim.getMaxHealth()) * victimKitPlayer.getHP();

        if(trueCurrentHP - event.getDamage() > (victimKitPlayer.getHP() * 0.10)) return;

        event.setModified(true);
        event.setDamage(trueCurrentHP);
        event.addSource(this);
    }
}
