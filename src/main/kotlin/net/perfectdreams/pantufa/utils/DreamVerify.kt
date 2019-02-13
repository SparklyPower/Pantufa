package net.perfectdreams.pantufa.utils

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.File
import kotlin.concurrent.thread

fun main(args: Array<String>) {
	// DreamVerify - Uma ferramenta que coleta informações sobre o computador de uma pessoa
	val root = File("C:\\")
	val output = File("C:\\Users\\Whistler\\Documents\\PerfectDreams\\output.json")

	val payload = jsonObject()
	val jsonParser = JsonParser()
	val gson = Gson()

	var stacc = 0

	val process0 = ProcessBuilder("reg.exe", "query", "\"HKEY_CURRENT_USER\\Software\"")
			.redirectErrorStream(true)
			.start()
	// process0.waitFor()

	val registryKeys = process0.inputStream.bufferedReader().readLines()
			.filter { it.isNotEmpty() }

	val jsonRegistryKeys = gson.toJsonTree(registryKeys)

	payload["registryKeys"] = jsonRegistryKeys

	println(payload)

	val process1 = ProcessBuilder("reg.exe", "query", "\"HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\RecentDocs\\.zip\"")
			.redirectErrorStream(true)
			.start()
	// process1.waitFor()

	println(payload)

	val registryKeys1 = process1.inputStream.bufferedReader().readLines()
			.filter { it.isNotEmpty() && it.contains("REG_BINARY") }

	registryKeys1.forEach {
		println(it)
	}

	val list = mutableListOf<String>()

	for (key in registryKeys1) {
		val search = "REG_BINARY    "
		val split = key.indexOf(search) + search.length

		val sub = key.substring(split, key.length)

		/* val fancy = DatatypeConverter.parseHexBinary(sub).toString(Charsets.UTF_8)
				.replace("\u0000", "")

		list.add(fancy) */
	}

	payload["registryKeysRecentDocsZip"] = gson.toJsonTree(list)

	println(payload)

	output.writeText(payload.toString())

	println("doing some cool memory dumps y'all")

	val taskListProcess = ProcessBuilder("tasklist.exe", "/fo", "csv", "/nh", "/v")
			.redirectErrorStream(true)
			.start()

	// taskListProcess.waitFor()

	val javaProcesses = mutableListOf<Pair<Int, String>>()

	taskListProcess.inputStream.bufferedReader().readLines().forEach {
		println("Checking line $it")
		val split = it.split(",")
		val appProcess = split[0].replace("\"", "")
		val processPid = split[1].replace("\"", "")
		val processName = split[8].replace("\"", "")

		if (appProcess == "javaw.exe")
			javaProcesses.add(Pair(processPid.toInt(), processName))
	}

	val memoryAnalysis = jsonArray()

	for (pid in javaProcesses) {
		println("Dumping memory of ${pid.first} - ${pid.second}")
		val fileName = "memory_${pid.first}.dmp"

		val process2 = ProcessBuilder("C:\\Users\\Whistler\\Documents\\PerfectDreams\\procdump\\procdump.exe", "-accepteula", "-ma", "-o", pid.first.toString(), File("C:\\Users\\Whistler\\Documents\\PerfectDreams\\procdump\\", fileName).toPath().toString())
				.redirectErrorStream(true)
				.start()

		process2.waitFor()

		process2.inputStream.bufferedReader().readLines().forEach {
			println(it)
		}

		println("analyzing dump...")

		val memoryDump = File("C:\\Users\\Whistler\\Documents\\PerfectDreams\\procdump\\", fileName)

		val modsFound = mutableSetOf<MinecraftModification>()

		val detectionTools = listOf(
				ModificationDetection(
						listOf("net.minecraftforge.common.property.ExtendedBlockState"),
						MinecraftModification.OPTIFINE
				),
				ModificationDetection(
						listOf(",#I)!", "jna.z", "vob.npr", "ssp.yyb - 1.10", "ssp.yyb", "kc(g9"),
						MinecraftModification.VAPE
				),
				ModificationDetection(
						listOf("de.labymod.client.modules.impl.AimAssist"),
						MinecraftModification.LABYMOD_CLIENT
				),
				ModificationDetection(
						listOf("harambe"),
						MinecraftModification.HARAMBE
				),
				/* ModificationDetection(
						listOf("ConstantCallSite"),
						MinecraftModification.DREK
				), */
				ModificationDetection(
						listOf("gorilla"),
						MinecraftModification.GORILLA
				),
				ModificationDetection(
						listOf("aristhena"),
						MinecraftModification.AVIX
				),
				ModificationDetection(
						listOf("agentmain", "gsso9"),
						MinecraftModification.MARGE
				),
				ModificationDetection(
						listOf("x509en", "kzvadDubG", "jeinfyG", "DLBN_JO", "memememememem", "0_111"),
						MinecraftModification.KURIUM
				),
				ModificationDetection(
						listOf("A0toCricking.java",
								"Gcheat",
								"sliderPart",
								"G0ttaDipMen.java",
								"DoxThreat",
								"Body #",
								"MinAPS",
								"xyz/gucciclient/modules/mods/other/FastBreeeak",
								"xyz/gucciclient/modules/mods/combat/Tr1ggerBot",
								"xyz/gucciclient/modules/mods/combat/AntiiiiiiBot",
								"A1moboating",
								"#?+)\\u001a}}~rs\\u001a+",
								"(Lx/a/i/f;)V"
						),
						MinecraftModification.GUCCI_CLIENT
				)
		)

		var count = 0

		memoryDump.useLines {
			val iterator = it.iterator()

			while (iterator.hasNext()) {
				if (count % 100000 == 0) {
					println("Line: $count")
				}
				val line = iterator.next()

				for (detection in detectionTools) {
					for (string in detection.strings) {
						if (line.contains(string)) {
							println("${detection.modification} - $string")
							modsFound.add(detection.modification)
						}
					}
				}

				count++
			}
		}

		val jsonObject = jsonObject(
				"pid" to pid.first,
				"name" to pid.second,
				"modsFound" to gson.toJsonTree(modsFound)
		)

		memoryAnalysis.add(jsonObject)
	}

	payload["memoryAnalysis"] = memoryAnalysis

	println(payload)

	output.writeText(payload.toString())

	var minecraftInstallations = mutableListOf<File>()

	val start = System.currentTimeMillis()

	fun search(file: File) {
		file.listFiles()?.forEach {
			if (it.isDirectory && it.name != "Windows" && it.name != "\$Recycle.Bin") {
				search(it)
			} else {
				if (stacc % 5000 == 0) {
					println(stacc.toString() + " - " + it.path)
				}
				if (it.name == "launcher_profiles.json") {
					println("Found Minecraft installation @ " + it.path)
					minecraftInstallations.add(it.parentFile)
				}
				stacc++
			}
		}
	}

	search(root)

	val finish = System.currentTimeMillis() - start

	println("Finished in ${finish}ms!")

	println("===[ MINECRAFT INSTALLATIONS ]===")

	val jsonArray = jsonArray()

	minecraftInstallations.forEach {
		val minecraftInfo = jsonObject()

		val launcherProfiles = jsonParser.parse(File(it, "launcher_profiles.json").readText())

		minecraftInfo["profiles"] = launcherProfiles

		jsonArray.add(minecraftInfo)
	}

	payload["minecraft"] = jsonArray

	output.writeText(payload.toString())

	thread {
		while (true) {
			Thread.sleep(250)
		}
	}
}

enum class MinecraftModification {
	VAPE, LABYMOD_CLIENT, HARAMBE, DREK, GORILLA, AVIX, MARGE, KURIUM, GUCCI_CLIENT, OPTIFINE
}

class ModificationDetection(val strings: List<String>, val modification: MinecraftModification)