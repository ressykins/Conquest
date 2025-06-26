package me.raindance.champions.kits.skills.druid;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Passive;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 202, skillType = SkillType.Druid, invType = InvType.INNATE)
public class Forestwalker extends Passive implements IEnergy {
    @Override
    public String getName() {
        return "Forestwalker";
    }

    @Override
    public int getEnergyUsage() {
        return 0;
    }

    @EventHandler
    public void hit(DamageApplyEvent event) {
        if(event.isCancelled()) return;
        if(event.getCause() == Cause.CUSTOM) return;
        if(isAlly(event.getVictim())) return;
        if(event.getAttacker() == getPlayer()) {
            getEnergyBar().incrementEnergy(20);
        }
    }
}
