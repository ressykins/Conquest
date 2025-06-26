package me.raindance.champions.kits.skills.duelist;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.util.EntityUtil;
import com.podcrash.api.util.PacketUtil;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@SkillMetadata(id = 304, skillType = SkillType.Duelist, invType = InvType.PRIMARY_PASSIVE)
public class MortalWound extends Passive {

    @Override
    public String getName() {
        return "Mortal Wound";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @EventHandler
    public void hit(DamageApplyEvent e) {
        //if the attacker is not the user return
        if(e.getAttacker() != getPlayer()) return;
        //if the victim is not below 50% health, return
        if(!EntityUtil.isBelow(e.getVictim(), 0.5)) return;
        //apply bleed + send a redstone block particle
        if(isAlly(e.getVictim())) return;
        e.addSource(this);
        StatusApplier.getOrNew((Player) e.getVictim()).applyStatus(Status.BLEED, 3, 1);
        AbstractPacket packet = ParticleGenerator.createBlockEffect(e.getVictim().getLocation(), Material.REDSTONE.getId());
        PacketUtil.asyncSend(packet, getPlayers());
    }
}
