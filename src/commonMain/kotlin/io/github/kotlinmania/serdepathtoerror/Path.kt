// port-lint: source path.rs

package io.github.kotlinmania.serdepathtoerror

/**
 * Path to the error value in the input, like `dependencies.serde.typo1`.
 *
 * Use `path.toString()` to get a string representation of the path with
 * segments separated by periods, or iterate over individual segments of the path.
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

    public fun isOnlyUnknown(): Boolean = segments.all { it.isUnknown() }

    public companion object {
        public fun empty(): Path = Path(emptyList())

        internal fun parseJsonPath(jsonPath: String): Path {
            val clean = jsonPath.removePrefix("$").removePrefix(".")
            if (clean.isEmpty()) return empty()

            val segments = mutableListOf<Segment>()
            val regex = Regex("""\[(?:'([^']+)'|"([^"]+)"|(\d+))\]|([^.\[\]]+)""")
            for (match in regex.findAll(clean)) {
                val singleQuoted = match.groups[1]?.value
                val doubleQuoted = match.groups[2]?.value
                val indexStr = match.groups[3]?.value
                val ident = match.groups[4]?.value

                if (singleQuoted != null) {
                    segments.add(Segment.Map(singleQuoted))
                } else if (doubleQuoted != null) {
                    segments.add(Segment.Map(doubleQuoted))
                } else if (indexStr != null) {
                    segments.add(Segment.Seq(indexStr.toInt()))
                } else if (ident != null) {
                    segments.add(Segment.Map(ident))
                }
            }
            return Path(segments.toList())
        }

        internal fun fromChain(initialChain: Chain): Path {
            val list = mutableListOf<Segment>()
            var chain: Chain? = initialChain
            while (chain != null) {
                when (chain) {
                    is Chain.Root -> break
                    is Chain.Seq -> {
                        list.add(Segment.Seq(chain.index))
                        chain = chain.parent
                    }
                    is Chain.Map -> {
                        list.add(Segment.Map(chain.key))
                        chain = chain.parent
                    }
                    is Chain.Struct -> {
                        list.add(Segment.Map(chain.key))
                        chain = chain.parent
                    }
                    is Chain.Enum -> {
                        list.add(Segment.Enum(chain.variant))
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
                        list.add(Segment.Unknown)
                        chain = chain.parent
                    }
                }
            }
            list.reverse()
            return Path(list.toList())
        }
    }
}

/**
 * Single segment of a path.
 */
public sealed class Segment {
    public data class Seq(public val index: Int) : Segment() {
        override fun toString(): String = "[$index]"
    }

    public data class Map(public val key: String) : Segment() {
        override fun toString(): String = key
    }

    public data class Enum(public val variant: String) : Segment() {
        override fun toString(): String = variant
    }

    public data object Unknown : Segment() {
        override fun toString(): String = "?"
    }

    public fun isUnknown(): Boolean = this is Unknown
}

public typealias Segments = Iterator<Segment>
