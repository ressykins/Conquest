package me.raindance.champions.kits.skills.vanguard;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;

import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.EntityUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 813, skillType = SkillType.Vanguard, invType = InvType.SECONDARY_PASSIVE)
public class Perseverance extends Passive implements IPassiveTimer, TimeResource {
    @Override
    public String getName() {
        return "Perseverance";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }
    
    @Override
    public void start() {
        runAsync(5,0);
    }

    @Override
    public void task() {
        if(EntityUtil.isBelow(getPlayer(), 0.5)) {
            if(EntityUtil.isBelow(getPlayer(), 0.25)) {
                StatusApplier.getOrNew(getPlayer()).applyStatus(Status.RESISTANCE, 2, 0, false, false);
            }
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.REGENERATION, 2, 0, false, false);
        }

    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void cleanup() {
    }

    // @EventHandler
    // public void damage(DamageApplyEvent e) {
    //     if(getPlayer() != e.getVictim()) return;
    //     if(!EntityUtil.isBelow(getPlayer(), 0.4)) return;

    //     if(onCooldown()) return;
    //     setLastUsed(System.currentTimeMillis());
    //     StatusApplier.getOrNew(getPlayer()).applyStatus(Status.REGENERATION, 3, 2, true, true);
    // }
}
