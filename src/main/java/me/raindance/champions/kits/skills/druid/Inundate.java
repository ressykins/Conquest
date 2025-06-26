package me.raindance.champions.kits.skills.druid;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.skill.SkillRechargeEvent;
import com.podcrash.api.events.skill.SkillUseEvent;
import com.podcrash.api.item.ItemManipulationManager;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.EntityUtil;
import com.podcrash.api.util.SkillTitleSender;
import com.podcrash.api.util.TitleSender;
import com.podcrash.api.world.BlockUtil;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.ChargeUp;
import com.podcrash.api.kits.skilltypes.Continuous;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.*;

@SkillMetadata(id = 210, skillType = SkillType.Druid, invType = InvType.SWORD)
public class Inundate extends Continuous implements IEnergy, IConstruct, IPassiveTimer {
    private int i = 0;
    private final List<Item> infernoItems = new ArrayList<>();
    private String NAME;
    private int energyUsage;
    private double fireSpeed;

    private ItemClearer clearer;

    public Inundate() {
        energyUsage = 40;
        fireSpeed = 1.05 + (0.15 * 4);
    }

    @Override
    public void afterConstruction() {
        NAME = getName() + getPlayer().getName();
    }

    @Override
    public void start() {
        clearer = new ItemClearer();
    }


    @Override
    public void stop() {
        clearer.unregister();
    }

    @Override
    public String getName() {
        return "Inundate";
    }

    @Override
    public int getEnergyUsage() {
        return energyUsage;
    }

    @Override
    protected void doContinuousSkill() {
        startContinuousAction();
    }

    @Override
    public void task() {
        if (hasEnergy(getEnergyUsageTicks())) {
            useEnergy(getEnergyUsageTicks());
            if(i++ % 2 == 0) return;
            shootFire();
            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.SWIM, 0.1f, 2f);
        } else this.getPlayer().sendMessage(getNoEnergyMessage());
    }

    @Override
    public boolean cancel() {
        return !getPlayer().isBlocking() || !hasEnergy(getEnergyUsageTicks());
    }

    private void shootFire() {
        // Vector dir sets the direction and speed of the fire
        Vector dir = getPlayer().getLocation().getDirection().normalize();
        // Location spawnLoc SHOULD be the place where the fire is spawning from
        Location spawnLoc = getPlayer().getEyeLocation().add(dir);
        dir.multiply(fireSpeed);

        Item blazePowder = ItemManipulationManager.intercept(Material.WATER, spawnLoc, dir,
                (Item item, LivingEntity entity, Location location) -> {


                    //makes sure that you hit a player and that it wasnt yourself
                    if(entity == null) return; //null check is required because... reasons?
                    if(entity instanceof Player && !entity.equals(getPlayer())) {
                        //deals damage to enemy players and catches them on fire
                        if(isAlly(((Player)entity)) && !BlockUtil.isInWater(entity)) {
                            entity.setHealth(Math.min(entity.getHealth() + 0.3, ((Player) entity).getMaxHealth()));
                            // entity.getWorld().playSound(entity.getLocation(), Sound.SPLASH, 0.1f, 2f);
                            Random rand = new Random();
                            WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
                                    EnumWrappers.Particle.WATER_SPLASH, 5,
                                    rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
                            getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));
                        }
                        //removes the blaze powder after hitting any player
                        item.remove();
                    }
                });

        blazePowder.setCustomName("RITB");
        ItemMeta meta = blazePowder.getItemStack().getItemMeta();
        meta.setDisplayName(NAME + Long.toString(System.currentTimeMillis()));
        blazePowder.getItemStack().setItemMeta(meta); //set the names are so that they don't stack
        infernoItems.add(blazePowder);
        //starts a timehandler for each blazepowder that removes it once it hits the ground
    }

    private class ItemClearer implements TimeResource {
        public ItemClearer() {
            this.run(1,0);
        }
        @Override
        public void task() {
            Iterator<Item> infernoIterator = infernoItems.iterator();
            while(infernoIterator.hasNext()) {
                Item item = infernoIterator.next();
                if(!item.isValid()) {
                    infernoIterator.remove();
                    continue;
                }
                if(!EntityUtil.onGround(item) && item.getLocation().distanceSquared(getPlayer().getLocation()) < 64) continue;
                item.remove();
                infernoIterator.remove();
            }
        }

        @Override
        public boolean cancel() {
            return false;
        }

        @Override
        public void cleanup() {
        }
    }
}

 