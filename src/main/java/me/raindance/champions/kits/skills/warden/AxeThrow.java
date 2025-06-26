package me.raindance.champions.kits.skills.warden;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.ItemCollideEvent;
import com.podcrash.api.item.ItemManipulationManager;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.TimeHandler;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.skilltypes.Instant;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.enums.InvType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@SkillMetadata(id = 909, skillType = SkillType.Warden, invType = InvType.AXE)
public class AxeThrow extends Instant implements IConstruct, ICooldown {

    private float cooldown = 2f;
    private float veloScale = 1.3f;
    private float damage = 8;
    private float duration = 2;
    private String identifier;
    private int invSlot;
    private ItemStack itemStack;


    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if (action != (Action.RIGHT_CLICK_AIR) && action != Action.RIGHT_CLICK_BLOCK) return;
        Vector vector = getPlayer().getLocation().getDirection().normalize().multiply(veloScale);
        invSlot = getPlayer().getInventory().getHeldItemSlot();
        itemStack = getPlayer().getInventory().getItemInHand();
        getPlayer().getInventory().setItem(invSlot, null);

        Item axe = ItemManipulationManager.intercept(itemStack.getType(), getPlayer().getEyeLocation(), vector,
                (item, entity, land) -> {
                    if (entity == null) {// not hit

                    }else {
                        if(entity instanceof Player){
                            DamageApplier.damage(entity, getPlayer(), this.damage, this,  true);
                            StatusApplier.getOrNew(entity).applyStatus(Status.SLOW, duration, 1);
                        }else entity.damage(damage);

                        SoundPlayer.sendSound(getPlayer(), "random.orb", 1, 63);
                        item.setVelocity(new Vector(0, 0, 0));
                    }

                    // Give the player back the axe after 4 seconds.
                    TimeHandler.repeatedTime(5, 0, new TimeResource() {
                        int i = 0;
                        @Override
                        public void task() {
                            i += 1;
                        }

                        @Override
                        public boolean cancel() {
                            return getPlayer().getInventory().contains(itemStack) || i >= 16;
                        }

                        @Override
                        public void cleanup() {
                            if(i >= 16 && !getPlayer().getInventory().contains(itemStack)) {
                                SoundPlayer.sendSound(getPlayer(), "random.pop", 1, 63);
                                WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(
                                        item.getLocation().clone().add(0, 1, 0).toVector(), EnumWrappers.Particle.EXPLOSION_NORMAL, 1, 0, 0, 0);
                                PacketUtil.syncSend(packet, getPlayers());
                                getPlayer().getInventory().addItem(itemStack);
                            }
                            item.remove();
                        }
                    });
                });
        axe.setCustomName(identifier);
        this.setLastUsed(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void pickUp(PlayerPickupItemEvent event){
        if(!(event.getItem().getCustomName() != null && event.getItem().getCustomName().equals(identifier))) { return; }

        Player player = event.getPlayer();
        if(player != getPlayer() || getGame().isRespawning(getPlayer()) || getPlayer().getInventory().contains(itemStack)) {
            event.setCancelled(true);
        } else {
            getPlayer().getInventory().addItem(itemStack);
            SoundPlayer.sendSound(getPlayer(), "random.pop", 1, 63);
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @EventHandler
    public void collideItem(ItemCollideEvent e) {
        if(e.isCancelled()) return;

        //identity check + owner of item check = cancel collision
        if((e.getItem().getCustomName() != null && e.getItem().getCustomName().equals(identifier)) && (e.getCollisionVictim() == getPlayer() || isAlly(e.getCollisionVictim())))
            e.setCancelled(true);
    }

    @Override
    public String getName() {
        return "Axe Throw";
    }

    @Override
    public float getCooldown() {
        return cooldown;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    public void afterConstruction() {
        identifier = getPlayer() == null ? null : getPlayer().getName() + "thrown_axe";
    }
}
