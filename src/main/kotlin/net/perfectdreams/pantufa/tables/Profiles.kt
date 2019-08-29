package net.perfectdreams.pantufa.tables

object Profiles : SnowflakeTable() {
	val money = double("money").index()
}