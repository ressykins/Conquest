package me.raindance.champions.kits.skills.sorcerer;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.podcrash.api.damage.Cause;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;

import me.raindance.champions.Main;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import com.podcrash.api.kits.iskilltypes.action.ICooldown;

import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.plugin.PodcrashSpigot;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.PacketUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 1015, skillType = SkillType.Sorcerer, invType = InvType.PRIMARY_PASSIVE)
public class Cryofreeze extends Passive implements ICooldown { // Crippling Blow is a Passive skill
    public Cryofreeze() {
        super();
    }

    @Override
    public String getName() {
        return "Cryofreeze";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public float getCooldown() {
        return 8;
    }

@EventHandler // The priority is monitor because, the Brute wants to watch you suffer rather than end your pain immediately
public void onHit(DamageApplyEvent event) { // When you smack someone it does stuff
    if (event.isCancelled() || (getPlayer() != event.getAttacker() || event.getCause() != Cause.MELEE)) return; // Something about non-players
    if (!(event.getVictim() instanceof Player)) return;
    if (getItemType(getPlayer().getItemInHand()) != ItemType.AXE) return;
    if (onCooldown()) return;
    PodcrashSpigot.getInstance().getLogger().info("cryofreeze on hit");
    Player victim = (Player) event.getVictim(); // The player who took the damage is the victim
    setLastUsed(System.currentTimeMillis());
    getPlayer().sendMessage(getUsedMessage(event.getVictim()));
    event.setDoKnockback(false);

    // Synchronous particle and sound effects
    Bukkit.getScheduler().runTask(Main.instance, () -> {
        StatusApplier.getOrNew(victim).applyStatus(Status.ROOTED, 2, 0, false, false);
        SoundPlayer.sendSound(getPlayer().getLocation(), "dig.glass", 1, 63);
        AbstractPacket bleedPacket = ParticleGenerator.createBlockEffect(victim.getLocation(), Material.ICE.getId());
        PacketUtil.asyncSend(bleedPacket, getPlayers());
    });

    event.addSource(this);
}

}
