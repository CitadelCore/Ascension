package space.marsden.mods.ascension

import li.cil.oc.util.ItemUtils

object Constants {
  object ItemName {
    // coprocessor card
    final val CoprocessorTier1 = "coprocessor1"
    final val CoprocessorTier2 = "coprocessor2"
    final val CoprocessorTier3 = "coprocessor3"
    final val CoprocessorCreative = "coprocessorcreative"
    final val CoprocessorCaseTier1 = "coprocessorcase1"
    final val CoprocessorCaseTier2 = "coprocessorcase2"
    final val CoprocessorCaseTier3 = "coprocessorcase3"
    final val CoprocessorCaseCreative = "coprocessorcasecreative"

    // sniffer card
    final val Sniffer = "sniffer"

    def Coprocessor(tier: Int): String = ItemUtils.caseNameWithTierSuffix("coprocessor", tier)
    def CoprocessorCase(tier: Int): String = ItemUtils.caseNameWithTierSuffix("coprocessorcase", tier)
  }
}
