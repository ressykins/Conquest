package me.raindance.champions.kits.skills.vanguard;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICharge;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.sound.SoundWrapper;
import com.podcrash.api.time.resources.EntityParticleResource;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerToggleSprintEvent;

@SkillMetadata(id = 804, skillType = SkillType.Vanguard, invType = InvType.PRIMARY_PASSIVE)
public class Stampede extends Passive implements IPassiveTimer, ICharge {
    private int charges = 0;
    private long time;
    private boolean toggle;
    private int timing;
    private int currentSpeed = -1;
    
    private StampedeParticleResource resource;
    public Stampede() {
        charges = 0;
        this.toggle = false;
        this.timing = 3;
    }

    @Override
    public String getName() {
        return "Stampede";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public void setPlayer(Player player) {
        super.setPlayer(player);
        WrapperPlayServerWorldParticles particles = ParticleGenerator.createParticle(EnumWrappers.Particle.CRIT, 2);
        resource = new StampedeParticleResource(getPlayer(), particles, null);

    }
    @Override
    public void start() {
        time = System.currentTimeMillis();
        run(1, 1);
        resource.run(1,1);
    }

    @Override
    public void addCharge() {
        charges = charges < 2 ? charges + 1 : 2;
    }

    @Override
    public int getCurrentCharges() {
        return charges;
    }

    @Override
    public int getMaxCharges() {
        return 2;
    }

    @Override
    public boolean isMaxAtStart() {
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void sprint(PlayerToggleSprintEvent event) {
        if (event.getPlayer() != getPlayer()) return;
        check(event.isSprinting());
    }

    private void check(boolean isSprinting) {
        toggle = isSprinting;
        if (toggle && !getGame().isRespawning(getPlayer())) {
            start();
        } else {
            unregister();
            reset();
        }
    }

    private void incSpeed(){
        currentSpeed++;
        StatusApplier.getOrNew(getPlayer()).removeStatus(Status.SPEED);
        addCharge();
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ZOMBIE_IDLE, 0.5f, 1);
        if(currentSpeed > 2) currentSpeed = 1;
    }

    private void resetSpeed(){
        if (currentSpeed >= 1) {
            StatusApplier.getOrNew(getPlayer()).removeStatus(Status.SPEED);
        }
        currentSpeed = -1;
    }

    @Override
    public void task() {
        if (getPlayer().isSprinting()) {
            if(currentSpeed >= 0) StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SPEED, 1, currentSpeed, false);
            if (currentSpeed != 1 && System.currentTimeMillis() - time >= 1000L * timing) {
                time = System.currentTimeMillis();
                incSpeed();
            }
        } else toggle = false;
    }

    @Override
    public boolean cancel() {
        return !toggle;
    }

    @Override
    public void cleanup() {
        StatusApplier.getOrNew(getPlayer()).removeStatus(Status.SPEED);
        reset();
    }

    public void reset() {
        charges = 0;
        toggle = false;
        resetSpeed();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void hit(DamageApplyEvent event) {
        if(event.isCancelled()) return;
        if (event.getAttacker() == getPlayer()) {
            if (event.getCause() != Cause.MELEE) return;
            if(charges == 0) return;
            event.setModified(true);
            event.addSource(this);
            event.setDamage(event.getDamage() + charges);
            event.setVelocityModifierX(event.getVelocityModifierX() * (2 * charges));
            event.setVelocityModifierY(event.getVelocityModifierY() * (1.25D * charges));
            event.setVelocityModifierZ(event.getVelocityModifierZ() * (2 * charges));
            getPlayer().getWorld().playSound(event.getVictim().getLocation(), Sound.ZOMBIE_WOOD, 0.5f, 1);
            reset();
            check(getPlayer().isSprinting());
        } else if (event.getVictim() == getPlayer()) {
            reset();
            check(getPlayer().isSprinting());
        }
    }

    private class StampedeParticleResource extends EntityParticleResource {
        private StampedeParticleResource(Entity entity, WrapperPlayServerWorldParticles packet, SoundWrapper sound) {
            super(entity, packet, sound);
        }

        @Override
        public boolean cancel() {
            return !toggle;
        }
    }
}
