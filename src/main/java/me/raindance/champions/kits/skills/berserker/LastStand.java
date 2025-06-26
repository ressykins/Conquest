package me.raindance.champions.kits.skills.berserker;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.DeathApplyEvent;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;

import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.plugin.PodcrashSpigot;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 113, skillType = SkillType.Berserker, invType = InvType.PRIMARY_PASSIVE)
public class LastStand extends Passive implements TimeResource, ICooldown {
    private boolean active = false;
    private int i = 0;
    private final Random rand = new Random();
    private Player lastStandKiller;

    @Override
    public void task() {
        i += 20;

        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
                EnumWrappers.Particle.REDSTONE, 9,
                rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
        getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));

        WrapperPlayServerWorldParticles particle2 = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
                EnumWrappers.Particle.SMOKE_LARGE, 9,
                rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
        getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle2));


    }

    @Override
    public boolean cancel() {
        return (i >= (5 * 20)) || getGame().isRespawning(getPlayer()) || !active;
    }

    @Override
    public void cleanup() {
        setLastUsed(System.currentTimeMillis());
        if(i >= (5 * 20) && active) {
            active = false;
            // PodcrashSpigot.getInstance().getLogger().info("cleanup, kill player");
            KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(getPlayer());
            double trueCurrentHP = (getPlayer().getHealth() / getPlayer().getMaxHealth()) * victimKitPlayer.getHP();
            DamageApplier.damage(getPlayer(), lastStandKiller, trueCurrentHP, this, false);
        }
        i = 0;
    }
    
    @EventHandler
    public void onDeath(DeathApplyEvent event) {
        if (event.getPlayer() == getPlayer() || active) {
            active = false; 
            i = 0;
        } 
    }

    @Override
    public String getName() {
        return "Last Stand";
    }

    @Override
    public float getCooldown() {
        return 24;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void damage(DamageApplyEvent e) {
        if (e.isCancelled()) return; 
        if (onCooldown()) return;
        if (e.getVictim() == getPlayer() && !active) {
            if (getPlayer().getHealth() - e.getDamage() > 0) return;
            e.setDamage(0);
            e.setModified(true);
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.wither.spawn", 0.9F, 10);
            getPlayer().sendMessage(String.format("%sBerserker> %sYou have entered Last Stand. You have 5 seconds to get a kill!",
            ChatColor.BLUE,
            ChatColor.GRAY));
            getChampionsPlayer().getEnergyBar().incrementEnergy(4);
            getPlayer().setHealth(5);
            lastStandKiller = (Player) e.getAttacker();

            active = true;
            TimeHandler.repeatedTime(20, 0, this);
        }
    }


    @EventHandler
    public void kill(DeathApplyEvent event) {
        if(event.getAttacker() != getPlayer()) return;
        PodcrashSpigot.getInstance().getLogger().info("killed player; was last stand active? " + active);
        if(active) {
            active = false;
            setLastUsed(System.currentTimeMillis());
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.blaze.death", 0.9F, 10);
            getPlayer().sendMessage(String.format("%sBerserker> %sYou have survived, for now...",
            ChatColor.BLUE,
            ChatColor.GRAY));
        } 

    }
}
