// port-lint: source lib.rs

package io.github.kotlinmania.serdepathtoerror

/**
 * State for bookkeeping across nested deserializer/serializer calls.
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

    public fun <E : Throwable> trigger(chain: Chain, err: E): E {
        triggerImpl(chain, err)
        return err
    }

    private fun triggerImpl(chain: Chain, err: Throwable?) {
        if (recordedPath == null) {
            val fromErr = err?.let { extractPathFromException(it) }
            recordedPath =
                if (fromErr != null && fromErr.segments.isNotEmpty()) {
                    fromErr
                } else {
                    Path.fromChain(chain)
                }
        }
    }

    public companion object {
        public fun new(): Track = Track()

        internal fun extractPathFromException(err: Throwable): Path? {
            val msg = err.message ?: return null
            val match = Regex("""at path:\s*(\$[^,\r\n]*)""", RegexOption.IGNORE_CASE).find(msg)
            if (match != null) {
                val pathStr = match.groupValues[1].trim()
                val parsed = Path.parseJsonPath(pathStr)
                val unknownKeyMatch =
                    Regex(
                        """(?:unknown key|unknown field|Encountered an unknown key)\s+['"`]([^'"`]+)['"`]""",
                        RegexOption.IGNORE_CASE,
                    ).find(msg)
                return if (unknownKeyMatch != null) {
                    val field = unknownKeyMatch.groupValues[1]
                    Path(parsed.segments + Segment.Map(field))
                } else {
                    parsed
                }
            }
            val unknownKeyMatch =
                Regex(
                    """(?:unknown key|unknown field|Encountered an unknown key)\s+['"`]([^'"`]+)['"`]""",
                    RegexOption.IGNORE_CASE,
                ).find(msg)
            if (unknownKeyMatch != null) {
                val field = unknownKeyMatch.groupValues[1]
                return Path(listOf(Segment.Map(field)))
            }
            return null
        }
    }
}
