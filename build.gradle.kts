// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ben.manes.versions) apply false
}

// ben-manes versions plugin has a ConcurrentModificationException bug with AGP 9.x.
// This custom task reads libs.versions.toml and checks Maven Central for updates.
tasks.register("dependencyUpdates") {
    group = "help"
    description = "Checks for dependency updates in the version catalog."
    doLast {
        val catalogFile = file("gradle/libs.versions.toml")
        if (!catalogFile.exists()) {
            logger.warn("Version catalog not found at gradle/libs.versions.toml")
            return@doLast
        }

        val lines = catalogFile.readLines()
        val versions = mutableMapOf<String, String>()
        var inVersions = false

        // Parse [versions] section
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed == "[versions]") { inVersions = true; continue }
            if (trimmed.startsWith("[") && trimmed != "[versions]") { inVersions = false; continue }
            if (inVersions && trimmed.contains("=")) {
                val (key, value) = trimmed.split("=", limit = 2).map { it.trim().trim('"') }
                versions[key] = value
            }
        }

        // Parse [libraries] section to map version refs to group:name
        val libraries = mutableListOf<Triple<String, String, String>>() // group, name, versionKey
        var inLibraries = false
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed == "[libraries]") { inLibraries = true; continue }
            if (trimmed.startsWith("[") && trimmed != "[libraries]") { inLibraries = false; continue }
            if (inLibraries && trimmed.contains("=")) {
                val groupMatch = Regex("""group\s*=\s*"([^"]+)"""").find(trimmed)
                val nameMatch = Regex("""name\s*=\s*"([^"]+)"""").find(trimmed)
                val versionRefMatch = Regex("""version\.ref\s*=\s*"([^"]+)"""").find(trimmed)
                if (groupMatch != null && nameMatch != null && versionRefMatch != null) {
                    libraries.add(Triple(groupMatch.groupValues[1], nameMatch.groupValues[1], versionRefMatch.groupValues[1]))
                }
            }
        }

        // Deduplicate by version key - only check one library per version
        val checked = mutableSetOf<String>()
        val updates = mutableListOf<String>()
        val upToDate = mutableListOf<String>()
        val failed = mutableListOf<String>()

        for ((group, name, versionKey) in libraries) {
            if (versionKey in checked) continue
            checked.add(versionKey)
            val currentVersion = versions[versionKey] ?: continue

            try {
                val url = java.net.URI("https://repo1.maven.org/maven2/${group.replace('.', '/')}/$name/maven-metadata.xml").toURL()
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                if (connection.responseCode == 200) {
                    val xml = connection.inputStream.bufferedReader().readText()
                    val latestMatch = Regex("<release>([^<]+)</release>").find(xml)
                        ?: Regex("<latest>([^<]+)</latest>").find(xml)
                    val latest = latestMatch?.groupValues?.get(1)
                    if (latest != null && latest != currentVersion) {
                        // Skip pre-release updates if current is stable
                        val preRelease = listOf("alpha", "beta", "rc", "cr", "dev", "eap", "preview", "snapshot")
                        val latestIsPreRelease = preRelease.any { latest.lowercase().contains(it) }
                        val currentIsPreRelease = preRelease.any { currentVersion.lowercase().contains(it) }
                        if (latestIsPreRelease && !currentIsPreRelease) {
                            upToDate.add("  $group:$name [$currentVersion] (latest stable)")
                        } else {
                            updates.add("  $group:$name [$currentVersion -> $latest]")
                        }
                    } else {
                        upToDate.add("  $group:$name [$currentVersion]")
                    }
                } else {
                    // Try Google's Maven repo
                    val gUrl = java.net.URI("https://dl.google.com/dl/android/maven2/${group.replace('.', '/')}/$name/maven-metadata.xml").toURL()
                    val gConn = gUrl.openConnection() as java.net.HttpURLConnection
                    gConn.connectTimeout = 5000
                    gConn.readTimeout = 5000
                    if (gConn.responseCode == 200) {
                        val gXml = gConn.inputStream.bufferedReader().readText()
                        val gLatestMatch = Regex("<release>([^<]+)</release>").find(gXml)
                            ?: Regex("<latest>([^<]+)</latest>").find(gXml)
                        val gLatest = gLatestMatch?.groupValues?.get(1)
                        if (gLatest != null && gLatest != currentVersion) {
                            val preRelease = listOf("alpha", "beta", "rc", "cr", "dev", "eap", "preview", "snapshot")
                            val latestIsPreRelease = preRelease.any { gLatest.lowercase().contains(it) }
                            val currentIsPreRelease = preRelease.any { currentVersion.lowercase().contains(it) }
                            if (latestIsPreRelease && !currentIsPreRelease) {
                                upToDate.add("  $group:$name [$currentVersion] (latest stable)")
                            } else {
                                updates.add("  $group:$name [$currentVersion -> $gLatest]")
                            }
                        } else {
                            upToDate.add("  $group:$name [$currentVersion]")
                        }
                    } else {
                        failed.add("  $group:$name [$currentVersion] - not found in repos")
                    }
                }
            } catch (e: Exception) {
                failed.add("  $group:$name [$currentVersion] - ${e.message}")
            }
        }

        println("\n------------------------------------------------------------")
        println("           Dependency Updates Report")
        println("------------------------------------------------------------\n")

        if (updates.isNotEmpty()) {
            println("The following dependencies have newer versions:")
            updates.sorted().forEach { println(it) }
            println()
        }

        if (upToDate.isNotEmpty()) {
            println("Up-to-date dependencies (${upToDate.size}):")
            upToDate.sorted().forEach { println(it) }
            println()
        }

        if (failed.isNotEmpty()) {
            println("Failed to check:")
            failed.sorted().forEach { println(it) }
            println()
        }

        println("------------------------------------------------------------")
        println(" ${updates.size} updates available, ${upToDate.size} up-to-date, ${failed.size} failed")
        println("------------------------------------------------------------")
    }
}
