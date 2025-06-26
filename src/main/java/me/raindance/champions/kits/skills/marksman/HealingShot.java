package me.raindance.champions.kits.skills.marksman;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.packetwrapper.abstractpackets.WrapperPlayServerEntityStatus;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.util.PacketUtil;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;

import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.skilltypes.BowShotSkill;
import com.podcrash.api.sound.SoundPlayer;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Random;

@SkillMetadata(id = 507, skillType = SkillType.Marksman, invType = InvType.BOW)
public class HealingShot extends BowShotSkill implements IConstruct {
    private WrapperPlayServerWorldParticles hearts;
    private int splashDuration = 4;
    private int onhitDuration = 8;
    private final Random rand = new Random();

    public HealingShot() {
        // Setting ignoreShot to true for HealingShot
        super();
        this.ignoreShot = true;
    }

    @Override
    public float getCooldown() {
        return 11;
    }

    @Override
    public String getName() {
        return "Healing Shot";
    }

    @Override
    public void afterConstruction() {
        this.hearts = ParticleGenerator.createParticle(null, EnumWrappers.Particle.HEART, 2, 0,0,0);
    }

    @Override
    protected void shotArrow(Arrow arrow, float force) {
        ParticleGenerator.generateProjectile(arrow, hearts);
    }

    @Override
    protected void shotEntity(DamageApplyEvent event, Player shooter, LivingEntity victim, Arrow arrow, float force) {
        if (isAlly(victim) || victim == getPlayer()) {
            // PodcrashSpigot.getInstance().getLogger().info("healing shot hit player");
            event.setModified(true);
            event.setDamage(0);
            event.setDoKnockback(false);   
            StatusApplier.getOrNew(victim).applyStatus(Status.ABSORPTION, onhitDuration, 0, true, false);
        }
        else {
            StatusApplier.getOrNew(victim).applyStatus(Status.DIZZY, onhitDuration, 0, true, false);
        }

        WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(victim.getLocation().toVector(), EnumWrappers.Particle.HEART,
                3, rand.nextFloat(), -0.9f, rand.nextFloat());
        WrapperPlayServerEntityStatus status = new WrapperPlayServerEntityStatus();
        status.setEntityId(victim.getEntityId());
        status.setEntityStatus(WrapperPlayServerEntityStatus.Status.ENTITY_HURT);
        PacketUtil.syncSend(new AbstractPacket[]{status, packet}, getPlayers());

        SoundPlayer.sendSound(victim.getLocation(), "random.levelup", 0.9F, 95);
        event.addSource(this);
        splash(arrow, arrow.getLocation());
    }

    @Override
    protected void shotGround(Player shooter, Location location, Arrow arrow, float force) {
        splash(arrow, arrow.getLocation());
    }

    private void splash(Arrow arrow, Location location) {
            getPlayer().getWorld().playEffect(location, Effect.POTION_BREAK, 16385);

            // Apply a potion effect to nearby entities (example: Poison for 5 seconds)
            double radius = 3.0;  // Radius around the impact
            List<Entity> nearbyEntities = arrow.getNearbyEntities(radius, radius, radius);
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    if(isAlly(livingEntity)) StatusApplier.getOrNew(livingEntity).applyStatus(Status.REGENERATION, splashDuration, 0, true, false);
                }
            }

            // Remove the arrow after impact to avoid duplication
            // arrow.remove();
    }
}
