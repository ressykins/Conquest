package me.raindance.champions.kits.skills.vanguard;

import com.podcrash.api.effect.status.Status;
import com.podcrash.api.effect.status.StatusApplier;
import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.game.GameManager;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.Passive;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.world.BlockUtil;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.game.DomGame;
import me.raindance.champions.kits.SkillType;
import me.raindance.champions.kits.enums.InvType;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;


@SkillMetadata(id = 811, skillType = SkillType.Vanguard, invType = InvType.SECONDARY_PASSIVE)
public class Valor extends Passive implements IPassiveTimer, TimeResource {
    @Override
    public String getName() {
        return "Valor";
    }

    @Override
    public void start() {
        runAsync(1,0);
    }

    @Override
    public void task() {
        double numEnemies = 0;
        for(Player p : BlockUtil.getPlayersInArea(getPlayer().getLocation(), 10, getPlayers())) {
            if(!isAlly(p)) numEnemies++;
        }

        if (numEnemies >= 2) StatusApplier.getOrNew(getPlayer()).applyStatus(Status.STRENGTH, 1.25f, 0, false, false);
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void cleanup() {
    }

    @EventHandler
    public void onDamage(DamageApplyEvent e) {
        if(e.getVictim().equals(getPlayer())) {
            double numEnemies = 0;
            for(Player p : BlockUtil.getPlayersInArea(getPlayer().getLocation(), 10, getPlayers())) {
                if(!isAlly(p)) numEnemies++;
            }

            if (numEnemies == 0) return;

            e.setDamage(Math.max(e.getDamage() * (1 - (numEnemies * 0.1)), e.getDamage() * 0.5));
            e.setModified(true);
        }
    }

}
