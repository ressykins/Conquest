package me.raindance.champions.kits.skills.sorcerer;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldEvent;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.events.ItemCollideEvent;
import com.podcrash.api.item.ItemManipulationManager;
import com.podcrash.api.util.EntityUtil;
import com.podcrash.api.util.PacketUtil;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.sound.SoundWrapper;
import com.podcrash.api.world.BlockUtil;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.*;

@SkillMetadata(id = 1012, skillType = SkillType.Sorcerer, invType = InvType.AXE)
public class DarkSingularity extends Instant implements IEnergy, ICooldown, IConstruct {
    private int currentItemID;

    private WrapperPlayServerWorldParticles particles;
    private String NAME;
    private int energy = 80;
    private int duration = 5; // Duration of the black hole
    private double pullRadius = 5; // Radius of the black hole's pull
    private double pullStrength = 0.5; // Strength of the pull
    private double impactPull = 0.8; // Strength of the pull
    private int explosionDamage = 12;

    public DarkSingularity() {
    }

    @Override
    public void afterConstruction() {
        particles = ParticleGenerator.createParticle(null, EnumWrappers.Particle.PORTAL, 1, 0,0,0);
        NAME = getPlayer().getName()  + getName();
    }

    @Override
    public float getCooldown() {
        return 14;
    }

    @Override
    public String getName() {
        return "Dark Singularity";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if(!rightClickCheck(action)) return;
        if(!onCooldown()) {
            if(!hasEnergy()) {
                getPlayer().sendMessage(getNoEnergyMessage());
                return;
            }
            this.setLastUsed(System.currentTimeMillis());
            useEnergy(energy);
            launch();

            getPlayer().sendMessage(getUsedMessage());
        }
    }

    private void launch() {
        Location location = getPlayer().getEyeLocation();
        Vector vector = location.getDirection();
        vector.normalize().multiply(1.15D);

        Item spawnItem = ItemManipulationManager.regular(Material.BEDROCK, location, vector);
        this.currentItemID = spawnItem.getEntityId();
        Item item = ItemManipulationManager.intercept(spawnItem, 0.5, ((item1, entity, land) -> {
            createBlackHole(land);
            item1.getWorld().playSound(item1.getLocation(), Sound.PORTAL_TRIGGER, 2f, 1f);
            item1.remove();
        }));
        ItemMeta meta = item.getItemStack().getItemMeta();
        item.setCustomName("RITB");
        meta.setDisplayName(NAME + item.getEntityId());
        item.getItemStack().setItemMeta(meta);
        ParticleGenerator.generateEntity(item, particles, new SoundWrapper("mob.guardian.idle", 0.6F, 88));
        // item.getWorld().playSound(item.getLocation(), Sound.PORTAL_TRIGGER, 2f, 1f);
    }

    private void createBlackHole(Location location) {
        // Initial Pull
        location.getWorld().getNearbyEntities(location, pullRadius, pullRadius, pullRadius).stream()
        .filter(e -> e instanceof LivingEntity && e != getPlayer() && !isAlly((LivingEntity) e))
        .filter(e -> e.getLocation().distance(location) > (1.5*1.5)) // Exclude entities too close
        .forEach(entity -> {
            Vector pull = location.toVector().subtract(entity.getLocation().toVector()).normalize();
            entity.setVelocity(entity.getVelocity().add(pull.multiply(impactPull)));
        });


        for (int i = 0; i < (duration * 2); i++) {
            final int iteration = i; // Copy to a final variable
            Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                // Pull nearby enemies
                location.getWorld().getNearbyEntities(location, pullRadius, pullRadius, pullRadius).stream()
                        .filter(e -> e instanceof LivingEntity && e != getPlayer() && !isAlly((LivingEntity) e))
                        .forEach(entity -> {
                            Vector pull = location.toVector().subtract(entity.getLocation().toVector()).normalize();
                            entity.setVelocity(entity.getVelocity().add(pull.multiply(pullStrength)));
                        });

                // Portal particles for visual feedback
                for (int j = 0; j < 80; j++) { // Increased particle count for density
                    // Reduced random offset range to keep particles closer to the center
                    double offsetX = (Math.random() - 0.5) * (pullRadius/2);
                    double offsetY = (Math.random() - 0.5) * (pullRadius/2);
                    double offsetZ = (Math.random() - 0.5) * (pullRadius/2);

                    // Calculate the location of the particle around the black hole
                    Location particleLocation = location.clone().add(offsetX, offsetY, offsetZ);

                    // Create the particle
                    WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(
                        null, EnumWrappers.Particle.PORTAL, 1, 0.1F, 0.1F, 0.1F // Reduced particle spread
                    );
                    particle.setLocation(particleLocation);

                    // Strengthened pull movement for better convergence
                    Vector pull = location.toVector().subtract(particleLocation.toVector()).normalize().multiply(0.8);

                    // Move the particle towards the center
                    Location newLocation = particleLocation.add(pull);

                    // Update the particle position
                    particle.setLocation(newLocation);

                    // Send the particle effect
                    PacketUtil.asyncSend(particle, getPlayers());
                }


                // Explode and execute on the last iteration
                if (iteration == (duration * 2) - 1) {
                    explode(location);
                }
            }, iteration * 10L);
        }
    }

    private void explode(Location location) {
        location.getWorld().getNearbyEntities(location, pullRadius, pullRadius, pullRadius).stream()
                .filter(e -> e instanceof LivingEntity && e != getPlayer() && !isAlly((LivingEntity) e))
                .forEach(entity -> {
                    LivingEntity victim = (LivingEntity) entity;

                    // Execute if health is below 15% of max
                    Player victimKit = (Player) victim;
                    KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(victimKit);
                    double trueCurrentHP = (victimKit.getHealth() / victimKit.getMaxHealth()) * victimKitPlayer.getHP();
            
                    if(trueCurrentHP - explosionDamage > (victimKitPlayer.getHP() * 0.15)) {
                        DamageApplier.damage(victim, getPlayer(), explosionDamage, this, false);
                    }
                    else {
                        DamageApplier.damage(victim, getPlayer(), trueCurrentHP, this, false);
                    }
                });

        // Particles and sound for the explosion
        WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(EnumWrappers.Particle.EXPLOSION_HUGE, 3);
        packet.setLocation(location);
        PacketUtil.syncSend(packet, getPlayers());
        location.getWorld().playSound(location, Sound.WITHER_DEATH, 2f, 1f);
    }


    @EventHandler
    public void collideItem(ItemCollideEvent e) {
        if(e.isCancelled()) return;
        //identity check + owner of item check = cancel collision
        if(e.getCollisionVictim() == getPlayer() && e.getItem().getEntityId() == currentItemID)
            e.setCancelled(true);
    }

    @Override
    public int getEnergyUsage() {
        return energy;
    }
}
