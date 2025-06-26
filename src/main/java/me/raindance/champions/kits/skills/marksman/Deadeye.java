package me.raindance.champions.kits.skills.marksman;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.Cause;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;

import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.TogglePassive;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.resources.TimeResource;

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

@SkillMetadata(id = 508, skillType = SkillType.Marksman, invType = InvType.DROP)
public class Deadeye extends TogglePassive implements ICooldown, TimeResource {
    // private boolean cancel = false;

    @Override
    public String getName() {
        return "Deadeye";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public float getCooldown() {
        return 8;
    }

    @Override
    public void toggle() {
        run(1, 0);
    }


    @Override
    public void task() {
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SLOW, 0.5f, 1, false, false);
        StatusApplier.getOrNew(getPlayer()).applyStatus(Status.JUMP_BOOST, 0.5f, 128, false, false);
        if (getPlayer().isSneaking()) {
            StatusApplier.getOrNew(getPlayer()).applyStatus(Status.SLOW, 0.25f, 5, false, true);
        }
    }

    @Override
    public boolean cancel() {
        return !isToggled();
    }

    @Override
    public void cleanup() {
        if(isToggled()) forceToggle();
        if(!onCooldown()) setLastUsed(System.currentTimeMillis());
        getPlayer().setFoodLevel(20);
        getPlayer().setWalkSpeed(0.2f);
    }

    @Override
    public boolean drop(PlayerDropItemEvent event) {
        if (event.getPlayer() != getPlayer() || onCooldown()) return false;

        if(!isToggled()) {
            SoundPlayer.sendSound(getPlayer().getLocation(), "fireworks.launch", 1.2F, 10);
            getPlayer().setFoodLevel(4);
            getPlayer().setWalkSpeed(0);
        }

        runToggle();
        return true;
    }
    // @Override
    // public boolean drop(PlayerDropItemEvent e) {
    //     if (e.getPlayer() != getPlayer() || onCooldown()) return false;

    //     if (active) {
    //         reset();
    //         return false;
    //     }

    //     active = true;
    //     getPlayer().setFoodLevel(4);
    //     getPlayer().setWalkSpeed(0);
    //     runAsync(1,0);

    //     SoundPlayer.sendSound(getPlayer().getLocation(), "fireworks.launch", 1.2F, 10);
    //     getPlayer().sendMessage(getUsedMessage());

    //     return true;
    // }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void damage(DamageApplyEvent e) {
        if(e.isCancelled()) return;
        if (e.getVictim() == this.getPlayer() && isToggled()) {
            forceToggle();
        }
    }

    @EventHandler(
            priority = EventPriority.LOWEST
    )
    protected void shotArrow(EntityShootBowEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Player && event.getProjectile() instanceof Arrow && isToggled()) {
            Player player = (Player) event.getEntity();
            if (player == getPlayer()) {
                Arrow arrow = (Arrow) event.getProjectile();
                if (!arrow.isCritical()) return;
                Vector newVector = arrow.getVelocity().multiply(1.3d);
                arrow.setVelocity(newVector);
                arrow.setMetadata("DeadeyeArrow", new FixedMetadataValue(Main.instance, true));
                ParticleGenerator.generateProjectile(arrow, ParticleGenerator.createParticle(arrow.getLocation().toVector(), EnumWrappers.Particle.REDSTONE, 10, 0, 0, 0));
            }
        }
    }


    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    protected void shotPlayer(DamageApplyEvent event) {
        if (event.getArrow() == null) return;
        if (event.getCause() == Cause.PROJECTILE && event.getAttacker() == getPlayer() && isToggled()) {
            event.setModified(true);
            Arrow arrow = (Arrow) event.getArrow();
            if (!arrow.isCritical()) return;
            if(!event.getArrow().hasMetadata("DeadeyeArrow")) return;

            Player player = (Player) event.getVictim();

            // Get the location of the player's head (eyes location)
            Vector headLocation = player.getEyeLocation().toVector();

            // Get the arrow's location
            Vector arrowLocation = arrow.getLocation().toVector();

            if (arrowLocation.getY() >= headLocation.getY() && player != getPlayer() && !isAlly(player)) {
                event.setDamage(event.getDamage() * 1.5);
                getPlayer().sendMessage(String.format("%s%s> %sYou landed a headshot on %s%s%s!",
                        ChatColor.BLUE, getChampionsPlayer().getName(), ChatColor.GRAY, ChatColor.YELLOW, event.getVictim().getName(), ChatColor.GRAY));
                event.addSource(this);
            }
        }
    }

    // public void reset() {
    //     active = false;
    // }


    // private final double bonusDamage;
    // private final float rate;

    // public Overcharge() {
    //     this.rate = (1.2f) / 20f;
    //     this.bonusDamage = 8;
    // }

    // @Override
    // public float getRate() {
    //     return rate;
    // }

    // @Override
    // public void doShoot(Arrow arrow, float charge) {
    //     ParticleGenerator.generateProjectile(arrow, ParticleGenerator.createParticle(arrow.getLocation().toVector(), EnumWrappers.Particle.REDSTONE, 10, 0, 0, 0));
    // }

    // @Override
    // public void shootPlayer(Arrow arrow, float charge, DamageApplyEvent e) {
    //     double bonus = bonusDamage * charge;
    //     e.setModified(true);
    //     e.setDamage(e.getDamage() + bonus);
    //     getPlayer().sendMessage(String.format("%s%s> %sYou shot %s%s %sfor %s more damage with %s%s%s.",
    //             ChatColor.BLUE, getChampionsPlayer().getName(), ChatColor.GRAY, ChatColor.YELLOW, e.getVictim().getName(), ChatColor.GRAY, bonus, ChatColor.GREEN, getName(), ChatColor.GRAY));
    //     e.addSource(this);
    // }

    // @Override
    // public void shootGround(Arrow arrow, float charge) {

    // }
}
