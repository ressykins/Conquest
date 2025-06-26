package me.raindance.champions.kits.skills.berserker;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.kits.EnergyBar;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import net.md_5.bungee.api.ChatColor;

import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Drop;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;

@SkillMetadata(id = 111, skillType = SkillType.Berserker, invType = InvType.DROP)
public class Intimidate extends Drop implements ICooldown, IEnergy {

    @Override
    public int getEnergyUsage() {
        return 0;
    }

    @Override
    public float getCooldown() {
        return 12;
    }

    @Override
    public String getName() {
        return "Intimidate";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public boolean drop(PlayerDropItemEvent e) {
        if (e.getPlayer() != getPlayer() || onCooldown()) return false;

        EnergyBar energyBar = getChampionsPlayer().getEnergyBar();
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

        setLastUsed(System.currentTimeMillis());

        energyBar.setEnergy(0);

        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.ghast.scream", 1.2F, 50);

        Location location = getPlayer().getLocation();
        WrapperPlayServerWorldParticles particles = ParticleGenerator.createParticle(EnumWrappers.Particle.NOTE, 2);
        List<Player> players = getPlayers();
        PacketUtil.asyncSend(particles, players);
        for (Player victim : players) {
            if(victim == getPlayer() || isAlly(victim)) continue;
            if(victim.getLocation().distanceSquared(location) > 8 * 8) continue;
            StatusApplier.getOrNew(victim).applyStatus(Status.WEAKNESS, 3, 1);
            StatusApplier.getOrNew(victim).applyStatus(Status.SILENCE, 3, 0);

        }

        getPlayer().sendMessage(getUsedMessage());
        return true;
    }
}
