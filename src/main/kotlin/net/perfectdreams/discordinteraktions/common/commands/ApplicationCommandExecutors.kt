package net.perfectdreams.discordinteraktions.common.commands

import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.commands.options.ApplicationCommandOptions
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.discordinteraktions.common.entities.messages.Message

/**
 * This is the class that should be inherited if you
 * want to create an Application Command.
 */
sealed class ApplicationCommandExecutor {
    /**
     * Used by the [ApplicationCommandDeclaration] to match declarations to executors.
     *
     * By default the class of the executor is used, but this may cause issues when using anonymous classes!
     *
     * To avoid this issue, you can replace the signature with another unique identifier
     */
    open fun signature(): Any = this::class
}

/**
 * This is the class that should be inherited if you
 * want to create an Slash Command.
 */
abstract class SlashCommandExecutor : ApplicationCommandExecutor() {
    open val options: ApplicationCommandOptions = ApplicationCommandOptions.NO_OPTIONS

    abstract suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments)
}

/**
 * This is the class that should be inherited if you
 * want to create an User Command.
 */
abstract class UserCommandExecutor : ApplicationCommandExecutor() {
    abstract suspend fun execute(context: ApplicationCommandContext, targetUser: User, targetMember: Member?)
}

/**
 * This is the class that should be inherited if you
 * want to create an Message Command.
 */
abstract class MessageCommandExecutor : ApplicationCommandExecutor() {
    abstract suspend fun execute(context: ApplicationCommandContext, targetMessage: Message)
}