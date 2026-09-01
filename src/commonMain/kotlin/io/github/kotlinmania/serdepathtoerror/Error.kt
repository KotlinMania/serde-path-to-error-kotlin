// port-lint: source lib.rs

package io.github.kotlinmania.serdepathtoerror

/**
 * Original deserializer error together with the path at which it occurred.
 */
public class Error(
    public val path: Path,
    public val original: Throwable,
) : Exception(formatMessage(path, original), original) {
    /**
     * The deserializer's underlying error that occurred.
     */
    public fun intoInner(): Throwable = original

    /**
     * Reference to the deserializer's underlying error that occurred.
     */
    public fun inner(): Throwable = original

    override fun toString(): String =
        if (!path.isOnlyUnknown()) {
            "$path: $original"
        } else {
            original.toString()
        }

    public companion object {
        public fun new(path: Path, inner: Throwable): Error = Error(path, inner)

        private fun formatMessage(path: Path, original: Throwable): String {
            val msg = original.message ?: original.toString()
            return if (!path.isOnlyUnknown()) {
                "$path: $msg"
            } else {
                msg
            }
        }
    }
}
