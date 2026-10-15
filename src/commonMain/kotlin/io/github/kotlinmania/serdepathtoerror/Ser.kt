// port-lint: source ser.rs
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

/**
 * Entry point for tracking path to Serialize error.
 */
public fun <T : Serialize, Ok> serialize(
    value: T,
    serializer: Serializer<Ok>,
): PathResult<Ok, SerdeError> {
    val track = Track.new()
    val trackedSerializer = Serializer(serializer, Chain.Root, track)
    val result = value.serialize(trackedSerializer)
    return when (result) {
        is SerdeResult.Success -> PathResult.ok(result.value)
        is SerdeResult.Failure -> PathResult.err(Error(track.path(), result.error))
    }
}

/**
 * Serializer adapter that records path to serialization errors.
 */
public class Serializer<Ok> internal constructor(
    private val ser: Serializer<Ok>,
    internal val chain: Chain,
    private val track: Track,
) : Serializer<Ok> {
    public constructor(ser: Serializer<Ok>, track: Track) : this(ser, Chain.Root, track)

    override fun isHumanReadable(): Boolean = ser.isHumanReadable()

    override fun serializeBool(v: Boolean): SerdeResult<Ok> =
        ser.serializeBool(v).mapError { track.trigger(chain, it) }

    override fun serializeI8(v: Byte): SerdeResult<Ok> =
        ser.serializeI8(v).mapError { track.trigger(chain, it) }

    override fun serializeI16(v: Short): SerdeResult<Ok> =
        ser.serializeI16(v).mapError { track.trigger(chain, it) }

    override fun serializeI32(v: Int): SerdeResult<Ok> =
        ser.serializeI32(v).mapError { track.trigger(chain, it) }

    override fun serializeI64(v: Long): SerdeResult<Ok> =
        ser.serializeI64(v).mapError { track.trigger(chain, it) }

    override fun serializeI128(value: String): SerdeResult<Ok> =
        ser.serializeI128(value).mapError { track.trigger(chain, it) }

    override fun serializeU8(v: UByte): SerdeResult<Ok> =
        ser.serializeU8(v).mapError { track.trigger(chain, it) }

    override fun serializeU16(v: UShort): SerdeResult<Ok> =
        ser.serializeU16(v).mapError { track.trigger(chain, it) }

    override fun serializeU32(v: UInt): SerdeResult<Ok> =
        ser.serializeU32(v).mapError { track.trigger(chain, it) }

    override fun serializeU64(v: ULong): SerdeResult<Ok> =
        ser.serializeU64(v).mapError { track.trigger(chain, it) }

    override fun serializeU128(value: String): SerdeResult<Ok> =
        ser.serializeU128(value).mapError { track.trigger(chain, it) }

    override fun serializeF32(v: Float): SerdeResult<Ok> =
        ser.serializeF32(v).mapError { track.trigger(chain, it) }

    override fun serializeF64(v: Double): SerdeResult<Ok> =
        ser.serializeF64(v).mapError { track.trigger(chain, it) }

    override fun serializeChar(v: Char): SerdeResult<Ok> =
        ser.serializeChar(v).mapError { track.trigger(chain, it) }

    override fun serializeStr(v: String): SerdeResult<Ok> =
        ser.serializeStr(v).mapError { track.trigger(chain, it) }

    override fun serializeBytes(v: ByteArray): SerdeResult<Ok> =
        ser.serializeBytes(v).mapError { track.trigger(chain, it) }

    override fun serializeNone(): SerdeResult<Ok> =
        ser.serializeNone().mapError { track.trigger(chain, it) }

    override fun <T : Serialize> serializeSome(value: T): SerdeResult<Ok> =
        ser
            .serializeSome(TrackedValue(value, Chain.Some(chain), track))
            .mapError { track.trigger(chain, it) }

    override fun serializeUnit(): SerdeResult<Ok> =
        ser.serializeUnit().mapError { track.trigger(chain, it) }

    override fun serializeUnitStruct(name: String): SerdeResult<Ok> =
        ser.serializeUnitStruct(name).mapError { track.trigger(chain, it) }

    override fun serializeUnitVariant(
        name: String,
        variantIndex: UInt,
        variant: String,
    ): SerdeResult<Ok> =
        ser.serializeUnitVariant(name, variantIndex, variant).mapError { track.trigger(chain, it) }

    override fun <T : Serialize> serializeNewtypeStruct(
        name: String,
        value: T,
    ): SerdeResult<Ok> =
        ser
            .serializeNewtypeStruct(name, TrackedValue(value, Chain.NewtypeStruct(chain), track))
            .mapError { track.trigger(chain, it) }

    override fun <T : Serialize> serializeNewtypeVariant(
        name: String,
        variantIndex: UInt,
        variant: String,
        value: T,
    ): SerdeResult<Ok> =
        ser
            .serializeNewtypeVariant(
                name,
                variantIndex,
                variant,
                TrackedValue(value, Chain.NewtypeVariant(chain), track),
            ).mapError { track.trigger(chain, it) }

    override fun serializeSeq(len: Int?): SerdeResult<SerializeSeq<Ok>> =
        ser.serializeSeq(len).map { WrapSeq(it, chain, track) }.mapError { track.trigger(chain, it) }

    override fun serializeTuple(len: Int): SerdeResult<SerializeTuple<Ok>> =
        ser.serializeTuple(len).map { WrapTuple(it, chain, track) }.mapError { track.trigger(chain, it) }

    override fun serializeTupleStruct(name: String, len: Int): SerdeResult<SerializeTupleStruct<Ok>> =
        ser.serializeTupleStruct(name, len).map { WrapTupleStruct(it, chain, track) }.mapError { track.trigger(chain, it) }

    override fun serializeTupleVariant(
        name: String,
        variantIndex: UInt,
        variant: String,
        len: Int,
    ): SerdeResult<SerializeTupleVariant<Ok>> =
        ser
            .serializeTupleVariant(name, variantIndex, variant, len)
            .map { WrapTupleVariant(it, chain, track) }
            .mapError { track.trigger(chain, it) }

    override fun serializeMap(len: Int?): SerdeResult<SerializeMap<Ok>> =
        ser.serializeMap(len).map { WrapMap(it, chain, track) }.mapError { track.trigger(chain, it) }

    override fun serializeStruct(name: String, len: Int): SerdeResult<SerializeStruct<Ok>> =
        ser.serializeStruct(name, len).map { WrapStruct(it, chain, track) }.mapError { track.trigger(chain, it) }

    override fun serializeStructVariant(
        name: String,
        variantIndex: UInt,
        variant: String,
        len: Int,
    ): SerdeResult<SerializeStructVariant<Ok>> =
        ser
            .serializeStructVariant(name, variantIndex, variant, len)
            .map { WrapStructVariant(it, chain, track) }
            .mapError { track.trigger(chain, it) }
}

