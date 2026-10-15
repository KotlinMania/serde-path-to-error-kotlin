// port-lint: tests tests/deserialize.rs
package io.github.kotlinmania.serdepathtoerror

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeException
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serde.serdeCatching
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.DeserializeSeed
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.EnumAccess
import io.github.kotlinmania.serdecore.de.I32Deserialize
import io.github.kotlinmania.serdecore.de.MapAccess
import io.github.kotlinmania.serdecore.de.SeqAccess
import io.github.kotlinmania.serdecore.de.StringDeserialize
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdejson.JsonDeserializer
import io.github.kotlinmania.serdejson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals

private fun <T> deSeed(deserialize: Deserialize<T>): DeserializeSeed<T> =
    object : DeserializeSeed<T> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<T> =
            deserialize.deserialize(deserializer)
    }

private class MapDeserialize<K, V>(
    private val keyDe: Deserialize<K>,
    private val valDe: Deserialize<V>,
) : Deserialize<Map<K, V>> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Map<K, V>> =
        deserializer.deserializeMap(
            object : Visitor<Map<K, V>> {
                override fun expecting(): String = "a map"

                override fun <A : MapAccess> visitMap(access: A): SerdeResult<Map<K, V>> =
                    serdeCatching {
                        val result = mutableMapOf<K, V>()
                        val keySeed = deSeed(keyDe)
                        val valSeed = deSeed(valDe)
                        while (true) {
                            val k = access.nextKeySeed(keySeed).getOrThrow() ?: break
                            val v = access.nextValueSeed(valSeed).getOrThrow()
                            result[k] = v
                        }
                        result
                    }
            },
        )
}

private class ListDeserialize<T>(
    private val elemDe: Deserialize<T>,
) : Deserialize<List<T>> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<List<T>> =
        deserializer.deserializeSeq(
            object : Visitor<List<T>> {
                override fun expecting(): String = "a sequence"

                override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<List<T>> =
                    serdeCatching {
                        val result = mutableListOf<T>()
                        val seed = deSeed(elemDe)
                        while (true) {
                            val elem = access.nextElementSeed(seed).getOrThrow() ?: break
                            result.add(elem)
                        }
                        result
                    }
            },
        )
}

private class OptionDeserialize<T>(
    private val innerDe: Deserialize<T>,
) : Deserialize<T?> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<T?> =
        deserializer.deserializeOption(
            object : Visitor<T?> {
                override fun expecting(): String = "an option"

                override fun visitNone(): SerdeResult<T?> = SerdeResult.success(null)

                override fun <D2 : Deserializer> visitSome(deserializer: D2): SerdeResult<T?> =
                    innerDe.deserialize(deserializer)
            },
        )
}

private object StructVariantVisitor : Visitor<String> {
    override fun expecting(): String = "struct variant"

    override fun <A : MapAccess> visitMap(access: A): SerdeResult<String> =
        serdeCatching {
            var ver: String? = null
            val keySeed = deSeed(StringDeserialize)
            val valSeed = deSeed(StringDeserialize)
            while (true) {
                val k = access.nextKeySeed(keySeed).getOrThrow() ?: break
                if (k == "version") {
                    ver = access.nextValueSeed(valSeed).getOrThrow()
                }
            }
            ver ?: throw AssertionError("missing version")
        }
}

private object TupleVariantVisitor : Visitor<Pair<String, String>> {
    override fun expecting(): String = "tuple variant"

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<Pair<String, String>> =
        serdeCatching {
            val first = access.nextElementSeed(deSeed(StringDeserialize)).getOrThrow()!!
            val second = access.nextElementSeed(deSeed(StringDeserialize)).getOrThrow()!!
            Pair(first, second)
        }
}

private object FieldSeed : DeserializeSeed<String> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<String> =
        deserializer.deserializeIdentifier(
            object : Visitor<String> {
                override fun expecting(): String = "field identifier"

                override fun visitStr(v: String): SerdeResult<String> =
                    if (v == "version") {
                        SerdeResult.success(v)
                    } else {
                        SerdeResult.failure(SerdeError.unknownField(v, listOf("version")))
                    }
            },
        )
}

private object U128Seed : DeserializeSeed<String> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<String> =
        deserializer.deserializeU128(
            object : Visitor<String> {
                override fun expecting(): String = "u128"

                override fun visitU128(v: String): SerdeResult<String> = SerdeResult.success(v)
            },
        )
}

// ── Test 1: test_struct ───────────────────────────────────────────────────

