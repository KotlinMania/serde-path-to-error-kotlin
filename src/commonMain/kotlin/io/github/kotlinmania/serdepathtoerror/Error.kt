package io.github.kotlinmania.serdepathtoerror

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeException
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.StdError

/**
 * Original deserializer error together with the path at which it occurred.
 */
public class Error<out E>(
    public val path: Path,
    public val original: E,
) : StdError {
    override val source: StdError?
        get() = (original as? StdError)?.source

    /**
     * Consumes this wrapper and returns the original error.
     */
    public fun intoInner(): E = original

    /**
     * Returns a reference to the original error.
     */
    public fun inner(): E = original

    public fun toSerdeError(): SerdeError =
        SerdeError(toString(), original as? SerdeError)

    override fun toString(): String =
        if (!path.isOnlyUnknown()) {
            "$path: $original"
        } else {
            original.toString()
        }

    public companion object {
        public fun <E> new(path: Path, inner: E): Error<E> = Error(path, inner)
    }
}

/**
 * Flat-class result type for path-tracked serialization and deserialization.
 *
 * Conforms to the KotlinMania Swift Export architecture:
 * flat-class shape (rather than sealed subclasses) with explicit invariant
 * enforcement in [init], no unsafe casts, and full Swift bridge compatibility.
 */
public class PathResult<T, E> internal constructor(
    public val value: T?,
    public val error: Error<E>?,
) {
    init {
        require((value == null) != (error == null)) {
            "PathResult must carry exactly one of value or error (got value=$value, error=$error)"
        }
    }

    public companion object {
        public fun <T, E> ok(value: T): PathResult<T, E> = PathResult(value, null)

        public fun <T, E> err(error: Error<E>): PathResult<T, E> = PathResult(null, error)
    }

    public fun getOrThrow(): T =
        when {
            value != null -> value
            error != null -> {
                val orig = error.original
                if (orig is Throwable) {
                    throw orig
                } else {
                    throw SerdeException(error.toSerdeError())
                }
            }
            else -> error("PathResult class invariant violated: both value and error are null")
        }

    public fun getOrNull(): T? = value

    public fun unwrapErr(): Error<E> =
        when {
            error != null -> error
            else -> error("called unwrapErr on successful PathResult with value: $value")
        }

    public fun isSuccess(): Boolean = value != null

    public fun isFailure(): Boolean = error != null

    public fun toSerdeResult(): SerdeResult<T> =
        when {
            value != null -> SerdeResult.success(value)
            error != null -> SerdeResult.failure(error.toSerdeError())
            else -> error("PathResult class invariant violated: both value and error are null")
        }

    override fun toString(): String =
        when {
            value != null -> "PathResult.Success($value)"
            error != null -> "PathResult.Failure($error)"
            else -> "PathResult.Invalid"
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathResult<*, *>) return false
        return value == other.value && error == other.error
    }

    override fun hashCode(): Int = (value?.hashCode() ?: 0) * 31 + (error?.hashCode() ?: 0)
}
