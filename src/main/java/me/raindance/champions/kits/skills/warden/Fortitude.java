package me.raindance.champions.kits.skills.warden;

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
import org.bukkit.event.entity.EntityDamageEvent;

@SkillMetadata(id = 906, skillType = SkillType.Warden, invType = InvType.INNATE)
public class Fortitude extends Passive {

    @Override
    public String getName() {
        return "Fortitude";
    }

    // @Override
    // public float getCooldown() {
    //     return 4.0F;
    // }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler(priority = EventPriority.LOW)
    protected void hit(DamageApplyEvent event) {
        if(event.isCancelled()) return;
        if (event.getVictim() == getPlayer()) {
            hit();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    protected void hit(EntityDamageEvent event) {
        if(event.isCancelled()) return;
        if (event.getEntity() == getPlayer() && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            hit();
        }
    }

    public void hit() {
        // if(onCooldown()) return;
        // setLastUsed(System.currentTimeMillis());
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.REGENERATION, 5, 0);
    }

    // @Override
    // public void setLastUsed(long time) {
    //     this.setLastUsedDirect(time);
    // }

}
