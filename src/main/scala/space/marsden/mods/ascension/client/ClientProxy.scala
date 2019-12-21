package space.marsden.mods.ascension.client

import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.{ModelBakery, ModelResourceLocation}
import net.minecraft.item.Item
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.client.model.ModelLoader
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import space.marsden.mods.ascension.common.CommonProxy
import space.marsden.mods.chaoscore.items.oc.traits.Delegate

@SideOnly(Side.CLIENT) class ClientProxy extends CommonProxy {
  override def isClient = true

  override def registerItemModel(instance: Delegate, id: String): Unit = {
    val location = "ascension:" + id
    ModelLoader.setCustomModelResourceLocation(instance.parent, instance.itemId, new ModelResourceLocation(location, "inventory"))
    ModelBakery.registerItemVariants(instance.parent, new ResourceLocation(location))
  }

  override def registerItemModel(item: Item, meta: Int, name: String): Unit = if (name.contains("#")) ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(name.split("#")(0), name.split("#")(1)))
  else ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(name, "inventory"))

  override def registerItemModel(block: Block, meta: Int, name: String): Unit = super.registerItemModel(block, meta, name)

  override def preInit(event: FMLPreInitializationEvent): Unit = {
    super.preInit(event)
    MinecraftForge.EVENT_BUS.register(this)
  }

  override def init(event: FMLInitializationEvent): Unit = super.init(event)

  override def postInit(event: FMLPostInitializationEvent): Unit = super.postInit(event)

  override def registerModels(): Unit = super.registerModels()

  override def registerSounds(): Unit = super.registerSounds()
}