package space.marsden.mods.ascension.server.component.data

import li.cil.oc.api.Persistable
import net.minecraft.nbt.NBTTagCompound

class CoprocessorFuses extends Persistable {
  var externalIoPolicy: Int = IoPolicy.ReadWrite

  override def load(nbt: NBTTagCompound): Unit = {
    externalIoPolicy = nbt.getInteger("io_policy")
  }

  override def save(nbt: NBTTagCompound): Unit = {
    nbt.setInteger("io_policy", externalIoPolicy)
  }

  object IoPolicy {
    final val ReadWrite = 0
    final val ReadOnly = 1
    final val Disallow = 2
    final val MaxValue = Disallow
  }
}