package me.raindance.champions.kits.skills.sorcerer;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.sound.SoundPlayer;

import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.TogglePassive;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.world.BlockUtil;
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

@SkillMetadata(id = 1008, skillType = SkillType.Sorcerer, invType = InvType.DROP)
public class Immolate extends TogglePassive implements IEnergy, TimeResource, IConstruct {
    // private final int MAX_LEVEL = 1;
    // private final Vector up = new Vector(0, 0.34, 0);
    private int energy = 15;
    private int radius = 5;
    // private String NAME;
    private final Random random = new Random();

    // private byte a = 0;
    private int i = 0;
    public Immolate(){}

    @Override
    public void afterConstruction() {
        // NAME = (getPlayer() == null) ? null : getPlayer().getName() + getName();
    }

    @Override
    public int getEnergyUsage() {
        return energy;
    }

    @Override
    public String getName() {
        return "Immolate";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public void toggle() {
        run(1, 0);
    }

    private void buff(Location location) {
        StatusApplier playerApplier = StatusApplier.getOrNew(getPlayer());
        if(getPlayer().getFireTicks() > 0)
            playerApplier.removeStatus(Status.FIRE);
        // playerApplier.applyStatus(Status.SPEED, 0.5f, 0);

        List<Player> players = BlockUtil.getPlayersInArea(location, radius, getPlayers());
        for(Player p : players) {
            if(!isAlly(p) || p != getPlayer()) {
                if(StatusApplier.getOrNew(p).has(Status.FIRE)) {
                    StatusApplier.getOrNew(p).applyStatus(Status.MARKED, 1, 0, false, true);
                }
            }
            else {
                StatusApplier.getOrNew(p).applyStatus(Status.FIRE_RESISTANCE, 1, 4, false, true);
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
                    WrapperPlayServerWorldParticles snow = ParticleGenerator.createParticle(up.toVector(), EnumWrappers.Particle.FLAME, 1,
                            random.nextFloat() * 0.5F, 2.75F, random.nextFloat() * 0.5F);
                    PacketUtil.syncSend(snow, getPlayers());
                }
            }
        }
        i++;

    }

    // private void spawnFire(Location location) {
    //     a++;
    //     if(a % 2 != 0) return;
    //     a = 0;
    //     Random random = new Random();
    //     float randomizer = 0.1F * random.nextFloat();
    //     up.setX(randomizer);
    //     up.setZ(randomizer);
    //     Item item = ItemManipulationManager.regular(Material.BLAZE_POWDER, location, up);
    //     item.setCustomName("RITB");
    //     ItemMeta meta = item.getItemStack().getItemMeta();
    //     meta.setDisplayName(NAME + item.getEntityId());
    //     item.getItemStack().setItemMeta(meta);
    //     Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, item::remove, 25);
    // }

    @Override
    public void task() {
        Location location = getPlayer().getLocation();

        location.getWorld().playSound(location, Sound.FIRE, 0.1f, 1f);
        useEnergy(getEnergyUsageTicks());

        // spawnFire(location);
        buff(location);
    }

    @Override
    public boolean cancel() {
        return !isToggled() || !hasEnergy(getEnergyUsageTicks()) || isInWater();
    }
    @Override
    public void cleanup() {

        if(!hasEnergy(getEnergyUsageTicks())) {
            forceToggle();
        }
    }

    @EventHandler(priority =  EventPriority.HIGH)
    public void damage(DamageApplyEvent event) {
        if(!isToggled()) return;
        if(event.getVictim().getFireTicks() <= 0) return;

        Location playerLocation = getPlayer().getLocation();
        Location enemyLocation = event.getVictim().getLocation();

        if (playerLocation.distance(enemyLocation) <= radius) {
            event.addSource(this);
            event.setModified(true);
            // event.setDamage(event.getDamage() * 1.3);
            SoundPlayer.sendSound(getPlayer().getLocation(), "random.fizz", 0.8F, 63);
        }
    }

    // @EventHandler(priority = EventPriority.HIGHEST)
    // public void pickUp(PlayerPickupItemEvent event) {
    //     if(event.getItem().getItemStack().getType() != Material.BLAZE_POWDER) return;
    //     if(event.getItem().getItemStack().getItemMeta() == null ||
    //             !event.getItem().getItemStack().getItemMeta().getDisplayName().contains(NAME)) return;
    //     Player victim = event.getPlayer();
    //     if(victim == getPlayer() || isAlly(victim)) return;
    //     StatusApplier.getOrNew(victim).applyStatus(Status.FIRE, 0.75F, 1);
    //     event.getItem().remove();
    // }

}
