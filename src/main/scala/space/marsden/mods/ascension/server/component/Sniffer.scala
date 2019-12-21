package space.marsden.mods.ascension.server.component

import java.util

import com.google.common.base.Charsets
import li.cil.oc.api.Network
import li.cil.oc.api.driver.DeviceInfo.{DeviceAttribute, DeviceClass}
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network.{EnvironmentHost, Packet, Visibility}
import li.cil.oc.server.PacketSender
import li.cil.oc.server.component.NetworkCard
import li.cil.oc.util.ResultWrapper.result
import net.minecraft.nbt.NBTTagCompound

import scala.collection.convert.WrapAsJava._
import scala.collection.convert.WrapAsScala._

class Sniffer(container: EnvironmentHost) extends NetworkCard(container) {
  this.setNode(Network.newNode(this, Visibility.Network).withComponent("modem", Visibility.Neighbors).withConnector().create())

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Network,
    DeviceAttribute.Description -> "Network Router Card",
    DeviceAttribute.Vendor -> "CORE Heavy Industries Plc",
    DeviceAttribute.Product -> "RT-912B (Sniff/Spoof option)",
    DeviceAttribute.Serial -> "CHI-601902"
  )

  override def getDeviceInfo: util.Map[String, String] = deviceInfo

  var sniff = false

  override def load(nbt: NBTTagCompound): Unit = {
    super.load(nbt)
    if (!container.world.isRemote) {
      sniff = nbt.getBoolean("sniff")
    }
  }

  override def save(nbt: NBTTagCompound): Unit = {
    super.save(nbt)
    if (!container.world.isRemote) {
      nbt.setBoolean("sniff", sniff)
    }
  }

  // this code is pretty much cloned from WakeMessageAware, however it omits the packet destination check
  protected override def receivePacket(packet: Packet, distance: Double, host: EnvironmentHost): Unit = {
    if (packet.source != node.address) {
      // if we are not in sniffing mode, reject the packet
      if (!sniff && !Option(packet.destination).forall(_ == node.address)) return
      if (isPacketAccepted(packet, distance)) {
        node.sendToReachable("computer.signal", Seq("modem_message", packet.source, Int.box(packet.port), Double.box(distance)) ++ packet.data: _*)
      }

      // Accept wake-up messages regardless of port because we close all ports
      // when our computer shuts down.
      val wakeup: Boolean = packet.data match {
        case Array(message: Array[Byte]) if wakeMessage.contains(new String(message, Charsets.UTF_8)) => true
        case Array(message: String) if wakeMessage.contains(message) => true
        case Array(message: Array[Byte], _*) if wakeMessageFuzzy && wakeMessage.contains(new String(message, Charsets.UTF_8)) => true
        case Array(message: String, _*) if wakeMessageFuzzy && wakeMessage.contains(message) => true
        case _ => false
      }
      if (wakeup) {
        host match {
          case ctx: Context => ctx.start()
          case _ => node.sendToNeighbors("computer.start")
        }
      }
    }
  }

  override protected def isPacketAccepted(packet: Packet, distance: Double): Boolean = {
    if (!sniff && !openPorts.contains(packet.port)) return false

    networkActivity()
    true
  }

  private def networkActivity() {
    host match {
      case h: EnvironmentHost => PacketSender.sendNetworkActivity(node, h)
      case _ =>
    }
  }

  @Callback(doc = """function(targetaddress:string, [sourceaddress:string,] port:number, data...) -- Sends the specified data to the specified target (from the source address if specified)""")
  override def send(context: Context, args: Arguments): Array[AnyRef] = {
    val target = args.checkString(0)
    var source = node.address()
    var port = 0
    var count = 2

    if (args.isString(1)) {
      source = args.checkString(1)
      port = checkPort(args.checkInteger(2))
      count = 3
    } else {
      port = checkPort(args.checkInteger(1))
    }

    // TODO: add spoofing energy cost
    val packet = Network.newPacket(source, target, port, args.drop(count).toArray)
    doSend(packet)
    networkActivity()
    result(true)
  }

  @Callback(doc = "function([sourceaddress:string,] port:number, data...) -- Broadcasts the specified data on the specified port (from the source address if specified)")
  override def broadcast(context: Context, args: Arguments): Array[AnyRef] = {
    var source = node.address()
    var port = 0
    var count = 1

    if (args.isString(1)) {
      source = args.checkString(0)
      port = checkPort(args.checkInteger(1))
      count = 2
    } else {
      port = checkPort(args.checkInteger(0))
    }

    // TODO: add spoofing energy cost
    val packet = Network.newPacket(source, null, port, args.drop(count).toArray)
    doBroadcast(packet)
    networkActivity()

    result(true)
  }

  @Callback(doc = """function(setSniff:boolean); Sets the sniffing mode of the network card.""", direct = true)
  def setSniff(context: Context, args: Arguments): Array[AnyRef] = {
    if (!args.isBoolean(0)) return result(-1, "invalid parameter")

    sniff = args.checkBoolean(0)
    result()
  }
}
