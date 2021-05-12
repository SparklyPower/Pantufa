package net.perfectdreams.pantufa.interactions.commands

import net.perfectdreams.discordinteraktions.commands.get
import net.perfectdreams.discordinteraktions.declarations.slash.SlashCommandDeclaration
import net.perfectdreams.discordinteraktions.declarations.slash.required
import net.perfectdreams.pantufa.PantufaBot
import net.perfectdreams.pantufa.utils.Constants
import net.perfectdreams.pantufa.utils.PantufaReply
import java.awt.Color

class ChatColorCommand(pantufa: PantufaBot) : PantufaInteractionCommand(pantufa, this) {
    companion object : SlashCommandDeclaration(
        name = "chatcolor",
        description = "Transforme uma cor RGB em uma cor que você possa usar no chat (e em outros lugares) do SparklyPower!"
    ) {
        override val options = Options

        object Options : SlashCommandDeclaration.Options() {
            val red = integer("red", "Quantidade de cor vermelha (em formato (R, G, B), é o primeiro número)")
                .required()
                .register()

            val green = integer("green", "Quantidade de cor verde (em formato (R, G, B), é o segundo número)")
                .required()
                .register()

            val blue = integer("blue", "Quantidade de cor azul (em formato (R, G, B), é o terceiro número)")
                .required()
                .register()
        }
    }

    override suspend fun executesPantufa(context: PantufaCommandContext) {
        val r = options.red.get(context.interactionContext)
        val g = options.green.get(context.interactionContext)
        val b = options.blue.get(context.interactionContext)

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