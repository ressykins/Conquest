package me.raindance.champions.kits.skills.sorcerer;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.skill.SkillRechargeEvent;
import com.podcrash.api.events.skill.SkillUseEvent;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.SkillTitleSender;
import com.podcrash.api.util.TitleSender;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.ChargeUp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.*;

@SkillMetadata(id = 1003, skillType = SkillType.Sorcerer, invType = InvType.SWORD)
public class Blizzard extends ChargeUp implements IEnergy, IConstruct, IPassiveTimer {
    // private int i = 0;
    private final Random random = new Random();
    private SnowballRemover remover;
    private String NAME;
    // private boolean cancel;
    private int amnt;
    private int energy;
    public Blizzard() {
        this.amnt = 7;
        this.energy = 60;
    }

    @Override
    public void start() {
        remover = new SnowballRemover();
        remover.run(1, 0);
    }

    @Override
    public void stop() {
        remover.unregister();
    }

    private class SnowballRemover implements TimeResource {
        private List<Entity> entities;

        public SnowballRemover() {
            this.entities = new ArrayList<>();
        }

        @Override
        public void task() {
            Iterator<Entity> iterator = entities.iterator();
            while(iterator.hasNext()) {
                Entity entity = iterator.next();
                if(!entity.getCustomName().equals(NAME)) continue;
                Location loc = entity.getLocation();
                if(loc.distanceSquared(getPlayer().getLocation()) <= 36) continue;
                entity.remove();
                iterator.remove();
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


    @Override
    public void afterConstruction() {
        this.NAME =  getPlayer().getName() + getName();
    }

    @Override
    public String getName() {
        return "Blizzard";
    }

    @Override
    public int getEnergyUsage() {
        return energy;
    }

    @Override
    public float getRate() {
        return 0.333f / 20f;
    }
    
    @Override
    public float getCooldown() {
        return 8;
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
            blizzard();
            getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.STEP_SNOW, 0.1f, 1f);
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

    private void blizzard(){
        Location loc = getPlayer().getEyeLocation();
        for(int i = 0; i < amnt; i++){
            Vector vector = loc.getDirection().normalize();
            Snowball snowball = (Snowball) loc.getWorld().spawnEntity(loc.add(vector.clone().add(new Vector(random.nextFloat() * 0.25F, random.nextFloat() * 0.25F, random.nextFloat() * 0.25F))), EntityType.SNOWBALL);
            snowball.setCustomName(NAME);
            //this part is copied XD
            double mult = 0.25 + 0.15 * 5;
            double x = (0.2 - (random.nextInt(40)/100d)) * mult;
            double y = (random.nextInt(20)/100d) * mult;
            double z = (0.2 - (random.nextInt(40)/100d)) * mult;

            snowball.setVelocity(vector.add(new Vector(x, y, z)).multiply(2));
            remover.entities.add(snowball);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void snowballHit(EntityDamageByEntityEvent event){
        if(!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) event.getEntity();
        if(victim == getPlayer()) {
            event.setCancelled(true);
            return;
        }
        if(event.getDamager() instanceof Snowball){
            Snowball snowball = (Snowball) event.getDamager();
            if(NAME.equalsIgnoreCase(snowball.getCustomName())){
                event.setCancelled(true);
                event.getEntity().setVelocity(snowball.getVelocity().multiply(0.1).add(new Vector(0, 0.1, 0)));
            }
        }
    }


    // @Override
    // protected void doContinuousSkill() {
    //     run(1, 0);
    // }

    // @Override
    // public void task() {
    //     double energyUsage = getEnergyUsageTicks();
    //     if(hasEnergy(energyUsage)) {
    //         useEnergy(energyUsage);
    //         if(i++ % 2 == 0) return;
    //         blizzard();
    //     } else {
    //         cancel = true;
    //         getPlayer().sendMessage(getNoEnergyMessage());
    //     }
    // }

    // @Override
    // public boolean cancel() {
    //     return cancel || !getPlayer().isBlocking();
    // }

    // @Override
    // public void cleanup() {
    //     super.cleanup();
    //     cancel = false;
    // }

}

 