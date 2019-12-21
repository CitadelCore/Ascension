package space.marsden.mods.ascension.common.items.data

import li.cil.oc.common.item.data.{ItemData, MicrocontrollerData}
import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.common.Tier
import li.cil.oc.util.ExtendedNBT._
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants.NBT
import space.marsden.mods.ascension.common.registries.Items

class CoprocessorData(itemName: String = "coprocessorcreative") extends ItemData(itemName) {
  def this(stack: ItemStack) {
    this()
    load(stack)
  }

  var items: Array[ItemStack] = Array.fill[ItemStack](32)(ItemStack.EMPTY)
  var energy = 0.0
  var maxEnergy = 0.0
  var tier: Int = Tier.One
  var container: ItemStack = ItemStack.EMPTY

  private final val ItemsTag = "oc:items"
  private final val SlotTag = "slot"
  private final val ItemTag = "item"
  private final val MachineTag = "machine"
  private final val EnergyTag = "energy"
  private final val MaxEnergyTag = "maxEnergy"
  private final val TierTag = "tier"

  override def load(nbt: NBTTagCompound) {
    nbt.getTagList(ItemsTag, NBT.TAG_COMPOUND).foreach((slotNbt: NBTTagCompound) => {
      val slot = slotNbt.getByte(SlotTag)
      if (slot >= 0 && slot < items.length) {
        items(slot) = new ItemStack(slotNbt.getCompoundTag(ItemTag))
      }
    })

    var machine = nbt.getCompoundTag(MachineTag)
    energy = machine.getDouble(EnergyTag)
    maxEnergy = machine.getDouble(MaxEnergyTag)
    tier = machine.getByte(TierTag)
  }

  override def save(nbt: NBTTagCompound) {
    nbt.setNewTagList(ItemsTag,
      items.zipWithIndex collect {
        case (stack, slot) if !stack.isEmpty => (stack, slot)
      } map {
        case (stack, slot) =>
          val slotNbt = new NBTTagCompound()
          slotNbt.setByte(SlotTag, slot.toByte)
          slotNbt.setNewCompoundTag(ItemTag, stack.writeToNBT)
      })
    nbt.setNewCompoundTag(MachineTag, tag => {
      tag.setDouble(EnergyTag, energy)
      tag.setDouble(MaxEnergyTag, maxEnergy)
      tag.setInteger(TierTag, tier)
    })
  }

  def copyItemStack(): ItemStack = {
    val stack = createItemStack()
    val newInfo = new MicrocontrollerData(stack)
    newInfo.save(stack)
    stack
  }

  override def createItemStack(): ItemStack = {
    if (itemName == null) ItemStack.EMPTY
    else {
      val stack = Items.get(itemName).createItemStack(1)
      save(stack)
      stack
    }
  }
}
