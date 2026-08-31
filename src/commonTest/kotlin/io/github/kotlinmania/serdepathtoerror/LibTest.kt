// port-lint: tests serde_path_to_error/src/lib.rs
package io.github.kotlinmania.serdepathtoerror

import kotlin.test.Test
import kotlin.test.assertEquals

class LibTest {
    @Test
    fun testVersion() {
        assertEquals("0.1.20", SerdePathToError.VERSION)
    }
}
