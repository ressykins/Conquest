package me.raindance.champions.kits.skills.marksman;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.game.GameManager;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.itemskill.item.Landmine;

import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.Drop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@SkillMetadata(id = 509, skillType = SkillType.Marksman, invType = InvType.DROP)
public class ProximityMine extends Drop implements ICooldown, TimeResource, IPassiveTimer {
    private Landmine proximityMine = new Landmine();
    private final List<Item> activeMines = new ArrayList<>();

    @Override
    public void start() {
        runAsync(1,0);
    }

    @Override
    public void task() {
        Iterator<Item> iterator = activeMines.iterator();
        while (iterator.hasNext()) {
            Item mine = iterator.next();
            if (!mine.isValid() || mine.isDead()) {
                iterator.remove();
                continue;
            }

            Location location = mine.getLocation();
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
                        proximityMine.snareTrap(getPlayer(), player, mine);
                        iterator.remove();
                        mine.remove();
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
        return "Proximity Mine";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public float getCooldown() {
        return 15;
    }

    @Override
    public boolean drop(PlayerDropItemEvent e) {
        if (onCooldown()) return false;
        setLastUsed(System.currentTimeMillis());
        getPlayer().sendMessage(getUsedMessage());

        Item thrownMine = proximityMine.throwItem(getPlayer(), Action.RIGHT_CLICK_AIR);

        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
            proximityMine.primeTrap(thrownMine);
            activeMines.add(thrownMine);
        }, 30L);

        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
            if (thrownMine.isValid()) {

                WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(
                    thrownMine.getLocation().clone().add(0, 1, 0).toVector(), EnumWrappers.Particle.EXPLOSION_NORMAL, 1, 0, 0, 0);
                PacketUtil.syncSend(packet, thrownMine.getWorld().getPlayers());
                thrownMine.remove();

                activeMines.remove(thrownMine); // Remove from active mines list
            }
        }, 600L);

        return true;
    }

    @EventHandler
    public void onDeath(DeathApplyEvent event) {
        if (event.getPlayer() == getPlayer()) {
            // Remove all active mines associated with this player
            for (Item mine : activeMines) {
                if (mine.isValid()) {
                    WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(
                        mine.getLocation().clone().add(0, 1, 0).toVector(), EnumWrappers.Particle.EXPLOSION_NORMAL, 1, 0, 0, 0);
                    PacketUtil.syncSend(packet, mine.getWorld().getPlayers());
                    mine.remove();
                }
            }

            // Clear the active mines list
            activeMines.clear();
        }
    }

    @EventHandler
    public void onEnchant(PlayerInteractEvent e) {
        if (e.getPlayer() == getPlayer()) {
            // Remove all active mines associated with this player
            for (Item mine : activeMines) {
                if (mine.isValid()) {
                    WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(
                        mine.getLocation().clone().add(0, 1, 0).toVector(), EnumWrappers.Particle.EXPLOSION_NORMAL, 1, 0, 0, 0);
                    PacketUtil.syncSend(packet, mine.getWorld().getPlayers());
                    mine.remove();
                }
            }

            // Clear the active mines list
            activeMines.clear();
        }
    }


}
