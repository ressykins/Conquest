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
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@SkillMetadata(id = 709, skillType = SkillType.Thief, invType = InvType.BOW)
public class SilencingArrow extends BowShotSkill {
    private final int duration = 3;

    @Override
    public float getCooldown() {
        return 8;
    }

    @Override
    public String getName() {
        return "Silencing Arrow";
    }

    @Override
    protected void shotArrow(Arrow arrow, float force) {
        Player player = getPlayer();
        player.sendMessage(getUsedMessage());
        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(arrow.getLocation().toVector(), EnumWrappers.Particle.SPELL, 1,
                0,0,0);
        ParticleGenerator.generateProjectile(arrow, particle);
    }

    @Override
    protected void shotEntity(DamageApplyEvent event, Player shooter, LivingEntity victim, Arrow arrow, float force) {
        if(event.isCancelled()) return;
        StatusApplier.getOrNew(victim).applyStatus(Status.SILENCE, duration, 1);
        victim.getWorld().playSound(victim.getLocation(), Sound.BAT_HURT, 1.0f, 1.5f);
        event.addSource(this);
        //shooter.sendMessage(String.format("Skill> You silenced %s for %d seconds.", victim.getName(), duration));
    }

    @Override
    protected void shotGround(Player shooter, Location location, Arrow arrow, float force) {
    }
}
