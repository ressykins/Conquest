package me.raindance.champions.kits.skills.berserker;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 108, skillType = SkillType.Berserker, invType = InvType.SECONDARY_PASSIVE)
public class BattleRage extends Passive {
    @Override
    public String getName() {
        return "Battle Rage";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }


    @EventHandler
    public void damage(DamageApplyEvent e) {
        if(e.getAttacker() == getPlayer() && !isAlly(e.getVictim())) {
            if(e.getCause() != Cause.MELEE) return;
            
            if (getChampionsPlayer().getEnergyBar().getEnergy() >= 4) {
                StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, 3, 0, true, false);
                StatusApplier.getOrNew(getPlayer()).applyStatus(Status.RESISTANCE, 3, 0, true, false);
            }
        }
    }
}
