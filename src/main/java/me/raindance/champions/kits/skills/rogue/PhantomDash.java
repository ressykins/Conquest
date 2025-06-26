package me.raindance.champions.kits.skills.rogue;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import org.bukkit.*;
import me.raindance.champions.kits.SkillType;

import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Instant;

import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Phantom Dash
 * Class: Rogue
 * Type: Axe Ability
 * Cooldown: 10 seconds
 * Description: Right click your axe to activate. Launch a Phantom Pearl at target direction.
 * If it hits a player, you teleport to them, dealing 5 damage if they are an enemy.
 * Details:
 * Phantom Pearls are ender pearls.
 * The pearl emits the particles of an ender pearl landing when it lands.
 */

@SkillMetadata(id = 607, skillType = SkillType.Rogue, invType = InvType.AXE)
public class PhantomDash extends Instant implements ICooldown, TimeResource {
    private boolean lock;

    private Projectile pearl; 
    private Location prevLocation;
    private int i = 0;
    private double teleportTimer = 5;  
    private PhantomDashTrail trail;
    private boolean _active;
    private final Random rand = new Random();

    protected void doSkill(PlayerEvent event, Action action) {
        // if the action isn't right click or if there is on cooldowm
        if(!rightClickCheck(action) || onCooldown()) return;

        // get the direction of the player because we need to launch projectile
        Location currentLocOfPlayer = getPlayer().getLocation();
        prevLocation = currentLocOfPlayer;

        Vector direction = currentLocOfPlayer.getDirection();

        // we will also be assuming that the pearl doesn't have a velocity when it spawns.

        Vector mulitplied = direction.multiply(2.4F); // magic number 
        

        // spawn the enderpearl, we may need custom of these classes but for now this is fine.
        this.lock = false;
        this.pearl = getPlayer().launchProjectile(EnderPearl.class, mulitplied);
        this.pearl.setShooter(getPlayer());
        this.pearl.setMetadata(getPlayer().toString(), new FixedMetadataValue(Main.instance, true));

        SoundPlayer.sendSound(getPlayer().getLocation(), "random.bow", 0.85F, 30);
        WrapperPlayServerWorldParticles particles = ParticleGenerator.createParticle(EnumWrappers.Particle.PORTAL, 2);
        ParticleGenerator.generateProjectile(pearl, particles);

        // AT THIS POINT: we have spawned the ender pearl.
        getPlayer().sendMessage(getUsedMessage());

        setLastUsed(System.currentTimeMillis());

        // if pearl still doesn't land after 5 seconds, cancel skill
        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
            if (!_active && pearl != null && !pearl.isDead()) {
                // Despawn the pearl and reset ability state
                pearl.remove();
                _active = false;
                i = 0;
                getPlayer().sendMessage(String.format("%s%s> %sYour Phantom Pearl has expired.",
                ChatColor.BLUE, 
                getChampionsPlayer().getName(),
                ChatColor.GRAY));
            }
        }, 60L);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void enderPearlHit(EntityDamageByEntityEvent event) {
        //checks
        Entity damager = event.getDamager();

        //if the damager is not a pearl or the pearl we need, return.
        //if(!(damager instanceof EnderPearl) || damager != this.pearl) return;
        //actually, just do this: check if the pearl is not the damager

        if(damager != this.pearl) return;

        Entity victim = event.getEntity(); // victim

        // we want to check if the damager hit is an actual living damager and not something random (like item frames)
        if(!(victim instanceof LivingEntity)) return;

        // at this point, the pearl has hit some living damager,
        // so we need to do 5 damage and have the user teleport.

        if (!damager.hasMetadata(getPlayer().toString())) return;

        doEffect((LivingEntity) victim);
    }

    public void doEffect(LivingEntity victim) {
        //to avoid doing the effect twice.
        if(lock) return;
        lock = true;

        //damage
        if(!isAlly(victim)) {
            DamageApplier.damage(victim, getPlayer(), 5, this, false);
            StatusApplier.getOrNew((LivingEntity) victim).applyStatus(Status.BLIND, 3, 0);
            StatusApplier.getOrNew((LivingEntity) victim).applyStatus(Status.SLOW, 3, 0);
        }

        //Set the cooldown
        _active = true;
        trail = new PhantomDashTrail();
        TimeHandler.repeatedTime(20, 0, trail);
        TimeHandler.repeatedTime(20, 0, this);

        WrapperPlayServerWorldParticles particles = ParticleGenerator.createParticle(EnumWrappers.Particle.PORTAL, 2);
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.endermen.portal", 0.85F, 73);
        particles.setLocation(victim.getLocation());
        PacketUtil.asyncSend(particles, getPlayers());
        //teleport
        
        // getPlayer().teleport(evade(victim, victim.getLocation()));
        // this.pearl = null;
    }
    
    /**
     * The forums r g0d: https://bukkit.org/threads/how-to-disable-enderpearl-teleport.128993/
     */
    // @EventHandler
    // public void disablePearlTeleport(PlayerTeleportEvent e) {
    //     //we want to make sure that everyone doesn't get blocked
    //     if(e.getPlayer() != getPlayer()) return;

    //     //we also want to make sure that pearls don't get blocked if you are not using this skill
    //     if(e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;

    //     //cancel
    //     e.setCancelled(true);
    // }

    /**
     * this is the other evade code used in the Evade skill
     * this is slightly modified for the player to always touch the ground
     * @param entity
     * @param victimLocation
     * @return
     */
    // private Location evade(LivingEntity entity, Location victimLocation) {
    //     // Based on the damager's location
    //     // End Location
    //     double constantY = victimLocation.getY() + 0.01d;
    //     Location tempLocation = victimLocation.clone();
    //     Vector vect = victimLocation.getDirection().normalize().multiply(-2.25);
    //     Location behind = victimLocation.add(vect);
    //     double d = 0.05;
    //     double inc = 0.05;
    //     double x2 = behind.getX(), y2 = behind.getY(), z2 = behind.getZ();
    //     // x1,y2,z1
    //     double x1 = tempLocation.getX(), y1 = tempLocation.getY(), z1 = tempLocation.getZ();
    //     // Possible points
    //     while (d < 1) {
    //         // x2,y2,z2

    //         double x = x1 + d * (x2 - x1);
    //         double z = z1 + d * (z2 - z1);
    //         Location testLocation = new Location(victimLocation.getWorld(), x, constantY, z);

    //         d += inc;

    //         if (!BlockUtil.isPassable(testLocation.getBlock())) break;
    //         tempLocation = testLocation;
    //         if (testLocation.distanceSquared(behind) < 0.20)
    //             break;

    //     }

    //     return tempLocation.setDirection(VectorUtil.fromAtoB(tempLocation, entity.getLocation()).normalize());
    // }

    //Generator methods for this

    @Override
    public float getCooldown() {
        return 15;
    }


    @Override
    public String getName() {
        return "Phantom Dash";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    
    @Override
    public void task() {
        i += 20;
    }

    @Override
    public boolean cancel() {
        return (i >= (teleportTimer * 20)) || getGame().isRespawning(getPlayer());
    }

    @Override
    public void cleanup() {
        if(i >= (teleportTimer * 20)) {
            WrapperPlayServerWorldParticles particles = ParticleGenerator.createParticle(EnumWrappers.Particle.PORTAL, 2);
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.endermen.portal", 0.85F, 73);
            particles.setLocation(getPlayer().getLocation());
            PacketUtil.asyncSend(particles, getPlayers());

            getPlayer().teleport(prevLocation);
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.endermen.portal", 0.85F, 73);
            particles.setLocation(prevLocation);
            PacketUtil.asyncSend(particles, getPlayers());
            _active = false;
            i = 0;
        }
    }


    // no particles if u switch kit
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEnchant(PlayerInteractEvent e) {
        if (e.getPlayer() == getPlayer()) {
            _active = false;
            i = 0;
        }
    }

    @EventHandler
    public void onDeath(DeathApplyEvent event) {
        if (event.getPlayer() == getPlayer()) {
            _active = false;
            i = 0;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamageByPearl(EntityDamageByEntityEvent event) {
        // Check if the victim is the player
        if (event.getEntity() != getPlayer()) return;
    
        // Check if the damager is the ender pearl
        if (!(event.getDamager() instanceof EnderPearl)) return;
    
        EnderPearl pearl = (EnderPearl) event.getDamager();
    
        // Ensure the pearl belongs to the player
        if (!pearl.hasMetadata(getPlayer().toString())) return;
    
        // Cancel the damage
        event.setCancelled(true);
    }

    private class PhantomDashTrail implements TimeResource {
        private final WrapperPlayServerWorldParticles smokeTrail = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
                                                                    EnumWrappers.Particle.SPELL_MOB, new int[]{102, 0, 102}, 9,
                                                                    rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
                                                                
        private final WrapperPlayServerWorldParticles smokeTrail2 = ParticleGenerator.createParticle(EnumWrappers.Particle.PORTAL, 9);

        @Override
        public void task() {
            smokeTrail.setLocation(prevLocation);
            PacketUtil.asyncSend(smokeTrail, getPlayers());
            smokeTrail2.setLocation(prevLocation);
            PacketUtil.asyncSend(smokeTrail2, getPlayers());

            WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
            EnumWrappers.Particle.SPELL_MOB, new int[]{102, 0, 102}, 9,
            rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
            getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));

            WrapperPlayServerWorldParticles particle2 = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
            EnumWrappers.Particle.PORTAL, 2,
            rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
            getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle2));

        }

        @Override
        public boolean cancel() {
            return !_active;
        }

        @Override
        public void cleanup() {

        }
    }
}
