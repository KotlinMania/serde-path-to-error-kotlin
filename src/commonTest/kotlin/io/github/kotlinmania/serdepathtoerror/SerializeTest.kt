// port-lint: tests tests/serialize.rs
package io.github.kotlinmania.serdepathtoerror

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.SerializeMap
import io.github.kotlinmania.serdecore.ser.SerializeSeq
import io.github.kotlinmania.serdecore.ser.SerializeStruct
import io.github.kotlinmania.serdecore.ser.SerializeStructVariant
import io.github.kotlinmania.serdecore.ser.SerializeTuple
import io.github.kotlinmania.serdecore.ser.SerializeTupleStruct
import io.github.kotlinmania.serdecore.ser.SerializeTupleVariant
import io.github.kotlinmania.serdecore.ser.Serializer
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializeTest {
    private fun testSerialize(value: Serialize, expected: String) {
        val ser = TestSerializer()
        val result =
            io.github.kotlinmania.serdepathtoerror
                .serialize(value, ser)
        val path = result.unwrapErr().path.toString()
        assertEquals(expected, path)
    }

    @Test
    fun testRefcellAlreadyBorrowed() {
        val outer = Outer(Inner(FailingRefCell()))
        testSerialize(outer, "k.refcell")
    }

    @Test
    fun testMapNonStringKey() {
        val innerMap = MapSerialize(listOf(FailingKey() to IntSerialize(0)))
        val midMap = MapSerialize(listOf(StringSerialize("k") to innerMap))
        val outerMap = MapSerialize(listOf(IntSerialize(100) to midMap))

        testSerialize(outerMap, "100.k")
    }

    private data class Outer(
        val k: Inner,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
            val s = serializer.serializeStruct("Outer", 1).getOrElse { return SerdeResult.failure(it) }
            s.serializeField("k", k).getOrElse { return SerdeResult.failure(it) }
            return s.end()
        }
    }

    private data class Inner(
        val refcell: FailingRefCell,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
            val s = serializer.serializeStruct("Inner", 1).getOrElse { return SerdeResult.failure(it) }
            s.serializeField("refcell", refcell).getOrElse { return SerdeResult.failure(it) }
            return s.end()
        }
    }

    private class FailingRefCell : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
            SerdeResult.failure(SerdeError.custom("Already borrowed"))
    }

    private class FailingKey : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
            SerdeResult.failure(SerdeError.custom("key must be a string"))
    }

    private data class IntSerialize(
        val value: Int,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
            serializer.serializeI32(value)
    }

    private data class StringSerialize(
        val value: String,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
            serializer.serializeStr(value)
    }

    private class MapSerialize<K : Serialize, V : Serialize>(
        private val entries: List<Pair<K, V>>,
    ) : Serialize {
        override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> {
            val map = serializer.serializeMap(entries.size).getOrElse { return SerdeResult.failure(it) }
            for ((k, v) in entries) {
                map.serializeKey(k).getOrElse { return SerdeResult.failure(it) }
                map.serializeValue(v).getOrElse { return SerdeResult.failure(it) }
            }
            return map.end()
        }
    }

    private class TestSerializer : Serializer<Unit> {
        override fun isHumanReadable(): Boolean = true

        override fun serializeBool(v: Boolean): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeI8(v: Byte): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeI16(v: Short): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeI32(v: Int): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeI64(v: Long): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeU8(v: UByte): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeU16(v: UShort): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeU32(v: UInt): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeU64(v: ULong): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeF32(v: Float): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeF64(v: Double): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeChar(v: Char): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeStr(v: String): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeBytes(v: ByteArray): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeNone(): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun <T : Serialize> serializeSome(value: T): SerdeResult<Unit> = value.serialize(this)

        override fun serializeUnit(): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeUnitStruct(name: String): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun serializeUnitVariant(name: String, variantIndex: UInt, variant: String): SerdeResult<Unit> = SerdeResult.success(Unit)

        override fun <T : Serialize> serializeNewtypeStruct(name: String, value: T): SerdeResult<Unit> = value.serialize(this)

        override fun <T : Serialize> serializeNewtypeVariant(name: String, variantIndex: UInt, variant: String, value: T): SerdeResult<Unit> = value.serialize(this)

        override fun serializeSeq(len: Int?): SerdeResult<SerializeSeq<Unit>> =
            SerdeResult.success(
                object : SerializeSeq<Unit> {
                    override fun <T : Serialize> serializeElement(value: T): SerdeResult<Unit> = value.serialize(this@TestSerializer)

                    override fun end(): SerdeResult<Unit> = SerdeResult.success(Unit)
                },
            )

        override fun serializeTuple(len: Int): SerdeResult<SerializeTuple<Unit>> =
            SerdeResult.success(
                object : SerializeTuple<Unit> {
                    override fun <T : Serialize> serializeElement(value: T): SerdeResult<Unit> = value.serialize(this@TestSerializer)

                    override fun end(): SerdeResult<Unit> = SerdeResult.success(Unit)
                },
            )

        override fun serializeTupleStruct(name: String, len: Int): SerdeResult<SerializeTupleStruct<Unit>> =
            SerdeResult.success(
                object : SerializeTupleStruct<Unit> {
                    override fun <T : Serialize> serializeField(value: T): SerdeResult<Unit> = value.serialize(this@TestSerializer)

                    override fun end(): SerdeResult<Unit> = SerdeResult.success(Unit)
                },
            )

        override fun serializeTupleVariant(name: String, variantIndex: UInt, variant: String, len: Int): SerdeResult<SerializeTupleVariant<Unit>> =
            SerdeResult.success(
                object : SerializeTupleVariant<Unit> {
                    override fun <T : Serialize> serializeField(value: T): SerdeResult<Unit> = value.serialize(this@TestSerializer)

                    override fun end(): SerdeResult<Unit> = SerdeResult.success(Unit)
                },
            )

        override fun serializeMap(len: Int?): SerdeResult<SerializeMap<Unit>> =
            SerdeResult.success(
                object : SerializeMap<Unit> {
                    override fun <T : Serialize> serializeKey(key: T): SerdeResult<Unit> = key.serialize(this@TestSerializer)

                    override fun <T : Serialize> serializeValue(value: T): SerdeResult<Unit> = value.serialize(this@TestSerializer)

                    override fun end(): SerdeResult<Unit> = SerdeResult.success(Unit)
                },
            )

        override fun serializeStruct(name: String, len: Int): SerdeResult<SerializeStruct<Unit>> =
            SerdeResult.success(
                object : SerializeStruct<Unit> {
                    override fun <T : Serialize> serializeField(key: String, value: T): SerdeResult<Unit> = value.serialize(this@TestSerializer)

                    override fun end(): SerdeResult<Unit> = SerdeResult.success(Unit)
                },
            )

        override fun serializeStructVariant(name: String, variantIndex: UInt, variant: String, len: Int): SerdeResult<SerializeStructVariant<Unit>> =
            SerdeResult.success(
                object : SerializeStructVariant<Unit> {
                    override fun <T : Serialize> serializeField(key: String, value: T): SerdeResult<Unit> = value.serialize(this@TestSerializer)

                    override fun end(): SerdeResult<Unit> = SerdeResult.success(Unit)
                },
            )
    }
}
