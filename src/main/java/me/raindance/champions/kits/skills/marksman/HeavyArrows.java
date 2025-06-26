package me.raindance.champions.kits.skills.marksman;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.util.EntityUtil;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;


@SkillMetadata(id = 512, skillType = SkillType.Marksman, invType = InvType.PRIMARY_PASSIVE)
public class HeavyArrows extends Passive {
    @Override
    public String getName() {
        return "Heavy Arrows";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    protected void shotArrow(EntityShootBowEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Player && event.getProjectile() instanceof Arrow) {
            Player player = (Player) event.getEntity();
            if (player == getPlayer()) {
                Arrow arrow = (Arrow) event.getProjectile();
                Vector newVector = arrow.getVelocity().multiply(0.7d);
                arrow.setVelocity(newVector);
                if (!getPlayer().isSneaking()) {
                    Vector fly = newVector.multiply(-0.30d).setY(newVector.getY() + 0.1d);
                    if (EntityUtil.onGround(getPlayer())) fly.setY(fly.getY() + 0.1d);
                    player.setVelocity(fly);
                    player.setFallDistance(0);
                }
            }
        }
    }

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    protected void shotPlayer(DamageApplyEvent event) {
        if (event.getArrow() == null) return;
        if (event.getCause() == Cause.PROJECTILE && event.getAttacker() == getPlayer()) {
            event.setModified(true);
            event.addSource(this);
            // event.setVelocityModifierX(event.getVelocityModifierX() * 1.5f);
            // event.setVelocityModifierY(event.getVelocityModifierY() * 1.5f);
            // event.setVelocityModifierZ(event.getVelocityModifierZ() * 1.5f);

            
            if(!EntityUtil.isBelow(event.getVictim(), 0.5)) {
                event.setModified(true);
                event.setDamage(event.getDamage() + 2);
            }

            if(event.getArrow().hasMetadata("RapidfireArrow")) return;

            // Retrieve the arrow's velocity and apply a knockback multiplier
            Vector arrowVelocity = event.getArrow().getVelocity();
            Vector knockback = arrowVelocity.multiply(2); // Increase knockback by 1.5x
            event.getVictim().setVelocity(knockback);
        }
    }
}
