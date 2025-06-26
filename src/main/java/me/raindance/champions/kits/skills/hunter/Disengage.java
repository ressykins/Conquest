package me.raindance.champions.kits.skills.hunter;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.util.Vector;

@SkillMetadata(id = 410, skillType = SkillType.Hunter, invType = InvType.SWORD)
public class Disengage extends Instant implements TimeResource, ICooldown {
    private boolean isDisengaging;
    private boolean tempFallCancel;
    private long time;
    private float effectTime;

    public Disengage() {
        effectTime = 2.0f;
    }

    @Override
    public float getCooldown() {
        return 8;
    }

    @Override
    public String getName() {
        return "Disengage";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if (!rightClickCheck(action) || onCooldown()) return;
        isDisengaging = true;
        time = System.currentTimeMillis();
        TimeHandler.repeatedTimeAsync(1, 0, this);
        getPlayer().sendMessage(getUsedMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hit(DamageApplyEvent event) {
        if (isDisengaging && event.getVictim() == getPlayer() && event.getCause() == Cause.MELEE) {
            if (isAlly(event.getAttacker())) return;
            LivingEntity victim = event.getAttacker();
            event.setCancelled(true);
            isDisengaging = false;
            tempFallCancel = true;
            StatusApplier.getOrNew(victim).applyStatus(Status.GROUND, effectTime, 2);
            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ZOMBIE_METAL, 0.5f, 1.6f);
            Vector vector = victim.getLocation().getDirection().normalize();
            vector.multiply(1.7825).setY(0.9);
            getPlayer().setVelocity(vector);
            getPlayer().sendMessage(getUsedMessage());
            setLastUsed(System.currentTimeMillis());


        }
    }

    @EventHandler(
            priority = EventPriority.LOW
    )
    public void fall(EntityDamageEvent event) {
        if (tempFallCancel && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (event.getEntity() == getPlayer()) {
                event.setCancelled(true);
                tempFallCancel = false;
            }
        }
    }

    @Override
    public void task() {
    }

    @Override
    public boolean cancel() {
        return (System.currentTimeMillis() - time >= 999L || !getPlayer().isBlocking());
    }

    @Override
    public void cleanup() {
        if (isDisengaging) {
            getPlayer().sendMessage(getFailedMessage());

            WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(EnumWrappers.Particle.VILLAGER_ANGRY, 4);
            packet.setLocation(getPlayer().getEyeLocation());
            PacketUtil.asyncSend(packet, getPlayers());
            
            setLastUsed(System.currentTimeMillis());
        }
        isDisengaging = false;
    }
}
