package me.raindance.champions.kits.skills.warden;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.events.skill.SkillRechargeEvent;
import com.podcrash.api.events.skill.SkillUseEvent;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.util.EntityUtil;
import com.podcrash.api.util.SkillTitleSender;
import com.podcrash.api.util.TitleSender;
import com.podcrash.api.util.VectorUtil;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.ChargeUp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

@SkillMetadata(id = 905, skillType = SkillType.Warden, invType = InvType.SWORD)
public class Earthquake extends ChargeUp {
    private double maxRadius = 8;
    private double minRadius = 4;
    private double minDamage = 4;
    private double maxDamage = 8;

    @Override
    public float getCooldown() {
        return 10;
    }
    @Override
    public String getName() {
        return "Earthquake";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    } 

    @Override
    public float getRate() {
        return 0.5f / 20f;
    }


    @EventHandler
    public void recharge(SkillRechargeEvent e) {
        if(e.getSkillName().equalsIgnoreCase(this.getName()) && getPlayer().isBlocking()) {
            if(isInWater()) {
                getPlayer().sendMessage(getWaterMessage());
                return;
            }
            if(!onCooldown()) {
                if(!(EntityUtil.onGround(getPlayer()))) {
                    getPlayer().sendMessage(getMustAirborneMessage());
                    return;
                }
                SkillUseEvent useEvent = new SkillUseEvent(this);
                Bukkit.getPluginManager().callEvent(useEvent);
                if(useEvent.isCancelled()) return;
                preTask(); // Run pre-task logic before TimeHandler task
                TimeHandler.repeatedTime(1, 0, this);
            }
        }
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
                    if(!(EntityUtil.onGround(getPlayer()))) {
                        getPlayer().sendMessage(getMustAirborneMessage());
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
    public void task() {
        charge();
        isUsing = true;
        WrappedChatComponent progress = SkillTitleSender.chargeUpProgressBar(this, this.getCharge());
        if(getCharge() < 1f) SoundPlayer.sendSound(this.getPlayer(), "note.harp", 0.75f, (int)(130 * getCharge()) );

        Random rand = new Random();
        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
                EnumWrappers.Particle.SMOKE_LARGE, 5,
                rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
        getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));

        TitleSender.sendTitle(this.getPlayer(), progress);
    }

    public void release(){
            double radius = Math.max(maxRadius * getCharge(), minRadius);
            double damage = Math.max(maxDamage * getCharge(), minDamage);
            Location location = getPlayer().getLocation();
            List<LivingEntity> players = location.getWorld().getLivingEntities();
            for(LivingEntity enemy : players) {
                if(getPlayer() == enemy) continue;
                double dist = location.distanceSquared(enemy.getLocation());
                if(dist > Math.pow(radius, 2)) continue;
                pound(location, enemy, getCharge(), damage);
            }
            ParticleGenerator.generateRangeParticles(location, radius, true, (int) radius);
    }


    private void pound(Location currentLoc, LivingEntity entity, double multiplier, double maxDamage) {
        // if(multiplier > 1) multiplier = 1;
        if(!isAlly(entity)) DamageApplier.damage(entity, getPlayer(), maxDamage, this, false);
        Vector vector = VectorUtil.fromAtoB(currentLoc, entity.getLocation()).normalize();
        vector.multiply(multiplier * 1.25D).setY(vector.getY() + 1);
        if(vector.getY() > 1D) vector.setY(1D);
        entity.setVelocity(vector);
    }
}


// public class EarthSmash extends Instant implements ICooldown, IConstruct {
//     private double normalRadius = 4;
//     private double slamRadius = 8;
//     private double boost = -1;
//     private boolean isFalling = false;
//     private CollideBeforeHitGround hitGround;

//     @Override
//     public float getCooldown() {
//         return 10;
//     }

//     @Override
//     public void afterConstruction() {
//         hitGround = new CollideBeforeHitGround(getPlayer(), 1L,  0D, 0D, 0D).then(() -> {
//             Location location = getPlayer().getLocation();
//             List<LivingEntity> players = location.getWorld().getLivingEntities();
//             for(LivingEntity enemy : players) {
//                 if(getPlayer() == enemy) continue;
//                 double dist = location.distanceSquared(enemy.getLocation());
//                 if(dist > Math.pow(slamRadius, 2)) continue;
//                 pound(location, enemy, 1.33333D - ((16D - dist)/16D), 10);
//             }
//             ParticleGenerator.generateRangeParticles(location, slamRadius, true, (int) slamRadius);
//             isFalling = false;
//             StatusApplier applier = StatusApplier.getOrNew(getPlayer());
//             applier.applyStatus(Status.GROUND, 2, 1);
//             getPlayer().sendMessage(getUsedMessage());
//         });
//     }

//     @Override
//     public void doSkill(PlayerEvent event, Action action) {
//         if(!rightClickCheck(action) || onCooldown()) return;
//         setLastUsed(System.currentTimeMillis());

//         if(!EntityUtil.onGround(getPlayer())) {
//             isFalling = true;
//             slamDown();
//         } else {
//             Location location = getPlayer().getLocation();
//             List<LivingEntity> players = location.getWorld().getLivingEntities();
//             for(LivingEntity enemy : players) {
//                 if(getPlayer() == enemy) continue;
//                 double dist = location.distanceSquared(enemy.getLocation());
//                 if(dist > Math.pow(normalRadius, 2)) continue;
//                 pound(location, enemy, 1.33333D - ((16D - dist)/16D), 5);
//             }
//             ParticleGenerator.generateRangeParticles(location, normalRadius, true, (int) normalRadius);
//             setLastUsed(System.currentTimeMillis());
//             getPlayer().sendMessage(getUsedMessage());
//         }
//     }

//     private void pound(Location currentLoc, LivingEntity entity, double multiplier, double maxDamage) {
//         if(multiplier > 1) multiplier = 1;
//         if(!isAlly(entity)) DamageApplier.damage(entity, getPlayer(), multiplier * maxDamage, this, false);
//         Vector vector = VectorUtil.fromAtoB(currentLoc, entity.getLocation()).normalize();
//         vector.multiply(multiplier * 1.25D).setY(vector.getY() + 1);
//         if(vector.getY() > 1D) vector.setY(1D);
//         entity.setVelocity(vector);
//     }

//     private void slamDown() {
//         WrapperPlayServerWorldParticles particles = ParticleGenerator.createParticle(EnumWrappers.Particle.EXPLOSION_NORMAL, 1);
//         AftershockParticleResource resource = new AftershockParticleResource(getPlayer(), particles, null);
//         resource.run(1);

//         SoundPlayer.sendSound(getPlayer().getLocation(), "random.fizz", 1f, 126, getPlayers());
//         getPlayer().setVelocity(getPlayer().getVelocity().setY(boost));
//         hitGround.run();
//     }

//     @Override
//     public String getName() {
//         return "Earth Smash";
//     }

//     @Override
//     public ItemType getItemType() {
//         return ItemType.SWORD;
//     }

//     private class AftershockParticleResource extends EntityParticleResource {
//         private AftershockParticleResource(Entity entity, WrapperPlayServerWorldParticles packet, SoundWrapper sound) {
//             super(entity, packet, sound);
//         }

//         @Override
//         public boolean cancel() {
//             return !isFalling || getGame().isRespawning(getPlayer());
//         }
//     }
// }
