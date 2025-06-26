package me.raindance.champions.kits.skills.thief;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.skill.SkillUseEvent;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Drop;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.SimpleTimeResource;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;

import java.util.List;

// import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

@SkillMetadata(id = 710, skillType = SkillType.Thief, invType = InvType.DROP)
public class SmokeBomb extends Drop implements ICooldown, IConstruct {
    private final int duration = 5;
    private SmokeBombTrail trail;
    private boolean isInvis;

    @Override
    public void afterConstruction() {
        trail = new SmokeBombTrail();
    }

    @Override
    public float getCooldown() {
        return 16;
    }

    @Override
    public String getName() {
        return "Smoke Bomb";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    public boolean drop(PlayerDropItemEvent e) {
        if (onCooldown()) {
            this.getPlayer().sendMessage(getCooldownMessage());
            return false;
        }
        StatusApplier applier = StatusApplier.getOrNew(getPlayer());
        applier.applyStatus(Status.CLOAK, duration, 1);
        isInvis = true;
        TimeHandler.delayTime(duration * 20L, new SimpleTimeResource() {
            @Override
            public void task() {
                isInvis = false;
            }
        });
        TimeHandler.repeatedTime(20, 0, trail);

        Location location = getPlayer().getLocation();
        WrapperPlayServerWorldParticles explosion = ParticleGenerator.createParticle(null, EnumWrappers.Particle.EXPLOSION_HUGE, 1, 0,0,0);
        explosion.setLocation(getPlayer().getLocation());
        PacketUtil.syncSend(explosion, getPlayers());
        List<Player> players = getPlayers();
        for (Player player : players) {
            if (player != getPlayer() && player.getLocation().distanceSquared(location) <= 9 && !isAlly(player)) {
                StatusApplier.getOrNew(player).applyStatus(Status.SLOW, 3, 0);
                StatusApplier.getOrNew(player).applyStatus(Status.BLIND, 3, 0);
            }
        }

        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.FIZZ, 2f, 0.5f);
        this.setLastUsed(System.currentTimeMillis());
        return true;
    }

    /*
    This must be changed to GameDamageEvent later
     */
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
    public void hit(DamageApplyEvent event) {
        if(event.isCancelled()) return;
        if(event.getVictim() == getPlayer() || event.getAttacker() == getPlayer())
            cancelInvis(getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamaged(EntityDamageEvent event) {
        if (event.getEntity() == getPlayer()) {
            cancelInvis(getPlayer());
        }
    }

    private class SmokeBombTrail implements TimeResource {
        private final WrapperPlayServerWorldParticles smokeTrail = ParticleGenerator.createParticle(EnumWrappers.Particle.SMOKE_LARGE, 2);
        private final StatusApplier applier = StatusApplier.getOrNew(getPlayer());
        @Override
        public void task() {
            smokeTrail.setLocation(getPlayer().getLocation());
            PacketUtil.syncSend(smokeTrail, getPlayers());
        }

        @Override
        public boolean cancel() {
            return !applier.isCloaked();
        }

        @Override
        public void cleanup() {

        }
    }
}
