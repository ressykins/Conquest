package me.raindance.champions.kits.skills.berserker;

import com.podcrash.api.events.DamageApplyEvent;
import com.podcrash.api.events.DeathApplyEvent;
import com.podcrash.api.time.resources.TimeResource;
import com.podcrash.api.kits.EnergyBar;
import com.podcrash.api.kits.KitPlayer;
import com.podcrash.api.kits.KitPlayerManager;

import me.raindance.champions.annotation.kits.SkillMetadata;
import me.raindance.champions.kits.enums.InvType;
import com.podcrash.api.kits.enums.ItemType;
import me.raindance.champions.kits.SkillType;
import com.podcrash.api.kits.iskilltypes.action.IPassiveTimer;
import com.podcrash.api.kits.skilltypes.Passive;

import org.bukkit.event.EventHandler;

@SkillMetadata(id = 105, skillType = SkillType.Berserker, invType = InvType.INNATE)
public class Fury extends Passive implements IPassiveTimer, TimeResource {
    private long lastHit;
    @Override
    public String getName() {
        return "Fury";
    }


    @Override
    public ItemType getItemType() {
        return ItemType.NULL;
    }

    @Override
    public void start() {
        lastHit = System.currentTimeMillis() - 3000;
        run(1, 0);
    }

    @Override
    public void task() {
        if(lastHit != 0 && System.currentTimeMillis() - lastHit >= 3000) {
            lastHit = 0;
            getEnergyBar().setEnergy(0);
        }
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void cleanup() {

    }

    @EventHandler
    public void hit(DamageApplyEvent e) {
        if(e.getAttacker() != getPlayer() || isAlly(e.getVictim())) return;
        EnergyBar energyBar = getEnergyBar();

        lastHit = System.currentTimeMillis();


        KitPlayer berserker = KitPlayerManager.getInstance().getKitPlayer(getPlayer());
        // Find the current percentage of health remaining, then multiply it by our real max HP value (e.g. 40 for zerk right now)
        double trueCurrentHP = (getPlayer().getHealth() / getPlayer().getMaxHealth()) * berserker.getHP();
        double trueMissingHP = berserker.getHP() - trueCurrentHP;
        double heal = trueMissingHP * 0.05;

        double currentEnergy = energyBar.getEnergy();
        if(currentEnergy >= 4) getChampionsPlayer().heal(heal);
        else energyBar.incrementEnergy(1);
    }

    @EventHandler
    public void die(DeathApplyEvent e) {
        if(e.getPlayer() != getPlayer()) return;
        getEnergyBar().incrementEnergy(-getEnergyBar().getMaxEnergy());
    }

    protected EnergyBar getEnergyBar() {
        return getChampionsPlayer().getEnergyBar();
    }


}
