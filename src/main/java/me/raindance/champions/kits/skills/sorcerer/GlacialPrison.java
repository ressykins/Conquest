package me.raindance.champions.kits.skills.sorcerer;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldEvent;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.events.ItemCollideEvent;
import com.podcrash.api.item.ItemManipulationManager;
import com.podcrash.api.util.PacketUtil;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.sound.SoundWrapper;
import com.podcrash.api.world.BlockUtil;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.*;

@SkillMetadata(id = 1004, skillType = SkillType.Sorcerer, invType = InvType.AXE)
public class GlacialPrison extends Instant implements IEnergy, ICooldown, IConstruct {
    private int currentItemID;

    private final Random random = new Random();
    private WrapperPlayServerWorldParticles particles;
    private String NAME;
    private int energy = 90;
    private int duration = 5;

    private List<Vector> tempArrayList = new ArrayList<>();

    public GlacialPrison() {
        setCanUseWhileCooldown(true);
    }

    @Override
    public void afterConstruction() {
        particles = ParticleGenerator.createParticle(null, EnumWrappers.Particle.SNOW_SHOVEL, 1, 0,0,0);
        NAME = getPlayer().getName()  + getName();
    }

    @Override
    public float getCooldown() {
        return 16;
    }

    @Override
    public String getName() {
        return "Glacial Prison";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if(!rightClickCheck(action)) return;
        if(!onCooldown()) {
            if(!hasEnergy()) {
                getPlayer().sendMessage(getNoEnergyMessage());
                return;
            }
            this.setLastUsed(System.currentTimeMillis());
            useEnergy(energy);
            launch();

            getPlayer().sendMessage(getUsedMessage());
        }else {
            //if the time elapsed is too long, cancel
            if(getCooldown() - cooldown() > duration) return;
            detomb();
        }
    }

    private void launch() {
        Location location = getPlayer().getEyeLocation();
        Vector vector = location.getDirection();
        vector.normalize().multiply(1.15D);

        Item spawnItem = ItemManipulationManager.regular(Material.ICE, location, vector);
        this.currentItemID = spawnItem.getEntityId();
        Item item = ItemManipulationManager.intercept(spawnItem, 0.5, ((item1, entity, land) -> {
            entomb(land);
            item1.getWorld().playEffect(item1.getLocation(), Effect.STEP_SOUND, 79);
            item1.remove();
        }));
        ItemMeta meta = item.getItemStack().getItemMeta();
        item.setCustomName("RITB");
        meta.setDisplayName(NAME + item.getEntityId());
        item.getItemStack().setItemMeta(meta);
        ParticleGenerator.generateEntity(item, particles, new SoundWrapper("random.fizz", 0.6F, 88));
        item.getWorld().playSound(item.getLocation(), Sound.SILVERFISH_HIT, 2f, 1f);
    }

    private void entomb(Location location) {
        Set<Vector> blocks = BlockUtil.getOuterBlocksWithinRange(location, 4, true);
        World world = location.getWorld();
        for(Vector block : blocks) {
            int totalDuration = 3 * this.duration/4 + (int) (0.5F + this.duration/4 * random.nextFloat());
            BlockUtil.restoreAfterBreak(block.toLocation(world), Material.ICE, (byte) 0, totalDuration);
            tempArrayList.add(block);
        }
    }

    private void detomb() {
        int i = 0;
        for(Vector block : tempArrayList) {
            Location blockLoc = block.toLocation(getPlayer().getWorld());
            if(blockLoc.getBlock().getType() != Material.ICE) continue;
            BlockUtil.setBlock(blockLoc, Material.AIR);
            if(i % 6 == 0) {
                WrapperPlayServerWorldEvent event = ParticleGenerator.createBlockEffect(block, Material.ICE.getId());
                PacketUtil.asyncSend(event, getPlayers());
            }
            i++;
        }
        tempArrayList.clear();
    }

    @EventHandler
    public void collideItem(ItemCollideEvent e) {
        if(e.isCancelled()) return;
        //identity check + owner of item check = cancel collision
        if(e.getCollisionVictim() == getPlayer() && e.getItem().getEntityId() == currentItemID)
            e.setCancelled(true);
    }

    @Override
    public int getEnergyUsage() {
        return energy;
    }
}
