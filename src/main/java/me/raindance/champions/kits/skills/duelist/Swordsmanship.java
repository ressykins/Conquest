package me.raindance.champions.kits.skills.duelist;

// import com.comphenix.protocol.wrappers.EnumWrappers;
// import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
// import com.podcrash.api.damage.Cause;
// import com.podcrash.api.effect.particle.ParticleGenerator;
// import com.podcrash.api.effect.status.Status;
// import com.podcrash.api.effect.status.StatusApplier;
// import com.podcrash.api.events.DamageApplyEvent;
// import com.podcrash.api.kits.KitPlayer;
// import com.podcrash.api.kits.KitPlayerManager;
// import com.podcrash.api.kits.enums.ItemType;
// import com.podcrash.api.kits.skilltypes.Passive;
// import com.podcrash.api.sound.SoundPlayer;
// import com.podcrash.api.util.EntityUtil;
// import me.raindance.champions.annotation.kits.SkillMetadata;
// import me.raindance.champions.kits.SkillType;
// import me.raindance.champions.kits.enums.InvType;

// import java.util.Random;

// import org.bukkit.ChatColor;
// import org.bukkit.entity.LivingEntity;
// import org.bukkit.entity.Player;
// import org.bukkit.event.EventHandler;

// @SkillMetadata(
//    id = 304,
//    skillType = SkillType.Duelist,
//    invType = InvType.PRIMARY_PASSIVE
// )
// public class Swordsmanship extends Passive {
//     private LivingEntity attacked = null;
//     private int i;

//    public Swordsmanship() {
//    }

//    public String getName() {
//       return "Swordsmanship";
//    }

//    public ItemType getItemType() {
//       return ItemType.NULL;
//    }

//    @EventHandler
//    public void hit(DamageApplyEvent event) {
//       if (event.getAttacker() != getPlayer()) return;
//       if (event.getVictim() != attacked || attacked == null) {
//          attacked = event.getVictim();
//          i = 0;
//          return;
//       }

//       i++;

//       if (i < 3) return;

//       StatusApplier.getOrNew(attacked).applyStatus(Status.MARKED, 3, 0);
//       event.setModified(true);
//       event.addSource(this);


//         Random rand = new Random();
//         WrapperPlayServerWorldParticles particle = ParticleGenerator.createParticle(attacked.getLocation().toVector(),
//                 EnumWrappers.Particle.CRIT_MAGIC, 5,
//                 rand.nextFloat() / 2f, 0.25f + (rand.nextFloat() - 0.15f), rand.nextFloat() / 2f);
//         attacked.getWorld().getPlayers().forEach(player -> ParticleGenerator.generate(player, particle));

//       SoundPlayer.sendSound(getPlayer().getLocation(), "dig.glass", 1, 63);

//       attacked = null;
//    }
// }
