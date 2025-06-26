package me.raindance.champions.kits.skills.marksman;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.Main;
import com.podcrash.api.effect.particle.ParticleGenerator;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.skilltypes.BowShotSkill;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.EntityUtil;
import com.podcrash.api.util.PacketUtil;
import net.jafama.FastMath;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@SkillMetadata(id = 503, skillType = SkillType.Marksman, invType = InvType.BOW)
public class ExplosiveShot extends BowShotSkill implements IConstruct {
    private int damage = 7;
    private double range;
    private WrapperPlayServerWorldParticles particle;
    private WrapperPlayServerWorldParticles explosion;

    @Override
    public float getCooldown() {
        return 13;
    }

    @Override
    public String getName() {
        return "Explosive Shot";
    }

    @Override
    public void afterConstruction() {
        this.range = FastMath.pow(5.5D, 2D);
        particle = ParticleGenerator.createParticle(null, EnumWrappers.Particle.EXPLOSION_NORMAL, 2, 0,0,0);
        explosion = ParticleGenerator.createParticle(null, EnumWrappers.Particle.EXPLOSION_HUGE, 1, 0,0,0);

    }

    @Override
    protected void shotArrow(Arrow arrow, float force) {
        Bukkit.getScheduler().runTaskLater(Main.instance, () -> ParticleGenerator.generateProjectile(arrow, particle), 1L);
    }

    @Override
    protected void shotEntity(DamageApplyEvent event, Player shooter, LivingEntity victim, Arrow arrow, float force) {
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(Main.instance, () -> explode(arrow, arrow.getLocation().getBlock().getLocation()));

    }

    @Override
    protected void shotGround(Player shooter, Location location, Arrow arrow, float force) {
        explode(arrow, location.getBlock().getLocation());
    }
    private void explode(Arrow arrow, Location location) {
        explosion.setLocation(location);
        PacketUtil.syncSend(explosion, getPlayers());
        for (final Player player : getPlayers()) {
            double deltaDistance = this.range - location.distanceSquared(player.getLocation());
            if (deltaDistance > 0) {
                double divide = deltaDistance/range;
                Vector vector = player.getLocation().add(0, 1, 0).subtract(location).toVector().normalize().multiply(0.45D + 0.6D * divide);
                vector.setY(vector.getY() + 0.5D + 0.3D * divide);
                if (vector.getY() > 1.0D) vector.setY(1.0D);
                if (EntityUtil.onGround(player)) {
                    vector.setY(vector.getY() + 0.2);
                }
                player.setVelocity(vector);

                if(player == getPlayer()) {
                    getPlayer().setFallDistance(-8f);
                }

                DamageApplier.damage(player, getPlayer(), damage * divide, this, false);
            }
            SoundPlayer.sendSound(location, "random.explode", 0.9F, 70);
        }
    }
}
