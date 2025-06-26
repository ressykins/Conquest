package me.raindance.champions.kits.skills.duelist;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.sound.SoundPlayer;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Drop;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;

@SkillMetadata(id = 312, skillType = SkillType.Duelist, invType = InvType.DROP)
public class Clash extends Drop implements ICooldown {
    
    private boolean challengeActive = false;
    private Player challengedPlayer = null;
    private Random rand;

    @Override
    public String getName() {
        return "Clash";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }

    @Override
    public float getCooldown() {
        return 16;
    }

    @Override
    public boolean drop(PlayerDropItemEvent e) {
        if (onCooldown()) return false;

        Location playerLocation = getPlayer().getLocation();
        List<Entity> nearbyEntities = getPlayer().getNearbyEntities(8, 8, 8);
        
        Player nearestPlayer = null;
        double closestDistance = Double.MAX_VALUE;

        // Loop through nearby entities and find the nearest player
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player && entity != getPlayer()) {  // Ignore the player itself
                Player nearbyPlayer = (Player) entity;
                double distance = nearbyPlayer.getLocation().distance(playerLocation);

                // If this player is closer, update the nearest player
                if (distance < closestDistance && !isAlly(nearbyPlayer) && !nearbyPlayer.isDead()) {
                    closestDistance = distance;
                    nearestPlayer = nearbyPlayer;
                }
            }
        }

        if (nearestPlayer == null) {
            getPlayer().sendMessage(String.format("%s%s> %sThere are no nearby enemies to duel!",
                    ChatColor.BLUE, getChampionsPlayer().getName(), ChatColor.GRAY));
            return false;
        }
        else {
            getPlayer().sendMessage(String.format("%s%s> %sYou challenged %s%s%s to a duel!",
                ChatColor.BLUE, 
                getChampionsPlayer().getName(),
                ChatColor.GRAY, 
                ChatColor.YELLOW, 
                nearestPlayer.getName(), 
                ChatColor.GRAY));
            nearestPlayer.sendMessage(String.format("%sFace-Off> %s%s%s challenged you to a duel!",
                ChatColor.BLUE,
                ChatColor.YELLOW,
                getPlayer().getName(),
                ChatColor.GRAY));

            challengedPlayer = nearestPlayer;
            challengeActive = true;
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.enderdragon.growl", 0.9F, 63);
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, 8, 0);
            setLastUsed(System.currentTimeMillis());


            Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                if(challengeActive) {
                    getPlayer().sendMessage(String.format("%s%s> %sYour duel against %s%s%s has expired.",
                    ChatColor.BLUE, 
                    getChampionsPlayer().getName(),
                    ChatColor.GRAY, 
                    ChatColor.YELLOW, 
                    challengedPlayer.getName(), 
                    ChatColor.GRAY));
                    challengedPlayer.sendMessage(String.format("%sFace-Off> %sThe duel has expired.",
                    ChatColor.BLUE,
                    ChatColor.GRAY));
                    challengedPlayer = null;
                    challengeActive = false;
                }
            }, 160L);
        }
        return true;
    }


    @EventHandler
    public void kill(DeathApplyEvent event) {
        if(event.getAttacker() != getPlayer()) return;
        if(event.getPlayer() == challengedPlayer && challengeActive) {
            WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(), EnumWrappers.Particle.VILLAGER_HAPPY,
                    3, rand.nextFloat(), 0.9f, rand.nextFloat());
            getPlayer().getWorld().getPlayers().forEach(p -> ParticleGenerator.generate(p, packet));
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.enderdragon.growl", 0.9F, 10);
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.REGENERATION, 6, 1);
            getPlayer().sendMessage(String.format("%s%s> %sYou won your duel against %s%s%s.",
            ChatColor.BLUE, 
            getChampionsPlayer().getName(),
            ChatColor.GRAY, 
            ChatColor.YELLOW, 
            challengedPlayer.getName(), 
            ChatColor.GRAY));
            challengedPlayer = null;
            challengeActive = false;
        }
    }

    @EventHandler
    public void hit(DamageApplyEvent event) {
        if(event.isCancelled()) return;
        if (event.getVictim() == getPlayer() && challengeActive && event.getAttacker() != challengedPlayer) {
            event.setDamage(event.getDamage() - 2);
            event.setModified(true);
        }
    }

    @EventHandler
    public void damage(DamageApplyEvent e) {
        if(getPlayer() != e.getAttacker() || !(e.getVictim() instanceof Player)) return;
        if (challengeActive && e.getVictim() != challengedPlayer) {
            e.setDamage(e.getDamage() - 2);
            e.setModified(true);
        }
    }


    @EventHandler
    public void onDeath(DeathApplyEvent event) {
        if (event.getPlayer() == getPlayer() && challengeActive) {
            getPlayer().sendMessage(String.format("%s%s> %sYou lost your duel against %s%s%s...",
                ChatColor.BLUE, 
                getChampionsPlayer().getName(),
                ChatColor.GRAY, 
                ChatColor.YELLOW, 
                challengedPlayer.getName(), 
                ChatColor.GRAY));
            challengedPlayer.sendMessage(String.format("%sFace-Off> You won your duel against %s%s%s!",
                ChatColor.BLUE,
                ChatColor.YELLOW,
                getPlayer().getName(),
                ChatColor.GRAY));
            challengeActive = false;
            challengedPlayer = null;
        }
    }
}


