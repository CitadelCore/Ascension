package space.marsden.mods.ascension.common.template

import li.cil.oc.Settings
import li.cil.oc.api.IMC
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.common.item.data.MicrocontrollerData
import li.cil.oc.common.template.Template
import li.cil.oc.util.ItemUtils
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import space.marsden.mods.ascension.Constants
import space.marsden.mods.ascension.common.items.data.CoprocessorData
import space.marsden.mods.ascension.common.registries.Items
import space.marsden.mods.ascension.server.component.Coprocessor

import scala.collection.convert.WrapAsJava._

object CoprocessorTemplate extends Template {
  override protected val suggestedComponents: Array[(String, IInventory => Boolean)] = Array(
    "BIOS" -> hasComponent("""eeprom"""))

  override protected def hostClass: Class[Coprocessor] = classOf[Coprocessor]

  def selectTier1(stack: ItemStack): Boolean = Items.get(stack) == Items.get(Constants.ItemName.CoprocessorCaseTier1)

  def selectTier2(stack: ItemStack): Boolean = Items.get(stack) == Items.get(Constants.ItemName.CoprocessorCaseTier2)

  def selectTier3(stack: ItemStack): Boolean = Items.get(stack) == Items.get(Constants.ItemName.CoprocessorCaseTier3)

  def selectTierCreative(stack: ItemStack): Boolean = Items.get(stack) == Items.get(Constants.ItemName.CoprocessorCaseCreative)

  def validate(inventory: IInventory): Array[AnyRef] = validateComputer(inventory)

  def assemble(inventory: IInventory): Array[Object] = {
    val tier = caseTier(inventory)
    val items = (1 until inventory.getSizeInventory).map(inventory.getStackInSlot)
    val data = new CoprocessorData(Constants.ItemName.Coprocessor(tier))
    data.tier = tier
    data.items = items.drop(1).filter(!_.isEmpty).toArray
    data.energy = Settings.get.bufferMicrocontroller.toInt
    data.maxEnergy = data.energy
    val stack = data.createItemStack()
    val energy = Settings.get.microcontrollerBaseCost + complexity(inventory) * Settings.get.microcontrollerComplexityCost

    Array(stack, Double.box(energy))
  }

  def selectDisassembler(stack: ItemStack): Boolean = Items.get(stack).name() match {
    case Constants.ItemName.CoprocessorCaseTier1 => true
    case Constants.ItemName.CoprocessorCaseTier2 => true
    case Constants.ItemName.CoprocessorCaseTier3 => true
    case Constants.ItemName.CoprocessorCaseCreative => true
    case _ => false
  }

  def disassemble(stack: ItemStack, ingredients: Array[ItemStack]): Array[ItemStack] = {
    val info = new CoprocessorData(stack)
    val itemName = Constants.ItemName.CoprocessorCase(info.tier)

    Array(Items.get(itemName).createItemStack(1)) ++ info.items
  }

  def register() {
    // Tier 1
    IMC.registerAssemblerTemplate(
      "Coprocessor Card (Tier 1)",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.selectTier1",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.validate",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.assemble",
      hostClass,
      null,
      null,
      asJavaIterable(Iterable(
        null,
        null,
        null,
        (Slot.CPU, Tier.One),
        (Slot.Memory, Tier.One),
        null,
        (Slot.EEPROM, Tier.Any)
      ).map(toPair)))

    // Tier 2
    IMC.registerAssemblerTemplate(
      "Coprocessor Card (Tier 2)",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.selectTier2",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.validate",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.assemble",
      hostClass,
      null,
      null,
      asJavaIterable(Iterable(
        (Slot.Card, Tier.One),
        null,
        null,
        (Slot.CPU, Tier.One),
        (Slot.Memory, Tier.One),
        null,
        (Slot.EEPROM, Tier.Any)
      ).map(toPair)))

    // Tier 3
    IMC.registerAssemblerTemplate(
      "Coprocessor Card (Tier 3)",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.selectTier3",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.validate",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.assemble",
      hostClass,
      null,
      null,
      asJavaIterable(Iterable(
        (Slot.Card, Tier.Two),
        null,
        null,
        (Slot.CPU, Tier.One),
        (Slot.Memory, Tier.One),
        null,
        (Slot.EEPROM, Tier.Any)
      ).map(toPair)))

    // Creative
    IMC.registerAssemblerTemplate(
      "Coprocessor Card (Creative)",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.selectTierCreative",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.validate",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.assemble",
      hostClass,
      null,
      null,
      asJavaIterable(Iterable(
        (Slot.Card, Tier.Three),
        (Slot.Card, Tier.Three),
        (Slot.Card, Tier.Three),
        (Slot.CPU, Tier.Three),
        (Slot.Memory, Tier.Three),
        (Slot.Memory, Tier.Three),
        (Slot.EEPROM, Tier.Any)
      ).map(toPair)))

    // Disassembler
    IMC.registerDisassemblerTemplate(
      "Coprocessor",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.selectDisassembler",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.disassemble")
  }

  override protected def maxComplexity(inventory: IInventory): Int =
    if (caseTier(inventory) == Tier.Two) 5
    else if (caseTier(inventory) == Tier.Four) 9001 // Creative
    else 4

  override protected def caseTier(inventory: IInventory): Int = {
    val stack = inventory.getStackInSlot(0)
    val descriptor = Items.get(stack)

    if (descriptor == Items.get(Constants.ItemName.CoprocessorCaseTier1)) Tier.One
    else if (descriptor == Items.get(Constants.ItemName.CoprocessorCaseTier2)) Tier.Two
    else if (descriptor == Items.get(Constants.ItemName.CoprocessorCaseTier3)) Tier.Three
    else if (descriptor == Items.get(Constants.ItemName.CoprocessorCaseCreative)) Tier.Four
    else Tier.None
  }
}
