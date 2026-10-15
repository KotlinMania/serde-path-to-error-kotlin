// port-lint: source de.rs
package io.github.kotlinmania.serdepathtoerror

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.DeserializeSeed
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.EnumAccess
import io.github.kotlinmania.serdecore.de.MapAccess
import io.github.kotlinmania.serdecore.de.SeqAccess
import io.github.kotlinmania.serdecore.de.VariantAccess
import io.github.kotlinmania.serdecore.de.Visitor

/**
 * Entry point for deserializing with path tracking.
 */
public fun <T> deserialize(
    deserializer: Deserializer,
    deserialize: Deserialize<T>,
): PathResult<T, SerdeError> {
    val track = Track.new()
    val trackedDeserializer = Deserializer(deserializer, Chain.Root, track)
    val result = deserialize.deserialize(trackedDeserializer)
    return when (result) {
        is SerdeResult.Success -> PathResult.ok(result.value)
        is SerdeResult.Failure -> PathResult.err(Error(track.path(), result.error))
    }
}

/**
 * Entry point for deserializing with path tracking using a [DeserializeSeed].
 */
public fun <T> deserialize(
    deserializer: Deserializer,
    seed: DeserializeSeed<T>,
): PathResult<T, SerdeError> {
    val track = Track.new()
    val trackedDeserializer = Deserializer(deserializer, Chain.Root, track)
    val result = seed.deserialize(trackedDeserializer)
    return when (result) {
        is SerdeResult.Success -> PathResult.ok(result.value)
        is SerdeResult.Failure -> PathResult.err(Error(track.path(), result.error))
    }
}

/**
 * Deserializer adapter that records path to deserialization errors.
 */
