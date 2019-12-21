package space.marsden.mods.ascension.common.drivers

import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.HostAware
import li.cil.oc.api.internal.Adapter
import li.cil.oc.api.network.{EnvironmentHost, ManagedEnvironment}
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import net.minecraft.item.ItemStack
import space.marsden.mods.ascension.Constants
import space.marsden.mods.ascension.common.items.CardCoprocessor
import space.marsden.mods.ascension.common.registries.Items
import space.marsden.mods.ascension.server.component.Coprocessor
import space.marsden.mods.chaoscore.items.oc.Delegator

object DriverCardCoprocessor extends DriverItem with HostAware {
  override def worksWith(stack: ItemStack): Boolean =
    isOneOf(stack,
      Items.get(Constants.ItemName.CoprocessorCreative),
      Items.get(Constants.ItemName.CoprocessorTier1),
      Items.get(Constants.ItemName.CoprocessorTier2),
      Items.get(Constants.ItemName.CoprocessorTier3)
    )

  override def worksWith(stack: ItemStack, host: Class[_ <: EnvironmentHost]): Boolean = {
    var works = worksWith(stack)
    works = works && !classOf[Adapter].isAssignableFrom(host) && !classOf[Coprocessor].isAssignableFrom(host)
    works
  }

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): ManagedEnvironment =
    if (host.world != null && host.world.isRemote) null
    else new Coprocessor(stack, host)

  override def slot(stack: ItemStack): String = Slot.Card

  override def tier(stack: ItemStack): Int =
    Delegator.subItem(stack) match {
      //case Some(card: CardCoprocessor) => card.tier
      case _ => Tier.One
    }

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack)) classOf[CardCoprocessor]
      else null
  }
}
