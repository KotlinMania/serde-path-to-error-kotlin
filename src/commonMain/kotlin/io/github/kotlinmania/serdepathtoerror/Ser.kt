// port-lint: source serde_path_to_error/src/ser.rs

@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package io.github.kotlinmania.serdepathtoerror

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

/**
 * Entry point for serializing with path tracking.
 */
public fun <T> serialize(serializer: SerializationStrategy<T>, encoder: Encoder, value: T) {
    val track = Track.new()
    val trackedEncoder = Serializer.new(encoder, track)
    try {
        serializer.serialize(trackedEncoder, value)
    } catch (e: Error) {
        throw e
    } catch (e: Throwable) {
        throw Error(track.path(), e)
    }
}

/**
 * Serializer adapter that records path to serialization errors.
 */
public class Serializer private constructor(
    private val ser: Encoder,
    private val chain: Chain,
    private val track: Track,
) : Encoder {
    public constructor(ser: Encoder, track: Track) : this(ser, Chain.Root, track)

    override val serializersModule: SerializersModule
        get() = ser.serializersModule

    override fun encodeNotNullMark() {
        try {
            ser.encodeNotNullMark()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun encodeNull() {
        try {
            ser.encodeNull()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun encodeBoolean(value: Boolean) {
        try {
            ser.encodeBoolean(value)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun encodeByte(value: Byte) {
        try {
            ser.encodeByte(value)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun encodeShort(value: Short) {
        try {
            ser.encodeShort(value)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun encodeInt(value: Int) {
        try {
            ser.encodeInt(value)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun encodeLong(value: Long) {
        try {
            ser.encodeLong(value)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun encodeFloat(value: Float) {
        try {
            ser.encodeFloat(value)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun encodeDouble(value: Double) {
        try {
            ser.encodeDouble(value)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun encodeChar(value: Char) {
        try {
            ser.encodeChar(value)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun encodeString(value: String) {
        try {
            ser.encodeString(value)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        try {
            ser.encodeEnum(enumDescriptor, index)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder =
        try {
            Serializer(ser.encodeInline(descriptor), Chain.NewtypeStruct(chain), track)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        try {
            serializer.serialize(this, value)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder =
        try {
            val composite = ser.beginStructure(descriptor)
            TrackedCompositeEncoder(composite, descriptor, chain, track)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    public companion object {
        public fun new(ser: Encoder, track: Track): Serializer = Serializer(ser, track)

        internal fun withChain(ser: Encoder, chain: Chain, track: Track): Serializer =
            Serializer(ser, chain, track)
    }
}

public class TrackedCompositeEncoder(
    private val delegate: CompositeEncoder,
    private val descriptor: SerialDescriptor,
    private val chain: Chain,
    private val track: Track,
) : CompositeEncoder {
    private var lastKeyString: String? = null

    override val serializersModule: SerializersModule
        get() = delegate.serializersModule

    override fun endStructure(descriptor: SerialDescriptor) {
        try {
            delegate.endStructure(descriptor)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    private fun childChain(index: Int): Chain =
        when (descriptor.kind) {
            StructureKind.CLASS, StructureKind.OBJECT -> {
                if (index in 0 until descriptor.elementsCount) {
                    Chain.Struct(chain, descriptor.getElementName(index))
                } else {
                    Chain.Seq(chain, index)
                }
            }
            StructureKind.LIST -> Chain.Seq(chain, index)
            StructureKind.MAP -> {
                if (index % 2 == 0) {
                    Chain.Seq(chain, index / 2)
                } else {
                    val key = lastKeyString
                    if (key != null) {
                        Chain.Map(chain, key)
                    } else {
                        Chain.NonStringKey(chain)
                    }
                }
            }
            is PolymorphicKind -> chain
            else -> Chain.Seq(chain, index)
        }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) =
        withElement(index) { delegate.encodeBooleanElement(descriptor, index, value) }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) =
        withElement(index) { delegate.encodeByteElement(descriptor, index, value) }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) =
        withElement(index) { delegate.encodeShortElement(descriptor, index, value) }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) =
        withElement(index) { delegate.encodeIntElement(descriptor, index, value) }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) =
        withElement(index) { delegate.encodeLongElement(descriptor, index, value) }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) =
        withElement(index) { delegate.encodeFloatElement(descriptor, index, value) }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) =
        withElement(index) { delegate.encodeDoubleElement(descriptor, index, value) }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) =
        withElement(index) { delegate.encodeCharElement(descriptor, index, value) }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        if (descriptor.kind == StructureKind.MAP && index % 2 == 0) {
            lastKeyString = value
        }
        withElement(index) { delegate.encodeStringElement(descriptor, index, value) }
    }

    override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        val child = childChain(index)
        return try {
            Serializer.withChain(delegate.encodeInlineElement(descriptor, index), child, track)
        } catch (e: Throwable) {
            throw track.trigger(child, e)
        }
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T,
    ) {
        val child = childChain(index)
        if (descriptor.kind == StructureKind.MAP && index % 2 == 0) {
            lastKeyString = value?.toString()
        }
        try {
            delegate.encodeSerializableElement(
                descriptor,
                index,
                TrackingSerializationStrategy(serializer, child, track),
                value,
            )
        } catch (e: Throwable) {
            throw track.trigger(child, e)
        }
    }

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?,
    ) {
        val child = childChain(index)
        if (descriptor.kind == StructureKind.MAP && index % 2 == 0) {
            lastKeyString = value?.toString()
        }
        try {
            delegate.encodeNullableSerializableElement(
                descriptor,
                index,
                TrackingSerializationStrategy(serializer, child, track),
                value,
            )
        } catch (e: Throwable) {
            throw track.trigger(child, e)
        }
    }

    private inline fun <R> withElement(index: Int, block: () -> R): R {
        val child = childChain(index)
        return try {
            block()
        } catch (e: Throwable) {
            throw track.trigger(child, e)
        }
    }
}

private class TrackingSerializationStrategy<T>(
    private val delegate: SerializationStrategy<T>,
    private val chain: Chain,
    private val track: Track,
) : SerializationStrategy<T> {
    override val descriptor: SerialDescriptor
        get() = delegate.descriptor

    override fun serialize(encoder: Encoder, value: T) {
        val trackedEncoder = Serializer.withChain(encoder, chain, track)
        try {
            delegate.serialize(trackedEncoder, value)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }
}

public class WrapSeq<S>(
    public val delegate: S,
    public val chain: Chain,
    public var index: Int,
    public val track: Track,
) {
    public companion object {
        public fun <S> new(delegate: S, chain: Chain, track: Track): WrapSeq<S> =
            WrapSeq(delegate, chain, 0, track)
    }
}

public class WrapMap<S>(
    public val delegate: S,
    public val chain: Chain,
    public var key: String?,
    public val track: Track,
) {
    public companion object {
        public fun <S> new(delegate: S, chain: Chain, track: Track): WrapMap<S> =
            WrapMap(delegate, chain, null, track)
    }
}
