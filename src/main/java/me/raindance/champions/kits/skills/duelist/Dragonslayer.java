package me.raindance.champions.kits.skills.duelist;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.sound.SoundPlayer;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 306, skillType = SkillType.Duelist, invType = InvType.PRIMARY_PASSIVE)
public class Dragonslayer extends Passive implements ICooldown {

    @Override
    public float getCooldown() {
        return 8;
    }

    @Override
    public String getName() {
        return "Dragonslayer";
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
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.enderdragon.hit", 0.9F, 63);

        Player victim = (Player) e.getVictim();
        KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(victim);
        double trueCurrentHP = (victim.getHealth() / victim.getMaxHealth()) * victimKitPlayer.getHP();
        double bonusDamage = trueCurrentHP * 0.10;
        
        e.setDamage(e.getDamage() + Math.max(bonusDamage, 1));
        e.setModified(true);

    }
}