private inline fun <T> SerdeResult<T>.mapError(transform: (SerdeError) -> SerdeError): SerdeResult<T> =
    when (this) {
        is SerdeResult.Success -> this
        is SerdeResult.Failure -> SerdeResult.failure(transform(error))
    }

internal class TrackedValue<T : Serialize>(
    private val value: T,
    private val chain: Chain,
    private val track: Track,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        value
            .serialize(Serializer(serializer, chain, track))
            .mapError { track.trigger(chain, it) }
}

internal class WrapSeq<Ok>(
    private val delegate: SerializeSeq<Ok>,
    private val chain: Chain,
    private val track: Track,
) : SerializeSeq<Ok> {
    private var index = 0

    override fun <T : Serialize> serializeElement(value: T): SerdeResult<Unit> {
        val elementChain = Chain.Seq(chain, index)
        index += 1
        return delegate
            .serializeElement(TrackedValue(value, elementChain, track))
            .mapError { track.trigger(chain, it) }
    }

    override fun end(): SerdeResult<Ok> =
        delegate.end().mapError { track.trigger(chain, it) }
}

internal class WrapTuple<Ok>(
    private val delegate: SerializeTuple<Ok>,
    private val chain: Chain,
    private val track: Track,
) : SerializeTuple<Ok> {
    private var index = 0

    override fun <T : Serialize> serializeElement(value: T): SerdeResult<Unit> {
        val elementChain = Chain.Seq(chain, index)
        index += 1
        return delegate
            .serializeElement(TrackedValue(value, elementChain, track))
            .mapError { track.trigger(chain, it) }
    }

    override fun end(): SerdeResult<Ok> =
        delegate.end().mapError { track.trigger(chain, it) }
}

