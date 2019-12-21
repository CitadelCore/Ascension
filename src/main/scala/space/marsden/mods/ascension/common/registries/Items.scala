package space.marsden.mods.ascension.common.registries

import java.util.concurrent.Callable

import li.cil.oc.{Settings, common}
import li.cil.oc.api.detail.ItemAPI
import li.cil.oc.api.detail.ItemInfo
import li.cil.oc.api.fs.FileSystem
import li.cil.oc.common.Loot
import li.cil.oc.common.Tier
import li.cil.oc.common.block.SimpleBlock
import li.cil.oc.common.item.traits.SimpleItem
import li.cil.oc.server.machine.luac.LuaStateFactory
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.GameData
import space.marsden.mods.ascension.{Ascension, Constants}
import space.marsden.mods.ascension.common.items.CardCoprocessor
import space.marsden.mods.ascension.common.items.data.CoprocessorData
import space.marsden.mods.chaoscore.items.oc.Delegator
import space.marsden.mods.chaoscore.items.oc.traits.Delegate

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object Items extends ItemAPI {
  val descriptors = mutable.Map.empty[String, ItemInfo]

  val names = mutable.Map.empty[Any, String]

  val aliases = Map()

  override def get(name: String): ItemInfo = descriptors.get(name).orNull

  override def get(stack: ItemStack): ItemInfo = names.get(getBlockOrItem(stack)) match {
    case Some(name) => get(name)
    case _ => null
  }

  def registerBlock(instance: Block, id: String): Block = {
    if (!descriptors.contains(id)) {
      instance match {
        case simple: SimpleBlock =>
          instance.setUnlocalizedName("item.ascension." + id)
          instance.setRegistryName(id)
          GameData.register_impl(instance)
          Ascension.proxy.registerItemModel(instance, 0, id)

          val item : Item = new common.block.Item(instance)
          item.setUnlocalizedName("item.ascension." + id)
          item.setRegistryName(id)
          GameData.register_impl(item)
          Ascension.proxy.registerItemModel(item, 0, id)
        case _ =>
      }
      descriptors += id -> new ItemInfo {
        override def name: String = id

        override def block = instance

        override def item = null

        override def createItemStack(size: Int): ItemStack = instance match {
          case simple: SimpleBlock => simple.createItemStack(size)
          case _ => new ItemStack(instance, size)
        }
      }
      names += instance -> id
    }
    instance
  }

  def registerItem[T <: Delegate](delegate: T, id: String): T = {
    if (!descriptors.contains(id)) {
      Ascension.proxy.registerItemModel(delegate, id)
      descriptors += id -> new ItemInfo {
        override def name: String = id

        override def block = null

        override def item: Delegator = delegate.parent

        override def createItemStack(size: Int): ItemStack = delegate.createItemStack(size)
      }
      names += delegate -> id
    }
    delegate
  }

  def registerItem(instance: Item, id: String): Item = {
    if (!descriptors.contains(id)) {
      instance match {
        case simple: SimpleItem =>
          simple.setUnlocalizedName("item.ascension." + id)
          GameData.register_impl(simple.setRegistryName(new ResourceLocation(Ascension.MODID, id)))
          Ascension.proxy.registerItemModel(instance, 0, id)
        case _ =>
      }
      descriptors += id -> new ItemInfo {
        override def name: String = id

        override def block = null

        override def item: Item = instance

        override def createItemStack(size: Int): ItemStack = instance match {
          case simple: SimpleItem => simple.createItemStack(size)
          case _ => new ItemStack(instance, size)
        }
      }
      names += instance -> id
    }
    instance
  }

  def registerStack(stack: ItemStack, id: String): ItemStack = {
    val immutableStack = stack.copy()
    descriptors += id -> new ItemInfo {
      override def name: String = id

      override def block = null

      override def createItemStack(size: Int): ItemStack = {
        val copy = immutableStack.copy()
        copy.setCount(size)
        copy
      }

      override def item: Item = immutableStack.getItem
    }
    stack
  }

  private def getBlockOrItem(stack: ItemStack): Any =
    if (stack.isEmpty) null
    else Delegator.subItem(stack).getOrElse(stack.getItem match {
      case block: ItemBlock => block.getBlock
      case item => item
    })

  // ----------------------------------------------------------------------- //

  val registeredItems: ArrayBuffer[ItemStack] = mutable.ArrayBuffer.empty[ItemStack]

  override def registerFloppy(name: String, color: EnumDyeColor, factory: Callable[FileSystem], doRecipeCycling: Boolean): ItemStack = {
    val stack = Loot.registerLootDisk(name, color, factory, doRecipeCycling)

    registeredItems += stack
    stack.copy()
  }

  override def registerEEPROM(name: String, code: Array[Byte], data: Array[Byte], readonly: Boolean): ItemStack = {
    val nbt = new NBTTagCompound()
    if (name != null) {
      nbt.setString("asc:label", name.trim.take(24))
    }
    if (code != null) {
      nbt.setByteArray("asc:eeprom", code.take(Settings.get.eepromSize))
    }
    if (data != null) {
      nbt.setByteArray("asc:userdata", data.take(Settings.get.eepromDataSize))
    }
    nbt.setBoolean("asc:readonly", readonly)

    val stackNbt = new NBTTagCompound()
    stackNbt.setTag("asc:data", nbt)

    val stack = get(li.cil.oc.Constants.ItemName.EEPROM).createItemStack(1)
    stack.setTagCompound(stackNbt)

    registeredItems += stack
    stack.copy()
  }

  // ----------------------------------------------------------------------- //

  private def safeGetStack(name: String) = Option(get(name)).map(_.createItemStack(1)).getOrElse(ItemStack.EMPTY)

  def createConfiguredCoprocessor(): ItemStack = {
    val data = new CoprocessorData()

    data.tier = Tier.Four
    data.energy = Settings.get.bufferMicrocontroller.toInt
    data.maxEnergy = data.energy
    data.items = Array(
      li.cil.oc.api.Items.get(li.cil.oc.Constants.ItemName.NetworkCard).createItemStack(1),
      li.cil.oc.api.Items.get(li.cil.oc.Constants.ItemName.RAMTier6).createItemStack(1),
      LuaStateFactory.setDefaultArch(li.cil.oc.api.Items.get(li.cil.oc.Constants.ItemName.CPUTier3).createItemStack(1)),
      li.cil.oc.api.Items.get(li.cil.oc.Constants.ItemName.LuaBios).createItemStack(1)
    ).padTo(32, ItemStack.EMPTY)

    data.createItemStack()
  }

  // ----------------------------------------------------------------------- //

  def init() {
    val components = newItem(new Delegator(), "component")

    registerItem(new CardCoprocessor(components, Tier.One), Constants.ItemName.CoprocessorTier1)
    registerItem(new CardCoprocessor(components, Tier.Two), Constants.ItemName.CoprocessorTier2)
    registerItem(new CardCoprocessor(components, Tier.Three), Constants.ItemName.CoprocessorTier3)
    registerItem(new CardCoprocessor(components, Tier.Four), Constants.ItemName.CoprocessorCreative)

    val misc: Delegator = newItem(new Delegator() {
      private def configuredItems: Array[ItemStack] = Array(
        createConfiguredCoprocessor()
      ) ++ registeredItems

      override def getSubItems(tab: CreativeTabs, list: NonNullList[ItemStack]): Unit = {
        super.getSubItems(tab, list)
        if(isInCreativeTab(tab)){
          configuredItems.foreach(list.add)
        }
      }
    }, "misc")

    //registerItem(new CardCoprocessor(misc, Tier.Four), "coprocessor")

    // Register aliases.
    for ((k, v) <- aliases) {
      descriptors.getOrElseUpdate(k, descriptors(v))
    }
  }

  private def newItem[T <: Item](item: T, name: String): T = {
    item.setUnlocalizedName("item.ascension." + name)
    GameData.register_impl(item.setRegistryName(new ResourceLocation(Ascension.MODID, name)))
    item
  }
}