private data class Dependency(
    val version: String,
) {
    companion object : Deserialize<Dependency> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Dependency> =
            deserializer.deserializeStruct(
                "Dependency",
                listOf("version"),
                object : Visitor<Dependency> {
                    override fun expecting(): String = "struct Dependency"

                    override fun <A : MapAccess> visitMap(access: A): SerdeResult<Dependency> =
                        serdeCatching {
                            var version: String? = null
                            val keySeed = deSeed(StringDeserialize)
                            val valSeed = deSeed(StringDeserialize)
                            while (true) {
                                val key = access.nextKeySeed(keySeed).getOrThrow() ?: break
                                if (key == "version") {
                                    version = access.nextValueSeed(valSeed).getOrThrow()
                                }
                            }
                            Dependency(version ?: throw AssertionError("missing field version"))
                        }
                },
            )
    }
}

private data class Package(
    val name: String,
    val dependencies: Map<String, Dependency>,
) {
    companion object : Deserialize<Package> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Package> =
            deserializer.deserializeStruct(
                "Package",
                listOf("name", "dependencies"),
                object : Visitor<Package> {
                    override fun expecting(): String = "struct Package"

                    override fun <A : MapAccess> visitMap(access: A): SerdeResult<Package> =
                        serdeCatching {
                            var name: String? = null
                            var deps: Map<String, Dependency>? = null
                            val keySeed = deSeed(StringDeserialize)
                            val nameSeed = deSeed(StringDeserialize)
                            val depsSeed = deSeed(MapDeserialize(StringDeserialize, Dependency))
                            while (true) {
                                val key = access.nextKeySeed(keySeed).getOrThrow() ?: break
                                when (key) {
                                    "name" -> name = access.nextValueSeed(nameSeed).getOrThrow()
                                    "dependencies" -> deps = access.nextValueSeed(depsSeed).getOrThrow()
                                }
                            }
                            Package(name.orEmpty(), deps.orEmpty())
                        }
                },
            )
    }
}

// ── Test 2: test_vec ──────────────────────────────────────────────────────

private data class DependencyNameVersion(
    val name: String,
    val version: String,
) {
    companion object : Deserialize<DependencyNameVersion> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<DependencyNameVersion> =
            deserializer.deserializeStruct(
                "Dependency",
                listOf("name", "version"),
                object : Visitor<DependencyNameVersion> {
                    override fun expecting(): String = "struct Dependency"

                    override fun <A : MapAccess> visitMap(access: A): SerdeResult<DependencyNameVersion> =
                        serdeCatching {
                            var name: String? = null
                            var version: String? = null
                            val keySeed = deSeed(StringDeserialize)
                            val valSeed = deSeed(StringDeserialize)
                            while (true) {
                                val key = access.nextKeySeed(keySeed).getOrThrow() ?: break
                                when (key) {
                                    "name" -> name = access.nextValueSeed(valSeed).getOrThrow()
                                    "version" -> version = access.nextValueSeed(valSeed).getOrThrow()
                                }
                            }
                            DependencyNameVersion(name.orEmpty(), version.orEmpty())
                        }
                },
            )
    }
}

private data class PackageVec(
    val dependencies: List<DependencyNameVersion>,
) {
    companion object : Deserialize<PackageVec> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<PackageVec> =
            deserializer.deserializeStruct(
                "Package",
                listOf("dependencies"),
                object : Visitor<PackageVec> {
                    override fun expecting(): String = "struct Package"

                    override fun <A : MapAccess> visitMap(access: A): SerdeResult<PackageVec> =
                        serdeCatching {
                            var deps: List<DependencyNameVersion>? = null
                            val keySeed = deSeed(StringDeserialize)
                            val depsSeed = deSeed(ListDeserialize(DependencyNameVersion))
                            while (true) {
                                val key = access.nextKeySeed(keySeed).getOrThrow() ?: break
                                when (key) {
                                    "dependencies" -> deps = access.nextValueSeed(depsSeed).getOrThrow()
                                }
                            }
                            PackageVec(deps.orEmpty())
                        }
                },
            )
    }
}

// ── Test 3: test_option ───────────────────────────────────────────────────

private data class PackageOpt(
    val dependency: Dependency?,
) {
    companion object : Deserialize<PackageOpt> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<PackageOpt> =
            deserializer.deserializeStruct(
                "Package",
                listOf("dependency"),
                object : Visitor<PackageOpt> {
                    override fun expecting(): String = "struct Package"

                    override fun <A : MapAccess> visitMap(access: A): SerdeResult<PackageOpt> =
                        serdeCatching {
                            var dep: Dependency? = null
                            val keySeed = deSeed(StringDeserialize)
                            val depSeed = deSeed(OptionDeserialize(Dependency))
                            while (true) {
                                val key = access.nextKeySeed(keySeed).getOrThrow() ?: break
                                when (key) {
                                    "dependency" -> dep = access.nextValueSeed(depSeed).getOrThrow()
                                }
                            }
                            PackageOpt(dep)
                        }
                },
            )
    }
}

// ── Test 4 & 5: test_struct_variant & test_tuple_variant ────────────────────

