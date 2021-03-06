package one.lindegaard.MobHunting.modifier;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import one.lindegaard.MobHunting.DamageInformation;
import one.lindegaard.MobHunting.HuntData;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;

public class SneakyBonus implements IModifier {

	@Override
	public String getName() {
		return ChatColor.BLUE + Messages.getString("bonus.sneaky.name"); //$NON-NLS-1$
	}

	@Override
	public double getMultiplier(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		return MobHunting.getConfigManager().bonusSneaky;
	}

	@Override
	public boolean doesApply(Entity deadEntity, Player killer, HuntData data, DamageInformation extraInfo,
			EntityDamageByEntityEvent lastDamageCause) {
		if (!(deadEntity instanceof Creature))
			return false;

		if (extraInfo.mele || extraInfo.weapon.getType() == Material.POTION)
			return ((Creature) deadEntity).getTarget() == null;

		return false;
	}

}
