package me.raindance.champions.kits.skills.hunter;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.PacketUtil;
import com.podcrash.api.kits.KitPlayerManager;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Continuous;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import java.util.Random;

@SkillMetadata(id = 409, skillType = SkillType.Hunter, invType = InvType.SWORD)
public class Rest extends Continuous {
    private final long duration = 2000L;
    private boolean active;
    private Random rand;
    private StatusApplier applier;
    private long start;
    private boolean effectActive;
    @Override
    protected void doContinuousSkill() {
        rand = new Random();
        active = true;
        applier = StatusApplier.getOrNew(getPlayer());
        start = System.currentTimeMillis();
        effectActive = false;
        startContinuousAction();
    }

    @Override
    public void task() {
        sendParticles();

        //if not enough time has passed, just wait until there is enough time
        if(System.currentTimeMillis() - start < duration) return;
        applier.applyStatus(Status.REGENERATION, 2.5f, 0, false, false);

        WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(EnumWrappers.Particle.REDSTONE, 4);
        Location headLocation = getPlayer().getEyeLocation().clone(); // Clone to avoid modifying the player's eye location
        headLocation.setY(headLocation.getY() + 0.5); // Adjust Y to move above the head (you can tweak 0.5 as needed)
        packet.setLocation(headLocation);
        
        PacketUtil.asyncSend(packet, getPlayers());
        

        // // Create the particle packet
        // WrapperPlayServerWorldParticles safeParticles = 
        //     ParticleGenerator.createParticle(
        //         getPlayer().getLocation().toVector().add(new Vector(0, 1, 0)), 
        //         EnumWrappers.Particle.VILLAGER_HAPPY, 
        //         2, 
        //         0, 0, 0
        //     );

        // // Get all allies and send the particle to each, excluding the player themselves
        // GameManager.getGame().getTeam(getPlayer()).getBukkitPlayers().stream()
        //     .filter(ally -> !ally.equals(getPlayer()))  // Exclude the player
        //     .forEach(ally -> PacketUtil.syncSend(safeParticles, ally));

        if(!effectActive) {
            getPlayer().sendMessage(getUsedMessage().replace("used", "activated"));
            applier.applyStatus(Status.CLOAK, 20000, 1);
            effectActive = true;
        }
    }

    private void sendParticles() {
        if(applier.has(Status.CLOAK)) return;
        WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(), EnumWrappers.Particle.HEART,
                3, rand.nextFloat(), 0.9f, rand.nextFloat());
        getPlayer().getWorld().getPlayers().forEach(p -> ParticleGenerator.generate(p, packet));
    }
    @Override
    public boolean cancel() {
        return !getPlayer().isBlocking() || !active;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if(effectActive) {
            SoundPlayer.sendSound(getPlayer().getLocation(), "mob.enderdragon.wings", 1, 63);
        }
        active = false;
        StatusApplier.getOrNew(getPlayer()).removeStatus(Status.REGENERATION, Status.CLOAK);
        effectActive = false;
    }

    @Override
    public String getName() {
        return "Rest";
    }

    @EventHandler
    public void damage(DamageApplyEvent event) {
        if(!active || event.getVictim() != getPlayer() || isAlly(event.getAttacker())) return;
        active = false;
        SoundPlayer.sendSound(getPlayer().getLocation(), "mob.wolf.hurt", 1, 63);
        String cancelMsg = String.format("%s%s> %s%s%s cancelled %sRest%s.",
                ChatColor.BLUE,
                KitPlayerManager.getInstance().getKitPlayer(getPlayer()).getName(),
                ChatColor.YELLOW, event.getAttacker().getName(), ChatColor.GRAY, ChatColor.GREEN, ChatColor.GRAY);
        getPlayer().sendMessage(cancelMsg);
    }

    @EventHandler
    public void onDamaged(EntityDamageEvent event) {
        if(!active || event.getEntity() != getPlayer()) return;
        active = false;
    }



}
