// port-lint: source wrap.rs
package io.github.kotlinmania.serdepathtoerror

/**
 * Wrapper that attaches chain context to a delegate.
 */
internal class Wrap<X>(
    val delegate: X,
    val chain: Chain,
    val track: Track,
) {
    companion object {
        fun <X> new(delegate: X, chain: Chain, track: Track): Wrap<X> =
            Wrap(delegate, chain, track)
    }
}

/**
 * Wrapper that attaches variant context to a delegate.
 */
internal class WrapVariant<X>(
    val delegate: X,
    val chain: Chain,
    val track: Track,
) {
    companion object {
        fun <X> new(delegate: X, chain: Chain, track: Track): WrapVariant<X> =
            WrapVariant(delegate, chain, track)
    }
}
