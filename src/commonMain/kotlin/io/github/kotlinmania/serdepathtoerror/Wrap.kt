// port-lint: source serde_path_to_error/src/wrap.rs

package io.github.kotlinmania.serdepathtoerror

/**
 * Wrapper that attaches context to a delegate.
 */
public class Wrap<X>(
    public val delegate: X,
    public val chain: Chain,
    public val track: Track,
) {
    public companion object {
        public fun <X> new(delegate: X, chain: Chain, track: Track): Wrap<X> =
            Wrap(delegate, chain, track)
    }
}

/**
 * Wrapper that attaches variant context to a delegate.
 */
public class WrapVariant<X>(
    public val delegate: X,
    public val chain: Chain,
    public val track: Track,
) {
    public companion object {
        public fun <X> new(delegate: X, chain: Chain, track: Track): WrapVariant<X> =
            WrapVariant(delegate, chain, track)
    }
}
