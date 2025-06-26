package me.raindance.champions.kits.skills.vanguard;

import com.packetwrapper.abstractpackets.WrapperPlayServerWorldParticles;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.podcrash.api.damage.DamageApplier;
import com.podcrash.api.events.ItemCollideEvent;
import com.podcrash.api.effect.particle.ParticleGenerator;
import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.item.ItemManipulationManager;
import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.sound.SoundPlayer;
import com.podcrash.api.sound.SoundWrapper;
import com.podcrash.api.util.VectorUtil;
import com.podcrash.api.kits.skilltypes.ChargeUp;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

// The skillMetadata is pretty self explanatory, the only thing you need to watch out for is making sure that there
// are no repeated ID's! The ID of a skill is vital to making sure kits are saved correctly, so avoid duplicates.
// As for the exact number you should choose, its pretty much up to you. Look through the other skills for the class,
// usually there is some sort of rough pattern to follow.
@SkillMetadata(id = 801, skillType = SkillType.Vanguard, invType = InvType.SWORD)
public class ChainedHook extends ChargeUp {
    // This currentItemID variable allows us to keep track of what chained hook item is actually ours: it's use
    // becomes more apparent in the collideItem() method.
    private int currentItemID;
    private int maxDamage = 10;

    // These three variables could be replaced into the code, but we leave them out here to make implementing balance
    // changes much more easier (we don't have to dig through the code to change stuff; this is ideal).
    private float cooldown = 8;

    private float itemSpeedMult = 1.6f;
    private double itemVerticalBoost = 0.2;
    private float victimSpeedMult = 1.5f;
    private double victimVerticalBoost = 0.8;

    private double victimVerticalBoostLimit = 2.0;
    private double hitboxRadius = 2.25;

    // This method should always return the name of the skill that we want to be displayed. It's necessary whenever we
    // make a new skill.
    @Override
    public String getName() {
        return "Chained Hook";
    }

    // This method should specify what item must be used to active this skill. Note the differences between the ItemType and
    // InvType enums; it is subtle but important.
    @Override
    public ItemType getItemType() {
        return ItemType.SWORD;
    }

    // All skills must have this method, unless they are a passive or something. It lets the game know how long it
    // should wait before allowing the player to use it again, but you probably already knew that.
    @Override
    public float getCooldown() {
        return cooldown;
    }

    // Your standard constructor for the class, normal stuff.
    public ChainedHook() {
        super();
    }

    @Override
    public float getRate() {
        return 0.5f / 20f;
    }
    // // For abilities that extend the Instant skilltype, the method doSkill() will be called whenever the player clicks
    // // (right or left). We still activate this method for left clicks because of skills similar to glacial blade (which would
    // // fall under the category of instant).
    // @Override
    // protected void doSkill(PlayerEvent event, Action action) {
    //     // Make sure we aren't on cool down, and that we actually right clicked.
    //     if(!rightClickCheck(action) || onCooldown()) return;
    //     // Now that we are sure the player can and wants to use the skill, tell the system that we have -officially- used the skill.
    //     // This is really important because it starts the cool down for when this skill can be used again.
    //     setLastUsed(System.currentTimeMillis());

    //     // The release method handles creating the projectile, and what we should do if it hits someone. It could be
    //     // named whatever, the name 'release' is just a remnant from when this skill was a ChargeUp.
    //     release();

    //     // Send the 'You used' message blah blah blah (easier than typing it out every time).
    //     getPlayer().sendMessage(getUsedMessage());
    // }

