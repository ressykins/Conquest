package me.raindance.champions.kits.skills.duelist;

import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;

import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.util.EntityUtil;

import org.bukkit.event.EventHandler;

@SkillMetadata(id = 302, skillType = SkillType.Duelist, invType = InvType.SECONDARY_PASSIVE)
public class Lifeline extends Passive implements ICooldown {

    @Override
    public String getName() {
        return "Lifeline";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }
    @Override
    public float getCooldown() {
        return 13;
    }

    @EventHandler
    public void damage(DamageApplyEvent e) {
        if(getPlayer() != e.getVictim()) return;
        if(!EntityUtil.isBelow(getPlayer(), 0.5)) return;

        if(onCooldown()) return;
        setLastUsed(System.currentTimeMillis());
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.ABSORPTION, 3, 1, true, true);
    }

    // @EventHandler
    // public void kill(DeathApplyEvent event) {
    //     if(event.getAttacker() != getPlayer()) return;
    //     if(event.getCause() != Cause.MELEE && event.getCause() != Cause.MELEESKILL) return;
    //     System.out.println("Do conditioning: " + (event.getAttacker() != getPlayer()));
    //     StatusApplier.getOrNew(getPlayer()).applyStatus(Status.ABSORPTION, 45, 0, false, true);
    //     getPlayer().sendMessage(getUsedMessage());
    // }
}
