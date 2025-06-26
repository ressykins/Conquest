package me.raindance.champions.kits.skills.druid;

import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.item.ItemManipulationManager;
import com.podcrash.api.time.resources.TimeResource;
import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.TogglePassive;
import org.bukkit.Bukkit;
import org.bukkit.GrassSpecies;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.LongGrass;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@SkillMetadata(id = 205, skillType = SkillType.Druid, invType = InvType.DROP)
public class Nurture extends TogglePassive implements IEnergy, TimeResource {
    private int energyUsage = 20;
    private int counter = 0;
    private double radius = 7;
    private Set<Player> currentlyBuffed = new HashSet<>();

    @Override
    public void toggle() {
        run(1, 0);
    }

    @Override
    public String getName() {
        return "Nurture";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public int getEnergyUsage() {
        return energyUsage;
    }

    @Override
    public void task() {

        useEnergy(getEnergyUsageTicks());
        counter++;
        if (counter == 25) {
            getGame().consumeBukkitPlayer(this::buff); //Apply regen every 25 ticks
            counter = 0;
        }
        if (counter % 2 == 0) {
            spawnGrass(getPlayer().getLocation());
        }
    }

    private void buff(Player victim) {
        if(victim != getPlayer() && !isAlly(victim)) return;
        if(victim.getLocation().distanceSquared(getPlayer().getLocation()) > Math.pow(radius, 2)) {
            if (currentlyBuffed.contains(victim)) {
                currentlyBuffed.remove(victim);
                StatusApplier.getOrNew(victim).removeStatus(Status.HEALTH_BOOST);
            }
            return;
        }

        StatusApplier.getOrNew(victim).applyStatus(Status.REGENERATION, 1.25f, 0, false, true);
        StatusApplier.getOrNew(victim).applyStatus(Status.HEALTH_BOOST, Integer.MAX_VALUE, 0, false, false);
        currentlyBuffed.add(victim);
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

        for (Player buffed : currentlyBuffed) {
            StatusApplier.getOrNew(buffed).removeStatus(Status.HEALTH_BOOST);
        }
        currentlyBuffed.clear();
    }

    private void spawnGrass(Location location) {
        Random random = new Random();
        float randomizer = 0.1F * random.nextFloat();
        org.bukkit.util.Vector up = new Vector(0, 0.34, 0);
        up.setX(randomizer);
        up.setZ(randomizer);
        LongGrass data = new LongGrass();
        data.setSpecies(GrassSpecies.NORMAL);
        Item item = ItemManipulationManager.regular(data, location, up);
        item.setCustomName("RITB");
        ItemMeta meta = item.getItemStack().getItemMeta();
        meta.setDisplayName(getName() + item.getEntityId());
        item.getItemStack().setItemMeta(meta);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.instance, item::remove, 10);
    }
}
