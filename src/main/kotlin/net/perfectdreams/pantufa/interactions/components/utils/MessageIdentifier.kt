@file:Suppress("UNCHECKED_CAST")
package net.perfectdreams.pantufa.interactions.components.utils

import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.pantufa.network.Databases
import net.perfectdreams.pantufa.tables.Transactions
import net.perfectdreams.pantufa.utils.UUIDSerializer
import net.perfectdreams.pantufa.dao.Transaction as TTransaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentMap
import kotlin.math.floor

val activeMessagePanels: ConcurrentMap<UUID, BaseMessagePanelData> = Caffeine.newBuilder()
    .expireAfterWrite(Duration.ofHours(12))
    .build<UUID, BaseMessagePanelData>()
    .asMap()

data class BaseMessagePanelData(
    var size: Long,
    val userId: Snowflake,
    val type: MessagePanelType,
    var data: SizedIterable<*>,
    val args: List<String>,
    val key: UUID
) {
    // TODO: Optimize this
    lateinit var options: Triple<UUID?, UUID?, TransactionCurrency?>
    var minecraftId: UUID? = null

    var showOnly: List<TransactionType>? = null
        set(value) {
            field = value
            data = transaction(Databases.sparklyPower) {
                TTransaction.find {
                    minecraftId?.let { uuid ->
                        (Transactions.payer eq uuid) or
                        (Transactions.receiver eq uuid) and
                        (options.third?.let { Transactions.currency eq it } ?: Op.TRUE) and
                        (Transactions.type inList showOnly!!)
                    } ?: run {
                        (options.first?.let { Transactions.payer eq it } ?: Op.TRUE) and
                        (options.second?.let { Transactions.receiver eq it } ?: Op.TRUE) and
                        (options.third?.let { Transactions.currency eq it } ?: Op.TRUE) and
                        (Transactions.type inList showOnly!!)
                    }
                }.orderBy(Transactions.time to SortOrder.DESC).apply { size = count() }
            }
        }

    val lastPage get() = floor(size.toDouble() / type.entriesPerPage).toLong()

    fun <T> fetchPage(page: Long) = transaction {
        (data as SizedIterable<T>).limit(type.entriesPerPage, page * type.entriesPerPage).toList()
    }
}

enum class MessagePanelType(val entriesPerPage: Int) {
    TRANSACTIONS(10),
    COMMANDS_LOG(7)
}

fun saveAndCreateData(
    size: Long,
    userId: Snowflake,
    type: MessagePanelType,
    data: SizedIterable<*>,
    args: List<String>
): BaseMessagePanelData {
    val uuid = UUID.randomUUID()
    val panelData = BaseMessagePanelData(size, userId, type, data, args, uuid)
    activeMessagePanels[uuid] = panelData
    return panelData
}

/**
 * Class used to identify messages, since a "customId" cannot have
 * more than 100 characters, and the data stored usually surpasses it.
 */
@Serializable
data class MessageIdentifier(
    @Serializable(with = UUIDSerializer::class) val key: UUID,
    val page: Long,
) {
    @Transient val encoded = Json.encodeToString(this)
}

/**
 * Decodes a [String] and checks if the message panel is still valid.
 *
 * @return [Pair] with [panel][BaseMessagePanelData] and [page][Long] if message is still valid, null otherwise.
 */
val String.decoded get(): Pair<BaseMessagePanelData, Long>? {
    val data = Json.decodeFromString<MessageIdentifier>(this)

    return activeMessagePanels[data.key]?.let {
        it to data.page
    }
}