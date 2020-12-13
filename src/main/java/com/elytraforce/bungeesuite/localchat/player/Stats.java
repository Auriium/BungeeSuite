package com.elytraforce.bungeesuite.localchat.player;

import java.util.ArrayList;

public class Stats {
    private int level;
    private int xp;
    private int money;
    private ArrayList<Integer> unlockedRewards;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public ArrayList<Integer> getUnlockedRewards() {
        return unlockedRewards;
    }

    public void setUnlockedRewards(ArrayList<Integer> unlockedRewards) {
        this.unlockedRewards = unlockedRewards;
    }

    public Stats(int level, int xp, int money, ArrayList<Integer> unlockedRewards) {
        this.level = level;
        this.xp = xp;
        this.money = money;
        this.unlockedRewards = unlockedRewards;
    }


}
