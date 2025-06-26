package me.raindance.champions.kits.skills.marksman;

import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Drop;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

@SkillMetadata(id = 502, skillType = SkillType.Marksman, invType = InvType.DROP)
public class Rapidfire extends Drop implements ICooldown, TimeResource {
    private boolean active = false;
    private int duration = 4;
    private int i = 0;

    private void updateXP() {
        double XP = i / (duration * 20);
        getPlayer().setExp((float) XP);
    }

    @Override
    public void task() {
        updateXP();
        i += 20;
    }

    @Override
    public boolean cancel() {
        return i >= (duration * 20);
    }

    @Override
    public void cleanup() {
        active = false;
        i = 0;
        setLastUsed(System.currentTimeMillis());
        // PodcrashSpigot.getInstance().getLogger().info("rapidfire ended, now on cooldown");
    }

    @Override
    public float getCooldown() {
        return 13;
    }

    @Override
    public String getName() {
        return "Rapidfire";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public boolean drop(PlayerDropItemEvent e) {
        if (e.getPlayer() != getPlayer() || onCooldown() || active) return false;

        active = true;
        // PodcrashSpigot.getInstance().getLogger().info("rapidfire is now " + active);
        TimeHandler.repeatedTime(20, 0, this);

        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.wither.idle", 1.2F, 63);
        getPlayer().sendMessage(getUsedMessage());

        return true;
    }

    @EventHandler(
            priority = EventPriority.HIGH
    )
    protected void shotArrow(EntityShootBowEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof Player && event.getProjectile() instanceof Arrow && active) {
            Player player = (Player) event.getEntity();
            if (player != getPlayer()) return;     

            // // Cancel the original arrow
            // event.setCancelled(true);
            
            // // Cast shooter and get initial arrow properties
            // Player shooter = (Player) event.getEntity();
            // Location shootLocation = shooter.getEyeLocation();
            // Vector arrowVelocity = player.getLocation().getDirection().normalize().multiply(3.0);

            // // Spawn a new arrow with the desired properties
            // Arrow newArrow = shooter.getWorld().spawnArrow(shootLocation, arrowVelocity, (float) arrowVelocity.length(), 0);
            // newArrow.setShooter(shooter);

            // // Customize new arrow properties if needed
            // newArrow.setCritical(true);  // Makes the arrow act as if it's a critical shot
            // GameDamagerConverterListener.forceAddArrow(newArrow, 1.0F);
            // SoundPlayer.sendSound(getPlayer().getLocation(), "random.bow", 0.12F, 70);
            
            // Player player = (Player) event.getEntity();
            // if (player == getPlayer()) {
            Arrow arrow = (Arrow) event.getProjectile();
            Vector velocity = player.getLocation().getDirection().normalize().multiply(3.0);
            arrow.setVelocity(velocity);

            arrow.setMetadata("RapidfireArrow", new FixedMetadataValue(Main.instance, true));
            // Optionally, mark the arrow as "critical" to match a fully charged bow
            arrow.setCritical(true);
            // }
        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    protected void shotPlayer(DamageApplyEvent event) {
        if (event.getArrow() == null) return;
        if (event.getCause() == Cause.PROJECTILE && event.getAttacker() == getPlayer() && active) {
            if(!event.getArrow().hasMetadata("RapidfireArrow")) return;
            event.setModified(true);
            event.addSource(this);
            // event.setDamage(event.getDamage() * 0.25f);
            event.setDamage(2);
            event.setDoKnockback(false);

        }
    }
}
