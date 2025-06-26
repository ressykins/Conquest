package me.raindance.champions.kits.skills.warden;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;

import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.sound.SoundPlayer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SkillMetadata(id = 912, skillType = SkillType.Warden, invType = InvType.PRIMARY_PASSIVE)
public class CripplingBlow extends Passive implements ICooldown { // Crippling Blow is a Passive skill
    public CripplingBlow() {
        super();
    }

    @Override
    public String getName() {
        return "Crippling Blow";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public float getCooldown() {
        return 12;
    }

    @EventHandler(priority = EventPriority.LOW) // The priority is monitor because, the Brute wants to watch you suffer rather than end your pain immediately
    public void onHit(DamageApplyEvent event) { // When you smack someone it does stuff
        if (event.isCancelled() || (getPlayer() != event.getAttacker() || event.getCause() != Cause.MELEE)) return; // Something about non-players
        if (!(event.getVictim() instanceof Player)) return;
        if (getItemType(getPlayer().getItemInHand()) != ItemType.AXE) return;
        if (onCooldown()) return;
        Player victim = (Player) event.getVictim(); // The player who took the damage is the victim
        setLastUsed(System.currentTimeMillis());
        getPlayer().sendMessage(getUsedMessage(event.getVictim()));
        event.setDoKnockback(false);
        StatusApplier.getOrNew(victim).applyStatus(Status.CRIPPLE, 3, 0);
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.guardian.hit", 1, 63);
        event.addSource(this);
    }
}
