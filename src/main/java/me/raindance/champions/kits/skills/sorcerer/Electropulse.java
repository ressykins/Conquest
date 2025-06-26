package me.raindance.champions.kits.skills.sorcerer;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.damage.Cause;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import net.md_5.bungee.api.ChatColor;

import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;

import com.podcrash.api.kits.iskilltypes.action.ICharge;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Drop;
import com.podcrash.api.mob.CustomEntityFirework;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;

@SkillMetadata(id = 1014, skillType = SkillType.Sorcerer, invType = InvType.DROP)
public class Electropulse extends Drop implements IEnergy, ICooldown, IConstruct, ICharge {
    private int energy = 60;
    private FireworkEffect firework;
    private final int MAX_CHARGES = 4;
    private int charges = 0;
    private long time;


    @EventHandler
    public void hit(DamageApplyEvent e) {
        if(e.getAttacker() != getPlayer()) return;
        if(isAlly(e.getVictim())) return;
        if(onCooldown()) return;
        if(e.getCause() != Cause.MELEE) return;
        time = System.currentTimeMillis();
        if(getCurrentCharges() < MAX_CHARGES) {
            addCharge();
            getPlayer().sendMessage(getCurrentChargeMessage());
        }
        start();
    }

    @Override
    public void addCharge() {
        if (charges < MAX_CHARGES) charges++;
    }

    @Override
    public int getCurrentCharges() {
        return charges;
    }

    @Override
    public int getMaxCharges() {
        return MAX_CHARGES;
    }

    @Override
    public int getEnergyUsage() {
        return energy;
    }

    public void resetCharge() {
        charges = 0;
    }

    private void start() {
        stop();
        TimeHandler.repeatedTime(1, 0, this);
    }

    private void stop() {
        TimeHandler.unregister(this);
    }

    @Override
    public void task() {
        if(getCurrentCharges() == MAX_CHARGES) {
            Random rand = new Random();
            WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
                    EnumWrappers.Particle.CRIT_MAGIC, 5,
                    rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
            getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));
            
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, 1, 1);
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.RESISTANCE, 1, 1);
        }
    }

    @Override
    public boolean isMaxAtStart() {
        return false;
    }

    @Override
    public boolean cancel() {
        return System.currentTimeMillis() - time >= 4000L || onCooldown();
    }

    @Override
    public void cleanup() {
        resetCharge();
        getPlayer().sendMessage(getCurrentChargeMessage());
        playSound();
    }

    private void playSound() {
        float i = (((float) getCurrentCharges()) / ((float) getMaxCharges()));
        SoundPlayer.sendSound(this.getPlayer(), "note.harp", 0.75f, (int) (130 * i));
    }

    @Override
    public float getCooldown() {
        return 8;
    }

    @Override
    public String getName() {
        return "Electropulse";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public void afterConstruction() {
        this.firework = FireworkEffect.builder()
                .withColor(Color.AQUA)
                .with(FireworkEffect.Type.STAR)
                .build();
    }

    @Override
    public boolean drop(PlayerDropItemEvent e) {
        if (onCooldown()) return false;
        if (!hasEnergy()) {
            getPlayer().sendMessage(getNoEnergyMessage());
            return false;
        }

        if(getCurrentCharges() < MAX_CHARGES) {
            getPlayer().sendMessage(String.format(
                "%sSorcerer> %sYou must have Maximum Charge to use %s%s%s.",
                ChatColor.BLUE,
                ChatColor.GRAY,
                ChatColor.GREEN,
                getName(),
                ChatColor.GRAY
            ));
            return false;
        }

        getPlayer().sendMessage(getUsedMessage());
        this.setLastUsed(System.currentTimeMillis());
        useEnergy(energy);

        CustomEntityFirework.spawn(getPlayer().getLocation(), firework, getPlayers());
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.zombie.remedy", 0.5F, 10);

        unleashShockwave();
        return true;
    }

    private void unleashShockwave() {
        Location location = getPlayer().getLocation();
        double radius = 6; // Radius of the shockwave
        int damage = 9; // Damage dealt by the shockwave
        int shockDuration = 4; // Shock status duration in seconds
        int silenceDuration = 2; // Silence status duration in seconds

        // Find all nearby enemies
        location.getWorld().getNearbyEntities(location, radius, radius, radius).stream()
            .filter(entity -> entity instanceof LivingEntity) // Only affect living entities
            .filter(entity -> entity != getPlayer()) // Exclude the player
            .forEach(entity -> {
                Player target = (Player) entity;

                // Deal damage
                DamageApplier.damage(target, getPlayer(), damage, this, false);

                // Apply Silence status if already Shocked
                if (StatusApplier.getOrNew(target).has(Status.SHOCK)) {
                    StatusApplier.getOrNew(target).applyStatus(Status.SILENCE, silenceDuration, 0);
                }

                // Apply Shock status
                StatusApplier.getOrNew(target).applyStatus(Status.SHOCK, shockDuration, 0);
            });

        // Add particle effects for the shockwave
        generateShockwaveParticles(location, radius);
    }

    private void generateShockwaveParticles(Location center, double radius) {
        // Create a circular shockwave effect with particles
        int particleCount = 50; // Number of particles in the shockwave
        for (int i = 0; i < particleCount; i++) {
            double angle = 2 * Math.PI * i / particleCount; // Angle for each particle
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location particleLocation = center.clone().add(x, 0, z);

            // Create particles at calculated positions
            WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(
                    particleLocation.toVector(),
                    EnumWrappers.Particle.CRIT_MAGIC,
                    1, 0, 0, 0
            );

            // Send the particle to all players
            center.getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));
        }
    }

}
