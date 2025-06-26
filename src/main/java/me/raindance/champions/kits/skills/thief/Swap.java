package me.raindance.champions.kits.skills.thief;

import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.util.PacketUtil;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.block.Action;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

@SkillMetadata(id = 713, skillType = SkillType.Thief, invType = InvType.AXE)
public class Swap extends Instant implements ICooldown {

    @Override
    public float getCooldown() {
        return 17; 
    }

    @Override
    public String getName() {
        return "Swap";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;  
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if (!rightClickCheck(action) || onCooldown()) return;

        Player player = (Player) event.getPlayer();

        // Check if the player is sneaking (to swap with an ally)
        boolean isSneaking = player.isSneaking();
        
        // Find the nearest target based on the player's line of sight
        Entity target = getNearestPlayerInSight(player, isSneaking);

        if (target != null && target instanceof Player) {
            Player targetPlayer = (Player) target;

            // Swap the positions of the player and target
            
            setLastUsed(System.currentTimeMillis());
            
            swapPositions(player, targetPlayer);
        } else {
            // Send a message if no valid target is found
            getPlayer().sendMessage(String.format("%sThief> %sThere are no targets in range and/or sight! (10 Blocks)",
            ChatColor.BLUE,
            ChatColor.GRAY));
            return;
        }
    }

    private Entity getNearestPlayerInSight(Player player, boolean isSneaking) {
        Player nearestPlayer = null;
        double nearestDistance = Double.MAX_VALUE;
        double maxDistance = 10; // Maximum distance to search for a target
        double fieldOfView = 60; // Field of view in degrees (30 degrees to the left/right of the center)
    
        for (Entity entity : player.getWorld().getEntities()) {
            if (entity instanceof Player && entity != player) {
                Player targetPlayer = (Player) entity;
                double distance = player.getLocation().distance(targetPlayer.getLocation());
    
                // Check if the target is within the 10-block radius
                if (distance > maxDistance) continue;
    
                // Calculate the angle between the player's facing direction and the direction to the target
                if (!isInFieldOfView(player, targetPlayer, fieldOfView)) continue;
    
                // Check whether the target is an ally or enemy based on sneaking
                if (isSneaking && !isAlly(targetPlayer)) continue;
                if (!isSneaking && isAlly(targetPlayer)) continue;
    
                // Update the nearest player
                if (distance < nearestDistance) {
                    nearestPlayer = targetPlayer;
                    nearestDistance = distance;
                }
            }
        }
    
        return nearestPlayer;
    }
    
    // Helper method to determine if a target is within the player's field of view
    private boolean isInFieldOfView(Player player, Player target, double fieldOfView) {
        org.bukkit.Location playerLocation = player.getEyeLocation();
        org.bukkit.Location targetLocation = target.getLocation().add(0, target.getEyeHeight() / 2, 0); // Target's eye level
    
        // Get the direction vector from the player to the target
        Vector toTarget = targetLocation.toVector().subtract(playerLocation.toVector()).normalize();
    
        // Get the player's facing direction
        Vector playerFacing = playerLocation.getDirection().normalize();
    
        // Calculate the angle (in degrees) between the player's facing direction and the direction to the target
        double angle = Math.toDegrees(playerFacing.angle(toTarget));
    
        // Check if the angle is within the field of view
        return angle <= fieldOfView / 2;
    }

    private void swapPositions(Player player, Player target) {
        if (player == null || target == null) return;
    
        // Get the current location of both players
        Location playerLocation = player.getLocation();
        Location targetLocation = target.getLocation();
    
        // Play sound and particle effects at original locations
        player.getWorld().playSound(playerLocation, Sound.ENDERMAN_TELEPORT, 2.0F, 0.5F);
        WrapperPlayServerWorldParticles particles = ParticleGenerator.createParticle(EnumWrappers.Particle.PORTAL, 2);
        particles.setLocation(playerLocation);
        PacketUtil.asyncSend(particles, getPlayers());
    
        target.getWorld().playSound(targetLocation, Sound.ENDERMAN_TELEPORT, 2.0F, 0.5F);
        WrapperPlayServerWorldParticles particles2 = ParticleGenerator.createParticle(EnumWrappers.Particle.PORTAL, 2);
        particles2.setLocation(targetLocation);
        PacketUtil.asyncSend(particles2, getPlayers());

        player.getLocation().setPitch((targetLocation.getPitch()));
        player.getLocation().setYaw(targetLocation.getYaw());

        target.getLocation().setPitch((playerLocation.getPitch()));
        target.getLocation().setYaw(playerLocation.getYaw());

        // Teleport players to their swapped locations
        player.teleport(targetLocation);
        target.teleport(playerLocation);

    }
    
    
    
    
    
}
