package space.marsden.mods.ascension.common.items

import li.cil.oc.util.Rarity
import net.minecraft.item.{EnumRarity, ItemStack}
import space.marsden.mods.chaoscore.items.oc.Delegator
import space.marsden.mods.chaoscore.items.oc.traits.Delegate

class CardSniffer(val parent: Delegator, val tier: Int) extends Delegate {
  override val unlocalizedName: String = super.unlocalizedName + tier

  override protected def tooltipName: Option[String] = Option(super.unlocalizedName)

  override def rarity(stack: ItemStack): EnumRarity = Rarity.byTier(tier)

  override def maxStackSize = 1
}
