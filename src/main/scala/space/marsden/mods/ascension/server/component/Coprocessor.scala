package space.marsden.mods.ascension.server.component

import java.lang.Iterable
import java.util

import li.cil.oc.api.driver.DeviceInfo
import li.cil.oc.api.driver.DeviceInfo.{DeviceAttribute, DeviceClass}
import li.cil.oc.api.machine.{Arguments, Callback, Context, MachineHost}
import li.cil.oc.api.network.{EnvironmentHost, Message, Node, Visibility}
import li.cil.oc.api.util.StateAware
import li.cil.oc.api.{Machine, Network}
import li.cil.oc.common.inventory.ComponentInventory
import li.cil.oc.util.ExtendedNBT._
import li.cil.oc.util.ResultWrapper.result
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import space.marsden.mods.ascension.common.drivers.ManagedEnvironmentWithComponentConnector
import space.marsden.mods.ascension.common.inventory.CoprocessorInventory
import space.marsden.mods.ascension.server.component.data.CoprocessorFuses

import scala.collection.convert.WrapAsJava._

class Coprocessor(var stack: ItemStack, val env: EnvironmentHost) extends ManagedEnvironmentWithComponentConnector with MachineHost with CoprocessorInventory with ComponentInventory with StateAware with DeviceInfo {
  lazy val machine: li.cil.oc.api.machine.Machine = Machine.create(this)
  this.setNode(Network.newNode(this, Visibility.Network).withComponent("coprocessor").withConnector(32).create())

  private val BUFFER_SIZE = 2000.0
  private var wasRunning = false
  private var hadErrored = false

  // these are to ensure start/stop methods don't get called multiple times on fail
  private var startAttempted = false
  private var stopAttempted = false

  // Holds the security fuses for the coprocessor.
  lazy val fuses: CoprocessorFuses = new CoprocessorFuses()

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.System,
    DeviceAttribute.Description -> "Coprocessor",
    DeviceAttribute.Vendor -> "KUISER Laboratories",
    DeviceAttribute.Product -> "MicroCORE Zero",
    DeviceAttribute.Serial -> "KU-Z1C00600"
  )

  override def getDeviceInfo: util.Map[String, String] = deviceInfo

  // Environment
  override def onConnect(node: Node): Unit = { // startup?
    if (node == this.node) {
      connectComponents()

      machine.node().connect(node)
      if (node.network() != null && !startAttempted) {
        machine.start()
        startAttempted = true
      }
    }
  }

  override def onDisconnect(node: Node): Unit = { // shutdown?
    if (node == this.node) {
      if (node.network() != null && !stopAttempted) {
        machine.stop()
        stopAttempted = true
      }
      machine.node().disconnect(node)

      disconnectComponents()
    }
  }

  override def onMessage(message: Message): Unit = {
    super.onMessage(message)

    // ignore messages from nodes that aren't neighbors
    if (node.isNeighborOf(message.source)) return

    //if(message.name().equals("computer.stopped")) {
    // stop the internal computer
    //    machine.stop();
    //}
    //if(message.name().equals("computer.started"))
    // start the internal computer
    //  machine.start();
  }

  override def load(nbt: NBTTagCompound): Unit = {
    super.load(nbt)
    if (!env.world.isRemote) {
      fuses.load(nbt.getCompoundTag("fuses"))
      machine.load(nbt.getCompoundTag("machine"))
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    if (!env.world.isRemote) {
      nbt.setNewCompoundTag("fuses", fuses.save)
      nbt.setNewCompoundTag("machine", machine.save)
    }
  }

  // MachineHost
  override def internalComponents: Iterable[ItemStack] = (0 until getSizeInventory).collect {
    case i if !getStackInSlot(i).isEmpty && isComponentSlot(i, getStackInSlot(i)) => getStackInSlot(i)
  }

  override def componentSlot(address: String): Int = components.indexWhere(_.exists(env => env.node != null && env.node().address() == address))

  override def onMachineConnect(node: Node): Unit = onConnect(node)

  override def onMachineDisconnect(node: Node): Unit = onDisconnect(node)

  // EnvironmentHost
  override def xPosition: Double = env.xPosition

  override def yPosition: Double = env.yPosition

  override def zPosition: Double = env.zPosition

  override def world : World = env.world

  override def markChanged(): Unit = env.markChanged()

  // ItemStackInventory
  override def host: EnvironmentHost = env

  // ComponentInventory
  override def container: ItemStack = stack;

  override protected def connectItemNode(node: Node): Unit = {
    if (node != null) {
      Network.joinNewNetwork(machine.node())
      machine.node().connect(node)
    }
  }

  override protected def onItemRemoved(slot: Int, stack: ItemStack): Unit = {
    super.onItemRemoved(slot, stack)
  }

  // ManagedEnvironment
  override def canUpdate: Boolean = true

  override def update(): Unit = {
    if (!env.world.isRemote) {
      machine.update()

      val isRunning = machine.isRunning
      val hasErrored = machine.lastError != null
      if (isRunning != wasRunning || hasErrored != hadErrored) {
        env.markChanged()
      }
      wasRunning = isRunning
      hadErrored = hasErrored
    }

    updateComponents()
  }

  // StateAware
  override def getCurrentState: util.EnumSet[StateAware.State] = {
    if (machine.isRunning) util.EnumSet.of(StateAware.State.IsWorking)
    else util.EnumSet.noneOf(classOf[StateAware.State])
  }

  // OC methods
  @Callback(doc = """function():string; Gets the last boot error, if permitted""", direct = true)
  def getLastError(context: Context, args: Arguments): Array[AnyRef] = {
    result(machine.lastError())
  }

  @Callback(doc = """function():table; Gets the values of the internal e-fuses""", direct = true)
  def getFuses(context: Context, args: Arguments): Array[AnyRef] = {
    result(Map(
      "io_policy" -> fuses.externalIoPolicy
    ))
  }

  @Callback(doc = """function(name:string, value:string or nil); Sets a single e-fuse""", direct = true)
  def setFuse(context: Context, args: Arguments): Array[AnyRef] = {
    if (!args.isString(0)) {
      return result(-1, "invalid parameter")
    }

    args.checkString(0) match {
      case "io_policy" => {
        if (args.isInteger(1)) {
          val value = args.checkInteger(1)
          if (value < 0 || value > fuses.IoPolicy.MaxValue)
            return result(-1, "fuse value out of range")
          fuses.externalIoPolicy = value
          result()
        } else {
          fuses.externalIoPolicy = fuses.IoPolicy.ReadWrite
          result()
        }
      }
      case _ => result(-1, "invalid fuse")
    }
  }

  @Callback(doc = """function(value:string):boolean; Invokes a RPC (remote procedure call) method on the coprocessor.""")
  def call(context: Context, args: Arguments): Array[AnyRef] = {
    try {
      // invoke the method on the coprocessor machine
      val res = machine.signal("rpc.call", args)
      result(res)
    } catch {
      case e: Exception =>
        result(false)
    }
  }
}