package me.raindance.champions.kits.skills.rogue;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.events.skill.SkillUseEvent;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;

import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.plugin.PodcrashSpigot;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.SimpleTimeResource;
import com.podcrash.api.util.EntityUtil;
import com.podcrash.api.util.PacketUtil;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

@SkillMetadata(id = 611, skillType = SkillType.Rogue, invType = InvType.SECONDARY_PASSIVE)
public class Vanish extends Passive implements ICooldown {
    private boolean isInvis;
    private final int duration = 2;


    @Override
    public float getCooldown() {
        return 10;
    }

    @EventHandler
    public void kill(DeathApplyEvent event) {
        if(event.getAttacker() != getPlayer()) return;
        if(onCooldown()) return;
        PodcrashSpigot.getInstance().getLogger().info("vanish on Kill");

        StatusApplier applier = StatusApplier.getOrNew(getPlayer());
        applier.applyStatus(Status.CLOAK, duration, 0);
        applier.applyStatus(Status.SPEED, 5, 1, false, true);
        isInvis = true;
        TimeHandler.delayTime(duration * 20L, new SimpleTimeResource() {
            @Override
            public void task() {
                isInvis = false;
            }
        });

        getPlayer().getWorld().playEffect(getPlayer().getLocation(), Effect.SMOKE, 1);
        WrapperPlayServerWorldParticles explosion = ParticleGenerator.createParticle(null, EnumWrappers.Particle.EXPLOSION_HUGE, 1, 0,0,0);
        explosion.setLocation(getPlayer().getLocation());
        PacketUtil.syncSend(explosion, getPlayers());

        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.FIZZ, 2f, 0.5f);
        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(EnumWrappers.Particle.SMOKE_LARGE, 2);
        particle.setLocation(getPlayer().getLocation());
        setLastUsed(System.currentTimeMillis());
    }

    @Override
    public String getName() {
        return "Vanish";
    }

    @EventHandler
    public void onSkillUse(SkillUseEvent event) {
        if(event.getPlayer().equals(getPlayer()))
            cancelInvis(getPlayer());
    }

    public void cancelInvis(Player player) {
        StatusApplier applier = StatusApplier.getOrNew(getPlayer());
        if (isInvis && applier.isCloaked()) {
            applier.removeCloak();
        }
        isInvis = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamaged(EntityDamageEvent event) {
        if (event.getEntity() == getPlayer()) {
            cancelInvis(getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hit(DamageApplyEvent event) {
        if(event.isCancelled()) return;
        if (onCooldown()) return;
        if (event.getAttacker() == getPlayer()) cancelInvis(getPlayer());

        if (event.getVictim() == getPlayer() && EntityUtil.isBelow(getPlayer(), 0.3) && !onCooldown()) {
            PodcrashSpigot.getInstance().getLogger().info("vanish: trigger when low invis");

            StatusApplier applier = StatusApplier.getOrNew(getPlayer());
            applier.applyStatus(Status.CLOAK, duration, 0);
            applier.applyStatus(Status.SPEED, 5, 1, false, true);
            isInvis = true;
            TimeHandler.delayTime(duration * 20L, new SimpleTimeResource() {
                @Override
                public void task() {
                    isInvis = false;
                }
            });

            getPlayer().getWorld().playEffect(getPlayer().getLocation(), Effect.SMOKE, 1);
            WrapperPlayServerWorldParticles explosion = ParticleGenerator.createParticle(null, EnumWrappers.Particle.EXPLOSION_HUGE, 1, 0,0,0);
            explosion.setLocation(getPlayer().getLocation());
            PacketUtil.syncSend(explosion, getPlayers());
    
            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.FIZZ, 2f, 0.5f);
            WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(EnumWrappers.Particle.SMOKE_LARGE, 2);
            particle.setLocation(getPlayer().getLocation());
            setLastUsed(System.currentTimeMillis());
        }
        else if (event.getVictim() == getPlayer()) {
            cancelInvis(getPlayer());
        }
    }
}
