package me.raindance.champions.kits.skills.berserker;

import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;

import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 114, skillType = SkillType.Berserker, invType = InvType.PRIMARY_PASSIVE)
public class Smite extends Passive implements ICooldown { 
    public Smite() {
        super();
    }

    @Override
    public String getName() {
        return "Smite";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public float getCooldown() {
        return 15;
    }

    @EventHandler
    public void onHit(DamageApplyEvent event) { 
        if (event.isCancelled()) return;
        if (event.getAttacker() != getPlayer()) return;
        if (onCooldown()) return;
        if (getChampionsPlayer().getEnergyBar().getEnergy() < 4) return; 

        setLastUsed(System.currentTimeMillis());
        getPlayer().sendMessage(getUsedMessage(event.getVictim()));

    
        Player victim = (Player) event.getVictim();
        KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(victim);
        double targetMaxHP = victimKitPlayer.getHP() * 0.10;

        event.setModified(true); // modify the damage
        event.addSource(this);
        event.setDamage(event.getDamage() + targetMaxHP);
        event.getVictim().getLocation().getWorld().strikeLightningEffect(event.getVictim().getLocation());
        StatusApplier.getOrNew(event.getVictim()).applyStatus(Status.SLOW, 3, 0);
        StatusApplier.getOrNew(event.getVictim()).applyStatus(Status.MARKED, 3, 0);
    }
}
