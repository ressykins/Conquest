package me.raindance.champions.kits.skills.berserker;

import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.kits.EnergyBar;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import net.md_5.bungee.api.ChatColor;

import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Drop;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerDropItemEvent;

@SkillMetadata(id = 102, skillType = SkillType.Berserker, invType = InvType.DROP)
public class Satiate extends Drop implements ICooldown, IEnergy {
    @Override
    public int getEnergyUsage() {
        return 0;
    }

    @Override
    public float getCooldown() {
        return 10;
    }

    @Override
    public String getName() {
        return "Satiate";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public boolean drop(PlayerDropItemEvent e) {
        EnergyBar energyBar = getChampionsPlayer().getEnergyBar();
        if(energyBar.getEnergy() < 1) {
            getPlayer().sendMessage(String.format(
                "%sBerserker> %sYou must have Fury to use %s%s%s.",
                ChatColor.BLUE,
                ChatColor.GRAY,
                ChatColor.GREEN,
                getName(),
                ChatColor.GRAY
            ));
            return false;
        }

        if (e.getPlayer() != getPlayer() || onCooldown()) return false;
        setLastUsed(System.currentTimeMillis());

        getChampionsPlayer().heal(2 * energyBar.getEnergy());
        energyBar.setEnergy(0);

        Location loc = getPlayer().getLocation();
        SoundPlayer.sendSound(loc, "mob.enderdragon.growl", 0.9F, 80);
        ParticleGenerator.createBlockEffect(loc, Material.REDSTONE_BLOCK.getId());
        return true;
    }
}
