package me.raindance.champions.kits.skills.marksman;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.item.ItemManipulationManager;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import net.jafama.FastMath;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IConstruct;
import com.podcrash.api.kits.skilltypes.BowShotSkill;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.util.EntityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

@SkillMetadata(id = 505, skillType = SkillType.Marksman, invType = InvType.BOW)
public class NapalmShot extends BowShotSkill implements IConstruct {

    private WrapperPlayServerWorldParticles fire;
    private List<Item> items = new ArrayList<>();
    private float burnDuration = 5.5F;
    private int flames = 40;
    private String NAME;
    private Resetter resetter = new Resetter();

    @Override
    public void afterConstruction() {
        this.fire = ParticleGenerator.createParticle(null, EnumWrappers.Particle.LAVA, 2, 0,0,0);
        NAME = getName() + getPlayer().getName();
    }

    @Override
    public float getCooldown() {
        return 15;
    }

    @Override
    public String getName() {
        return "Napalm Shot";
    }

    @Override
    protected void shotArrow(Arrow arrow, float force) {
        ParticleGenerator.generateProjectile(arrow, fire);
        arrow.setFireTicks(1000);
        SoundPlayer.sendSound(getPlayer().getLocation(), "fireworks.blast", 1F, 65);
    }

    @Override
    protected void shotEntity(DamageApplyEvent event, Player shooter, LivingEntity victim, Arrow arrow, float force) {
        if(isAlly(victim)) return;
        victim.setFireTicks(0);
        //float duration = 1.5F + (float) victim.getLocation().distance(shooter.getLocation())/scaling;
        event.addSource(this);
        StatusApplier.getOrNew(victim).applyStatus(Status.FIRE, burnDuration, 0, false);
        SoundPlayer.sendSound(victim.getLocation(), "random_explode", 1F, 126);
        hit(arrow, victim.getLocation());
    }

    @Override
    protected void shotGround(Player shooter, Location location, Arrow arrow, float force) {
        SoundPlayer.sendSound(location, "random_explode", 1F, 126);
        hit(arrow, location);
    }

    private void hit(Arrow arrow, Location location){
        if(arrow.getFireTicks() <= 0) return;
        final Random random = new Random();
        double yoffset = 0.2;
        int section = flames / 4;
        Vector vector = new Vector(0D, yoffset * section/flames, 0D);

        

        for(int i = 0; i < flames; i++) {
            Item item = ItemManipulationManager.regular(Material.BLAZE_POWDER, location, vector.setX(vector.getY() * FastMath.sin(i)).setZ(vector.getY() * FastMath.cos(i)));
            item.setCustomName("RITB");
            ItemStack itemStack = item.getItemStack();
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(NAME + (random.nextDouble() * System.currentTimeMillis()));
            itemStack.setItemMeta(meta);
            items.add(item);
            if(i > section) {
                section += flames/4;
                vector.setY(yoffset * section/flames);
            }
        }
        resetter.setTime(System.currentTimeMillis());
        resetter.run(1, 1);
    }


    private class Resetter implements TimeResource {
        private long time;
        private Resetter() {

        }

        public void setTime(long time) {
            this.time = time;
        }

        @Override
        public void task() {

        }

        @Override
        public boolean cancel() {
            return System.currentTimeMillis() - time >= 5000L || items.size() == 0;
        }

        @Override
        public void cleanup() {
            if(items.size() != 0) {
                for (Item item : items) {
                    if (item.isValid()) item.remove();
                }
                items.clear();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void pickUp(PlayerPickupItemEvent event){
        if(!items.contains(event.getItem())) return;
        Item item = event.getItem();
        if(EntityUtil.onGround(item, 0.025)){
            item.remove();
            if(event.getPlayer().getLocation().getBlock().getType().equals(Material.WATER)) return;
            StatusApplier.getOrNew(event.getPlayer()).applyStatus(Status.FIRE, burnDuration, 0);
            DamageApplier.damage(event.getPlayer(), getPlayer(), 0.5, this, false);
            items.remove(item);
        }
    }

}
