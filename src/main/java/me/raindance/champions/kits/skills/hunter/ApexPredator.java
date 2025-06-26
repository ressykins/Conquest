package me.raindance.champions.kits.skills.hunter;

import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 403, skillType = SkillType.Hunter, invType = InvType.PRIMARY_PASSIVE)
public class ApexPredator extends Passive {
    public ApexPredator() {
        super();
    }

    @Override
    public String getName() {
        return "Apex Predator";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void damage(DamageApplyEvent e) {
        if (getPlayer() != e.getAttacker() || !(e.getVictim() instanceof Player)) return;

        StatusApplier applier = StatusApplier.getOrNew(e.getVictim());
    
        // Use a flag to ensure only one application of the effect
        // boolean appliedEffect = false;
        int bonus = 0;
    
        for (Status status : applier.getEffects()) {
            if (status.isNegative()) {
                bonus++;
            }
        }

        if (bonus < 1) return;
        e.addSource(this);
        e.setDamage(e.getDamage() + bonus);
        e.setModified(true);
    }
}
