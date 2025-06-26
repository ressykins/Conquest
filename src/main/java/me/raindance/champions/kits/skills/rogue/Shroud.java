package me.raindance.champions.kits.skills.rogue;

import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.events.StatusApplyEvent;
import com.podcrash.api.events.game.GameStartEvent;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.enums.InvType;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SkillMetadata(id = 610, skillType = SkillType.Rogue, invType = InvType.SECONDARY_PASSIVE)
public class Shroud extends Passive implements ICooldown, TimeResource, IConstruct {
    
    private boolean dead = false;
    // private long started = -1;

    @Override
    public void task() {
    }

    @Override
    public boolean cancel() {
        return !onCooldown();
    }

    @Override
    public void cleanup() {
        if(!onCooldown() && !dead) {
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.INVISIBILITY, Integer.MAX_VALUE, 0, true);
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.blaze.breathe", 0.75f, 200);
        }
    }

    @Override
    public float getCooldown() {
        return 12;
    }

    @Override
    public String getName() {
        return "Shroud";
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onStatusApply(StatusApplyEvent event) {
        if (onCooldown()) return;
        if (event.getEntity() != getPlayer()) return;
        if (event.getStatus().isNegative()) {
            event.setCancelled(true);
            StatusApplier.getOrNew(getPlayer()).removeStatus(Status.INVISIBILITY);
            getPlayer().getLocation().getWorld().playSound(getPlayer().getLocation(), Sound.BLAZE_BREATH, 0.3f, 0.5f);
            runAsync(1,0);
            setLastUsed(System.currentTimeMillis());
        }
    }

    @Override
    public ItemType getItemType() {
        return null;
    }

    @EventHandler
    public void onDeath(DeathApplyEvent event) {
        if (event.getPlayer() == getPlayer()) dead = true;
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onStart(GameStartEvent e) {
        TimeHandler.delayTime(30, () -> {
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.INVISIBILITY, Integer.MAX_VALUE, 0, true);
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.blaze.breathe", 0.75f, 200);
        });
    }

    @Override
    public void afterConstruction() {
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.INVISIBILITY, Integer.MAX_VALUE, 0, true);
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.blaze.breathe", 0.75f, 200);
    }

    // @EventHandler(priority = EventPriority.HIGH)
    // public void onRespawn(GameResurrectEvent event) {
    //     if (event.getWho() != getPlayer()) return;
    //     StatusApplier.getOrNew(getPlayer()).applyStatus(Status.INVISIBILITY, Integer.MAX_VALUE, 0, true);
    //     SoundPlayer.sendSound(getPlayer().getLocation(), "mob.blaze.breathe", 0.75f, 200);
    //     dead = false;
    // }

    @Override
    public void afterRespawn() {
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.INVISIBILITY, Integer.MAX_VALUE, 0, true);
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.blaze.breathe", 0.75f, 200);
        TimeHandler.delayTime(3, () -> {
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.INVISIBILITY, Integer.MAX_VALUE, 0, true);
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.blaze.breathe", 0.75f, 200);
        });
        dead = false;
    }

}
