package space.marsden.mods.ascension.common.drivers

import li.cil.oc.api.detail.ItemInfo
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.api.internal
import li.cil.oc.common.Tier
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import space.marsden.mods.ascension.common.registries.Items

trait DriverItem extends li.cil.oc.api.driver.DriverItem {
  override def tier(stack: ItemStack): Int = Tier.One

  override def dataTag(stack: ItemStack): NBTTagCompound = DriverItem.dataTag(stack)

  protected def isOneOf(stack: ItemStack, items: ItemInfo*): Boolean = items.filter(_ != null).contains(Items.get(stack))

  protected def isAdapter(host: Class[_ <: EnvironmentHost]): Boolean = classOf[internal.Adapter].isAssignableFrom(host)

  protected def isComputer(host: Class[_ <: EnvironmentHost]): Boolean = classOf[internal.Case].isAssignableFrom(host)

  protected def isRobot(host: Class[_ <: EnvironmentHost]): Boolean = classOf[internal.Robot].isAssignableFrom(host)

  protected def isRotatable(host: Class[_ <: EnvironmentHost]): Boolean = classOf[internal.Rotatable].isAssignableFrom(host)

  protected def isServer(host: Class[_ <: EnvironmentHost]): Boolean = classOf[internal.Server].isAssignableFrom(host)

  protected def isTablet(host: Class[_ <: EnvironmentHost]): Boolean = classOf[internal.Tablet].isAssignableFrom(host)

  protected def isMicrocontroller(host: Class[_ <: EnvironmentHost]): Boolean = classOf[internal.Microcontroller].isAssignableFrom(host)

  protected def isDrone(host: Class[_ <: EnvironmentHost]): Boolean = classOf[internal.Drone].isAssignableFrom(host)

  object DriverItem {
    def dataTag(stack: ItemStack): NBTTagCompound = {
      if (!stack.hasTagCompound) {
        stack.setTagCompound(new NBTTagCompound())
      }
      val nbt = stack.getTagCompound
      if (!nbt.hasKey("asc:data")) {
        nbt.setTag("asc:data", new NBTTagCompound())
      }
      nbt.getCompoundTag("asc:data")
    }
  }
}