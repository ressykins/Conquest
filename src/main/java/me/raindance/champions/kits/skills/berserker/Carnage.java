package me.raindance.champions.kits.skills.berserker;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.DamageApplier;
import me.raindance.champions.Main;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;



import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.util.Vector;
import org.bukkit.event.block.Action;

@SkillMetadata(id = 107, skillType = SkillType.Berserker, invType = InvType.AXE)
public class Carnage extends Instant implements ICooldown, IConstruct {
    private int distance;
    private int distanceSquared;
    private int maxDamage;
    private final static double[][] pleaseLoad = new double[60][2];
    
    public Carnage() {
        this.distance = 5;
        this.distanceSquared = distance * distance;
        this.maxDamage = 7;
        // this.multiplier = 2.7F;
    }


    @Override
    public float getCooldown() {
        return 10;
    }

    @Override
    public String getName() {
        return "Carnage";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }


    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if(!rightClickCheck(action)) return;
        if(onCooldown()) return;

        Location center = getPlayer().getLocation();
        setLastUsed(System.currentTimeMillis());

        spiral(center);
        for(Player player : getPlayers()) {
            if (player == getPlayer() || isAlly(player)) continue;

            double diff = center.distanceSquared(player.getLocation());
            if (diff > distanceSquared) continue;

            whirlwind(player, diff);
        }

        getPlayer().sendMessage(getUsedMessage());
    }

    @Override
    public void afterConstruction() {
        if(pleaseLoad[4][1] == 0 && pleaseLoad[32][0] == 0) {
            final double pp = (6D * Math.PI);
            int length = pleaseLoad.length;
            final double add = pp/length;
            for(int i = 0; i < length; i++) {
                double theta = (i * add);
                theta = (theta/pp) * distance;
                pleaseLoad[i][0] = theta * (float) Math.cos(i);
                pleaseLoad[i][1] = theta * (float) Math.sin(i);
            }

        }
    }

    private void spiral(Location playerLocation) {
        new TimeResource() {
            private int a = 0;
            @Override
            public void task() {
                for(int i = a; i < a + 7; i++) {
                    playerLocation.getWorld().playSound(playerLocation, Sound.STEP_WOOL, 2f, 1f + (float) ((i/10D % (Math.PI / 2d)) / (Math.PI / 2)));
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
            for(int i = 0; i < pleaseLoad.length; i++) {
                double x = pleaseLoad[i][0];
                double z = pleaseLoad[i][1];
                Vector vector = new Vector(x, 0, z);
                playerLocation.add(vector);
                WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(playerLocation.toVector(),
                        EnumWrappers.Particle.REDSTONE, 4,
                        0.05F, 0.35F, 0.05F);
                playerLocation.subtract(vector);
                PacketUtil.syncSend(particle, getPlayers());
            }
        });

    }

    private void whirlwind(LivingEntity player, double dist) {
        DamageApplier.damage(player, getPlayer(), Math.max(maxDamage, 3), this, false);
        getChampionsPlayer().heal(3);
        if (getChampionsPlayer().getEnergyBar().getEnergy() >= 4) {
            StatusApplier.getOrNew(player).applyStatus(Status.BLEED, 4, 1);
            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ANVIL_LAND, 1f, 1);
            AbstractPacket packet = ParticleGenerator.createBlockEffect(player.getLocation(), Material.REDSTONE.getId());
            PacketUtil.asyncSend(packet, getPlayers());
        }
    }
}
