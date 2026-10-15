// port-lint: source path.rs
package io.github.kotlinmania.serdepathtoerror

/**
 * Breadcrumb chain of elements traversed during serialization or deserialization.
 */
internal sealed class Chain {
    data object Root : Chain()

    data class Seq(
        val parent: Chain,
        val index: Int,
    ) : Chain()

    data class Map(
        val parent: Chain,
        val key: String,
    ) : Chain()

    data class Struct(
        val parent: Chain,
        val key: String,
    ) : Chain()

    data class Enum(
        val parent: Chain,
        val variant: String,
    ) : Chain()

    data class Some(
        val parent: Chain,
    ) : Chain()

    data class NewtypeStruct(
        val parent: Chain,
    ) : Chain()

    data class NewtypeVariant(
        val parent: Chain,
    ) : Chain()

    data class NonStringKey(
        val parent: Chain,
    ) : Chain()
}
