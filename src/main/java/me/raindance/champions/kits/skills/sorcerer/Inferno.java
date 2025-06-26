package me.raindance.champions.kits.skills.sorcerer;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.skill.SkillRechargeEvent;
import com.podcrash.api.events.skill.SkillUseEvent;
import com.podcrash.api.item.ItemManipulationManager;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.ChargeUp;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.EntityUtil;
import com.podcrash.api.util.SkillTitleSender;
import com.podcrash.api.util.TitleSender;
import com.podcrash.api.world.BlockUtil;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SkillMetadata(id = 1005, skillType = SkillType.Sorcerer, invType = InvType.SWORD)
public class Inferno extends ChargeUp implements IEnergy, IConstruct, IPassiveTimer {
    // private int i = 0;
    private final List<Item> infernoItems = new ArrayList<>();
    private String NAME;
    private int energyUsage;
    private double fireSpeed;
    private float duration;
    private final double damage = 0.3D;

    private ItemClearer clearer;

    public Inferno() {
        energyUsage = 50;
        fireSpeed = 1.05 + (0.15 * 6);
        duration = 5;
    }

    @Override
    public float getCooldown() {
        return 6;
    }

    @Override
    public String getName() {
        return "Inferno";
    }

    @Override
    public int getEnergyUsage() {
        return energyUsage;
    }


    @Override
    public float getRate() {
        return 0.333f / 20f;
    }



    @Override
    public void release() {
        // if(!onCooldown()) {
        //     getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.FIZZ, 0.1f, 1f);
        // this.setLastUsed(System.currentTimeMillis());
        // }
    }


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

    @Override
    public void task() {
        charge();
        isUsing = true;
        WrappedChatComponent progress = SkillTitleSender.chargeUpProgressBar(this, this.getCharge());
        if(getCharge() < 1f) {
            shootFire();
            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.GHAST_FIREBALL, 0.1f, 1f);
        } 
        else {
            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.FIZZ, 0.1f, 1f);
            this.setLastUsed(System.currentTimeMillis());
        }
        TitleSender.sendTitle(this.getPlayer(), progress);
    }

    @Override
    public boolean cancel() {
        return !getPlayer().isBlocking() || onCooldown();
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

    // @Override
    // protected void doContinuousSkill() {
    //     if(onCooldown()) {
    //         getPlayer().sendMessage(getCooldownMessage());
    //         return;
    //     } 
    //     if(!hasEnergy()) {
    //         getPlayer().sendMessage(getNoEnergyMessage());
    //         return;
    //     }
    //     useEnergy();
    //     startContinuousAction();
    // }

    // @Override
    // public void task() {
    //     if (hasEnergy(getEnergyUsageTicks())) {
    //         // useEnergy(getEnergyUsageTicks());
    //         if(i++ % 2 == 0) return;
    //         shootFire();
    //         getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.GHAST_FIREBALL, 0.1f, 1f);
    //     } 
    //     else {
    //         this.getPlayer().sendMessage(getNoEnergyMessage());
    //     };
    // }

    // @Override
    // public boolean cancel() {
    //     return !getPlayer().isBlocking() || !hasEnergy(getEnergyUsageTicks()) || onCooldown();
    // }

    // @Override
    // public void cleanup() {
    //     if (!getPlayer().isBlocking() && !onCooldown()) {
    //         getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.FIZZ, 0.1f, 1f);
    //         this.setLastUsed(System.currentTimeMillis());
    //     }
    // }


    private void shootFire() {
        // Vector dir sets the direction and speed of the fire
        Vector dir = getPlayer().getLocation().getDirection().normalize();
        // Location spawnLoc SHOULD be the place where the fire is spawning from
        Location spawnLoc = getPlayer().getEyeLocation().add(dir);
        dir.multiply(fireSpeed);

        Item blazePowder = ItemManipulationManager.intercept(Material.BLAZE_POWDER, spawnLoc, dir,
                (Item item, LivingEntity entity, Location location) -> {
                    //makes sure that you hit a player and that it wasnt yourself
                    if(entity == null) return; //null check is required because... reasons?
                    if(entity instanceof Player && !entity.equals(getPlayer())) {
                        //deals damage to enemy players and catches them on fire
                        if(!isAlly(((Player)entity)) && !BlockUtil.isInWater(entity)) {
                            StatusApplier.getOrNew((Player) entity).applyStatus(Status.FIRE, duration, 1);
                            DamageApplier.damage(entity, getPlayer(), damage, this, false);
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
