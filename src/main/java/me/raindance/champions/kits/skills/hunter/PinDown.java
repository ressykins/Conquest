package me.raindance.champions.kits.skills.hunter;

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
import com.podcrash.api.util.EntityUtil;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.bukkit.event.block.Action;

@SkillMetadata(id = 413, skillType = SkillType.Hunter, invType = InvType.BOW)
public class PinDown extends Instant implements ICooldown {
    private final int duration = 3;
    private Arrow arrow;



    @Override
    public String getName() {
        return "Pin Down";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.BOW;
    }

    @Override
    public float getCooldown() {
        return 12;
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
                        ParticleGenerator.createParticle(arrow.getLocation().toVector(), EnumWrappers.Particle.SPELL_MOB, new int[]{0, 0, 0}, 2,0,0,0);
                ParticleGenerator.generateProjectile(arrow, packet);
                arrow.setMetadata("PinDownArrow", new FixedMetadataValue(Main.instance, true));
                
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
            if(!e.getArrow().hasMetadata("PinDownArrow")) return;
            Player player = (Player) e.getVictim();
            e.addSource(this);


        
            e.setDoKnockback(false);

            double baseDamage = 8;
            double bonusDamage = 2;  // Adjust this value for extra damage
    
            // Check if the player is airborne by their Y velocity
            if (!EntityUtil.onGround(player)) {
                // Apply downward knockback
                Vector downwardKnockback = new Vector(0, -1.5, 0);  // Adjust -1.5 as needed for downward force
                player.setVelocity(downwardKnockback);
                
                // Apply bonus damage for airborne target
                e.setDamage(baseDamage + bonusDamage);
                StatusApplier.getOrNew(player).applyStatus(Status.GROUND, duration, 1, false, true);
                
            } else {
                e.setDamage(baseDamage);  // Regular damage if the player is not airborne
            }
            
            // Apply the slow status effect
            StatusApplier.getOrNew(player).applyStatus(Status.SLOW, duration, 1, false, true);

        }
    }


}