internal class WrapTupleStruct<Ok>(
    private val delegate: SerializeTupleStruct<Ok>,
    private val chain: Chain,
    private val track: Track,
) : SerializeTupleStruct<Ok> {
    private var index = 0

    override fun <T : Serialize> serializeField(value: T): SerdeResult<Unit> {
        val elementChain = Chain.Seq(chain, index)
        index += 1
        return delegate
            .serializeField(TrackedValue(value, elementChain, track))
            .mapError { track.trigger(chain, it) }
    }

    override fun end(): SerdeResult<Ok> =
        delegate.end().mapError { track.trigger(chain, it) }
}

internal class WrapTupleVariant<Ok>(
    private val delegate: SerializeTupleVariant<Ok>,
    private val chain: Chain,
    private val track: Track,
) : SerializeTupleVariant<Ok> {
    private var index = 0

    override fun <T : Serialize> serializeField(value: T): SerdeResult<Unit> {
        val elementChain = Chain.Seq(chain, index)
        index += 1
        return delegate
            .serializeField(TrackedValue(value, elementChain, track))
            .mapError { track.trigger(chain, it) }
    }

    override fun end(): SerdeResult<Ok> =
        delegate.end().mapError { track.trigger(chain, it) }
}

internal class WrapMap<Ok>(
    private val delegate: SerializeMap<Ok>,
    private val chain: Chain,
    private val track: Track,
) : SerializeMap<Ok> {
    private var capturedKey: String? = null

    override fun <T : Serialize> serializeKey(key: T): SerdeResult<Unit> {
        capturedKey = null
        val capture = CaptureKeySerialize(key) { capturedKey = it }
        return delegate.serializeKey(capture).mapError { track.trigger(chain, it) }
    }

    override fun <T : Serialize> serializeValue(value: T): SerdeResult<Unit> {
        val key = capturedKey
        capturedKey = null
        val valueChain = if (key != null) Chain.Map(chain, key) else Chain.NonStringKey(chain)
        return delegate
            .serializeValue(TrackedValue(value, valueChain, track))
            .mapError { track.trigger(chain, it) }
    }

    override fun <K : Serialize, V : Serialize> serializeEntry(key: K, value: V): SerdeResult<Unit> {
        val resKey = serializeKey(key)
        if (resKey.isFailure) return resKey
        return serializeValue(value)
    }

    override fun end(): SerdeResult<Ok> =
        delegate.end().mapError { track.trigger(chain, it) }
}

internal class WrapStruct<Ok>(
    private val delegate: SerializeStruct<Ok>,
    private val chain: Chain,
    private val track: Track,
) : SerializeStruct<Ok> {
    override fun <T : Serialize> serializeField(key: String, value: T): SerdeResult<Unit> {
        val fieldChain = Chain.Struct(chain, key)
        return delegate
            .serializeField(key, TrackedValue(value, fieldChain, track))
            .mapError { track.trigger(chain, it) }
    }

    override fun skipField(key: String): SerdeResult<Unit> =
        delegate.skipField(key).mapError { track.trigger(chain, it) }

    override fun end(): SerdeResult<Ok> =
        delegate.end().mapError { track.trigger(chain, it) }
}

internal class WrapStructVariant<Ok>(
    private val delegate: SerializeStructVariant<Ok>,
    private val chain: Chain,
    private val track: Track,
) : SerializeStructVariant<Ok> {
    override fun <T : Serialize> serializeField(key: String, value: T): SerdeResult<Unit> {
        val fieldChain = Chain.Struct(chain, key)
        return delegate
            .serializeField(key, TrackedValue(value, fieldChain, track))
            .mapError { track.trigger(chain, it) }
    }

    override fun skipField(key: String): SerdeResult<Unit> =
        delegate.skipField(key).mapError { track.trigger(chain, it) }

    override fun end(): SerdeResult<Ok> =
        delegate.end().mapError { track.trigger(chain, it) }
}

internal class CaptureKeySerialize<T : Serialize>(
    private val delegate: T,
    private val onKey: (String) -> Unit,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        delegate.serialize(CaptureKeySerializer(serializer, onKey))
}

