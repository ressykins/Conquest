package me.raindance.champions.kits.itemskill.item;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.damage.DamageSource;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.item.ItemManipulationManager;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.kits.annotation.ItemMetaData;
import com.podcrash.api.plugin.PodcrashSpigot;

import me.raindance.champions.kits.itemskill.TrapItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import java.util.List;

//when there's a better item system, change this
@ItemMetaData(mat = Material.STONE_PLATE, actions = {Action.RIGHT_CLICK_AIR, Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK})
public class BearTrap extends TrapItem {
    public BearTrap() {
        super(2500);
        despawnDelay = 40 * 1000;
    }
    @Override
    public Item throwItem(Player player, Action action) {
        Location location = player.getEyeLocation();
        Vector direction = player.getLocation().getDirection();
        Vector vector = new Vector(0, 0, 0);
        if(isLeft(action)) vector = throwVector(direction);
        return ItemManipulationManager.regular(Material.STONE_PLATE, location, vector);
    }

    @Override
    public void primeTrap(Item item) {
        AbstractPacket packet2 = ParticleGenerator.createBlockEffect(item.getLocation().toVector(), Material.OBSIDIAN.getId());
        for(Player p : item.getWorld().getPlayers()) {
            packet2.sendPacket(p);
        }
    }

    @Override
    public void snareTrap(Player owner, Player player, Item item) {
        Location land = item.getLocation();

        World world = land.getWorld();
        List<LivingEntity> entities = world.getLivingEntities();
        for (LivingEntity entity : entities) {
            if (entity.getLocation().add(0, 1, 0).distanceSquared(land) > 5.5D) continue;
            if (entity == owner) continue;
            // DamageApplier.damage(entity, owner, 3, this, false);
            StatusApplier.getOrNew(entity).applyStatus(Status.ROOTED, 2, 0, false, false);
        }


        PodcrashSpigot.getInstance().getLogger().info("triggered snareTrap, rooted player");
        SoundPlayer.sendSound(owner, "random.door_close", 1F, 66);
        SoundPlayer.sendSound(land, "random.door_close", 1F, 66);
    }

    @Override
    public String getName() {
        return "Bear Trap";
    }
}
