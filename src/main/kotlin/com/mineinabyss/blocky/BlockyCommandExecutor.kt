package com.mineinabyss.blocky

import com.mineinabyss.idofront.commands.Command
import com.mineinabyss.idofront.commands.CommandHolder
import com.mineinabyss.idofront.commands.execution.IdofrontCommandExecutor

object BlockyCommandExecutor : IdofrontCommandExecutor() {
    override val commands: CommandHolder = commands(blockyPlugin) {
        ("blocky")(desc = "Commands related to Blocky-plugin") {

        }
    }
}