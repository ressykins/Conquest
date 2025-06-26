package me.raindance.champions.kits.skills.berserker;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.DeathApplyEvent;
import me.raindance.champions.Main;
import com.podcrash.api.effect.particle.ParticleGenerator;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.resources.EntityParticleResource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SkillMetadata(id = 103, skillType = SkillType.Berserker, invType = InvType.PRIMARY_PASSIVE)
public class Bloodlust extends Passive {
    private int duration;
    // private double healAmount = 4;

    private PotionEffect strength, speed;
    private Bloodlust instance;
    private BloodlustParticleResource resource;
    private long current;

    public Bloodlust() {
        super();
        this.instance = this;
        this.duration = 8;
        this.strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration * 20, 0);
    }

    @Override
    public String getName() {
        return "Bloodlust";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    private class BloodlustParticleResource extends EntityParticleResource {
        public BloodlustParticleResource() {
            super(instance.getPlayer(), ParticleGenerator.createParticle(EnumWrappers.Particle.REDSTONE, 2), null);
        }

        @Override
        public boolean cancel() {
            return System.currentTimeMillis() - getLastUsed() >= duration * 1000L || getPlayer().isDead();
        }
    }

    @EventHandler(
            priority = EventPriority.NORMAL
    )
    public void kill(DeathApplyEvent e) {
        //check if the player who died isn't nothing
        //check if the player who killed was this player, and if this player was not the victim as well (/kill)
        //If this player isn't dead to prevent bs ressurection tactics
        if (e.getPlayer() != null && (e.getAttacker() == getPlayer() && e.getPlayer() != getPlayer())  && !getPlayer().isDead()) {
            int potencySpeed = -1;
            for (PotionEffect potion : getPlayer().getActivePotionEffects()) {
                if (potion.getType().equals(PotionEffectType.SPEED)) {
                    potencySpeed = potion.getAmplifier();
                }
            }

            int speedLevel = potencySpeed + 1;
            if (speedLevel > 2) speedLevel = 2;
            this.speed = new PotionEffect(PotionEffectType.SPEED, duration * 20, speedLevel);

            KitPlayer berserker = KitPlayerManager.getInstance().getKitPlayer(getPlayer());
            // Find the current percentage of health remaining, then multiply it by our real max HP value (e.g. 40 for zerk right now)
            double trueCurrentHP = (getPlayer().getHealth() / getPlayer().getMaxHealth()) * berserker.getHP();
            double trueMissingHP = berserker.getHP() - trueCurrentHP;
            double heal = trueMissingHP * 0.15;

            getChampionsPlayer().heal(heal);
            Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                boolean a = getPlayer().addPotionEffect(strength, true);
                boolean b = getPlayer().addPotionEffect(speed, true);
                if (a && b) {
                    getPlayer().sendMessage(String.format("%sBrute> %sYou gained Bloodlust.", ChatColor.BLUE, ChatColor.GRAY));
                    current = System.currentTimeMillis();
                }
            }, 1L);
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.zombiepig.zpigangry", 1f, 110);
            this.setLastUsed(System.currentTimeMillis());
            resource.runAsync(1,1);
        }
    }

    @EventHandler
    public void damage(DamageApplyEvent event) {
        if (event.isCancelled()) return;
        if (event.getAttacker() == getPlayer() && (event.getCause() == Cause.MELEE || event.getCause() == Cause.MELEESKILL)) {
            if (System.currentTimeMillis() - current <= duration * 1000L) event.addSource(this);
        }
    }

    @Override
    public void setPlayer(Player player) {
        super.setPlayer(player);
        resource = new BloodlustParticleResource();
    }
}
