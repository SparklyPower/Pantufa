package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.common.context.SlashCommandArguments
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandExecutorDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.options.CommandOptions
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import java.awt.Color

class ChatColorExecutor(pantufa: PantufaBot) : PantufaInteractionCommand(pantufa) {
    companion object : SlashCommandExecutorDeclaration(ChatColorExecutor::class) {
        object Options : CommandOptions() {
            val red = integer("red", "Quantidade de cor vermelha (em formato (R, G, B), é o primeiro número)")
                .register()

            val green = integer("green", "Quantidade de cor verde (em formato (R, G, B), é o segundo número)")
                .register()

            val blue = integer("blue", "Quantidade de cor azul (em formato (R, G, B), é o terceiro número)")
                .register()
        }

        override val options = Options
    }

    override suspend fun executePantufa(context: PantufaCommandContext, args: SlashCommandArguments) {
        val r = args[options.red]
        val g = args[options.green]
        val b = args[options.blue]

        if (r !in 0..255 || g !in 0..255 || b !in 0..255) {
            context.reply(
                PantufaReply(
                    "Cor inválida!",
                    Constants.ERROR
                )
            )
        }

        val color = Color(r, g, b)
        val hex = String.format("%02x%02x%02x", color.red, color.green, color.blue)

        val strBuilder = buildString {
            this.append("&x")
            hex.forEach {
                this.append("&")
                this.append(it)
            }
        }

        context.reply(
            PantufaReply(
                "Formato de cor para o chat: `$strBuilder`"
            )
        )
    }
}