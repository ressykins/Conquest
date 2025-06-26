package me.raindance.champions.kits.skills.sorcerer;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.EnergyBar;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;

import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 1016, skillType = SkillType.Sorcerer, invType = InvType.PRIMARY_PASSIVE)
public class NullAndVoid extends Passive implements IEnergy {
    private int consumeEnergy = 20;
    private int gainEnergy = 10;

    public NullAndVoid() {
        super();
    }

    @Override
    public int getEnergyUsage() { 
        return consumeEnergy; 
    }

    @Override
    public String getName() {
        return "Null and Void";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void onHit(DamageApplyEvent event) {
        if (event.isCancelled()) return;
        if(event.getCause() != Cause.MELEE) return;
        if(event.getAttacker() != getPlayer()) return;

        EnergyBar energyBar = getEnergyBar();
        event.addSource(this);

        if(getItemType(getPlayer().getItemInHand()) == ItemType.AXE) {
            energyBar.incrementEnergy(gainEnergy);
            event.getVictim().getLocation().getWorld().playSound(event.getVictim().getLocation(), Sound.WITHER_HURT, 0.3f, 0.5f);
        }

        if(getItemType(getPlayer().getItemInHand()) == ItemType.SWORD) {
            if (energyBar.getEnergy() < consumeEnergy) {
                useEnergy(energyBar.getEnergy());
            }
            else {
                useEnergy(getEnergyUsage());
            }
            double bonus = (energyBar.getMaxEnergy() - energyBar.getEnergy()) * 0.02;
            event.setDamage(event.getDamage() + bonus);
            event.setModified(true);
            event.getVictim().getLocation().getWorld().playSound(event.getVictim().getLocation(), Sound.BLAZE_BREATH, 0.3f, 0.5f);
        }
    }
}
