package me.raindance.champions.kits.skills.druid;

import com.packetwrapper.abstractpackets.AbstractPacket;
import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.util.PacketUtil;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;

import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IEnergy;
import com.podcrash.api.kits.skilltypes.Interaction;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

@SkillMetadata(id = 206, skillType = SkillType.Druid, invType = InvType.SWORD)
public class NaturePower extends Interaction implements IEnergy {
    private int energyUsage = 70;
    private float duration = 5;

    public NaturePower() {
        canMiss = false;
     }

    public void doSkill(LivingEntity clickedEntity) {
        if(onCooldown()) return;

        if (isAlly(clickedEntity)) {
            StatusApplier.getOrNew(clickedEntity).applyStatus(Status.ABSORPTION, duration, 1);
            StatusApplier.getOrNew(clickedEntity).applyStatus(Status.STRENGTH, duration, 0);

            WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(clickedEntity.getLocation().toVector(), EnumWrappers.Particle.HEART,
                    3, 0, 0.9f, 0);
            getPlayer().getWorld().getPlayers().forEach(p -> ParticleGenerator.generate(p, packet));
            AbstractPacket leafBreak = ParticleGenerator.createBlockEffect(clickedEntity.getLocation(), Material.LEAVES.getId());
            PacketUtil.asyncSend(leafBreak, getPlayer().getWorld().getPlayers());

            SoundPlayer.sendSound(clickedEntity.getLocation(), "random.levelup", 0.9F, 95);
        }

        else {
            StatusApplier.getOrNew(clickedEntity).applyStatus(Status.SLOW, duration, 1);
            StatusApplier.getOrNew(clickedEntity).applyStatus(Status.WEAKNESS, duration, 1);

            AbstractPacket leafBreak = ParticleGenerator.createBlockEffect(clickedEntity.getLocation(), Material.SOUL_SAND.getId());
            PacketUtil.asyncSend(leafBreak, getPlayer().getWorld().getPlayers());

            clickedEntity.getLocation().getWorld().playSound(clickedEntity.getLocation(), Sound.WITHER_HURT, 0.3f, 0.5f);
        }

        setLastUsed(System.currentTimeMillis());
        useEnergy();
        landed();
    }

    @Override
    public float getCooldown() {
        return 7;
    }

    @Override
    public int getEnergyUsage() {
        return energyUsage;
    }

    @Override
    public String getName() {
        return "Nature Power";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }
}
