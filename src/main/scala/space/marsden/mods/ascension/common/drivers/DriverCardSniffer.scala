package space.marsden.mods.ascension.common.drivers

import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.internal.Adapter
import li.cil.oc.api.network.{EnvironmentHost, ManagedEnvironment}
import li.cil.oc.common.{Slot, Tier}
import net.minecraft.item.ItemStack
import space.marsden.mods.ascension.Constants
import space.marsden.mods.ascension.common.drivers.DriverCardCoprocessor.isOneOf
import space.marsden.mods.ascension.common.items.{CardCoprocessor, CardSniffer}
import space.marsden.mods.ascension.common.registries.Items
import space.marsden.mods.ascension.server.component.{Coprocessor, Sniffer}
import space.marsden.mods.chaoscore.items.oc.Delegator

object DriverCardSniffer extends DriverItem {
  override def worksWith(stack: ItemStack): Boolean = isOneOf(stack, Items.get(Constants.ItemName.Sniffer))

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): ManagedEnvironment =
    if (host.world != null && host.world.isRemote) null
    else new Sniffer(host)

  override def slot(stack: ItemStack): String = Slot.Card

  override def tier(stack: ItemStack): Int = Tier.Two

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack)) classOf[CardSniffer]
      else null
  }
}
