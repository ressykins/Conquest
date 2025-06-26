package me.raindance.champions.kits.skills.warden;

// import com.podcrash.api.damage.Cause;
// import com.podcrash.api.events.DamageApplyEvent;
// import com.podcrash.api.kits.skilltypes.Passive;
// import me.raindance.champions.annotation.kits.SkillMetadata;
// import me.raindance.champions.kits.SkillType;
// import me.raindance.champions.kits.enums.InvType;
// import org.bukkit.Effect;
// import org.bukkit.Sound;
// import org.bukkit.event.EventHandler;

// @SkillMetadata(id = 912, skillType = SkillType.Warden, invType = InvType.SECONDARY_PASSIVE)
// public class BlastShield extends Passive {
//     private double damageReductionPercent = 0.25;

//     @Override
//     public String getName() {
//         return "Blast Shield";
//     }

//     @EventHandler
//     public void onAbilityDamage(DamageApplyEvent e) {
//         if(e.getVictim().equals(getPlayer()) && e.getCause().equals(Cause.CUSTOM)) {
//             getPlayer().getWorld().playEffect(getPlayer().getEyeLocation(), Effect.MAGIC_CRIT, 3);
//             getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.FIZZ, 0.5f, 1.5f);
//             e.setDamage(e.getDamage() * (1 - damageReductionPercent));
//         }
//     }
// }