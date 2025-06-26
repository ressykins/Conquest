package me.raindance.champions.kits.skills.vanguard;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.util.VectorUtil;
import me.raindance.champions.Main;
import com.podcrash.api.effect.particle.ParticleGenerator;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.skilltypes.ChargeUp;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.util.SkillTitleSender;
import com.podcrash.api.util.TitleSender;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@SkillMetadata(id = 809, skillType = SkillType.Vanguard, invType = InvType.SWORD)
public class Whirlwind extends ChargeUp implements IConstruct {
    private int distance;
    private int distanceSquared;
    private int maxDamage;
    private float multiplier;
    private final static double[][] pleaseLoad = new double[60][2];
    
    public Whirlwind() {
        this.distance = 10;
        this.distanceSquared = distance * distance;
        this.maxDamage = 9;
        this.multiplier = 2.7F;
    }


    @Override
    public float getCooldown() {
        return 14;
    }

    @Override
    public String getName() {
        return "Whirlwind";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }

    @Override
    public float getRate() {
        return 1f / 20f;
    }

    @Override
    public void task() {
        charge();
        isUsing = true;
        WrappedChatComponent progress = SkillTitleSender.chargeUpProgressBar(this, this.getCharge());
        if(getCharge() < 1f) SoundPlayer.sendSound(this.getPlayer(), "note.harp", 0.75f, (int)(130 * getCharge()) );

        Random rand = new Random();
        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
                EnumWrappers.Particle.FIREWORKS_SPARK, 5,
                rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
        getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));

        TitleSender.sendTitle(this.getPlayer(), progress);
    }

    public void release() {
        Location center = getPlayer().getLocation();

        spiral(center);
        for(Player player : getPlayers()) {
            if (player == getPlayer() || isAlly(player)) continue;

            double diff = center.distanceSquared(player.getLocation());
            double pullDistance = Math.max(16, (distance * getCharge()) * (distance * getCharge()));
            if (diff > pullDistance) continue;

            whirlwind(player, diff);
        }
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
                        EnumWrappers.Particle.FIREWORKS_SPARK, 4,
                        0.05F, 0.35F, 0.05F);
                playerLocation.subtract(vector);
                PacketUtil.syncSend(particle, getPlayers());
            }
        });

    }

    // @Override
    // protected void doSkill(PlayerEvent event, Action action) {
    //     if(!rightClickCheck(action)) return;
    //     if(onCooldown()) return;

    //     Location center = getPlayer().getLocation();
    //     setLastUsed(System.currentTimeMillis());

    //     spiral(center);
    //     for(Player player : getPlayers()) {
    //         if (player == getPlayer() || isAlly(player)) continue;

    //         double diff = center.distanceSquared(player.getLocation());
    //         if (diff > distanceSquared) continue;

    //         whirlwind(player, diff);
    //     }

    //     getPlayer().sendMessage(getUsedMessage());
    // }

    private void whirlwind(LivingEntity player, double dist) {
        Vector toCenter = VectorUtil.fromAtoB(player.getLocation(), getPlayer().getLocation()).normalize();
        double percentage = (dist / distanceSquared) + 0.5;
        if(percentage > 1) percentage = 1D;
        toCenter.multiply(percentage * 1.25).setY(0.24D);

        player.setVelocity(toCenter);
        DamageApplier.damage(player, getPlayer(), Math.max(maxDamage * getCharge(), 3), this, false);
    }
}
