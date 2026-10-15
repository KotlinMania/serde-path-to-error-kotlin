package io.github.kotlinmania.serdepathtoerror

/**
 * State for bookkeeping across nested deserializer calls.
 *
 * You don't need this if you are using [deserialize]. If you are managing your own
 * [Deserializer] or [Serializer], see the usage example in the crate documentation.
 */
public class Track {
    private var recordedPath: Path? = null

    /**
     * Empty state with no error having happened yet.
     */
    public constructor()

    /**
     * Gets path at which the error occurred. Only meaningful after we know
     * that an error has occurred. Returns an empty path otherwise.
     */
    public fun path(): Path = recordedPath ?: Path.empty()

    internal fun <E> trigger(chain: Chain, err: E): E {
        triggerImpl(chain)
        return err
    }

    private fun triggerImpl(chain: Chain) {
        if (recordedPath == null) {
            recordedPath = Path.fromChain(chain)
        }
    }

    public companion object {
        public fun new(): Track = Track()
    }
}
