package me.raindance.champions.kits.skills.marksman;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@SkillMetadata(id = 506, skillType = SkillType.Marksman, invType = InvType.INNATE)
public class RangersFocus extends Passive {
    private List<Arrow> arrows = new ArrayList<>();

    // private void updateXP() {
    //     if(!onCooldown()) getPlayer().setExp(0);
    //     else {
    //         double cooldown = cooldown();
    //         double XP = cooldown / getCooldown();

    //         getPlayer().setExp((float) XP);
    //     }
    // }

    // @Override
    // public void start() {
    //     run(1, 0);
    // }

    // @Override
    // public void task() {
    //     updateXP();
    // }

    // @Override
    // public boolean cancel() {
    //     return false;
    // }

    // @Override
    // public void cleanup() {
    //     arrows.clear();
    // }

    // @Override
    // public float getCooldown() {
    //     return 3;
    // }

    @Override
    public String getName() {
        return "Ranger's Focus";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler(priority = EventPriority.LOW)
    protected void shotArrow(EntityShootBowEvent event) {
        if (event.isCancelled()) return;
        // if (onCooldown()) return;
        if(getPlayer() != event.getEntity()) return;
        if(!(event.getProjectile() instanceof Arrow)) return;
        // if (!getPlayer().isSneaking()) return;

        // getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.FIZZ, 0.5f, 2.0f);
        arrows.add((Arrow) event.getProjectile());
        // this.setLastUsed(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOW)
    protected void shotPlayer(DamageApplyEvent event) {
        if (event.getArrow() == null) return;
        if(event.getCause() != Cause.PROJECTILE || event.getAttacker() != getPlayer()) return;
        Arrow arr = event.getArrow();
        event.setModified(true);
        //event.setDamage(event.getDamage() - 1);
        if (!arrows.contains(arr)) return;
        Location vLocation = event.getVictim().getLocation();
        Location dLocation = getPlayer().getLocation();
        double distance = vLocation.distance(dLocation);
        event.addSource(this);
        //double damage = event.getDamage() + ((3.8 * .0009667) * FastMath.pow(distance, 2));
        double damage = event.getDamage() + (0.125 * distance);
        event.setDamage(Math.min(damage, 16));
        // event.setChangeXP(damage);
    }


    // grappling hook
    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        // Player player = event.getPlayer();
        // PodcrashSpigot.getInstance().getLogger().info(event.getState().toString());

        if (event.getState() == PlayerFishEvent.State.IN_GROUND && event.getPlayer() == getPlayer()) {
            Location hookLocation = event.getHook().getLocation();
            Vector pullVector = hookLocation.toVector().subtract(getPlayer().getLocation().toVector());
            pullVector.normalize().multiply(1.5);  // Adjust speed as needed
            pullVector.setY(pullVector.getY() + 0.5);

            // Apply the velocity to the player
            getPlayer().setVelocity(pullVector);
            getPlayer().setFallDistance(-4f);

        }
    }
}
