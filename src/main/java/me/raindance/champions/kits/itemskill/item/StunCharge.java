package me.raindance.champions.kits.itemskill.item;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.item.ItemManipulationManager;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.kits.annotation.ItemMetaData;
import me.raindance.champions.kits.itemskill.TrapItem;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import java.util.List;

//when there's a better item system, change this
@ItemMetaData(mat = Material.REDSTONE_LAMP_OFF, actions = {Action.RIGHT_CLICK_AIR, Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK, Action.RIGHT_CLICK_BLOCK})
public class StunCharge extends TrapItem {
    public StunCharge() {
        super(2500);
        despawnDelay = 40 * 1000;
    }
    @Override
    public Item throwItem(Player player, Action action) {
        Location location = player.getEyeLocation();
        Vector direction = player.getLocation().getDirection();
        Vector vector = new Vector(0, 0, 0);
        if(isLeft(action)) vector = throwVector(direction);

        return ItemManipulationManager.regular(Material.REDSTONE_LAMP_OFF, location, vector);
    }

    @Override
    public void primeTrap(Item item) {
        AbstractPacket packet2 = ParticleGenerator.createBlockEffect(item.getLocation().toVector(), Material.OBSIDIAN.getId());
        for(Player p : item.getWorld().getPlayers()) {
            packet2.sendPacket(p);
        }
        SoundPlayer.sendSound(item.getLocation(), "dig.stone", 1.25F, 66);
    }

    @Override
    public void snareTrap(Player owner, Player player, Item item) {
        Location land = item.getLocation();
        World world = land.getWorld();
        world.strikeLightningEffect(land);
        List<LivingEntity> entities = world.getLivingEntities();
        for (LivingEntity entity : entities) {
            if (entity.getLocation().distanceSquared(land) > 4D) continue;
            StatusApplier.getOrNew(entity).applyStatus(Status.SILENCE, 4, 0);
            StatusApplier.getOrNew(entity).applyStatus(Status.SHOCK, 4, 0);
            StatusApplier.getOrNew(entity).applyStatus(Status.GROUND, 4, 0);
        }
    }

    @Override
    public String getName() {
        return "Stun Charge";
    }
}
