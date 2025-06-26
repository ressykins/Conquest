package me.raindance.champions.kits.skills.marksman;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.Cause;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.skill.SkillUseEvent;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;

import me.raindance.champions.kits.SkillType;

import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.listeners.GameDamagerConverterListener;
import com.podcrash.api.sound.SoundPlayer;

// import java.util.HashSet;
// import java.util.Set;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.event.block.Action;

@SkillMetadata(id = 510, skillType = SkillType.Marksman, invType = InvType.BOW)
public class SnakeShot extends Instant implements ICooldown {
    private final int duration = 5;
    private Arrow arrow;
    // private Set<String> affected = new HashSet<>();

    public SnakeShot() {
        super();
    }

    @Override
    public String getName() {
        return "Snake Shot";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.BOW;
    }

    @Override
    public float getCooldown() {
        return 9;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
            if (!onCooldown()){
                SoundPlayer.sendSound(getPlayer().getLocation(), "random.bow", 0.12F, 70);
                arrow = getPlayer().launchProjectile(Arrow.class);
                arrow.setVelocity(arrow.getVelocity());
                GameDamagerConverterListener.forceAddArrow(arrow, 0.4f);
                arrow.setShooter(getPlayer());
                WrapperPlayServerWorldParticles packet =
                        ParticleGenerator.createParticle(arrow.getLocation().toVector(), EnumWrappers.Particle.SPELL_MOB, new int[]{0, 255,0}, 2,0,0,0);
                ParticleGenerator.generateProjectile(arrow, packet);
                arrow.setMetadata("SnakeShotArrow", new FixedMetadataValue(Main.instance, true));
                
                // ThrowableStatusApplier.applyProj(new StatusWrapper(Status.POISON, this.duration, 1, false, true), arrow);
                getPlayer().sendMessage(getUsedMessage());
                setLastUsed(System.currentTimeMillis());
            }
        }
    }

    // Essentially, override the right click checker method to make it require left clicks instead (because that's how we want the skill to activate).
    @Override
    public boolean rightClickCheck(Action action) { return action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK; }

    @EventHandler
    public void onUse(SkillUseEvent e) {
        if(e.getSkill().equals(this) && (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void shoot(DamageApplyEvent e) {
        if (!isAlly(e.getVictim()) && e.getAttacker() == getPlayer() && e.getArrow() != null && e.getCause() == Cause.PROJECTILE) {
            if(!(e.getVictim() instanceof Player)) return;
            if(e.isCancelled()) return;
            if(!e.getArrow().hasMetadata("SnakeShotArrow")) return;
            Player player = (Player) e.getVictim();
            e.addSource(this);
            e.setDamage(8); 
            StatusApplier.getOrNew(player).applyStatus(Status.POISON, duration, 1, false, true);
        }
    }

    // @EventHandler
    // public void shootBow(EntityShootBowEvent e) {
    //     if(e.getEntity() == getPlayer()) {
    //         WrapperPlayServerWorldParticles packet =
    //                 ParticleGenerator.createParticle(e.getProjectile().getLocation().toVector(), EnumWrappers.Particle.SPELL_MOB, new int[]{0, 255,0}, 2,0,0,0);
    //         ParticleGenerator.generateProjectile((Projectile) e.getProjectile(), packet);
    //     }
    // }
    // @EventHandler(
    //         priority = EventPriority.LOW
    // )
    // public void shoot(DamageApplyEvent e) {
    //     if (!isAlly(e.getVictim()) && e.getAttacker() == getPlayer() && e.getArrow() != null && e.getCause() == Cause.PROJECTILE) {
    //         if(!(e.getVictim() instanceof Player)) return;
    //         Player player = (Player) e.getVictim();
    //         e.addSource(this);
    //         if (!e.getArrow().isCritical()) return;
    //         if (StatusApplier.getOrNew(player).has(Status.POISON)) {
    //             StatusApplier.getOrNew(player).applyStatus(Status.POISON, duration, 1, false, true);
    //         }
    //         else {
    //             StatusApplier.getOrNew(player).applyStatus(Status.POISON, duration, 0, false, false);
    //         }
    //     }
    // }
}
