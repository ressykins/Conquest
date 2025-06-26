package me.raindance.champions.kits.skills.marksman;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;

import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.sound.SoundPlayer;
import org.bukkit.event.EventHandler;


@SkillMetadata(id = 511, skillType = SkillType.Marksman, invType = InvType.SECONDARY_PASSIVE)
public class ViperFang extends Passive implements ICooldown {
    private final int duration = 5;

    @Override
    public float getCooldown() {
        return 5;
    }

    @Override
    public String getName() {
        return "Viper Fang";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void hit(DamageApplyEvent e) {
        if(onCooldown() || e.getAttacker() != getPlayer()) return;
        if(isAlly(e.getVictim())) return;
        if(e.getCause() != Cause.MELEE) return;
        setLastUsed(System.currentTimeMillis());
        getPlayer().sendMessage(getUsedMessage(e.getVictim()));
        e.addSource(this);

        if (!StatusApplier.getOrNew(e.getVictim()).has(Status.POISON)) {
            StatusApplier.getOrNew(e.getVictim()).applyStatus(Status.POISON, duration, 0, false, false);
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.spider.say", 0.9F, 63);
        }
        else {
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.spider.say", 0.9F, 10);
            e.setDamage(e.getDamage() + 4);
            e.setModified(true);
        }
    }
}
