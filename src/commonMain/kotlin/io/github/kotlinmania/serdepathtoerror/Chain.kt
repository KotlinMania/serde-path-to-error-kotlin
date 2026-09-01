// port-lint: source path.rs
package io.github.kotlinmania.serdepathtoerror

/**
 * Breadcrumb chain of elements traversed during serialization or deserialization.
 */
public sealed class Chain {
    public data object Root : Chain()

    public data class Seq(
        public val parent: Chain,
        public val index: Int,
    ) : Chain()

    public data class Map(
        public val parent: Chain,
        public val key: String,
    ) : Chain()

    public data class Struct(
        public val parent: Chain,
        public val key: String,
    ) : Chain()

    public data class Enum(
        public val parent: Chain,
        public val variant: String,
    ) : Chain()

    public data class Some(
        public val parent: Chain,
    ) : Chain()

    public data class NewtypeStruct(
        public val parent: Chain,
    ) : Chain()

    public data class NewtypeVariant(
        public val parent: Chain,
    ) : Chain()

    public data class NonStringKey(
        public val parent: Chain,
    ) : Chain()
}
