package space.marsden.mods.ascension

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent, FMLServerStartingEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper
import net.minecraftforge.fml.common.registry.GameRegistry
import org.apache.logging.log4j.{LogManager, Logger}
import space.marsden.mods.ascension.common.CommonProxy
import space.marsden.mods.ascension.common.registries.Items


@Mod(
  modid = Ascension.MODID,
  name = Ascension.NAME,
  version = Ascension.VERSION,
  dependencies = "required-after:opencomputers;after:rtfm",
  modLanguage = "scala",
  useMetadata = true
)
@Mod.EventBusSubscriber object Ascension {
  final val MODID = "ascension"
  final val NAME = "Ascension"
  final val VERSION = "1.0"

  def log: Logger = LogManager.getLogger(NAME)
  var logger: Option[Logger] = None

  var debug = false

  @SidedProxy(clientSide = "space.marsden.mods.ascension.client.ClientProxy", serverSide = "space.marsden.mods.ascension.common.CommonProxy")
  var proxy: CommonProxy = _

  var network: SimpleNetworkWrapper = _
  var tab: CreativeTabs = new CreativeTabs("tabAscension") {
    override def getTabIconItem: ItemStack = Items.get(Constants.ItemName.CoprocessorCreative).createItemStack(1)
  }

  @SubscribeEvent
  def onRegisterModels(event: ModelRegistryEvent): Unit = {
    proxy.registerModels()
  }

  @EventHandler
  def preInit(event: FMLPreInitializationEvent): Unit = {
    logger = Option(event.getModLog)
    proxy.registerSounds()
    network = NetworkRegistry.INSTANCE.newSimpleChannel("Ascension")

    proxy.preInit(event)

    val packetId = 0
    // network.register
  }

  @EventHandler
  def init(event: FMLInitializationEvent): Unit = {
    Ascension.proxy.init(event)
  }

  @EventHandler
  def postInit(event: FMLPostInitializationEvent): Unit = {
    Ascension.proxy.postInit(event);
  }

  def registerItem(item: Item, name: String): Unit = {
    GameRegistry.findRegistry(classOf[Item]).register(item.setRegistryName(new ResourceLocation(Ascension.MODID, name)))
  }
}
