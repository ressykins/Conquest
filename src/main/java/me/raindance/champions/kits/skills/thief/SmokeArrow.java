package me.raindance.champions.kits.skills.thief;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.BowShotSkill;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.PacketUtil;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@SkillMetadata(id = 702, skillType = SkillType.Thief, invType = InvType.BOW)
public class SmokeArrow extends BowShotSkill {
    private final int duration = 3;

    @Override
    public float getCooldown() {
        return 7;
    }

    @Override
    public String getName() {
        return "Smoke Arrow";
    }

    @Override
    protected void shotArrow(Arrow arrow, float force) {
        Player player = getPlayer();
        player.sendMessage(getUsedMessage());
        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(arrow.getLocation().toVector(), EnumWrappers.Particle.SMOKE_LARGE, 1,
                0,0,0);
        ParticleGenerator.generateProjectile(arrow, particle);
    }

    @Override
    protected void shotEntity(DamageApplyEvent event, Player shooter, LivingEntity victim, Arrow arrow, float force) {
        if(event.isCancelled()) return;
        StatusApplier.getOrNew(victim).applyStatus(Status.BLIND, duration, 1);
        StatusApplier.getOrNew(victim).applyStatus(Status.SLOW, duration, 1);
        SoundPlayer.sendSound(victim.getLocation(), "mob.blaze.breathe", 1, 100);
        WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(victim.getLocation().toVector(), EnumWrappers.Particle.EXPLOSION_LARGE, 1, 0, 0, 0);
        event.addSource(this);
        PacketUtil.syncSend(packet, getPlayers());
    }

    @Override
    protected void shotGround(Player shooter, Location location, Arrow arrow, float force) {
        return;
    }

}
