// port-lint: source path.rs
package io.github.kotlinmania.serdepathtoerror

/**
 * Path to the error value in the input, like `dependencies.serde.typo1`.
 *
 * Use `path.toString()` to get a string representation of the path with
 * segments separated by periods, or use `path.iter()` / iterate over
 * individual segments of the path.
 */
public data class Path(
    public val segments: List<Segment>,
) : Iterable<Segment> {
    /**
     * Returns an iterator over the segments of this path.
     */
    public fun iter(): Iterator<Segment> = segments.iterator()

    override fun iterator(): Iterator<Segment> = segments.iterator()

    override fun toString(): String {
        if (segments.isEmpty()) {
            return "."
        }

        val sb = StringBuilder()
        var separator = ""
        for (segment in segments) {
            if (segment !is Segment.Seq) {
                sb.append(separator)
            }
            sb.append(segment.toString())
            separator = "."
        }
        return sb.toString()
    }

    public fun isEmpty(): Boolean = segments.isEmpty()

    public fun isOnlyUnknown(): Boolean = segments.all { it is Segment.Unknown }

    public companion object {
        public fun empty(): Path = Path(emptyList())

        internal fun fromChain(initialChain: Chain): Path {
            var chain: Chain? = initialChain
            val segments = mutableListOf<Segment>()
            while (chain != null) {
                when (chain) {
                    is Chain.Root -> break
                    is Chain.Seq -> {
                        segments.add(Segment.Seq(chain.index))
                        chain = chain.parent
                    }
                    is Chain.Map -> {
                        segments.add(Segment.Map(chain.key))
                        chain = chain.parent
                    }
                    is Chain.Struct -> {
                        segments.add(Segment.Map(chain.key))
                        chain = chain.parent
                    }
                    is Chain.Enum -> {
                        segments.add(Segment.Enum(chain.variant))
                        chain = chain.parent
                    }
                    is Chain.Some -> {
                        chain = chain.parent
                    }
                    is Chain.NewtypeStruct -> {
                        chain = chain.parent
                    }
                    is Chain.NewtypeVariant -> {
                        chain = chain.parent
                    }
                    is Chain.NonStringKey -> {
                        segments.add(Segment.Unknown)
                        chain = chain.parent
                    }
                }
            }
            segments.reverse()
            return Path(segments)
        }
    }
}

/**
 * Single segment of a path.
 */
public sealed class Segment {
    public data class Seq(
        public val index: Int,
    ) : Segment() {
        override fun toString(): String = "[$index]"
    }

    public data class Map(
        public val key: String,
    ) : Segment() {
        override fun toString(): String = key
    }

    public data class Enum(
        public val variant: String,
    ) : Segment() {
        override fun toString(): String = variant
    }

    public data object Unknown : Segment() {
        override fun toString(): String = "?"
    }

    public fun isUnknown(): Boolean = this is Unknown
}
