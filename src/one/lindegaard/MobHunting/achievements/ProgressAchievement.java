package one.lindegaard.MobHunting.achievements;

import one.lindegaard.MobHunting.mobs.MinecraftMob;

public interface ProgressAchievement extends Achievement
{
	public int getMaxProgress();
	
	public String inheritFrom();
	
	public String nextLevelId();
	
	public MinecraftMob getExtendedMobType();
}
