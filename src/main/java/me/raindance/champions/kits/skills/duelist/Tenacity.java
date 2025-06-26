package me.raindance.champions.kits.skills.duelist;

import com.podcrash.api.effect.status.Status;
import com.podcrash.api.events.StatusApplyEvent;
import com.podcrash.api.kits.skilltypes.Passive;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.enums.InvType;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 310, skillType = SkillType.Duelist, invType = InvType.SECONDARY_PASSIVE)
public class Tenacity extends Passive {
    @Override
    public String getName() {
        return "Tenacity";
    }

    @EventHandler
    public void onStatusApply(StatusApplyEvent event) {
        if (event.getEntity() == getPlayer()) {
            Status status = event.getStatus();
            if (status.isNegative()) {
                event.setDuration(event.getDuration() / 2);
                event.setModified(true);
            }
        }
    }
}
