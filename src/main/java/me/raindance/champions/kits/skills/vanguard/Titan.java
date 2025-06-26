package me.raindance.champions.kits.skills.vanguard;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.damage.Cause;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.StatusApplyEvent;
import com.podcrash.api.time.resources.TimeResource;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;


@SkillMetadata(id = 808, skillType = SkillType.Vanguard, invType = InvType.PRIMARY_PASSIVE)
public class Titan extends Passive implements TimeResource, IPassiveTimer {

    private long lastHit = -1000L;

    @Override
    public String getName() {
        return "Titanic Swing";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public void start() {
        runAsync(5,0);
    }

    @Override
    public void task() {
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.MINING_FATIGUE, 90, 14, true, true);
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void cleanup() {

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void hit(DamageApplyEvent event) {
        if (event.isCancelled()) return;
        if (event.getAttacker() != getPlayer()) return;
        if (event.getCause() != Cause.MELEE) return;

        if(lastHit == -1000L || System.currentTimeMillis() - lastHit >= 1000L) {
            lastHit = System.currentTimeMillis();
        }
        else {
            event.setCancelled(true);
        }
        
        double playerMaxHP = KitPlayerManager.getInstance().getKitPlayer(getPlayer()).getHP();
        Player victim = (Player) event.getVictim();
        KitPlayer victimKitPlayer = KitPlayerManager.getInstance().getKitPlayer(victim);
        double targetMaxHP = victimKitPlayer.getHP();
        double bonus = (playerMaxHP - targetMaxHP) * 0.2;

        event.setModified(true);
        event.addSource(this);
        event.setDamage(event.getDamage() + bonus);
        // event.setVelocityModifierX(event.getVelocityModifierX() * (1 + bonus));
        // event.setVelocityModifierY(event.getVelocityModifierY() * (1 + bonus));
        // event.setVelocityModifierZ(event.getVelocityModifierZ() * (1 + bonus));
        StatusApplier.getOrNew(victim).applyStatus(Status.SLOW, 2, 0, true);

        // Apply custom knockback based on the bonus
        Vector victimVelocity = victim.getVelocity();
        Vector knockbackDirection = victim.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).normalize();
        double knockbackStrength = (bonus * 0.1);  // Multiply bonus to adjust knockback strength

        // Apply the new velocity
        victim.setVelocity(victimVelocity.add(knockbackDirection.multiply(knockbackStrength)));

        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(EnumWrappers.Particle.EXPLOSION_NORMAL, 2);
        particle.setLocation(victim.getLocation());
    }

    @EventHandler
    public void onSlow(StatusApplyEvent e) {
        if (e.getStatus().equals(Status.CRIPPLE)) {
            // TODO add a sound effect for this maybe ...
            e.setCancelled(true);
        }
    }
}
