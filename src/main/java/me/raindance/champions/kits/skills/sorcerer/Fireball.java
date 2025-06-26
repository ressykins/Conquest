package me.raindance.champions.kits.skills.sorcerer;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.ItemCollideEvent;
import com.podcrash.api.item.ItemManipulationManager;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.world.BlockUtil;
import me.raindance.champions.Main;
import com.podcrash.api.events.skill.SkillUseEvent;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;

import com.podcrash.api.kits.iskilltypes.action.ICharge;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

@SkillMetadata(id = 1010, skillType = SkillType.Sorcerer, invType = InvType.PRIMARY_PASSIVE)
public class Fireball extends Passive implements IEnergy, ICooldown, IConstruct, ICharge, IPassiveTimer {
    private int currentItemID;

    private double damage = 4;
    private float cooldown = 1;
    
    private float burnDuration = 3;
    private int energyCost = 20;

    private int charges = 3;
    private int chargeCooldown = 5;

    private String NAME;
    private WrapperPlayServerWorldParticles particles;

    private float speedMultiplier = 1.5f;
    private double arcAngle = 0.2;

    public Fireball() {}

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (getItemType(getPlayer().getItemInHand()) == ItemType.SWORD && getPlayer().isSneaking()) {
                // Check to make sure the skill is not on cool down, and the player has the energy required to activate.
                if(onCooldown()) return;
                if(!hasEnergy()) {
                    getPlayer().sendMessage(getNoEnergyMessage());
                    return;
                }
                if (getCurrentCharges() < 1) {
                    getPlayer().sendMessage(getNoChargeMessage());
                    return;
                }

                // use fireball
                Location location = getPlayer().getEyeLocation();
                location.getWorld().playSound(location, Sound.GHAST_FIREBALL, 0.4f, 100.8f);
                Vector vector = location.getDirection().normalize().multiply(speedMultiplier);
                vector.setY(vector.getY() + arcAngle);
                Item spawnItem = ItemManipulationManager.regular(Material.FIREBALL, location, vector);
                this.currentItemID = spawnItem.getEntityId();
                org.bukkit.entity.Item iitem = ItemManipulationManager.intercept(spawnItem, 1.1,
                        (item, entity, land) -> {
                            item.remove();
                            if (entity == null) return;
                            if(entity instanceof Player){
                                if(isAlly(entity)) {
                                    location.getWorld().playSound(location, Sound.DIG_WOOL, 1f, 31.5f);
                                    item.remove();
                                    return;
                                }
                                if(!isAlly((entity)) && !BlockUtil.isInWater(entity)) {
                                    StatusApplier applier = StatusApplier.getOrNew(entity);
                                    if(!applier.has(Status.FIRE))
                                        applier.applyStatus(Status.FIRE, burnDuration, 1);
                                    DamageApplier.damage(entity, getPlayer(), damage, this, false);
                                }
                            }else entity.damage(damage);
                            WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(land.toVector(), EnumWrappers.Particle.EXPLOSION_LARGE, new int[]{0,0,0}, 1, 0,0,0);
                            PacketUtil.asyncSend(packet, getPlayers());
                            location.getWorld().playSound(location, Sound.DIG_WOOL, 1f, 31.5f);
                            location.getWorld().playSound(location, Sound.EXPLODE, 0.3f, 100f);
                        });
                ItemMeta meta = iitem.getItemStack().getItemMeta();
                iitem.setCustomName("RITB");
                meta.setDisplayName(NAME + Long.toString(System.currentTimeMillis()));
                iitem.getItemStack().setItemMeta(meta);
                useEnergy(getEnergyUsage());
                Bukkit.getScheduler().runTaskLater(Main.instance, () -> ParticleGenerator.generateEntity(iitem, particles, null), 1L);
                charges--;
                getPlayer().sendMessage(getCurrentChargeMessage());
                // Tell the game that we have finished using the skill (This is to trigger the cool down and create the cool down bar)
                setLastUsed(System.currentTimeMillis());
            }
        }
    }

    // @Override
    // protected void doSkill(PlayerEvent event, Action action) {

    //     // Check to make sure the skill is not on cool down, and the player has the energy required to activate.
    //     if(onCooldown()) return;
    //     if(!getPlayer().isSneaking()) return;
    //     if(!hasEnergy()) {
    //         getPlayer().sendMessage(getNoEnergyMessage());
    //         return;
    //     }
    //     if (getCurrentCharges() < 1) {
    //         getPlayer().sendMessage(getNoChargeMessage());
    //         return;
    //     }

    //     // use fireball
    //     Location location = getPlayer().getEyeLocation();
    //     location.getWorld().playSound(location, Sound.GHAST_FIREBALL, 0.4f, 100.8f);
    //     Vector vector = location.getDirection().normalize().multiply(speedMultiplier);
    //     vector.setY(vector.getY() + arcAngle);
    //     Item spawnItem = ItemManipulationManager.regular(Material.FIREBALL, location, vector);
    //     this.currentItemID = spawnItem.getEntityId();
    //     org.bukkit.entity.Item iitem = ItemManipulationManager.intercept(spawnItem, 1.1,
    //             (item, entity, land) -> {
    //                 item.remove();
    //                 if (entity == null) return;
    //                 if(entity instanceof Player){
    //                     if(isAlly(entity)) {
    //                         location.getWorld().playSound(location, Sound.DIG_WOOL, 1f, 31.5f);
    //                         item.remove();
    //                         return;
    //                     }
    //                     if(!isAlly((entity)) && !BlockUtil.isInWater(entity)) {
    //                         StatusApplier applier = StatusApplier.getOrNew(entity);
    //                         if(!applier.has(Status.FIRE))
    //                             applier.applyStatus(Status.FIRE, burnDuration, 1);
    //                         DamageApplier.damage(entity, getPlayer(), damage, this, false);
    //                     }
    //                 }else entity.damage(damage);
    //                 WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(land.toVector(), EnumWrappers.Particle.EXPLOSION_LARGE, new int[]{0,0,0}, 1, 0,0,0);
    //                 PacketUtil.asyncSend(packet, getPlayers());
    //                 location.getWorld().playSound(location, Sound.DIG_WOOL, 1f, 31.5f);
    //                 location.getWorld().playSound(location, Sound.EXPLODE, 0.3f, 100f);
    //             });
    //     ItemMeta meta = iitem.getItemStack().getItemMeta();
    //     iitem.setCustomName("RITB");
    //     meta.setDisplayName(NAME + Long.toString(System.currentTimeMillis()));
    //     iitem.getItemStack().setItemMeta(meta);
    //     useEnergy(getEnergyUsage());
    //     Bukkit.getScheduler().runTaskLater(Main.instance, () -> ParticleGenerator.generateEntity(iitem, particles, null), 1L);
    //     charges--;
    //     getPlayer().sendMessage(getCurrentChargeMessage());
    //     // Tell the game that we have finished using the skill (This is to trigger the cool down and create the cool down bar)
    //     setLastUsed(System.currentTimeMillis());
    // }

    @EventHandler
    public void collideItem(ItemCollideEvent e) {
        if(e.isCancelled()) return;
        //identity check + owner of item check = cancel collision
        if(e.getCollisionVictim() == getPlayer() && e.getItem().getEntityId() == currentItemID)
            e.setCancelled(true);
    }

    @EventHandler
    public void onUse(SkillUseEvent e) {
        if(e.getSkill().equals(this) && (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
            e.setCancelled(true);
        }
    }

    // Essentially, override the right click checker method to make it require left clicks instead (because that's how we want the skill to activate).
    // @Override
    // public boolean rightClickCheck(Action action) { return action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK; }

    @Override
    public int getEnergyUsage() { return energyCost; }

    @Override
    public String getName() { return "Fireball"; }

    @Override
    public ItemType getItemType() { return ItemType.SWORD; }

    @Override
    public float getCooldown() { return cooldown; }

    @Override
    public void afterConstruction() {
        NAME = getName() + getPlayer().getName();
        particles = ParticleGenerator.createParticle(EnumWrappers.Particle.SMOKE_NORMAL, 1);
    }



    @Override
    public boolean hasCooldown() {
        return false;
    }



    // charge related stuff

    @Override
    public void task() {
        addCharge();
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void start() {
        runAsync(chargeCooldown * 20, 0);
    }

    @Override
    public void addCharge() {
        if(charges >= getMaxCharges()) return;
        charges++;
        getPlayer().sendMessage(getCurrentChargeMessage());
    }

    @Override
    public int getCurrentCharges() {
        return charges;
    }

    @Override
    public int getMaxCharges() {
        return 3;
    }

}
