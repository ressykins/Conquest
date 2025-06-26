package me.raindance.champions.kits.skills.hunter;

import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.BowShotSkill;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@SkillMetadata(id = 404, skillType = SkillType.Hunter, invType = InvType.BOW)
public class RopedArrow extends BowShotSkill {

    public RopedArrow() {
        super();
    }

    @Override
    public String getName() {
        return "Roped Arrow";
    }

    @Override
    public float getCooldown() {
        return 6;
    }

    @Override
    protected void shotArrow(Arrow arrow, float force) {

    }

    @Override
    protected void shotEntity(DamageApplyEvent event, Player shooter, LivingEntity victim, Arrow arrow, float force) {
        //getPlayer().sendMessage(String.format("You shot %s", victim.getName()));
        //boost(victim.getLocation(), force, arrow.getVelocity());
        // event.setVelocityModifierX(-1.5d);
        // event.setVelocityModifierZ(-1.5d);

        // Vector curVelocity = victim.getVelocity();
        // victim.setVelocity(curVelocity.setY(0.5));
        Vector direction = shooter.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize();

        // Adjust the strength of the reversed knockback; you can change this multiplier as desired
        double knockbackStrength = 2.5; 
    
        // Set the reversed velocity with a slight upward push
        Vector knockbackVelocity = direction.multiply(knockbackStrength).setY(0.5);
        victim.setVelocity(knockbackVelocity);
    }

    @Override
    protected void shotGround(Player shooter, Location location, Arrow arrow, float force) {
        boost(arrow.getLocation(), force, arrow.getVelocity());
    }

    private void boost(Location endpoint, float force, Vector arrowvelocity) {
        getPlayer().sendMessage(getUsedMessage());
        if (getPlayer().isSneaking()) return;
        Location playerLoc = getPlayer().getLocation();
        force = (3 * force + 1f) / 4f;
        Vector vector = endpoint.toVector().subtract(playerLoc.toVector()).normalize().multiply(force);
        double multiplier = 0.8D;
        vector.setY(vector.getY() + 0.2d * multiplier);
        Vector playerV = getPlayer().getLocation().getDirection();
        if (playerV.getY() < 0) playerV.setY(0);
        vector.setY(playerV.getY() + vector.getY());
        double yMax = 0.5d + 0.52d * multiplier;
        if (vector.getY() > yMax) vector.setY(yMax);
        getPlayer().setVelocity(vector.multiply(0.3d + 1.2 * multiplier));
        getPlayer().setFallDistance(-4f);
    }
}
