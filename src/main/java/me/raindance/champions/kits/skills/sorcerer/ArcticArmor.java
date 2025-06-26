package me.raindance.champions.kits.skills.sorcerer;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.Cause;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.TogglePassive;
import com.podcrash.api.time.resources.BlockBreakThenRestore;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.world.BlockUtil;
import com.podcrash.api.world.CraftBlockUpdater;
import net.jafama.FastMath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;
import java.util.Set;

@SkillMetadata(id = 1006, skillType = SkillType.Sorcerer, invType = InvType.DROP)
public class ArcticArmor extends TogglePassive implements IEnergy, TimeResource {
    private final int energeUsage = 20;
    private final int radius = 5;
    private final double radiusSquared = FastMath.pow(radius, 2);
    private final Random random = new Random();

    private int i = 0;
    public ArcticArmor() {}

    @Override
    public String getName() {
        return "Arctic Armor";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public int getEnergyUsage() {
        return energeUsage;
    }


    @Override
    public void toggle() {
        run(1, 0);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void hit(DamageApplyEvent event) {
        if(event.isCancelled()) return;
        if(event.getVictim() != getPlayer()) return;
        if(event.getCause() != Cause.MELEE && event.getCause() != Cause.PROJECTILE) return;
        if(event.getCause() == Cause.MELEE) {
            StatusApplier.getOrNew(event.getAttacker()).applyStatus(Status.SLOW, 3, 0);
        } 
    }

    private void protect() {
        Location location = getPlayer().getLocation();
        location.getWorld().playSound(location, Sound.AMBIENCE_RAIN, 0.3f, 0f);
        Iterable<Block> waterBlocks = BlockUtil.getSpecificWithinRange(location, radius, Material.WATER, Material.STATIONARY_WATER, Material.ICE);

        List<BlockBreakThenRestore> restores = CraftBlockUpdater.getMassBlockUpdater(location.getWorld()).getRestores();
        for (Block block : waterBlocks) {
            if(block.getY() < location.getBlockY()) {
                if(block.getType() != Material.ICE) {
                    BlockUtil.restoreAfterBreak(block.getLocation(), Material.ICE, (byte) 0, 4);
                } else {
                    for(BlockBreakThenRestore restore : restores) {
                        Location restoreLoc = restore.getLocation();
                        if(block.getX() == restoreLoc.getBlockX() && block.getZ() == restoreLoc.getZ()) restore.setDuration(4);
                    }
                }
            }

        }

        for(Player player : getPlayers()) {
            StatusApplier applier = StatusApplier.getOrNew(player);
            if(player == getPlayer()) applier.applyStatus(Status.RESISTANCE, 0.5F, 0, true, true);
            else if(isAlly(player) && player.getLocation().distanceSquared(getPlayer().getLocation()) <= this.radiusSquared) {
                applier.applyStatus(Status.RESISTANCE, 0.5F, 1, true);
            }
        }

        if (i == 2) {
            i = 0;
            return;
        }
        Set<Vector> vectors = BlockUtil.getOuterBlocksWithinRange(location, radius, false);
        double currentY = location.getY();
        double minus = currentY - 1;
        for (Vector vector : vectors) {
            Location up = vector.toLocation(location.getWorld());
            Block current = up.getBlock();
            if(current.getRelative(BlockFace.UP).getType() == Material.AIR && current.getRelative(BlockFace.DOWN).getType() != Material.AIR) {
                if(minus < current.getY() && current.getY() <= currentY){
                    WrapperPlayServerWorldParticles snow = ParticleGenerator.createParticle(up.toVector(), EnumWrappers.Particle.SNOW_SHOVEL, 1,
                            random.nextFloat() * 0.5F, 2.75F, random.nextFloat() * 0.5F);
                    PacketUtil.syncSend(snow, getPlayers());
                }
            }
        }
        i++;
    }

    @Override
    public void task() {
        useEnergy(getEnergyUsageTicks());
        protect();
    }

    @Override
    public boolean cancel() {
        return !isToggled() || !hasEnergy(getEnergyUsageTicks());
    }

    @Override
    public void cleanup() {
        if(!hasEnergy(getEnergyUsageTicks())) {
            forceToggle();
        }
    }
}