    public void release() {
        // Create a new vector based on where the player is looking.
        double amnt = 0.2F + 0.8F * getCharge();
        
        Vector vector = getPlayer().getLocation().getDirection();
        // Normalize the vector (aka change the magnitude to one) before multiplying by our speed multiplier.
        Vector itemVector = vector.clone().normalize().multiply(itemSpeedMult);
        // Give the item a small boost in the Y direction so that it follows more of an arc
        itemVector = itemVector.setY(itemVector.getY() + itemVerticalBoost).multiply(0.5F + 0.5F * getCharge());

        Location attackerLocation = getPlayer().getLocation();

        // Create a new item at the attacker's eye height with the initial velocity that we defined earlier.
        Item spawnItem = ItemManipulationManager.regular(Material.TRIPWIRE_HOOK, getPlayer().getEyeLocation(), itemVector);
        // Save this item's ID (which is the actual 'flesh hook' that the attacker throws) so that later in the
        // collideItem() method we can check to see if OUR specific flesh hook (defined by its ID) has hit the the victim.
        this.currentItemID = spawnItem.getEntityId();

        // Everything below will be run when the item spawnItem collides with something (a player, a block ect)
        ItemManipulationManager.intercept(spawnItem, hitboxRadius,
                ((item, entity, land) -> {
                    // First, we remove the item because we have hit something. The remove() method will physically
                    // remove the item from the world, but since we know where it hit and everything, this is ok.
                    item.remove();
                    // If its not an entity we don't really care
                    if(entity == null) return;

                    Location victimLocation = entity.getLocation();
                    // Here we find the general direction to which the victim should be pulled; the method fromAtoB()
                    // just subtracts the first vector from the second one, and then we normalize it so that the actual
                    // distance between the two players doesn't affect the pull strength.
                    Vector victimVector = VectorUtil.fromAtoB(victimLocation, attackerLocation).normalize();
                    // Now we multiply the victim's resultant velocity by the multiplier so we can change how
                    // strongly they will be pulled.
                    victimVector.multiply(victimSpeedMult * amnt);
                    // This line of code is really similar to what we did with the item's velocity, but now we are
                    // limiting the power of the vertical boost to our defined limit. Math.min(x, y) will always
                    // return the smallest of the two inputted values, so its nice for establishing limits.
                    victimVector.setY(Math.min(victimVector.getY() + victimVerticalBoost * amnt, victimVerticalBoostLimit));

                    // Now we can finally pull the entity that we hit towards us.
                    entity.setVelocity(victimVector);

                    // Play the nice sound and apply damage to those noobs who did not train long enough in Diradus-1.
                    SoundPlayer.sendSound(getPlayer(), "random.successful_hit", 0.75F, 50);
                    DamageApplier.damage(entity, getPlayer(), Math.max(maxDamage * getCharge(), 3), this, false);
                    StatusApplier.getOrNew(entity).applyStatus(Status.SLOW, 3, 1);
                }));
        // This makes sure the player can't pick up the item.
        // (I'm not actually 100% sure if it's necessary, probably better to be safe).
        spawnItem.setPickupDelay(1000);

        // These lines of code essentially grab the ItemMeta of our chained hook item, and change it's name property
        // to something unique. Why do we do this? So the item won't stack with other items, usually more of a problem
        // with skills similar to inferno.
        ItemMeta meta = spawnItem.getItemStack().getItemMeta();
        meta.setDisplayName(Long.toString(System.currentTimeMillis()));
        spawnItem.getItemStack().setItemMeta(meta);

        // This plays the sound when the skill is used, not when it hits someone. Sounds can be found here:
        // https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/mapping-and-modding-tutorials/2213619-1-8-all-playsound-sound-arguments
        // volume is what you would expect, and for some reason 63 pitch means that the sound will stay the same. If you
        // are testing the sounds with /playsound in game, this essentially means 63 = 1, 126 = 2 and so on.
        SoundWrapper sound = new SoundWrapper("fire.ignite", 0.8F, 70);

        // ParticleGenerator.generateEntity() is a really cool and fun method that will cause the specified particles to
        // follow an entity that we provide. We can also provide it with a sound to play as it follows the entity as well!
        WrapperPlayServerWorldParticles packet = ParticleGenerator.createParticle(EnumWrappers.Particle.CRIT, 2);
        ParticleGenerator.generateEntity(spawnItem, packet, sound);
    }

    // This method will be run whenever our item collides with something. ItemCollideEvent is called right before our
    // intercept method (handled above) does all the heavy calculations, so it's more efficient to cancel it here on
    // certain occasions before we waste our time.

    @EventHandler
    public void collideItem(ItemCollideEvent e) {
        // If another ability has caught this event and canceled it before us, we will respect that and not allow the
        // event to finish.
        if(e.isCancelled()) return;
        // Now we make sure that we didn't hit ourselves with our own item.
        if(e.getCollisionVictim() == getPlayer() && e.getItem().getEntityId() == currentItemID)
            e.setCancelled(true);
    }
}

