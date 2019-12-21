package space.marsden.mods.ascension.common.inventory

import li.cil.oc.api.Driver
import li.cil.oc.common.InventorySlots.InventorySlot
import li.cil.oc.common.{Slot, Tier}
import li.cil.oc.common.inventory.ItemStackInventory
import li.cil.oc.util.ItemUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import space.marsden.mods.ascension.server.component.Coprocessor

trait CoprocessorInventory extends ItemStackInventory {
  def tier: Int = ItemUtils.caseTier(container) max 0

  override def getSizeInventory: Int = CoprocessorSlots.slots(tier).length

  override protected def inventoryName = "coprocessor"

  override def getInventoryStackLimit = 1

  override def isUsableByPlayer(player: EntityPlayer) = false

  override def isItemValidForSlot(slot: Int, stack: ItemStack): Boolean =
    Option(Driver.driverFor(stack, classOf[Coprocessor])).fold(false)(driver => {
      val provided = CoprocessorSlots.slots(tier)(slot)
      driver.slot(stack) == provided.slot && driver.tier(stack) <= provided.tier
    })

  object CoprocessorSlots {
    val slots: Array[Array[InventorySlot]] = Array(
      Array(
        InventorySlot(Slot.Card, Tier.Any),
        InventorySlot(Slot.Memory, Tier.Any),
        InventorySlot(Slot.CPU, Tier.Any),
        InventorySlot(Slot.EEPROM, Tier.Any)
      )
    )
  }
}
