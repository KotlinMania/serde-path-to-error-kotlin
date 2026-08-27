// port-lint: tests deserialize.rs

@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package io.github.kotlinmania.serdepathtoerror

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import io.github.kotlinmania.serdepathtoerror.Error as PathError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class TrackingDeserializer<T>(
    private val delegate: KSerializer<T>,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor
        get() = delegate.descriptor

    override fun deserialize(decoder: Decoder): T =
        io.github.kotlinmania.serdepathtoerror.deserialize(delegate, decoder)

    override fun serialize(encoder: Encoder, value: T) =
        delegate.serialize(encoder, value)
}

@Serializable
internal data class Package(
    val name: String,
    val dependencies: Map<String, Dependency>,
)

@Serializable
internal data class Dependency(
    val version: String,
)

@Serializable
internal data class PackageVec(
    val dependencies: List<DependencyNameVersion>,
)

@Serializable
internal data class DependencyNameVersion(
    val name: String,
    val version: String,
)

@Serializable
internal data class PackageOpt(
    val dependency: Dependency? = null,
)

@Serializable
internal data class PackageStructVariant(
    val dependency: DependencyStructVariant,
)

@Serializable
internal data class DependencyStructVariant(
    val Struct: DependencyVersion,
)

@Serializable
internal data class DependencyVersion(
    val version: String,
)

@Serializable
internal data class PackageTupleVariant(
    val dependency: DependencyTuple,
)

@Serializable
internal data class DependencyTuple(
    val Tuple: List<String>,
)

@Serializable
internal data class PackageUnknown(
    val dependency: DependencyStrict,
)

@Serializable
internal data class DependencyStrict(
    val version: String,
)

@Serializable
internal data class PackageFixed(
    val dependency: FixedPair,
)

@Serializable(with = FixedPairSerializer::class)
internal data class FixedPair(val first: String, val second: String)

internal object FixedPairSerializer : KSerializer<FixedPair> {
    override val descriptor: SerialDescriptor =
        ListSerializer(String.serializer()).descriptor

    override fun deserialize(decoder: Decoder): FixedPair {
        val list = ListSerializer(String.serializer()).deserialize(decoder)
        if (list.size < 2) {
            throw IllegalArgumentException("expected 2 elements, found ${list.size}")
        }
        return FixedPair(list[0], list[1])
    }

    override fun serialize(encoder: Encoder, value: FixedPair) {
        ListSerializer(String.serializer()).serialize(encoder, listOf(value.first, value.second))
    }
}

@Serializable
internal data class Container(
    val n: String,
)

class DeserializeTest {

    private val json = Json {
        ignoreUnknownKeys = false
        isLenient = false
    }

    private fun <T> testDeserializeError(
        serializer: KSerializer<T>,
        jsonString: String,
        expected: String,
    ) {
        val err = assertFailsWith<PathError> {
            json.decodeFromString(TrackingDeserializer(serializer), jsonString)
        }
        val path = err.path.toString()
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
        testDeserializeError(Package.serializer(), j, "dependencies.serde.version")
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
        testDeserializeError(PackageVec.serializer(), j, "dependencies[1].version")
    }

    @Test
    fun testOption() {
        val j = """{
            "dependency": {
                "version": 1
            }
        }"""
        testDeserializeError(PackageOpt.serializer(), j, "dependency.version")
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
        testDeserializeError(PackageStructVariant.serializer(), j, "dependency.Struct.version")
    }

    @Test
    fun testTupleVariant() {
        val j = """{
            "dependency": {
                "Tuple": ["serde", 1]
            }
        }"""
        testDeserializeError(PackageTupleVariant.serializer(), j, "dependency.Tuple[1]")
    }

    @Test
    fun testUnknownField() {
        val j = """{
            "dependency": {
                "version": "1.0",
                "name": "serde"
            }
        }"""
        testDeserializeError(PackageUnknown.serializer(), j, "dependency.name")
    }

    @Test
    fun testInvalidLength() {
        val j = """{
            "dependency": ["serde"]
        }"""
        testDeserializeError(PackageFixed.serializer(), j, "dependency")
    }

    @Test
    fun testU128() {
        val j = """{
            "n": "130033514578017493995102500318550798591"
        }"""
        val container = json.decodeFromString(
            TrackingDeserializer(Container.serializer()),
            j,
        )
        assertEquals("130033514578017493995102500318550798591", container.n)
    }

    @Test
    fun testMapNonStringKey() {
        val j = """{
            "100": {
                "version": false
            }
        }"""
        val mapSerializer = MapSerializer(Int.serializer(), Dependency.serializer())
        testDeserializeError(mapSerializer, j, "100.version")
    }
}
