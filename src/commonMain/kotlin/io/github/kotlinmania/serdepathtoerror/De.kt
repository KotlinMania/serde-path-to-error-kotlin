// port-lint: source de.rs

@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package io.github.kotlinmania.serdepathtoerror

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule

/**
 * Entry point for deserializing with path tracking.
 */
public fun <T> deserialize(deserializer: DeserializationStrategy<T>, decoder: Decoder): T {
    val track = Track.new()
    val trackedDecoder = Deserializer.new(decoder, track)
    return try {
        deserializer.deserialize(trackedDecoder)
    } catch (e: Error) {
        throw e
    } catch (e: Throwable) {
        val fromErr = Track.extractPathFromException(e)
        val finalPath =
            if (fromErr != null && fromErr.segments.isNotEmpty()) {
                fromErr
            } else {
                track.path()
            }
        throw Error(finalPath, e)
    }
}

/**
 * Deserializer adapter that records path to deserialization errors.
 */
public class Deserializer private constructor(
    private val de: Decoder,
    private val chain: Chain,
    private val track: Track,
) : Decoder {
    public constructor(de: Decoder, track: Track) : this(de, Chain.Root, track)

    override val serializersModule: SerializersModule
        get() = de.serializersModule

    override fun decodeNotNullMark(): Boolean =
        try {
            de.decodeNotNullMark()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun decodeNull(): Nothing? =
        try {
            de.decodeNull()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun decodeBoolean(): Boolean =
        try {
            de.decodeBoolean()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun decodeByte(): Byte =
        try {
            de.decodeByte()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun decodeShort(): Short =
        try {
            de.decodeShort()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun decodeInt(): Int =
        try {
            de.decodeInt()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun decodeLong(): Long =
        try {
            de.decodeLong()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun decodeFloat(): Float =
        try {
            de.decodeFloat()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun decodeDouble(): Double =
        try {
            de.decodeDouble()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun decodeChar(): Char =
        try {
            de.decodeChar()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun decodeString(): String =
        try {
            de.decodeString()
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int =
        try {
            de.decodeEnum(enumDescriptor)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder =
        try {
            Deserializer(de.decodeInline(descriptor), Chain.NewtypeStruct(chain), track)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T =
        try {
            deserializer.deserialize(this)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder =
        try {
            val composite = de.beginStructure(descriptor)
            TrackedCompositeDecoder(composite, descriptor, chain, track)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }

    public companion object {
        public fun new(de: Decoder, track: Track): Deserializer = Deserializer(de, track)

        internal fun withChain(de: Decoder, chain: Chain, track: Track): Deserializer =
            Deserializer(de, chain, track)
    }
}

public class TrackedCompositeDecoder(
    private val delegate: CompositeDecoder,
    private val descriptor: SerialDescriptor,
    private val chain: Chain,
    private val track: Track,
) : CompositeDecoder {
    private var lastKeyString: String? = null
    private var currentElementIndex = 0

    override val serializersModule: SerializersModule
        get() = delegate.serializersModule

    override fun endStructure(descriptor: SerialDescriptor) {
        try {
            delegate.endStructure(descriptor)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        try {
            val idx = delegate.decodeElementIndex(descriptor)
            currentElementIndex = idx
            idx
        } catch (e: Throwable) {
            val errChain = extractChainFromException(e) ?: chain
            throw track.trigger(errChain, e)
        }

    private fun extractChainFromException(e: Throwable): Chain? {
        val msg = e.message ?: return null
        val match =
            Regex(
                """(?:unknown key|unknown field|Encountered an unknown key)\s+['"`]([^'"`]+)['"`]""",
                RegexOption.IGNORE_CASE,
            ).find(msg)
        if (match != null) {
            val field = match.groupValues[1]
            return Chain.Struct(chain, field)
        }
        return null
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
            is PolymorphicKind -> {
                if (index in 0 until descriptor.elementsCount) {
                    Chain.Enum(chain, descriptor.getElementName(index))
                } else {
                    Chain.Seq(chain, index)
                }
            }
            else -> Chain.Seq(chain, index)
        }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        withElement(index) { delegate.decodeBooleanElement(descriptor, index) }

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        withElement(index) { delegate.decodeByteElement(descriptor, index) }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        withElement(index) { delegate.decodeShortElement(descriptor, index) }

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int =
        withElement(index) { delegate.decodeIntElement(descriptor, index) }

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long =
        withElement(index) { delegate.decodeLongElement(descriptor, index) }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        withElement(index) { delegate.decodeFloatElement(descriptor, index) }

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        withElement(index) { delegate.decodeDoubleElement(descriptor, index) }

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        withElement(index) { delegate.decodeCharElement(descriptor, index) }

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        val s = withElement(index) { delegate.decodeStringElement(descriptor, index) }
        if (descriptor.kind == StructureKind.MAP && index % 2 == 0) {
            lastKeyString = s
        }
        return s
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        val child = childChain(index)
        return try {
            Deserializer.withChain(delegate.decodeInlineElement(descriptor, index), child, track)
        } catch (e: Throwable) {
            throw track.trigger(child, e)
        }
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?,
    ): T {
        val child = childChain(index)
        return try {
            val result =
                delegate.decodeSerializableElement(
                    descriptor,
                    index,
                    TrackingDeserializationStrategy(deserializer, child, track),
                    previousValue,
                )
            if (descriptor.kind == StructureKind.MAP && index % 2 == 0) {
                lastKeyString = result?.toString()
            }
            result
        } catch (e: Throwable) {
            throw track.trigger(child, e)
        }
    }

    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?,
    ): T? {
        val child = childChain(index)
        return try {
            val result =
                delegate.decodeNullableSerializableElement(
                    descriptor,
                    index,
                    TrackingDeserializationStrategy(deserializer, child, track),
                    previousValue,
                )
            if (descriptor.kind == StructureKind.MAP && index % 2 == 0) {
                lastKeyString = result?.toString()
            }
            result
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

private class TrackingDeserializationStrategy<T>(
    private val delegate: DeserializationStrategy<T>,
    private val chain: Chain,
    private val track: Track,
) : DeserializationStrategy<T> {
    override val descriptor: SerialDescriptor
        get() = delegate.descriptor

    override fun deserialize(decoder: Decoder): T {
        val trackedDecoder = Deserializer.withChain(decoder, chain, track)
        return try {
            delegate.deserialize(trackedDecoder)
        } catch (e: Throwable) {
            throw track.trigger(chain, e)
        }
    }
}
