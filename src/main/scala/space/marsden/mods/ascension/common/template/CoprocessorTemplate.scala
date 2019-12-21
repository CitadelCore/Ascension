package space.marsden.mods.ascension.common.template

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.common.Slot
import li.cil.oc.common.Tier
import li.cil.oc.common.item.data.MicrocontrollerData
import li.cil.oc.common.template.Template
import li.cil.oc.util.ItemUtils
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import space.marsden.mods.ascension.common.items.data.CoprocessorData
import space.marsden.mods.ascension.server.component.Coprocessor

import scala.collection.convert.WrapAsJava._

object CoprocessorTemplate extends Template {
  override protected val suggestedComponents: Array[(String, IInventory => Boolean)] = Array(
    "BIOS" -> hasComponent("""eeprom"""))

  override protected def hostClass: Class[Coprocessor] = classOf[Coprocessor]

  def selectTier1(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.ItemName.MicrocontrollerCaseTier1)

  def selectTier2(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.ItemName.MicrocontrollerCaseTier2)

  def selectTier3(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.ItemName.MicrocontrollerCaseTier2)

  def selectTierCreative(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.ItemName.MicrocontrollerCaseCreative)

  def validate(inventory: IInventory): Array[AnyRef] = validateComputer(inventory)

  def assemble(inventory: IInventory): Array[Object] = {
    val items = (0 until inventory.getSizeInventory).map(inventory.getStackInSlot)
    val data = new CoprocessorData()
    data.tier = caseTier(inventory)
    data.items = items.drop(1).filter(_ != null).toArray
    data.energy = Settings.get.bufferMicrocontroller.toInt
    data.maxEnergy = data.energy
    val stack = data.createItemStack()
    val energy = Settings.get.microcontrollerBaseCost + complexity(inventory) * Settings.get.microcontrollerComplexityCost

    Array(stack, Double.box(energy))
  }

  def selectDisassembler(stack: ItemStack): Boolean = api.Items.get(stack) == api.Items.get(Constants.BlockName.Microcontroller)

  def disassemble(stack: ItemStack, ingredients: Array[ItemStack]): Array[ItemStack] = {
    val info = new MicrocontrollerData(stack)
    val itemName = Constants.ItemName.MicrocontrollerCase(info.tier)

    Array(api.Items.get(itemName).createItemStack(1)) ++ info.components
  }

  def register() {
    // Tier 1
    api.IMC.registerAssemblerTemplate(
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
    api.IMC.registerAssemblerTemplate(
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
    api.IMC.registerAssemblerTemplate(
      "Coprocessor Card (Tier 3)",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.selectTier2",
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
    api.IMC.registerAssemblerTemplate(
      "Coprocessor Card (Creative)",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.selectTierCreative",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.validate",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.assemble",
      hostClass,
      null,
      Array(
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three,
        Tier.Three
      ),
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
    api.IMC.registerDisassemblerTemplate(
      "Coprocessor",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.selectDisassembler",
      "space.marsden.mods.ascension.common.template.CoprocessorTemplate.disassemble")
  }

  override protected def maxComplexity(inventory: IInventory): Int =
    if (caseTier(inventory) == Tier.Two) 5
    else if (caseTier(inventory) == Tier.Four) 9001 // Creative
    else 4

  override protected def caseTier(inventory: IInventory): Int = ItemUtils.caseTier(inventory.getStackInSlot(0))
}
