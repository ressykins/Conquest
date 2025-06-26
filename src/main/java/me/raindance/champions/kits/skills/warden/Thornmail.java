package me.raindance.champions.kits.skills.warden;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SkillMetadata(id = 907, skillType = SkillType.Warden, invType = InvType.PRIMARY_PASSIVE)
public class Thornmail extends Passive {
    // @Override
    // public float getCooldown() {
    //     return 9;
    // }

    @Override
    public String getName() {
        return "Thornmail";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void hit(DamageApplyEvent event) {
        if(event.isCancelled()) return;
        if(event.getVictim() != getPlayer()) return;
        if(event.getCause() != Cause.MELEE && event.getCause() != Cause.PROJECTILE) return;
        if(event.getCause() == Cause.MELEE) {
            DamageApplier.damage(event.getAttacker(), getPlayer(), 2, this, false);
        } 
    }
}