private sealed class DependencyEnum {
    data class Struct(
        val version: String,
    ) : DependencyEnum()

    data class Tuple(
        val first: String,
        val second: String,
    ) : DependencyEnum()

    companion object : Deserialize<DependencyEnum> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<DependencyEnum> =
            deserializer.deserializeEnum(
                "Dependency",
                listOf("Struct", "Tuple"),
                object : Visitor<DependencyEnum> {
                    override fun expecting(): String = "enum Dependency"

                    override fun <A : EnumAccess> visitEnum(access: A): SerdeResult<DependencyEnum> =
                        serdeCatching {
                            val (variantName, variantAccess) = access.variantSeed(deSeed(StringDeserialize)).getOrThrow()
                            when (variantName) {
                                "Struct" -> {
                                    val version =
                                        variantAccess
                                            .structVariant(listOf("version"), StructVariantVisitor)
                                            .getOrThrow()
                                    Struct(version)
                                }
                                "Tuple" -> {
                                    val pair =
                                        variantAccess
                                            .tupleVariant(2, TupleVariantVisitor)
                                            .getOrThrow()
                                    Tuple(pair.first, pair.second)
                                }
                                else -> throw AssertionError("unknown variant $variantName")
                            }
                        }
                },
            )
    }
}

private data class PackageEnum(
    val dependency: DependencyEnum,
) {
    companion object : Deserialize<PackageEnum> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<PackageEnum> =
            deserializer.deserializeStruct(
                "Package",
                listOf("dependency"),
                object : Visitor<PackageEnum> {
                    override fun expecting(): String = "struct Package"

                    override fun <A : MapAccess> visitMap(access: A): SerdeResult<PackageEnum> =
                        serdeCatching {
                            var dep: DependencyEnum? = null
                            val keySeed = deSeed(StringDeserialize)
                            val depSeed = deSeed(DependencyEnum)
                            while (true) {
                                val key = access.nextKeySeed(keySeed).getOrThrow() ?: break
                                when (key) {
                                    "dependency" -> dep = access.nextValueSeed(depSeed).getOrThrow()
                                }
                            }
                            PackageEnum(dep ?: throw AssertionError("missing dependency"))
                        }
                },
            )
    }
}

// ── Test 6: test_unknown_field ────────────────────────────────────────────

private data class DependencyDenyUnknown(
    val version: String,
) {
    companion object : Deserialize<DependencyDenyUnknown> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<DependencyDenyUnknown> =
            deserializer.deserializeStruct(
                "Dependency",
                listOf("version"),
                object : Visitor<DependencyDenyUnknown> {
                    override fun expecting(): String = "struct Dependency"

                    override fun <A : MapAccess> visitMap(access: A): SerdeResult<DependencyDenyUnknown> =
                        serdeCatching {
                            var version: String? = null
                            val valSeed = deSeed(StringDeserialize)
                            while (true) {
                                val key = access.nextKeySeed(FieldSeed).getOrThrow() ?: break
                                if (key == "version") {
                                    version = access.nextValueSeed(valSeed).getOrThrow()
                                }
                            }
                            DependencyDenyUnknown(version ?: throw AssertionError("missing version"))
                        }
                },
            )
    }
}

private data class PackageUnknown(
    val dependency: DependencyDenyUnknown,
) {
    companion object : Deserialize<PackageUnknown> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<PackageUnknown> =
            deserializer.deserializeStruct(
                "Package",
                listOf("dependency"),
                object : Visitor<PackageUnknown> {
                    override fun expecting(): String = "struct Package"

                    override fun <A : MapAccess> visitMap(access: A): SerdeResult<PackageUnknown> =
                        serdeCatching {
                            var dep: DependencyDenyUnknown? = null
                            val keySeed = deSeed(StringDeserialize)
                            val depSeed = deSeed(DependencyDenyUnknown)
                            while (true) {
                                val key = access.nextKeySeed(keySeed).getOrThrow() ?: break
                                when (key) {
                                    "dependency" -> dep = access.nextValueSeed(depSeed).getOrThrow()
                                }
                            }
                            PackageUnknown(dep ?: throw AssertionError("missing dependency"))
                        }
                },
            )
    }
}

// ── Test 7: test_invalid_length ───────────────────────────────────────────

private data class FixedPair(
    val first: String,
    val second: String,
) {
    companion object : Deserialize<FixedPair> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<FixedPair> =
            deserializer.deserializeTuple(
                2,
                object : Visitor<FixedPair> {
                    override fun expecting(): String = "a tuple of size 2"

                    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<FixedPair> =
                        serdeCatching {
                            val strSeed = deSeed(StringDeserialize)
                            val first =
                                access.nextElementSeed(strSeed).getOrThrow()
                                    ?: throw SerdeException(SerdeError.invalidLength(0, this))
                            val second =
                                access.nextElementSeed(strSeed).getOrThrow()
                                    ?: throw SerdeException(SerdeError.invalidLength(1, this))
                            FixedPair(first, second)
                        }
                },
            )
    }
}

