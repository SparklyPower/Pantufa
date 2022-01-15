package net.perfectdreams.pantufa.tables

import net.perfectdreams.pantufa.gson
import net.perfectdreams.pantufa.jsonParser
import net.perfectdreams.pantufa.utils.SonhosPaymentReason
import net.perfectdreams.pantufa.utils.exposed.rawJsonb
import org.jetbrains.exposed.dao.id.LongIdTable

object SonhosTransaction : LongIdTable() {
	val reason = enumeration("source", SonhosPaymentReason::class)
	val givenBy = long("given_by").index().nullable()
	val receivedBy = long("received_by").index().nullable()
	val givenAt = long("given_at")
	val quantity = decimal("quantity", 12, 2)
	val metadata = rawJsonb("metadata", gson, jsonParser).nullable()
}