public class Deserializer internal constructor(
    private val de: Deserializer,
    internal val chain: Chain,
    private val track: Track,
) : Deserializer {
    public constructor(de: Deserializer, track: Track) : this(de, Chain.Root, track)

    override fun isHumanReadable(): Boolean = de.isHumanReadable()

    override fun <V> deserializeAny(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeAny(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeBool(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeBool(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeI8(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeI8(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeI16(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeI16(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeI32(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeI32(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeI64(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeI64(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeI128(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeI128(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeU8(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeU8(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeU16(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeU16(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeU32(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeU32(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeU64(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeU64(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeU128(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeU128(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeF32(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeF32(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeF64(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeF64(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeChar(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeChar(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeStr(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeStr(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeString(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeString(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeBytes(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeBytes(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeByteBuf(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeByteBuf(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeOption(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeOption(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeUnit(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeUnit(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeUnitStruct(name: String, visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeUnitStruct(name, WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeNewtypeStruct(name: String, visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeNewtypeStruct(name, WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeSeq(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeSeq(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeTuple(len: Int, visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeTuple(len, WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeTupleStruct(name: String, len: Int, visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeTupleStruct(name, len, WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeMap(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeMap(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeStruct(name: String, fields: List<String>, visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeStruct(name, fields, WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeEnum(name: String, variants: List<String>, visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeEnum(name, variants, WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeIgnoredAny(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeIgnoredAny(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }

    override fun <V> deserializeIdentifier(visitor: Visitor<V>): SerdeResult<V> =
        de.deserializeIdentifier(WrapVisitor(visitor, chain, track)).mapError { track.trigger(chain, it) }
}

private inline fun <T> SerdeResult<T>.mapError(transform: (SerdeError) -> SerdeError): SerdeResult<T> =
    when (this) {
        is SerdeResult.Success -> this
        is SerdeResult.Failure -> SerdeResult.failure(transform(error))
    }

internal class WrapVisitor<V>(
    private val delegate: Visitor<V>,
    private val chain: Chain,
    private val track: Track,
) : Visitor<V> {
    override fun expecting(): String = delegate.expecting()

    override fun visitBool(v: Boolean): SerdeResult<V> =
        delegate.visitBool(v).mapError { track.trigger(chain, it) }

    override fun visitI8(v: Byte): SerdeResult<V> =
        delegate.visitI8(v).mapError { track.trigger(chain, it) }

    override fun visitI16(v: Short): SerdeResult<V> =
        delegate.visitI16(v).mapError { track.trigger(chain, it) }

    override fun visitI32(v: Int): SerdeResult<V> =
        delegate.visitI32(v).mapError { track.trigger(chain, it) }

    override fun visitI64(v: Long): SerdeResult<V> =
        delegate.visitI64(v).mapError { track.trigger(chain, it) }

    override fun visitI128(v: String): SerdeResult<V> =
        delegate.visitI128(v).mapError { track.trigger(chain, it) }

    override fun visitU8(v: UByte): SerdeResult<V> =
        delegate.visitU8(v).mapError { track.trigger(chain, it) }

    override fun visitU16(v: UShort): SerdeResult<V> =
        delegate.visitU16(v).mapError { track.trigger(chain, it) }

    override fun visitU32(v: UInt): SerdeResult<V> =
        delegate.visitU32(v).mapError { track.trigger(chain, it) }

    override fun visitU64(v: ULong): SerdeResult<V> =
        delegate.visitU64(v).mapError { track.trigger(chain, it) }

    override fun visitU128(v: String): SerdeResult<V> =
        delegate.visitU128(v).mapError { track.trigger(chain, it) }

    override fun visitF32(v: Float): SerdeResult<V> =
        delegate.visitF32(v).mapError { track.trigger(chain, it) }

    override fun visitF64(v: Double): SerdeResult<V> =
        delegate.visitF64(v).mapError { track.trigger(chain, it) }

    override fun visitChar(v: Char): SerdeResult<V> =
        delegate.visitChar(v).mapError { track.trigger(chain, it) }

    override fun visitStr(v: String): SerdeResult<V> =
        delegate.visitStr(v).mapError { track.trigger(chain, it) }

    override fun visitString(v: String): SerdeResult<V> =
        delegate.visitString(v).mapError { track.trigger(chain, it) }

    override fun visitBytes(v: ByteArray): SerdeResult<V> =
        delegate.visitBytes(v).mapError { track.trigger(chain, it) }

    override fun visitNone(): SerdeResult<V> =
        delegate.visitNone().mapError { track.trigger(chain, it) }

    override fun <D : Deserializer> visitSome(deserializer: D): SerdeResult<V> =
        delegate
            .visitSome(Deserializer(deserializer, Chain.Some(chain), track))
            .mapError { track.trigger(chain, it) }

    override fun visitUnit(): SerdeResult<V> =
        delegate.visitUnit().mapError { track.trigger(chain, it) }

    override fun <D : Deserializer> visitNewtypeStruct(deserializer: D): SerdeResult<V> =
        delegate
            .visitNewtypeStruct(Deserializer(deserializer, Chain.NewtypeStruct(chain), track))
            .mapError { track.trigger(chain, it) }

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<V> =
        delegate.visitSeq(PathSeqAccess(access, chain, track)).mapError { track.trigger(chain, it) }

    override fun <A : MapAccess> visitMap(access: A): SerdeResult<V> =
        delegate.visitMap(PathMapAccess(access, chain, track)).mapError { track.trigger(chain, it) }

    override fun <A : EnumAccess> visitEnum(access: A): SerdeResult<V> =
        delegate.visitEnum(PathEnumAccess(access, chain, track)).mapError { track.trigger(chain, it) }
}

internal class PathSeqAccess(
    private val delegate: SeqAccess,
    private val chain: Chain,
    private val track: Track,
) : SeqAccess {
    private var index = 0

    override fun <T> nextElementSeed(seed: DeserializeSeed<T>): SerdeResult<T?> {
        val elementChain = Chain.Seq(chain, index)
        index += 1
        return delegate
            .nextElementSeed(TrackedSeed(seed, elementChain, track))
            .mapError { track.trigger(chain, it) }
    }

    override fun sizeHint(): Int? = delegate.sizeHint()
}

internal class PathMapAccess(
    private val delegate: MapAccess,
    private val chain: Chain,
    private val track: Track,
) : MapAccess {
    private var capturedKey: String? = null

    override fun <K> nextKeySeed(seed: DeserializeSeed<K>): SerdeResult<K?> {
        val captureSeed = CaptureKeySeed(seed) { capturedKey = it }
        return delegate.nextKeySeed(captureSeed).mapError { err ->
            val key = capturedKey
            capturedKey = null
            val errorChain = if (key != null) Chain.Map(chain, key) else Chain.NonStringKey(chain)
            track.trigger(errorChain, err)
        }
    }

    override fun <V> nextValueSeed(seed: DeserializeSeed<V>): SerdeResult<V> {
        val key = capturedKey
        capturedKey = null
        val valueChain = if (key != null) Chain.Map(chain, key) else Chain.NonStringKey(chain)
        return delegate
            .nextValueSeed(TrackedSeed(seed, valueChain, track))
            .mapError { track.trigger(chain, it) }
    }

    override fun sizeHint(): Int? = delegate.sizeHint()
}

internal class PathEnumAccess(
    private val delegate: EnumAccess,
    private val chain: Chain,
    private val track: Track,
) : EnumAccess {
    override fun <V> variantSeed(seed: DeserializeSeed<V>): SerdeResult<Pair<V, VariantAccess>> {
        var capturedVariant: String? = null
        val captureSeed = CaptureKeySeed(seed) { capturedVariant = it }
        return delegate
            .variantSeed(captureSeed)
            .mapError { track.trigger(chain, it) }
            .map { (v, access) ->
                val variant = capturedVariant
                val variantChain = if (variant != null) Chain.Enum(chain, variant) else chain
                Pair(v, PathVariantAccess(access, variantChain, track))
            }
    }
}

internal class PathVariantAccess(
    private val delegate: VariantAccess,
    private val chain: Chain,
    private val track: Track,
) : VariantAccess {
    override fun unitVariant(): SerdeResult<Unit> =
        delegate.unitVariant().mapError { track.trigger(chain, it) }

    override fun <T> newtypeVariantSeed(seed: DeserializeSeed<T>): SerdeResult<T> =
        delegate
            .newtypeVariantSeed(TrackedSeed(seed, Chain.NewtypeVariant(chain), track))
            .mapError { track.trigger(chain, it) }

    override fun <V> tupleVariant(len: Int, visitor: Visitor<V>): SerdeResult<V> =
        delegate
            .tupleVariant(len, WrapVisitor(visitor, chain, track))
            .mapError { track.trigger(chain, it) }

    override fun <V> structVariant(fields: List<String>, visitor: Visitor<V>): SerdeResult<V> =
        delegate
            .structVariant(fields, WrapVisitor(visitor, chain, track))
            .mapError { track.trigger(chain, it) }
}

internal class TrackedSeed<T>(
    private val seed: DeserializeSeed<T>,
    private val chain: Chain,
    private val track: Track,
) : DeserializeSeed<T> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<T> =
        seed
            .deserialize(Deserializer(deserializer, chain, track))
            .mapError { track.trigger(chain, it) }
}

internal class CaptureKeySeed<T>(
    private val delegate: DeserializeSeed<T>,
    private val onKey: (String) -> Unit,
) : DeserializeSeed<T> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<T> =
        delegate.deserialize(CaptureKeyDeserializer(deserializer, onKey))
}

internal class CaptureKeyDeserializer(
    private val delegate: Deserializer,
    private val onKey: (String) -> Unit,
) : Deserializer {
    override fun isHumanReadable(): Boolean = delegate.isHumanReadable()

    override fun <V> deserializeAny(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeAny(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeBool(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeBool(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeI8(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeI8(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeI16(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeI16(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeI32(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeI32(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeI64(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeI64(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeI128(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeI128(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeU8(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeU8(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeU16(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeU16(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeU32(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeU32(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeU64(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeU64(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeU128(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeU128(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeF32(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeF32(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeF64(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeF64(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeChar(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeChar(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeStr(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeStr(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeString(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeString(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeBytes(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeBytes(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeByteBuf(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeByteBuf(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeOption(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeOption(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeUnit(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeUnit(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeUnitStruct(name: String, visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeUnitStruct(name, CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeNewtypeStruct(name: String, visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeNewtypeStruct(name, CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeSeq(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeSeq(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeTuple(len: Int, visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeTuple(len, CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeTupleStruct(name: String, len: Int, visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeTupleStruct(name, len, CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeMap(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeMap(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeStruct(name: String, fields: List<String>, visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeStruct(name, fields, CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeEnum(name: String, variants: List<String>, visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeEnum(name, variants, CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeIdentifier(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeIdentifier(CaptureKeyVisitor(visitor, onKey))

    override fun <V> deserializeIgnoredAny(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeIgnoredAny(CaptureKeyVisitor(visitor, onKey))
}

internal class CaptureKeyVisitor<V>(
    private val delegate: Visitor<V>,
    private val onKey: (String) -> Unit,
) : Visitor<V> {
    override fun expecting(): String = delegate.expecting()

    override fun visitBool(v: Boolean): SerdeResult<V> {
        onKey(if (v) "true" else "false")
        return delegate.visitBool(v)
    }

    override fun visitI8(v: Byte): SerdeResult<V> {
        onKey(v.toString())
        return delegate.visitI8(v)
    }

    override fun visitI16(v: Short): SerdeResult<V> {
        onKey(v.toString())
        return delegate.visitI16(v)
    }

    override fun visitI32(v: Int): SerdeResult<V> {
        onKey(v.toString())
        return delegate.visitI32(v)
    }

    override fun visitI64(v: Long): SerdeResult<V> {
        onKey(v.toString())
        return delegate.visitI64(v)
    }

    override fun visitI128(v: String): SerdeResult<V> {
        onKey(v)
        return delegate.visitI128(v)
    }

    override fun visitU8(v: UByte): SerdeResult<V> {
        onKey(v.toString())
        return delegate.visitU8(v)
    }

    override fun visitU16(v: UShort): SerdeResult<V> {
        onKey(v.toString())
        return delegate.visitU16(v)
    }

    override fun visitU32(v: UInt): SerdeResult<V> {
        onKey(v.toString())
        return delegate.visitU32(v)
    }

    override fun visitU64(v: ULong): SerdeResult<V> {
        onKey(v.toString())
        return delegate.visitU64(v)
    }

    override fun visitU128(v: String): SerdeResult<V> {
        onKey(v)
        return delegate.visitU128(v)
    }

    override fun visitStr(v: String): SerdeResult<V> {
        onKey(v)
        return delegate.visitStr(v)
    }

    override fun visitString(v: String): SerdeResult<V> {
        onKey(v)
        return delegate.visitString(v)
    }

    override fun visitF32(v: Float): SerdeResult<V> = delegate.visitF32(v)

    override fun visitF64(v: Double): SerdeResult<V> = delegate.visitF64(v)

    override fun visitChar(v: Char): SerdeResult<V> = delegate.visitChar(v)

    override fun visitBytes(v: ByteArray): SerdeResult<V> = delegate.visitBytes(v)

    override fun visitNone(): SerdeResult<V> = delegate.visitNone()

    override fun <D : Deserializer> visitSome(deserializer: D): SerdeResult<V> = delegate.visitSome(deserializer)

    override fun visitUnit(): SerdeResult<V> = delegate.visitUnit()

    override fun <D : Deserializer> visitNewtypeStruct(deserializer: D): SerdeResult<V> = delegate.visitNewtypeStruct(deserializer)

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<V> = delegate.visitSeq(access)

    override fun <A : MapAccess> visitMap(access: A): SerdeResult<V> = delegate.visitMap(access)

    override fun <A : EnumAccess> visitEnum(access: A): SerdeResult<V> = delegate.visitEnum(access)
}
