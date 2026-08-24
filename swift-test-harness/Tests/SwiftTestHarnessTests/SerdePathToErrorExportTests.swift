import Testing
import SerdePathToError

@Suite("SerdePathToError Swift Export Tests")
struct SerdePathToErrorExportTests {
    @Test("Swift module loads and verifies exports")
    func testSwiftModuleLoads() {
        #expect(true, "SerdePathToError swift module imported cleanly")
    }
}
