package me.raindance.champions.kits.itemskill.item;

import com.packetwrapper.abstractpackets.WrapperPlayServerEntityStatus;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldEvent;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.kits.annotation.ItemMetaData;
import me.raindance.champions.kits.itemskill.IItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

@ItemMetaData(mat = Material.BREAD, actions = {Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK})
public class Mead implements IItem {
    @Override
    public String getName() {
        return "Bread";
    }

    @Override
    public void useItem(Player player, Action action) {
        eatBread(player);
    }

    private void eatBread(Player player) {
        StatusApplier.getOrNew(player).applyStatus(Status.STRENGTH, 5, 0, true, true);
        StatusApplier.getOrNew(player).applyStatus(Status.MARKED, 5, 0, true, true);
        Location location = player.getEyeLocation();
        SoundPlayer.sendSound(location, "random.drink", 0.75F, 88);
    }
}
