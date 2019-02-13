package net.perfectdreams.pantufa.utils

fun String.stripColor(): String {
	return this.replace(Regex("(?i)§[0-9A-FK-OR]"), "")
}