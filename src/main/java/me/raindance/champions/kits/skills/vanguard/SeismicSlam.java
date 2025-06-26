package me.raindance.champions.kits.skills.vanguard;

import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.events.skill.SkillUseEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.EntityUtil;
import net.jafama.FastMath;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.util.Vector;

import java.util.List;

@SkillMetadata(id = 802, skillType = SkillType.Vanguard, invType = InvType.AXE)
public class SeismicSlam extends Instant implements TimeResource, ICooldown {
    private boolean usage = false;
    private final double reach = Math.pow((5.5d), 2d);
    private final int damage = 6;

    @Override
    public float getCooldown() {
        return 11;
    }

    @Override
    public String getName() {
        return "Seismic Slam";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if (StatusApplier.getOrNew(getPlayer()).has(Status.GROUND)) {
            getPlayer().sendMessage(getCannotUseWhileMessage("Grounded"));
            return;
        }
        if (StatusApplier.getOrNew(getPlayer()).has(Status.ROOTED)) {
            getPlayer().sendMessage(getCannotUseWhileMessage("Rooted"));
            return;
        }
        if (!usage) {
            usage = true;
            Vector vector = getPlayer().getLocation().getDirection();
            if(vector.getY() < 0) vector.setY(vector.getY() * -1);
            vector.normalize();
            vector.multiply(0.3);
            vector.setY(vector.getY() + 0.8d);
            if(vector.getY() > 0.8D) vector.setY(0.8);
            if(EntityUtil.onGround(getPlayer())) vector.setY(vector.getY() + 0.2);
            getPlayer().setVelocity(vector);
            setLastUsed(System.currentTimeMillis());
            getPlayer().sendMessage(getUsedMessage());
            getPlayer().setFallDistance(-3f);
            this.runAsync(1, 0);
        }
    }

    @EventHandler
    public void die(DeathApplyEvent event) {
        if (event.getPlayer() == getPlayer())
            usage = false;
    }

    @Override
    public void task() {

    }

    @Override
    public boolean cancel() {
        return !usage || (System.currentTimeMillis() - getLastUsed() >= 200L && EntityUtil.onGround(getPlayer()));
    }

    @Override
    public void cleanup() {
        if (!usage) return;
        usage = false;
        SkillUseEvent event = new SkillUseEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        ParticleGenerator.generateRangeParticles(getPlayer().getLocation(), FastMath.sqrt(this.reach), true, (int) FastMath.sqrt(this.reach));
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ZOMBIE_WOOD, 2f, 0.2f);
        List<Player> players = getPlayer().getWorld().getPlayers();
        for (Player possibleVictim : players) {
            double diff = reach - possibleVictim.getLocation().distanceSquared(getPlayer().getLocation());
            if (possibleVictim != getPlayer() && diff > 0) {

                Vector vector = possibleVictim.getLocation().subtract(getPlayer().getLocation()).toVector();
                double multiplier = (1 - diff/reach) + 0.33;
                if(multiplier > 1) multiplier = 1;
                vector.normalize().multiply(1.7d * multiplier).setY(0.9);
                DamageApplier.damage(possibleVictim, getPlayer(), this.damage, this, false);
                possibleVictim.setVelocity(vector);
            }
        }
    }
}
