package com.mineinabyss.blocky.helpers.ui

import net.wesjd.anvilgui.AnvilGUI

sealed class UniversalScreens {
    class Anvil(val builder: AnvilGUI.Builder): UniversalScreens()
}
