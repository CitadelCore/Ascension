package space.marsden.mods.chaoscore.items

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.NonNullList
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import space.marsden.mods.ascension.Ascension

class ItemMultiple(val mod: String, val parts: Array[String]) extends Item {
  this.setCreativeTab(CreativeTabs.MISC)
  this.setHasSubtypes(true)
  this.setMaxDamage(0)

  def registerItemModels(): Unit = {
    if (!Ascension.proxy.isClient) return
    for (i <- 0 until parts.length) {
      registerItemModel(i)
    }
  }

  protected def registerItemModel(meta: Int) = Ascension.proxy.registerItemModel(this, meta, "ascension:" + parts(meta))

  override def getUnlocalizedName = "item.ascension.unknown"

  override def getUnlocalizedName(stack: ItemStack): String = {
    if (stack.isEmpty) return "item.ascension.unknown"
    "item." + this.mod + "." + this.parts(stack.getItemDamage % parts.length) + "0"
  }

  @SideOnly(Side.CLIENT) override def getSubItems(tab: CreativeTabs, list: NonNullList[ItemStack]): Unit = {
    if (!this.isInCreativeTab(tab)) return
    for (i <- parts.indices) {
      list.add(new ItemStack(this, 1, i))
    }
  }
}