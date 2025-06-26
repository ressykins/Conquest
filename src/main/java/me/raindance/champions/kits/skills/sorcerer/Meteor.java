package me.raindance.champions.kits.skills.sorcerer;

import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.VectorUtil;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.util.Vector;

import java.util.List;

import static com.podcrash.api.world.BlockUtil.getPlayersInArea;

@SkillMetadata(id = 1007, skillType = SkillType.Sorcerer, invType = InvType.AXE)
public class Meteor extends Instant implements IEnergy, ICooldown {
    private int energyUsage = 70;
    private int radius = 5;
    private int duration = 5;

    public Meteor() {}

    @Override
    public float getCooldown() {
        return 12;
    }

    @Override
    public String getName() {
        return "Meteor";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    public int getEnergyUsage() {
        return energyUsage;
    }

    @Override
    public void doSkill(PlayerEvent event, Action action) {
        if (!rightClickCheck(action) || onCooldown()) return;
        if(!hasEnergy()) {
            getPlayer().sendMessage(getNoEnergyMessage());
            return;
        }
        //Location loc = getPlayer().getEyeLocation().toVector().add(getPlayer().getLocation().getDirection().multiply(2))
        //        .toLocation(getPlayer().getWorld(), getPlayer().getLocation().getYaw(), getPlayer().getLocation().getPitch());
        //Fireball fireball = getPlayer().getWorld().spawn(loc, Fireball.class);
        Fireball fireball = getPlayer().launchProjectile(Fireball.class);
        fireball.setIsIncendiary(false);
        fireball.setYield(0);
        fireball.setVelocity(getPlayer().getLocation().getDirection().multiply(1.25));
        useEnergy(energyUsage);
        this.setLastUsed(System.currentTimeMillis());
        SoundPlayer.sendSound(getPlayer().getLocation(), "item.fireCharge.use", 0.75F, 63, getPlayers());

        getPlayer().sendMessage(getUsedMessage());
    }

    @EventHandler
    private void hit(ProjectileHitEvent event) {
        if(!(event.getEntity() instanceof Fireball) || !event.getEntity().getShooter().equals(getPlayer())) return;
        List<Player> playersAffected = getPlayersInArea(event.getEntity().getLocation(), radius, getPlayers());

        for(Player p: playersAffected) {
            Vector exp = VectorUtil.fromAtoB(event.getEntity().getLocation(), p.getLocation()).normalize();

            
            
            // exp.multiply(1.05).setY(exp.getY() + 0.6);
            // p.setVelocity(exp);

        
            p.setVelocity(exp.multiply(0.4).setY(1.25));

            if (p == getPlayer()) {
                getPlayer().setFallDistance(-8f);
            }
            if(isAlly(p)) continue;
            StatusApplier.getOrNew(p).applyStatus(Status.FIRE, duration, 5);
            double dist = p.getLocation().distanceSquared(event.getEntity().getLocation());
            double multiplier = (37D - dist) /36D;
            DamageApplier.damage(p, getPlayer(), 8 * multiplier, this, false);
        }
        //TODO: this needs refactor
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void fireballHit(DamageApplyEvent event){
        if(event.containsSource(this)) {
            event.setVelocityModifierX(event.getVelocityModifierX() * 0.6);
            event.setVelocityModifierY(event.getVelocityModifierY() * 0.6);
            event.setVelocityModifierZ(event.getVelocityModifierZ() * 0.6);
        }
    }
}
