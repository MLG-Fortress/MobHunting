package one.lindegaard.MobHunting.modifier;

import org.bukkit.ChatColor;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;

public class ReturnToSenderBonus implements IModifier
{

	@Override
	public String getName()
	{
		return ChatColor.GOLD + Messages.getString("bonus.returntosender.name"); //$NON-NLS-1$
	}

	@Override
	public double getMultiplier(LivingEntity deadEntity, Player killer, HuntData data, DamageInformation extraInfo, EntityDamageByEntityEvent lastDamageCause)
	{
		return MobHunting.config().bonusReturnToSender;
	}

	@Override
	public boolean doesApply( LivingEntity deadEntity, Player killer, HuntData data, DamageInformation extraInfo, EntityDamageByEntityEvent lastDamageCause )
	{
		if(!(deadEntity instanceof Ghast))
			return false;
		if(!(deadEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent))
			return false;

		return (((EntityDamageByEntityEvent)deadEntity.getLastDamageCause()).getDamager() instanceof LargeFireball);
	}

}