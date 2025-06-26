package me.raindance.champions.kits.skills.hunter;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.game.GameManager;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.itemskill.item.BearTrap;

import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.Drop;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.plugin.PodcrashSpigot;
import com.podcrash.api.sound.SoundPlayer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@SkillMetadata(id = 411, skillType = SkillType.Hunter, invType = InvType.AXE)
public class AnimalTrap extends Instant implements ICooldown, TimeResource, IPassiveTimer {
    private BearTrap bearTrap = new BearTrap();
    private final List<Item> activeTraps = new ArrayList<>();

    @Override
    public void start() {
        runAsync(1,0);
    }

    @Override
    public void task() {
        Iterator<Item> iterator = activeTraps.iterator();
        while (iterator.hasNext()) {
            Item trap = iterator.next();
            if (!trap.isValid() || trap.isDead()) {
                iterator.remove();
                continue;
            }

            Location location = trap.getLocation();
            double radiusSquared = 2.25; 

            WrapperPlayServerWorldParticles safeParticles =
                    ParticleGenerator.createParticle(location.toVector().add(new Vector(0, 1, 0)), EnumWrappers.Particle.VILLAGER_HAPPY, 2, 0, 0, 0);
            PacketUtil.syncSend(safeParticles, GameManager.getGame().getTeam(getPlayer()).getBukkitPlayers());

            // Loop through all living entities in the world
            for (LivingEntity entity : location.getWorld().getLivingEntities()) {
                if (entity.getLocation().distanceSquared(location) <= radiusSquared) {
                    if (entity instanceof Player && !isAlly(entity) && !entity.isDead()) {
                        Player player = (Player) entity;
                        if (getGame().isSpectating(player)) return;
                        // Trigger snareTrap when a player is within range
                            
                        // **Avoid Double Activation**: Mark trap as triggered
                        synchronized (trap) {
                            if (trap.hasMetadata("TrapTriggered")) continue;
                            trap.setMetadata("TrapTriggered", new FixedMetadataValue(Main.instance, true));
                        }
                        PodcrashSpigot.getInstance().getLogger().info("triggered trap");

                        // Run player-related logic on the main thread
                        Bukkit.getScheduler().runTask(Main.instance, () -> {
                            StatusApplier.getOrNew(player).applyStatus(Status.ROOTED, 2, 0, false, false);
                            SoundPlayer.sendSound(player, "random.door_close", 1F, 66);
                        });

                        // StatusApplier.getOrNew(player).applyStatus(Status.ROOTED, 2, 0, false, false);
                        // SoundPlayer.sendSound(player, "random.door_close", 1F, 66);
                        // bearTrap.snareTrap(getPlayer(), player, trap);
                        iterator.remove();
                        trap.remove();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void cleanup() {
    }


    @Override
    public String getName() {
        return "Animal Trap";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    public float getCooldown() {
        return 10;
    }


    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if (rightClickCheck(action)) {
            if (!onCooldown()) {
                setLastUsed(System.currentTimeMillis());
                getPlayer().sendMessage(getUsedMessage());
        
                Item thrownTrap = bearTrap.throwItem(getPlayer(), Action.RIGHT_CLICK_AIR);
        
                Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                    bearTrap.primeTrap(thrownTrap);
                    activeTraps.add(thrownTrap);
                }, 30L);
        
                Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                    if (thrownTrap.isValid()) {
        
                        WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(
                            thrownTrap.getLocation().clone().add(0, 1, 0).toVector(), EnumWrappers.Particle.EXPLOSION_NORMAL, 1, 0, 0, 0);
                        PacketUtil.syncSend(packet, thrownTrap.getWorld().getPlayers());
                        thrownTrap.remove();
        
                        activeTraps.remove(thrownTrap); // Remove from active mines list
                    }
                }, 600L);
            }
        }
    }

    @EventHandler
    public void onDeath(DeathApplyEvent event) {
        if (event.getPlayer() == getPlayer()) {
            // Remove all active mines associated with this player
            for (Item trap : activeTraps) {
                if (trap.isValid()) {
                    WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(
                        trap.getLocation().clone().add(0, 1, 0).toVector(), EnumWrappers.Particle.EXPLOSION_NORMAL, 1, 0, 0, 0);
                    PacketUtil.syncSend(packet, trap.getWorld().getPlayers());
                    trap.remove();
                }
            }

            // Clear the active mines list
            activeTraps.clear();
        }
    }

    @EventHandler
    public void onEnchant(PlayerInteractEvent e) {
        if (e.getPlayer() == getPlayer()) {
            // Remove all active mines associated with this player
            for (Item trap : activeTraps) {
                if (trap.isValid()) {
                    WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(
                        trap.getLocation().clone().add(0, 1, 0).toVector(), EnumWrappers.Particle.EXPLOSION_NORMAL, 1, 0, 0, 0);
                    PacketUtil.syncSend(packet, trap.getWorld().getPlayers());
                    trap.remove();
                }
            }

            // Clear the active mines list
            activeTraps.clear();
        }
    }
}
