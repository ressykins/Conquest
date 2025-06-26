package me.raindance.champions.kits.skills.sorcerer;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.skill.SkillRechargeEvent;
import com.podcrash.api.events.skill.SkillUseEvent;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.util.SkillTitleSender;
import com.podcrash.api.util.TitleSender;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.mob.CustomEntityFirework;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.world.BlockUtil;
import com.podcrash.api.kits.skilltypes.ChargeUp;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

import static com.podcrash.api.world.BlockUtil.*;

@SkillMetadata(id = 1001, skillType = SkillType.Sorcerer, invType = InvType.SWORD)
public class ShadowBeam extends ChargeUp implements IEnergy, IConstruct {
    private double damage = 15;
    private double range = 60;
    private int energyUsage = 80;
    private FireworkEffect firework;

    private double detectionRadius = 1.5;
    private int damageRadius = 2;

    public ShadowBeam() {}

    @Override
    public float getRate() {
        return 0.333f / 20f;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }

    @Override
    public float getCooldown() {
        return 10;
    }

    @Override
    public String getName() {
        return "Shadow Beam";
    }

    public int getEnergyUsage() {
        return energyUsage;
    }
    @Override
    public void afterConstruction() {
        this.firework = FireworkEffect.builder()
                .withColor(Color.BLACK)
                .with(FireworkEffect.Type.BALL_LARGE)
                .build();

    }

    // @Override
    // protected void doSkill(PlayerEvent event, Action action) {
    //     if(onCooldown() || !rightClickCheck(action)) return;
    //     if(!hasEnergy()) {
    //         getPlayer().sendMessage(getNoEnergyMessage());
    //         return;
    //     }
    //     useEnergy();
    //     setLastUsed(System.currentTimeMillis());
    //     release();

    //     getPlayer().sendMessage(getUsedMessage());
    // }

    @EventHandler
    public void recharge(SkillRechargeEvent e) {
        if(e.getSkillName().equalsIgnoreCase(this.getName()) && getPlayer().isBlocking()) {
            if(isInWater()) {
                getPlayer().sendMessage(getWaterMessage());
                return;
            }
            if(!onCooldown()) {
                if(!hasEnergy()) {
                    getPlayer().sendMessage(getNoEnergyMessage());
                    return;
                }
                SkillUseEvent useEvent = new SkillUseEvent(this);
                Bukkit.getPluginManager().callEvent(useEvent);
                if(useEvent.isCancelled()) return;
                preTask(); // Run pre-task logic before TimeHandler task
                TimeHandler.repeatedTime(1, 0, this);
            }
        }
    }

    @Override
    @EventHandler(
            priority = EventPriority.HIGH
    )
    public void block(PlayerInteractEvent e){
        if(e.getPlayer() == this.getPlayer()){
            if(rightClickCheck(e.getAction()) && isHolding()){
                if(isInWater()) {
                    getPlayer().sendMessage(getWaterMessage());
                    return;
                }
                if(!onCooldown()) {
                    if(!hasEnergy()) {
                        getPlayer().sendMessage(getNoEnergyMessage());
                        return;
                    }
                    useEnergy();
                    SkillUseEvent useEvent = new SkillUseEvent(this);
                    Bukkit.getPluginManager().callEvent(useEvent);
                    if(useEvent.isCancelled()) return;
                    TimeHandler.repeatedTime(1, 0, this);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void damage(EntityDamageEvent e) {
        if(e.isCancelled()) return;
        if (e.getEntity() == this.getPlayer() && isUsing) {
            resetWhenDamaged();
        }
    }

    private void resetWhenDamaged() {
        getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ZOMBIE_REMEDY, 0.5f, 1);
        resetCharge();
    }


    @Override
    public void task() {
        charge();
        isUsing = true;
        WrappedChatComponent progress = SkillTitleSender.chargeUpProgressBar(this, this.getCharge());
        if(getCharge() < 1f)  SoundPlayer.sendSound(this.getPlayer(), "mob.wither.idle", 0.2f, (int)(130 * getCharge()));

        Random rand = new Random();
        WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
                EnumWrappers.Particle.SPELL_MOB, new int[]{0,0,0}, 5,
                rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
        getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));

        TitleSender.sendTitle(this.getPlayer(), progress);
    }

    public void release(){
        Location cur = getPlayer().getEyeLocation();
        Vector inc = cur.getDirection().normalize();
        cur.add(inc);
        List<Player> players = getPlayers();
        double fireRange = Math.max(6, range * getCharge());
        for(int i = 0; i < fireRange; i += 1) {
            //if the block wasn't passible, stop
            if(!isPassable(cur.getBlock())) break;

            //if a player is within the point within a sphere, then break
            if(hasPlayersInArea(cur, detectionRadius, players, getPlayer()))
                break;
            WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(cur.toVector(), EnumWrappers.Particle.SPELL_MOB, new int[]{0,0,0}, 5, 0,0,0);
            PacketUtil.asyncSend(packet, players);
            cur.add(inc);
        }
        burst(cur, players);
    }

    // Location endLoc = null;
    // for(int i = 0; i < range; i += 1) {
    //     if(isPassable(cur.getBlock())  && playerIsHere(cur, getPlayers()) == null) {
    //         WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(cur.clone().add(0, 1, 0).toVector(), EnumWrappers.Particle.FIREWORKS_SPARK, 5, 0,0,0);
    //         player.getWorld().getPlayers().forEach(p -> ParticleGenerator.generate(p, packet));
    //         cur.add(inc);
    //     } else {
    //         endLoc = cur;
    //         break;
    //     }
    // }
    // burst(endLoc);
    // resetCharge();

    /*
    private boolean hasPlayersInArea(Location location, double radius, List<Player> players) {
        double radiusSquared = radius * radius;
        for(Player player : players) {
            if(player == getPlayer() && isAlly(player)) continue;
            Location loc = player.getLocation();
            double distanceSquared = loc.distanceSquared(location);
            if(distanceSquared <= radiusSquared)
                return true;
        }
        return false;
    }

     */
    private void burst(Location endLoc, List<Player> players) {
        if (endLoc == null) return;
        CustomEntityFirework.spawn(endLoc, firework, players);
        SoundPlayer.sendSound(endLoc, "mob.zombie.remedy", 0.5F, 10);
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.zombie.remedy", 0.5F, 10);
        for (Player p : BlockUtil.getPlayersInArea(endLoc, damageRadius, players)) {
            if (isAlly(p) || p == getPlayer()) continue;
            double dealtDamage = Math.max(5, damage * getCharge());
            DamageApplier.damage(p, getPlayer(), dealtDamage, this, true);
            return;
        }
    }

    @EventHandler
    public void damage(DamageApplyEvent event) {
        if(!event.containsSource(this)) return; //this time only run when it contains it...
        event.setVelocityModifierX(event.getVelocityModifierX() * 0.6);
        event.setVelocityModifierY(event.getVelocityModifierY() * 0.6);
        event.setVelocityModifierZ(event.getVelocityModifierZ() * 0.6);
    }
}
