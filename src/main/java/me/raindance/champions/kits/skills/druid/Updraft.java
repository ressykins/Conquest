package me.raindance.champions.kits.skills.druid;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;

import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.world.BlockUtil;

@SkillMetadata(id = 209, skillType = SkillType.Druid, invType = InvType.PRIMARY_PASSIVE)
public class Updraft extends Passive implements IPassiveTimer, TimeResource {
    @Override
    public String getName() {
        return "Updraft";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public void start() {
        runAsync(1,0);
    }

    @Override
    public void task() {
        if (getPlayer().isSneaking()) {
            List<Player> players = BlockUtil.getPlayersInArea(getPlayer().getLocation(), 5, getPlayers());
            for(Player p : players) {
                if(isAlly(p) || p == getPlayer()) {
                    StatusApplier.getOrNew(p).applyStatus(Status.JUMP_BOOST, 1, 3, false, false);

                    Random rand = new Random();

                    // Get the player's current location and adjust the Y-coordinate to be at their feet
                    Location playerLocation = getPlayer().getLocation().clone();
                    playerLocation.setY(playerLocation.getY() - 1);  // Position the particle 1 block below the player's feet
                    
                    // Create a cloud particle with randomized offsets
                    WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(
                            playerLocation.toVector(),
                            EnumWrappers.Particle.CLOUD, 
                            5, 
                            rand.nextFloat() / 2f, 
                            0.25f + (rand.nextFloat() - 0.15f), 
                            rand.nextFloat() / 2f
                    );
                    
                    // Send the particle to all players
                    getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));
                    
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
    
}
