package me.raindance.champions.kits.skills.marksman;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.effect.particle.ParticleGenerator;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.listeners.GameDamagerConverterListener;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

@SkillMetadata(id = 514, skillType = SkillType.Marksman, invType = InvType.SECONDARY_PASSIVE)
public class Deflection extends Instant implements TimeResource, ICooldown {
    private boolean isDeflecting;
    private long time;

    public Deflection() {
    }

    @Override
    public float getCooldown() {
        return 15;
    }

    @Override
    public String getName() {
        return "Deflection";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if (!rightClickCheck(action) || onCooldown()) return;
        isDeflecting = true;
        time = System.currentTimeMillis();
        TimeHandler.repeatedTimeAsync(1, 0, this);
        getPlayer().sendMessage(getUsedMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(DamageApplyEvent event) {
        if (!isDeflecting || event.getVictim() != getPlayer()) return;
        if (isAlly(event.getAttacker())) return;

        event.addSource(this);

        if (event.getCause() == Cause.MELEE) {
            // Melee attack logic
            LivingEntity attacker = event.getAttacker();
            event.setCancelled(true);

            // Knock back attacker
            Vector vector = attacker.getLocation().getDirection().normalize().multiply(-1.5).setY(0.5);
            attacker.setVelocity(vector);

            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ZOMBIE_METAL, 0.5f, 1.0f);
            getPlayer().sendMessage(getUsedMessage());
            setLastUsed(System.currentTimeMillis());
        } 

        else if (event.getCause() == Cause.PROJECTILE) {
            if (!(event.getArrow() instanceof Arrow)) return;

            event.getArrow().remove();
            event.setCancelled(true);

            // Schedule the arrow creation and handling on the main thread
            Bukkit.getScheduler().runTask(Main.instance, () -> {
                Arrow arrow = getPlayer().launchProjectile(Arrow.class);
                arrow.setVelocity(getPlayer().getLocation().getDirection().normalize().multiply(2));
                GameDamagerConverterListener.forceAddArrow(arrow, 0.4f);
                arrow.setShooter(getPlayer());
                arrow.setMetadata("DeflectedArrow", new FixedMetadataValue(Main.instance, true));

                getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ZOMBIE_METAL, 0.5f, 2.0f);
                setLastUsed(System.currentTimeMillis());
            });
        }


        isDeflecting = false;
    }


    @EventHandler
    public void shoot(DamageApplyEvent e) {
        if (!isAlly(e.getVictim()) && e.getAttacker() == getPlayer() && e.getArrow() != null && e.getCause() == Cause.PROJECTILE) {
            if(!(e.getVictim() instanceof Player)) return;
            if(e.isCancelled()) return;
            if(!e.getArrow().hasMetadata("DeflectedArrow")) return;
            e.addSource(this);
            e.setDamage(8); 
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
        if (isDeflecting) {
            getPlayer().sendMessage(getFailedMessage());

            WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(EnumWrappers.Particle.VILLAGER_ANGRY, 4);
            packet.setLocation(getPlayer().getEyeLocation());
            PacketUtil.asyncSend(packet, getPlayers());
            
            setLastUsed(System.currentTimeMillis());
        }
        isDeflecting = false;
    }
}
