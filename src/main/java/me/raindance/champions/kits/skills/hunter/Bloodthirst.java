package me.raindance.champions.kits.skills.hunter;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.kits.enums.ItemType;

import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.util.EntityUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
@SkillMetadata(id = 412, skillType = SkillType.Hunter, invType = InvType.PRIMARY_PASSIVE)
public class Bloodthirst extends Passive {

    @Override
    public String getName() {
        return "Bloodthirst";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void damage(DamageApplyEvent e) {
        if (getPlayer() != e.getAttacker() || !(e.getVictim() instanceof Player)) return;
        

        if (StatusApplier.getOrNew(e.getVictim()).has(Status.BLEED) && EntityUtil.isBelow(e.getVictim(), 0.50)) {
            getChampionsPlayer().heal(e.getDamage() * 0.3);
        }
        else {
            getChampionsPlayer().heal(e.getDamage() * 0.15);
        }
    }
}