internal class CaptureKeySerializer<Ok>(
    private val delegate: Serializer<Ok>,
    private val onKey: (String) -> Unit,
) : Serializer<Ok> {
    override fun isHumanReadable(): Boolean = delegate.isHumanReadable()

    override fun serializeStr(v: String): SerdeResult<Ok> {
        onKey(v)
        return delegate.serializeStr(v)
    }

    override fun serializeBool(v: Boolean): SerdeResult<Ok> {
        onKey(v.toString())
        return delegate.serializeBool(v)
    }

    override fun serializeI8(v: Byte): SerdeResult<Ok> {
        onKey(v.toString())
        return delegate.serializeI8(v)
    }

    override fun serializeI16(v: Short): SerdeResult<Ok> {
        onKey(v.toString())
        return delegate.serializeI16(v)
    }

    override fun serializeI32(v: Int): SerdeResult<Ok> {
        onKey(v.toString())
        return delegate.serializeI32(v)
    }

    override fun serializeI64(v: Long): SerdeResult<Ok> {
        onKey(v.toString())
        return delegate.serializeI64(v)
    }

    override fun serializeI128(value: String): SerdeResult<Ok> {
        onKey(value)
        return delegate.serializeI128(value)
    }

    override fun serializeU8(v: UByte): SerdeResult<Ok> {
        onKey(v.toString())
        return delegate.serializeU8(v)
    }

    override fun serializeU16(v: UShort): SerdeResult<Ok> {
        onKey(v.toString())
        return delegate.serializeU16(v)
    }

    override fun serializeU32(v: UInt): SerdeResult<Ok> {
        onKey(v.toString())
        return delegate.serializeU32(v)
    }

    override fun serializeU64(v: ULong): SerdeResult<Ok> {
        onKey(v.toString())
        return delegate.serializeU64(v)
    }

    override fun serializeU128(value: String): SerdeResult<Ok> {
        onKey(value)
        return delegate.serializeU128(value)
    }

    override fun serializeF32(v: Float): SerdeResult<Ok> = delegate.serializeF32(v)

    override fun serializeF64(v: Double): SerdeResult<Ok> = delegate.serializeF64(v)

    override fun serializeChar(v: Char): SerdeResult<Ok> = delegate.serializeChar(v)

    override fun serializeBytes(v: ByteArray): SerdeResult<Ok> = delegate.serializeBytes(v)

    override fun serializeNone(): SerdeResult<Ok> = delegate.serializeNone()

    override fun <T : Serialize> serializeSome(value: T): SerdeResult<Ok> = delegate.serializeSome(value)

    override fun serializeUnit(): SerdeResult<Ok> = delegate.serializeUnit()

    override fun serializeUnitStruct(name: String): SerdeResult<Ok> = delegate.serializeUnitStruct(name)

    override fun serializeUnitVariant(name: String, variantIndex: UInt, variant: String): SerdeResult<Ok> =
        delegate.serializeUnitVariant(name, variantIndex, variant)

    override fun <T : Serialize> serializeNewtypeStruct(name: String, value: T): SerdeResult<Ok> =
        delegate.serializeNewtypeStruct(name, value)

    override fun <T : Serialize> serializeNewtypeVariant(name: String, variantIndex: UInt, variant: String, value: T): SerdeResult<Ok> =
        delegate.serializeNewtypeVariant(name, variantIndex, variant, value)

    override fun serializeSeq(len: Int?): SerdeResult<SerializeSeq<Ok>> = delegate.serializeSeq(len)

    override fun serializeTuple(len: Int): SerdeResult<SerializeTuple<Ok>> = delegate.serializeTuple(len)

    override fun serializeTupleStruct(name: String, len: Int): SerdeResult<SerializeTupleStruct<Ok>> =
        delegate.serializeTupleStruct(name, len)

    override fun serializeTupleVariant(name: String, variantIndex: UInt, variant: String, len: Int): SerdeResult<SerializeTupleVariant<Ok>> =
        delegate.serializeTupleVariant(name, variantIndex, variant, len)

    override fun serializeMap(len: Int?): SerdeResult<SerializeMap<Ok>> = delegate.serializeMap(len)

    override fun serializeStruct(name: String, len: Int): SerdeResult<SerializeStruct<Ok>> = delegate.serializeStruct(name, len)

    override fun serializeStructVariant(name: String, variantIndex: UInt, variant: String, len: Int): SerdeResult<SerializeStructVariant<Ok>> =
        delegate.serializeStructVariant(name, variantIndex, variant, len)
}
