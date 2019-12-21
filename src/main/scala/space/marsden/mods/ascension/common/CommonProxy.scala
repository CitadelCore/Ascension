package space.marsden.mods.ascension.common

import li.cil.oc.api.Driver
import li.cil.oc.api.driver.{DriverItem, EnvironmentProvider}
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import space.marsden.mods.ascension.common.drivers.{DriverCardCoprocessor, DriverCardSniffer}
import space.marsden.mods.ascension.common.registries.Items
import space.marsden.mods.ascension.common.template.CoprocessorTemplate
import space.marsden.mods.chaoscore.items.oc.traits.Delegate

class CommonProxy {
  def isClient = false

  def registerItemModel(instance: Delegate, id: String): Unit = {}

  def registerItemModel(item: Item, meta: Int, name: String): Unit = {}

  def registerItemModel(block: Block, meta: Int, name: String): Unit = {
    registerItemModel(Item.getItemFromBlock(block), meta, name)
  }

  def getWorld(dimId: Int) = null

  def registerSounds(): Unit = {
  }

  def preInit(event: FMLPreInitializationEvent): Unit = {
    Items.init()
  }

  def init(event: FMLInitializationEvent): Unit = {
    // register templates
    CoprocessorTemplate.register()

    // register drivers
    Driver.add(DriverCardCoprocessor)
    Driver.add(DriverCardCoprocessor.Provider)

    Driver.add(DriverCardSniffer)
    Driver.add(DriverCardSniffer.Provider)
  }

  def postInit(event: FMLPostInitializationEvent) : Unit = {

  }

  def registerRenderers(): Unit = {
  }

  def registerModels(): Unit = {
  }
}