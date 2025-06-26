package me.raindance.champions.kits.skills.sorcerer;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.callback.helpers.TrapSetter;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.ItemCollideEvent;
import com.podcrash.api.events.TrapPrimeEvent;
import com.podcrash.api.item.ItemManipulationManager;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.PacketUtil;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Instant;
import com.podcrash.api.sound.SoundWrapper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

@SkillMetadata(id = 1009, skillType = SkillType.Sorcerer, invType = InvType.AXE)
public class LightningOrb extends Instant implements IEnergy, ICooldown, IConstruct {
    private WrapperPlayServerWorldParticles particles;
    private String NAME;
    private int energy = 60;
    private float distance = 16;
    private int damage = 8;

    private int currentItemID;
    @Override
    public void afterConstruction() {
        this.NAME = getPlayer().getName()  + getName();
        this.particles = ParticleGenerator.createParticle(null, EnumWrappers.Particle.SNOW_SHOVEL, 1, 0, 0, 0);

    }

    @Override
    public float getCooldown() {
        return 10;
    }

    @Override
    public String getName() {
        return "Lightning Orb";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.AXE;
    }

    @Override
    public int getEnergyUsage() {
        return energy;
    }

    @Override
    protected void doSkill(PlayerEvent event, Action action) {
        if(!rightClickCheck(action) || onCooldown()) return;
        if(!hasEnergy()) {
            getPlayer().sendMessage(getNoEnergyMessage());
            return;
        }
        this.setLastUsed(System.currentTimeMillis());
        Location location = getPlayer().getEyeLocation();
        Vector vector = location.getDirection();
        vector.normalize().multiply(1.15D);
        useEnergy(energy);
        Item spawnItem = ItemManipulationManager.regular(Material.DIAMOND_BLOCK, location, vector);
        Item item = ItemManipulationManager.intercept(spawnItem, 1.1,(item1, entity, land) -> {
            if(entity == null) TrapSetter.spawnTrap(item1,1000);
            else collide(item1, land);
        });
        item.setCustomName("RITB");
        ItemMeta meta = item.getItemStack().getItemMeta();
        meta.setDisplayName(NAME + item.getEntityId());
        item.getItemStack().setItemMeta(meta);
        ParticleGenerator.generateEntity(item, particles, new SoundWrapper("random.fizz", 0.6F, 88));
        SoundPlayer.sendSound(item.getLocation(), "mob.silverfish.hit", 1F, 90);

        this.currentItemID = item.getEntityId();

        getPlayer().sendMessage(getUsedMessage());
    }

    @EventHandler
    public void trapPrime(TrapPrimeEvent event) {
        Item item = event.getItem();
        if(item.getEntityId() != currentItemID) return;
        collide(item, item.getLocation());
    }

    @EventHandler
    public void collideItem(ItemCollideEvent e) {
        if(e.isCancelled()) return;
        //identity check + owner of item check = cancel collision
        if((e.getCollisionVictim() == getPlayer() || isAlly(e.getCollisionVictim())) && e.getItem().getEntityId() == currentItemID)
            e.setCancelled(true);
    }


    private void collide(Item item, Location location) {
        for(Player player : getPlayers()) {
            if(player == getPlayer() && isAlly(player)) continue;
            Location playerLocation = player.getLocation();
            if(location.distanceSquared(playerLocation) > distance) continue;
            playerLocation.getWorld().strikeLightningEffect(playerLocation);
            StatusApplier.getOrNew(player).applyStatus(Status.SLOW, 4, 1);
            StatusApplier.getOrNew(player).applyStatus(Status.SHOCK, 4, 1);
            DamageApplier.damage(player, getPlayer(), damage, this, false);
        }

        WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(
                location.clone().add(0, 1, 0).toVector(), EnumWrappers.Particle.EXPLOSION_NORMAL, 5, 0, 0, 0);
        PacketUtil.syncSend(packet, getPlayers());
        TrapSetter.deleteTrap(item);
        item.remove();
    }

}
