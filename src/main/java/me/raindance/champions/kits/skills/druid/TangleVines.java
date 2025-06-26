package me.raindance.champions.kits.skills.druid;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.item.ItemManipulationManager;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.EntityUtil;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.util.VectorUtil;
import com.podcrash.api.world.BlockUtil;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Instant;
import net.jafama.FastMath;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

//TODO: path finding algorithm (bruh)
@SkillMetadata(id = 208, skillType = SkillType.Druid, invType = InvType.SWORD)
public class TangleVines extends Instant implements TimeResource, IEnergy, ICooldown {
    private Location currentPointer;
    private boolean usage;
    //temp variable to avoid recreation of lists.
    //TODO: if it doesn't loop anymore, delete this.
    private List<Player> players;

    private int radius = 4;
    private double damage = 8;
    private double speedFactor = 0.35;
    private float rootDuration = 2.0F;


    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if(usage || !rightClickCheck(action) || onCooldown()) return;
        if(!EntityUtil.onGround(getPlayer())) {
            getPlayer().sendMessage(getMustAirborneMessage());
            return;
        }
        usage = true;
        currentPointer = BlockUtil.getHighestUnderneath(getPlayer().getLocation());
        players = getPlayers();
        getPlayer().sendMessage(getUsedMessage());
        run(1, 0);
    }


    private void produceParticles(Location currentLocation) {
        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(currentLocation.toVector(), EnumWrappers.Particle.BLOCK_CRACK, new int[]{Material.VINE.getId()}, 30, 1/8F, 1F, 1/8F);
        PacketUtil.asyncSend(particle, players);
        getPlayer().getWorld().playSound(currentLocation, Sound.STEP_STONE, 0.9f, 1f);
    }

    private void movePointer(Location currentPointer) {
        Location crosshairView = getPlayer().getTargetBlock((HashSet<Byte>) null, 25).getLocation();
        //pseudo correction, just in case the pointer tries to go in air.
        if(crosshairView.getBlock().getType() == Material.AIR) {
            BlockUtil.getHighestUnderneath(crosshairView);
        }

        //find initial direction to go to.
        Vector direction = VectorUtil.fromAtoB(currentPointer, crosshairView).normalize();
        //SLOOOOOWWWW DOWN
        direction.multiply(speedFactor);

        currentPointer.add(direction);
        Block eval = currentPointer.getBlock();
        Material evalType = eval.getType();
        if(!locationIsValidForRupture(eval, evalType))
            currentPointer.subtract(direction);

    }

    private boolean locationIsValidForRupture(Block eval, Material evalType) {
        //if the suspected direction goes some form of water or air, cancel
        switch (evalType) {
            case STATIONARY_WATER:
            case WATER:
            case AIR:
                return false;
            default:
                break;
        }
        //if the something can't pass through the block, then cancel
        return !BlockUtil.isPassable(eval);
    }

    private void spawnVines(Location location) {
        final Random random = new Random();
        double yoffset = 0.2;
        int size = 8;
        int section = size / 4;
        final String NAME = getName() + getPlayer().getName();
        Set<Integer> items = new HashSet<>(size, .99F);
        Vector vector = new Vector(0D, yoffset * section/size, 0D);
        location = location.clone().add(0, 0.15, 0);
        for(int i = 0; i < size; i++){
            Item item = ItemManipulationManager.regular(Material.VINE, location, vector.setX(vector.getY() * FastMath.sin(i)).setZ(vector.getY() * FastMath.cos(i)));
            item.setCustomName("RITB");
            ItemStack itemStack = item.getItemStack();
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(NAME + (random.nextDouble() * System.currentTimeMillis()));
            itemStack.setItemMeta(meta);
            items.add(item.getEntityId());
            if(i > section) {
                section += size/4;
                vector.setY(yoffset * section/size);
            }
        }

        final World world = location.getWorld();
        TimeHandler.delayTime(150, () ->
            world.getEntities().forEach(entity -> {
                if(items.contains(entity.getEntityId()))
                    entity.remove();
            })
        );
    }

    private void sendSpecialEffects(Location currentPointer) {
        SoundPlayer.sendSound(currentPointer, "dig.wood", 1.2F, 89);

        Vector up = currentPointer.toVector().add(new Vector(0, 1.5D, 0));
        AbstractPacket packet = ParticleGenerator.createBlockEffect(up, Material.VINE.getId());
        PacketUtil.asyncSend(packet, players);
    }
    private void explode() {
        spawnVines(currentPointer);
        sendSpecialEffects(currentPointer);

        for (Player player : BlockUtil.getPlayersInArea(currentPointer, radius, getPlayers())) {
            if(isAlly(player) || player == getPlayer()) continue;
            StatusApplier.getOrNew(player).applyStatus(Status.ROOTED, rootDuration, 0);
            DamageApplier.damage(player, getPlayer(), damage, this, false);
        }

        ParticleGenerator.generateRangeParticles(currentPointer, radius, true, radius);
    }


    @Override
    public void task() {
        useEnergy(getEnergyUsageTicks());
        produceParticles(currentPointer);
        movePointer(currentPointer);
    }

    @Override
    public boolean cancel() {
        return !hasEnergy(getEnergyUsageTicks()) || !getPlayer().isBlocking();
    }

    @Override
    public void cleanup() {
        if(!hasEnergy(getEnergyUsageTicks()))
            getPlayer().sendMessage(getNoEnergyMessage());

        explode();
        //clear the variables to save some space
        players = null;
        currentPointer = null;

        //cooldown
        setLastUsed(System.currentTimeMillis());
        usage = false;
    }

    @Override
    public float getCooldown() {
        return 6;
    }

    @Override
    public int getEnergyUsage() {
        return 30;
    }

    @Override
    public String getName() {
        return "Tangle Vines";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }
}
