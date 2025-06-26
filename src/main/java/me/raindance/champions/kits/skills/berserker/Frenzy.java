package me.raindance.champions.kits.skills.berserker;

import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.kits.EnergyBar;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import net.md_5.bungee.api.ChatColor;

import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Drop;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerDropItemEvent;

@SkillMetadata(id = 112, skillType = SkillType.Berserker, invType = InvType.DROP)
public class Frenzy extends Drop implements ICooldown, IEnergy {
    private boolean active = false;

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
        return "Frenzy";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public boolean drop(PlayerDropItemEvent e) {
        EnergyBar energyBar = getChampionsPlayer().getEnergyBar();

        if (active) return false;
        
        if(energyBar.getEnergy() < 4) {
            getPlayer().sendMessage(String.format(
                "%sBerserker> %sYou must have Maximum Fury to use %s%s%s.",
                ChatColor.BLUE,
                ChatColor.GRAY,
                ChatColor.GREEN,
                getName(),
                ChatColor.GRAY
            ));
            return false;
        }

        if (e.getPlayer() != getPlayer() || onCooldown() || active) return false;

        getPlayer().sendMessage(getUsedMessage());
        active = true;
        energyBar.setEnergy(0);

        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.horse.angry", 1.2F, 63);
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, 5, 1, true, true);
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.STRENGTH, 5, 0, true, true);

        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
            setLastUsed(System.currentTimeMillis());
            active = false;
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.horse.armor", 1.2F, 10);
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.GROUND, 3, 0, true, true);
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.CRIPPLE, 3, 0, true, true);
        }, (20L * 5));

        return true;
    }
}
