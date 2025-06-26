package me.raindance.champions.kits.skills.thief;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.location.Coordinate;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.*;
import com.podcrash.api.kits.skilltypes.Drop;
import com.podcrash.api.time.TimeHandler;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.player.PlayerDropItemEvent;

import java.util.LinkedList;

@SkillMetadata(id = 707, skillType = SkillType.Thief, invType = InvType.DROP)
public class Recall extends Drop implements ICooldown, IContinuousPassive, IPassiveTimer, IConstruct {

    private final int time;
    private LinkedList<Coordinate> locations = new LinkedList<>();

    @Override
    public float getCooldown() {
        return 13;
    }

    @Override
    public String getName() {
        return "Recall";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    public Recall() {
        time = 3;
    }

    @Override
    public void afterConstruction() {
        this.setLastUsed(0);
    }

    @Override
    public void start() {
        if (getPlayer() != null) TimeHandler.repeatedTimeSeconds(1, 0L, this);
    }

    public boolean drop(PlayerDropItemEvent e) {
        if(!onCooldown()) {
            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ZOMBIE_UNFECT, 2.0F, 2.0F);
            Location start = getPlayer().getLocation();
            recall();
            Location end = getPlayer().getLocation();
            WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(EnumWrappers.Particle.SPELL_WITCH, 3);
            ParticleGenerator.generateLocAs(packet, start, end);
            this.setLastUsed(System.currentTimeMillis());
            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ZOMBIE_UNFECT, 2.0F, 2.0F);
            getPlayer().setFallDistance(0);
            return true;
        } else this.getPlayer().sendMessage(getCooldownMessage());
        return false;
    }


    private void recall() {
        Location current = getPlayer().getLocation();
        Location newLoc = locations.get(time).toLocation(getPlayer().getWorld());
        newLoc.setPitch(current.getPitch());
        newLoc.setYaw(current.getYaw());
        getPlayer().teleport(newLoc);

        KitPlayer thief = KitPlayerManager.getInstance().getKitPlayer(getPlayer());
        // Find the current percentage of health remaining, then multiply it by our real max HP value (e.g. 40 for zerk right now)
        double trueCurrentHP = (getPlayer().getHealth() / getPlayer().getMaxHealth()) * thief.getHP();
        double trueMissingHP = thief.getHP() - trueCurrentHP;
        double heal = trueMissingHP * 0.2;

        getChampionsPlayer().heal(heal);
    }
    /*
    Record locations
     */

    @Override
    public void task() {
        if (locations.size() > time + 1) this.locations.removeLast();
        this.locations.addFirst(Coordinate.from(getPlayer().getLocation()));
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void cleanup() {
        locations = null;
    }
}
