package me.raindance.champions.kits.skills.hunter;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.StatusApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 402, skillType = SkillType.Hunter, invType = InvType.PRIMARY_PASSIVE)
public class Footwork extends Passive {
    @Override
    public String getName() {
        return "Footwork";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void bowHit(DamageApplyEvent event) {
        if(event.getAttacker() != getPlayer() || event.getCause() != Cause.PROJECTILE || isAlly(event.getVictim())) return;
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, 3, 0, true, false);
    }

    @EventHandler
    public void onSlow(StatusApplyEvent e) {
        if (e.getStatus().equals(Status.SLOW)) {
            // TODO add a sound effect for this maybe ...
            e.setCancelled(true);
        }
    }
}
