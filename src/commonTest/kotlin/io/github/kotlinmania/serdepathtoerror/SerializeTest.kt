// port-lint: tests serde_path_to_error/tests/serialize.rs

@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package io.github.kotlinmania.serdepathtoerror

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import io.github.kotlinmania.serdepathtoerror.Error as PathError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal object FailingSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Failing", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        throw UnsupportedOperationException("deserialize not supported")
    }

    override fun serialize(encoder: Encoder, value: String) {
        throw IllegalStateException("Already borrowed")
    }
}

@Serializable
internal data class Outer(
    val k: Inner,
)

@Serializable
internal data class Inner(
    @Serializable(with = FailingSerializer::class)
    val refcell: String,
)

@Serializable
internal data class FailingNode(
    @Serializable(with = FailingSerializer::class)
    val value: String = "",
)

internal class TrackingSerializer<T>(
    private val delegate: KSerializer<T>,
) : KSerializer<T> {
    override val descriptor: SerialDescriptor
        get() = delegate.descriptor

    override fun deserialize(decoder: Decoder): T = delegate.deserialize(decoder)

    override fun serialize(encoder: Encoder, value: T) {
        io.github.kotlinmania.serdepathtoerror.serialize(delegate, encoder, value)
    }
}

class SerializeTest {

    private fun <T> testSerializeError(serializer: KSerializer<T>, value: T, expected: String) {
        val err = assertFailsWith<PathError> {
            Json.encodeToString(TrackingSerializer(serializer), value)
        }
        val path = err.path.toString()
        assertEquals(expected, path)
    }

    @Test
    fun testRefcellAlreadyBorrowed() {
        val outer = Outer(
            k = Inner(refcell = "content"),
        )
        testSerializeError(Outer.serializer(), outer, "k.refcell")
    }

    @Test
    fun testMapNonStringKey() {
        val innerMap = mapOf("dummy" to FailingNode())
        val midMap = mapOf("k" to innerMap)
        val map = mapOf(100 to midMap)

        val mapSerializer = MapSerializer(
            Int.serializer(),
            MapSerializer(
                String.serializer(),
                MapSerializer(String.serializer(), FailingNode.serializer()),
            ),
        )

        testSerializeError(mapSerializer, map, "100.k.dummy.value")
    }
}
