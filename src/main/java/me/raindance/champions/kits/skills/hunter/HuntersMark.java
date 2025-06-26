package me.raindance.champions.kits.skills.hunter;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.sound.SoundPlayer;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.BowShotSkill;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@SkillMetadata(id = 407, skillType = SkillType.Hunter, invType = InvType.BOW)
public class HuntersMark extends BowShotSkill {

    public HuntersMark() {
        super();
    }

    @Override
    public String getName() {
        return "Hunter's Mark";
    }

    @Override
    public float getCooldown() {
        return 15;
    }

    @Override
    protected void shotArrow(Arrow arrow, float force) {
        WrapperPlayServerWorldParticles bubbles = ParticleGenerator.createParticle(EnumWrappers.Particle.WATER_BUBBLE, 2);
        WrapperPlayServerWorldParticles redstone = ParticleGenerator.createParticle(EnumWrappers.Particle.REDSTONE, 2);
        ParticleGenerator.generateProjectile(arrow, bubbles);
        ParticleGenerator.generateProjectile(arrow, redstone);
    }

    @Override
    protected void shotEntity(DamageApplyEvent event, Player shooter, LivingEntity victim, Arrow arrow, float force) {
        //getPlayer().sendMessage(String.format("You shot %s", victim.getName()));
        event.addSource(this);
        event.setModified(true);
        // event.setDamage(event.getDamage() + 4);
        StatusApplier.getOrNew(victim).applyStatus(Status.BLEED, 5, 1);
        StatusApplier.getOrNew(victim).applyStatus(Status.MARKED, 5, 1);
        SoundPlayer.sendSound(victim.getLocation(), "mob.wolf.growl", 1F, 90);
    }

    @Override
    protected void shotGround(Player shooter, Location location, Arrow arrow, float force) {
    }
}
