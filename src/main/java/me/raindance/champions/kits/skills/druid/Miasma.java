package me.raindance.champions.kits.skills.druid;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;
import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.kits.skilltypes.TogglePassive;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.util.Vector;

import java.util.Random;

@SkillMetadata(id = 203, skillType = SkillType.Druid, invType = InvType.DROP)
public class Miasma extends TogglePassive implements IEnergy, TimeResource, IConstruct {
    private int energyUsage = 30;
    private final double[][] pleaseLoad = new double[60][2];
    private double radius = 7;
    private int i = 0;

    // @Override
    // protected void doSkill(PlayerEvent event, Action action) {
    //     if(!rightClickCheck(action) || onCooldown() || !hasEnergy()) return;
    //     useEnergy();
    //     setLastUsed(System.currentTimeMillis());

    //     spiral(getPlayer().getLocation());
    //     getGame().consumeBukkitPlayer(this::use);

    //     getPlayer().sendMessage(getUsedMessage());
    // }


    @Override
    public void afterConstruction() {
        if(pleaseLoad[4][1] == 0 && pleaseLoad[32][0] == 0) {
            final double pp = (6D * Math.PI);
            int length = pleaseLoad.length;
            final double add = pp/length;
            for(int i = 0; i < length; i++) {
                double theta = (i * add);
                theta = (theta/pp) * radius;
                pleaseLoad[i][0] = theta * (float) Math.cos(i);
                pleaseLoad[i][1] = theta * (float) Math.sin(i);
            }

        }
    }

    @Override
    public void toggle() {
        run(1, 0);
    }

    @Override
    public void task() {
        i++;
        Location location = getPlayer().getLocation();

        useEnergy(getEnergyUsageTicks());

        spiral(location);
        getGame().consumeBukkitPlayer(this::use);
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

    private void use(Player victim) {
        if(victim == getPlayer() || isAlly(victim)) return;
        if(victim.getLocation().distanceSquared(getPlayer().getLocation()) > (radius * radius)) return;

        StatusApplier.getOrNew(victim).applyStatus(Status.POISON, 3, 1, true);
        StatusApplier.getOrNew(victim).applyStatus(Status.DIZZY, 3, 1, false);

    }

    private void spiral(Location playerLocation) {
        new TimeResource() {
            private int a = 0;
            @Override
            public void task() {
                for(int i = a; i < radius; i++) {
                    // playerLocation.getWorld().playSound(playerLocation, Sound.FIZZ, 1.5f, 1f + (float) ((i/10D % (Math.PI / 2d)) / (Math.PI / 2)));
                }
                a++;
            }

            @Override
            public boolean cancel() {
                return a > 10;
            }

            @Override
            public void cleanup() {

            }
        }.run(0,0);

        Bukkit.getScheduler().runTaskAsynchronously(Main.instance, () -> {
            Random random = new Random();
            for(int i = 0; i < pleaseLoad.length; i++) {
                double x = pleaseLoad[i][0];
                double z = pleaseLoad[i][1];
                Vector vector = new Vector(x, 0, z);
                playerLocation.add(vector);
                float[] data = random.nextFloat() < 0.5 ? new float[]{0F, 0F, 1} :  new float[]{-1, 1F, 0.694F};
                WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(playerLocation.toVector(),
                        EnumWrappers.Particle.REDSTONE,5,
                        data[0], data[1], data[2]);
                particle.setParticleData(1);
                playerLocation.subtract(vector);
                PacketUtil.asyncSend(particle, getPlayers());
            }
        });

    }


    @Override
    public String getName() {
        return "Miasma";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public int getEnergyUsage() {
        return energyUsage;
    }
}
