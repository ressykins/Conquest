// package me.raindance.champions.kits.skills.rogue;

// import com.podcrash.api.damage.DamageApplier;
// import com.podcrash.api.effect.status.Status;
// import com.podcrash.api.effect.status.StatusApplier;
// import com.podcrash.api.events.DeathApplyEvent;
// import com.podcrash.api.sound.SoundPlayer;
// import me.raindance.champions.annotation.kits.SkillMetadata;
// import me.raindance.champions.kits.enums.InvType;
// import com.podcrash.api.kits.enums.ItemType;
// import me.raindance.champions.kits.SkillType;
// import com.podcrash.api.kits.skilltypes.Interaction;
// import org.bukkit.entity.LivingEntity;
// import org.bukkit.event.EventHandler;

// @SkillMetadata(id = 612, skillType = SkillType.Rogue, invType = InvType.SWORD)
// public class SwiftStrike extends Interaction {
//     @Override
//     public void doSkill(LivingEntity clickedEntity) {
//         if(onCooldown()) return;
//         if(isAlly(clickedEntity)) return;
//         setLastUsed(System.currentTimeMillis());

//         landed();
//     }

//     @Override
//     public float getCooldown() {
//         return 8;
//     }

//     @Override
//     public String getName() {
//         return "Swift Strike";
//     }

//     @Override
//     public ItemType getItemType() {
//         return ItemType.SWORD;
//     }

// }
