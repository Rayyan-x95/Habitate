package com.ninety5.habitate.data.local

import org.junit.Assert.assertTrue
import org.junit.Test

class MigrationCoverageTest {

    @Test
    fun migrationChain_isContiguousToCurrentVersion() {
        val dbVersion = HABITATE_DB_VERSION

        val pattern = Regex("""MIGRATION_(\d+)_(\d+)""")
        val migrationPairs = mutableSetOf<Pair<Int, Int>>()

        fun collectPairs(clazz: Class<*>) {
            clazz.declaredFields.forEach { field ->
                val match = pattern.matchEntire(field.name) ?: return@forEach
                val from = match.groupValues[1].toInt()
                val to = match.groupValues[2].toInt()
                migrationPairs += (from to to)
            }
        }

        collectPairs(HabitateDatabase::class.java)
        collectPairs(HabitateDatabase.Companion::class.java)

        if (dbVersion > 1) {
            assertTrue(
                "No Room migration fields found in HabitateDatabase",
                migrationPairs.isNotEmpty()
            )
        }

        for (from in 1 until dbVersion) {
            val expected = from to (from + 1)
            assertTrue(
                "Missing migration ${expected.first} -> ${expected.second}",
                migrationPairs.contains(expected)
            )
        }
    }
}
