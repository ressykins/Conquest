package me.raindance.champions.kits.skills.sorcerer;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.skill.SkillUseEvent;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.EnergyBar;
import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.TogglePassive;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.world.BlockUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;
import java.util.Set;

@SkillMetadata(id = 1013, skillType = SkillType.Sorcerer, invType = InvType.DROP)
public class ChaosForm extends TogglePassive implements IEnergy, TimeResource, IConstruct {

    private int energy = 25;
    private int radius = 6;
    private int i = 0;

    public ChaosForm(){
    }

    @Override
    public void afterConstruction() {
    }

    @Override
    public int getEnergyUsage() {
        return energy;
    }

    @Override
    public String getName() {
        return "Chaos Form";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public void toggle() {
        run(1, 0);
    }


    @Override
    public void task() {
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.GROUND, 1.75F, 0);
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SLOW, 1.75F, 1);
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.INVISIBILITY, 1.75F, 0);

        Location location = getPlayer().getLocation();

        location.getWorld().playSound(location, Sound.BLAZE_BREATH, 0.3f, 0.5f);
        useEnergy(getEnergyUsageTicks());

        // Drain health from enemies within radius
        i++;
        if (i == 20) {
            drainHealthFromEnemies();
            i = 0;
        }
    }

    @Override
    public boolean cancel() {
        return !isToggled() || !hasEnergy(getEnergyUsageTicks()) || isInWater();
    }
    @Override
    public void cleanup() {
        i = 0;
        if(!hasEnergy(getEnergyUsageTicks())) {
            forceToggle();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void damage(DamageApplyEvent e){
        if(e.isCancelled()) return;
        if(e.getVictim() == getPlayer() && isToggled()) {
            e.setModified(true);
            e.setDoKnockback(false);
        }
    }

    private void drainHealthFromEnemies() {
        Location playerLocation = getPlayer().getLocation();

        playerLocation.getWorld().getNearbyEntities(playerLocation, radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .filter(entity -> entity != getPlayer() && !isAlly((Player) entity) && !getGame().isSpectating((Player) entity) && !getGame().isRespawning((Player) entity))
                .forEach(entity -> {

                    // Deal damage to the enemy
                    Player victim = (Player) entity;
                    KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(victim);
                    double targetMaxHP = victimKitPlayer.getHP() * 0.05;

                    DamageApplier.damage(victim, getPlayer(), targetMaxHP, this, false);
                    getChampionsPlayer().heal(targetMaxHP);

                    // Generate heart particles traveling from the enemy to the player
                    generateHeartParticles(victim.getLocation(), playerLocation);
                });
    }

    private void generateHeartParticles(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        double distance = from.distance(to);
    
        for (double d = 0; d < distance; d += 0.2) { // Spacing of particles along the path
            Location current = from.clone().add(direction.clone().multiply(d));
            WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(
                    current.toVector(), EnumWrappers.Particle.SPELL_WITCH, 1, 0, 0, 0
            );
    
            // Send the particle to all players in the world
            from.getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));
        }
    }

}
