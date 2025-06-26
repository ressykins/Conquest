package me.raindance.champions.kits.skills.vanguard;

import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;

import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;


@SkillMetadata(id = 807, skillType = SkillType.Vanguard, invType = InvType.SECONDARY_PASSIVE)
public class Colossus extends Passive {
    @Override
    public String getName() {
        return "Colossus";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void damage(DamageApplyEvent e){
        if(e.isCancelled()) return;
        if(e.getVictim() == getPlayer() && StatusApplier.getOrNew(getPlayer()).has(Status.ABSORPTION)) {
            e.setModified(true);
            e.setDoKnockback(false);
        }
    }
}
