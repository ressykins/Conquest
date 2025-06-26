package me.raindance.champions.kits.skills.warden;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import me.raindance.champions.annotation.kits.SkillMetadata;

@SkillMetadata(id = 901, skillType = SkillType.Warden, invType = InvType.SECONDARY_PASSIVE)
public class Debilitate extends Passive {
    private LivingEntity markedEnemy = null;
    private long appliedMark = -3000L;

    @Override
    public String getName() {
        return "Debilitate";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void damage(DamageApplyEvent e) {
        if(e.isCancelled()) return;
        if(isAlly(e.getVictim())) return;
        if(e.getAttacker() == getPlayer()) {
            markedEnemy = e.getVictim();
            appliedMark = System.currentTimeMillis();
        }
        if(isAlly(e.getAttacker()) && e.getCause() == Cause.MELEE && e.getVictim() == markedEnemy && System.currentTimeMillis() - appliedMark <= 3000L && e.getAttacker() != getPlayer()) {
            e.setDamage(e.getDamage() + 1);
            e.setModified(true);
            e.addSource(this);
        }
    }
}
