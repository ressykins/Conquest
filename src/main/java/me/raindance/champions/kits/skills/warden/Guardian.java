package me.raindance.champions.kits.skills.warden;


import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.util.EntityUtil;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.event.EventHandler;

@SkillMetadata(id = 913, skillType = SkillType.Warden, invType = InvType.PRIMARY_PASSIVE)
public class Guardian extends Passive {

    private final double radiusSquared = 5 * 5;

    @Override
    public String getName() {
        return "Guardian";
    }

    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    // @Override
    // public float getCooldown() {
    //     return 14;
    // }

    // @Override
    // public boolean drop(PlayerDropItemEvent e) {
    //     if(onCooldown()) return false;
    //     setLastUsed(System.currentTimeMillis());
    //     active = true;
    //     StatusApplier.getOrNew(e.getPlayer()).applyStatus(Status.RESISTANCE, duration, 0);
    //     SoundPlayer.sendSound(getPlayer().getLocation(), "mob.irongolem.death", 0.85F, 92);
    //     new GuardianProtect().run(2, 0);
    //     return true;
    // }

    @EventHandler
    public void hit(DamageApplyEvent e) {
        // if(!active) return;
        if(e.getVictim() == getPlayer()) {
            e.setModified(true);
            return;
        }
        if(!isAlly(e.getVictim())) return;
        if(e.getVictim().getLocation().distanceSquared(getPlayer().getLocation()) >= radiusSquared) return;
        if(EntityUtil.isBelow(getPlayer(), 0.5)) return;

        e.setDamage(.8D * e.getDamage());
        e.setModified(true);

        DamageApplier.damage(getPlayer(), e.getAttacker(), .2D * e.getDamage(), this, false);

    }

    // private final class GuardianProtect implements TimeResource {
    //     private Random rand;
    //     private GuardianProtect() {
    //         rand = new Random();
    //     }
    //     @Override
    //     public void task() {
    //         WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(getPlayer().getLocation().toVector(),
    //                 EnumWrappers.Particle.SPELL_MOB, new int[]{211, 211, 211}, 5,
    //                 rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
    //         getPlayer().getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));
    //     }

    //     @Override
    //     public boolean cancel() {
    //         return !active || System.currentTimeMillis() - getLastUsed() >= (long) duration * 1000L;
    //     }

    //     @Override
    //     public void cleanup() {
    //         active = false;
    //     }
    // }

}
