package me.raindance.champions.kits.skills.druid;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldEvent;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.resources.TimeResource;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Instant;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.util.Vector;

@SkillMetadata(id = 207, skillType = SkillType.Druid, invType = InvType.AXE)
public class DreamDust extends Instant implements ICooldown, IEnergy, TimeResource {
    private Projectile projectile;
    private LivingEntity affectedPlayer;
    private boolean isAsleep;
    private int i = 0;

    @Override
    public float getCooldown() {
        return 10;
    }

    @Override
    public int getEnergyUsage() {
        return 80;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if(!rightClickCheck(action) || onCooldown()) return;
        if(!hasEnergy()) {
            getPlayer().sendMessage(getNoEnergyMessage());
            return;
        }
        useEnergy();
        //Set the cooldown
        setLastUsed(System.currentTimeMillis());

        //Get the direction of the player because we need to launch projectile
        Location currentLocOfPlayer = getPlayer().getLocation();

        Vector direction = currentLocOfPlayer.getDirection();

        //we will also be assuming that the pearl doesn't have a velocity when it spawns.

        Vector mulitplied = direction.multiply(2.4F); //magic number

        //spawn the enderpearl, we may need custom of these classes but for now this is fine.
        this.projectile = getPlayer().launchProjectile(Egg.class, mulitplied);
        projectile.setShooter(getPlayer());
        WrapperPlayServerWorldEvent packet = ParticleGenerator.createBlockEffect(projectile.getLocation(), Material.GRASS.getId());
        ParticleGenerator.generateProjectile(projectile, packet);
        getPlayer().sendMessage(getUsedMessage());
    }

    @Override
    public String getName() {
        return "Dream Dust";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @EventHandler
    public void eggHit(EntityDamageByEntityEvent event) {
        //checks
        Entity damager = event.getDamager();

        if(damager != this.projectile) return;
        Entity victim = event.getEntity();// victim

        //we want to check if the damager hit is an actual living damager and not something random (like item frames)
        if(!(victim instanceof LivingEntity)) return;

        //don't allow friendly fire with this skill
        if(isAlly((LivingEntity) victim)) return;

        affectedPlayer = (LivingEntity) victim;
        isAsleep = true;

        if(victim instanceof Player)
        SoundPlayer.sendSound(getPlayer(), "random.successful_hit", 0.8F, 20);
        event.setCancelled(true);
        this.projectile = null;

        run(1, 0);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hit(DamageApplyEvent event) {
        if(event.isCancelled()) return;
        if(event.getVictim() == affectedPlayer && isAsleep)  {
            isAsleep = false;
            event.setDamage(event.getDamage() * 1.5);
            event.setModified(true);
            affectedPlayer.getLocation().getWorld().playSound(affectedPlayer.getLocation(), Sound.NOTE_SNARE_DRUM, 1.0f, 1.0f);
        }
    }


    @Override
    public void task() {

        if (i % 20 == 0) {
            Random rand = new Random();

            // Get the player's current location and move the particle effect upwards
            Location particleLocation = affectedPlayer.getLocation().add(0, 2.2, 0);
            
            // Create the particle effect
            WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(
                    particleLocation.toVector(),
                    EnumWrappers.Particle.VILLAGER_ANGRY,
                    5,
                    rand.nextFloat() / 2f,
                    0.25f + (rand.nextFloat() - 0.15f),
                    rand.nextFloat() / 2f
            );
            
            affectedPlayer.getLocation().getWorld().playSound(affectedPlayer.getLocation(), Sound.CAT_PURR, 1.0f, 1.0f);

            // Send the particle effect to all players in the world
            getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));

            StatusApplier.getOrNew(affectedPlayer).applyStatus(Status.ROOTED, 4, 0);
            StatusApplier.getOrNew(affectedPlayer).applyStatus(Status.BLIND, 4, 3);
            StatusApplier.getOrNew(affectedPlayer).applyStatus(Status.WEAKNESS, 4, 99);
        }
        i++;
    }

    @Override
    public boolean cancel() {
        return (i >= (4 * 20)) || !isAsleep;
    }

    @Override
    public void cleanup() {
        i = 0;
        StatusApplier applier = StatusApplier.getOrNew(affectedPlayer);
        applier.getEffects().forEach(status -> {
            if (status == Status.ROOTED || status == Status.BLIND || status == Status.WEAKNESS)
                applier.removeStatus(status);
        });
        affectedPlayer = null;
    }

}