private data class PackageFixed(
    val dependency: FixedPair,
) {
    companion object : Deserialize<PackageFixed> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<PackageFixed> =
            deserializer.deserializeStruct(
                "Package",
                listOf("dependency"),
                object : Visitor<PackageFixed> {
                    override fun expecting(): String = "struct Package"

                    override fun <A : MapAccess> visitMap(access: A): SerdeResult<PackageFixed> =
                        serdeCatching {
                            var dep: FixedPair? = null
                            val keySeed = deSeed(StringDeserialize)
                            val depSeed = deSeed(FixedPair)
                            while (true) {
                                val key = access.nextKeySeed(keySeed).getOrThrow() ?: break
                                when (key) {
                                    "dependency" -> dep = access.nextValueSeed(depSeed).getOrThrow()
                                }
                            }
                            PackageFixed(dep ?: throw AssertionError("missing dependency"))
                        }
                },
            )
    }
}

// ── Test 9: test_u128 ─────────────────────────────────────────────────────

private data class Container(
    val n: String,
) {
    companion object : Deserialize<Container> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<Container> =
            deserializer.deserializeStruct(
                "Container",
                listOf("n"),
                object : Visitor<Container> {
                    override fun expecting(): String = "struct Container"

                    override fun <A : MapAccess> visitMap(access: A): SerdeResult<Container> =
                        serdeCatching {
                            var n: String? = null
                            val keySeed = deSeed(StringDeserialize)
                            while (true) {
                                val key = access.nextKeySeed(keySeed).getOrThrow() ?: break
                                if (key == "n") {
                                    n = access.nextValueSeed(U128Seed).getOrThrow()
                                }
                            }
                            Container(n.orEmpty())
                        }
                },
            )
    }
}

class DeserializeTest {
    private fun <T> testDeserialize(json: String, deserialize: Deserialize<T>, expected: String) {
        val parser = JsonParser(json)
        val de = JsonDeserializer(parser)
        val result =
            io.github.kotlinmania.serdepathtoerror
                .deserialize(de, deserialize)
        val path = result.unwrapErr().path.toString()
        assertEquals(expected, path)
    }

    @Test
    fun testStruct() {
        val j = """{
            "name": "demo",
            "dependencies": {
                "serde": {
                    "version": 1
                }
            }
        }"""
        testDeserialize(j, Package, "dependencies.serde.version")
    }

    @Test
    fun testVec() {
        val j = """{
            "dependencies": [
                {
                    "name": "serde",
                    "version": "1.0"
                },
                {
                    "name": "serde_json",
                    "version": 1
                }
            ]
        }"""
        testDeserialize(j, PackageVec, "dependencies[1].version")
    }

    @Test
    fun testOption() {
        val j = """{
            "dependency": {
                "version": 1
            }
        }"""
        testDeserialize(j, PackageOpt, "dependency.version")
    }

    @Test
    fun testStructVariant() {
        val j = """{
            "dependency": {
                "Struct": {
                    "version": 1
                }
            }
        }"""
        testDeserialize(j, PackageEnum, "dependency.Struct.version")
    }

    @Test
    fun testTupleVariant() {
        val j = """{
            "dependency": {
                "Tuple": ["serde", 1]
            }
        }"""
        testDeserialize(j, PackageEnum, "dependency.Tuple[1]")
    }

    @Test
    fun testUnknownField() {
        val j = """{
            "dependency": {
                "version": "1.0",
                "name": "serde"
            }
        }"""
        testDeserialize(j, PackageUnknown, "dependency.name")
    }

    @Test
    fun testInvalidLength() {
        val j = """{
            "dependency": ["serde"]
        }"""
        testDeserialize(j, PackageFixed, "dependency")
    }

    @Test
    fun testSyntaxError() {
        val j = """{
            "dependency": {
                "error": *
        }"""
        testDeserialize(j, PackageOpt, "dependency.error")
    }

    @Test
    fun testU128() {
        val j = """{
            "n": 130033514578017493995102500318550798591
        }"""
        val parser = JsonParser(j)
        val de = JsonDeserializer(parser)
        val result =
            io.github.kotlinmania.serdepathtoerror
                .deserialize(de, Container)
        val container = result.getOrThrow()
        assertEquals("130033514578017493995102500318550798591", container.n)
    }

    @Test
    fun testMapNonStringKey() {
        val j = """{
            "100": {
                "version": false
            }
        }"""
        testDeserialize(j, MapDeserialize(I32Deserialize, Dependency), "100.version")
    }
}
