package space.marsden.mods.ascension.common.drivers

import li.cil.oc.api.network.{ComponentConnector, Node}
import li.cil.oc.api.prefab.AbstractManagedEnvironment

class ManagedEnvironmentWithComponentConnector extends AbstractManagedEnvironment {
  protected var connector: ComponentConnector = _

  override def node: Node = {
    if (this.connector != null) {
      this.connector;
    } else {
      super.node();
    }
  }

  protected override def setNode(value: Node): Unit = {
    if(value == null) {
      this.connector = null;
    } else value match {
      case component: ComponentConnector =>
        this.connector = component
      case _ =>
    }

    super.setNode(value)
  }
}
