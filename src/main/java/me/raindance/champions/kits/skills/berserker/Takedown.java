package me.raindance.champions.kits.skills.berserker;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.callback.sources.CollideBeforeHitGround;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.game.GameManager;
import com.podcrash.api.events.skill.SkillUseEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.util.EntityUtil;
import com.podcrash.api.util.PacketUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.util.Vector;

import java.util.List;

@SkillMetadata(id = 106, skillType = SkillType.Berserker, invType = InvType.AXE)
public class Takedown extends Instant implements ICooldown, IConstruct {
    private final float hitbox = 0.45f;
    private CollideBeforeHitGround hitGround;

    @Override
    public float getCooldown() {
        return 10;
    }

    @Override
    public String getName() {
        return "Takedown";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    public void afterConstruction() {

        WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(EnumWrappers.Particle.CRIT, 2);
        this.hitGround = new CollideBeforeHitGround(getPlayer(), 1, hitbox, hitbox, hitbox)
                .then(() -> {
                    SkillUseEvent event = new SkillUseEvent(this);
                    Bukkit.getPluginManager().callEvent(event);
                    if(event.isCancelled()) return;
                    List<Entity> entities = CollideBeforeHitGround.getValidEntitiesInRange(getPlayer(), hitbox, hitbox, hitbox);
                    if (entities.size() == 0) return;
                    getPlayer().setVelocity(new Vector(0, 0, 0));
                    for (Entity entity : entities) {
                        if (entity instanceof LivingEntity && entity != getPlayer() && !isAlly((LivingEntity) entity)) {
                            if (entity instanceof Player && GameManager.isSpectating((Player) entity)) break;
                            LivingEntity living = (LivingEntity) entity;
                            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ZOMBIE_WOOD, 2f, 0.2f);

                            if (getChampionsPlayer().getEnergyBar().getEnergy() >= 4) {
                                Player victim = (Player) living;
                                KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(victim);
                                // Find the current percentage of health remaining, then multiply it by our real max HP value (e.g. 40 for zerk right now)
                                double trueCurrentHP = (victim.getHealth() / victim.getMaxHealth()) * victimKitPlayer.getHP();
                                double trueMissingHP = victimKitPlayer.getHP() - trueCurrentHP;
                                double bonus = trueMissingHP * 0.15;
                                DamageApplier.damage(living, getPlayer(), 8 + bonus, this, false);
                            }
                            else {
                                DamageApplier.damage(living, getPlayer(), 8, this, false);
                            }
                            /*
                            StatusApplier.getOrNew((Player) entity).applyStatus(Status.SLOW, effect, 3);
                            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SLOW, effect, 3);

                             */

                            StatusApplier.getOrNew(living).applyStatus(Status.GROUND, 2, 3);
                            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.GROUND, 2, 3);

                            getPlayer().sendMessage(getUsedMessage(living));

                            break; //only attack one player
                        }
                    }
                }).doWhile(() -> {
                    packet.setLocation(getPlayer().getLocation());
                    PacketUtil.asyncSend(packet, getPlayers());
                });
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        this.setLastUsed(System.currentTimeMillis());
        Vector vector = getPlayer().getLocation().getDirection().normalize().multiply(1.1d).setY(0.1f);
        getPlayer().setVelocity(vector);
        getPlayer().setFallDistance(0);
        hitGround.run();
        getPlayer().sendMessage(getUsedMessage());
    }

    @Override
    public boolean canUseSkill(PlayerEvent event) {
        if(!super.canUseSkill(event)) return false;
        boolean a = EntityUtil.onGround(event.getPlayer());
        if(a) {
            if(!onCooldown()) {
                getPlayer().sendMessage(getMustGroundMessage());
            }
        }
        return !a;
    }


}
