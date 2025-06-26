package me.raindance.champions.kits.skills.hunter;

import com.podcrash.api.callback.sources.CollideBeforeHitGround;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.skill.SkillUseEvent;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
// import com.podcrash.api.location.BoundingBox;
// import com.podcrash.api.location.Coordinate;
import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.skilltypes.ChargeUp;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.util.EntityUtil;
import org.bukkit.Bukkit;
// import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.List;

@SkillMetadata(id = 408, skillType = SkillType.Hunter, invType = InvType.SWORD)
public class WolfsPounce extends ChargeUp implements IConstruct {
    private final double damage = 5;
    private double charge;
    private final int effectTime = 3;
    private final double hitbox = 1.5;
    private CollideBeforeHitGround hitGround;


    @Override
    public void afterConstruction() {
        this.hitGround = new CollideBeforeHitGround(getPlayer(), 1, hitbox, hitbox, hitbox).then(() -> {
            List<Entity> entities = CollideBeforeHitGround.getValidEntitiesInRange(getPlayer(), hitbox, hitbox, hitbox);
            if (entities.size() == 0) return;
            // Location location = getPlayer().getLocation();
            for (Entity entity : entities) {
                if(!(entity instanceof LivingEntity)) continue;
                if(entity == getPlayer() || isAlly((LivingEntity) entity)) continue;

                getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.WOLF_BARK, 0.5f, 1.0f);
                DamageApplier.damage((LivingEntity) entity, getPlayer(), damage * charge, true);
                StatusApplier.getOrNew((LivingEntity) entity).applyStatus(Status.SLOW, effectTime, 0);
                getPlayer().sendMessage(getUsedMessage((LivingEntity) entity));
            }
        });
    }

    // private boolean checkBoxIntersection(BoundingBox box1, BoundingBox box2) {
    //     Coordinate min1 = box1.getA();
    //     Coordinate max1 = box1.getB();

    //     for(Coordinate point : box2.getBox()) {
    //         double x = point.getX();
    //         double y = point.getY();
    //         double z = point.getZ();

    //         if(min1.getX() <= x && x <= max1.getX()) {
    //             if(min1.getY() <= y && y <= max1.getY()) {
    //                 return (min1.getZ() <= z && z <= max1.getZ());
    //             }
    //         }
    //     }

    //     return false;
    // }

    @Override
    public float getRate() {
        //return (0.24f + 0.08f * 4) / 20f;
        return 0.7f / 20f;
    }

    @Override
    public float getCooldown() {
        return 4F;
    }

    @Override
    public String getName() {
        return "Wolf's Pounce";
    }

    /*
    @Override
    @EventHandler(priority = EventPriority.HIGH)
    public void block(PlayerInteractEvent e){
        if(((Entity) e.getEntity()).isOnGround()) super.block(e);
    }
    */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void damage(DamageApplyEvent e) {
        if(e.isCancelled()) return;
        if (e.getVictim() == this.getPlayer() && isUsing) {
            resetWhenDamaged();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void damage(EntityDamageEvent e) {
        if(e.isCancelled()) return;
        if (e.getEntity() == this.getPlayer() && isUsing) {
            resetWhenDamaged();
        }
    }

    private void resetWhenDamaged() {
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.WOLF_HURT, 1, 1);
        resetCharge();
    }


    @Override
    @EventHandler(
            priority = EventPriority.HIGH
    )
    public void block(PlayerInteractEvent e){
        if(e.getPlayer() == this.getPlayer()){
            if(rightClickCheck(e.getAction()) && isHolding()){
                if(isInWater()) {
                    getPlayer().sendMessage(getWaterMessage());
                    return;
                }
                if(!onCooldown()) {
                    if (StatusApplier.getOrNew(e.getPlayer()).has(Status.GROUND)) {
                        e.getPlayer().sendMessage(getCannotUseWhileMessage("Grounded"));
                        return;
                    }
                    if (StatusApplier.getOrNew(e.getPlayer()).has(Status.ROOTED)) {
                        e.getPlayer().sendMessage(getCannotUseWhileMessage("Rooted"));
                        return;
                    }
                    SkillUseEvent useEvent = new SkillUseEvent(this);
                    Bukkit.getPluginManager().callEvent(useEvent);
                    if(useEvent.isCancelled()) return;
                    TimeHandler.repeatedTime(1, 0, this);
                }
            }
        }
    }

    @Override
    public void release() {
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.WOLF_BARK, 1, (float) (0.5 + 1.5 * getCharge()));
        getPlayer().setFallDistance(-4f);
        Vector vector = getPlayer().getLocation().getDirection();
        vector = vector.normalize().multiply(0.5d + 1.35d * getCharge());
        charge = getCharge();
        getPlayer().sendMessage(getUsedMessage());
        vector.setY(vector.getY() + 0.2);
        double yMax = 0.5d + 0.82d * getCharge();
        if (vector.getY() > yMax) vector.setY(yMax);
        if (EntityUtil.onGround(getPlayer())) vector.setY(vector.getY() + 0.2);
        getPlayer().setVelocity(vector);
        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
            this.hitGround.run();
        }, 3L);
    }

    @Override
    public void task() {
        if (EntityUtil.onGround(getPlayer())) {
            super.task();
        }
    }

    @Override
    public void charge() {
        if (EntityUtil.onGround(getPlayer())) {
            super.charge();
        }
    }

    @Override
    public void cleanup() {
        if (getCharge() > 0f) super.cleanup();
    }
}
