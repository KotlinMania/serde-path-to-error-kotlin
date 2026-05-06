# serde-path-to-error-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fserde--path--to--error--kotlin-blue.svg)](https://github.com/KotlinMania/serde-path-to-error-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/serde-path-to-error-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/serde-path-to-error-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/serde-path-to-error-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/serde-path-to-error-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`dtolnay/path-to-error`](https://github.com/dtolnay/path-to-error).

**Original Project:** This port is based on [`dtolnay/path-to-error`](https://github.com/dtolnay/path-to-error). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `dtolnay/path-to-error`

> The text below is reproduced and lightly edited from [`https://github.com/dtolnay/path-to-error`](https://github.com/dtolnay/path-to-error). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## Serde path to error

[<img alt="github" src="https://img.shields.io/badge/github-dtolnay/path--to--error-8da0cb?style=for-the-badge&labelColor=555555&logo=github" height="20">](https://github.com/dtolnay/path-to-error)
[<img alt="crates.io" src="https://img.shields.io/crates/v/serde_path_to_error.svg?style=for-the-badge&color=fc8d62&logo=rust" height="20">](https://crates.io/crates/serde_path_to_error)
[<img alt="docs.rs" src="https://img.shields.io/badge/docs.rs-serde__path__to__error-66c2a5?style=for-the-badge&labelColor=555555&logo=docs.rs" height="20">](https://docs.rs/serde_path_to_error)
[<img alt="build status" src="https://img.shields.io/github/actions/workflow/status/dtolnay/path-to-error/ci.yml?branch=master&style=for-the-badge" height="20">](https://github.com/dtolnay/path-to-error/actions?query=branch%3Amaster)

Find out the path at which a deserialization error occurred. This crate provides
a wrapper that works with any existing Serde `Deserializer` and exposes the
chain of field names leading to the error.

```toml
[dependencies]
serde = "1.0"
serde_path_to_error = "0.1"
```

```rust
use serde::Deserialize;
use std::collections::BTreeMap as Map;

#[derive(Deserialize)]
struct Package {
    name: String,
    dependencies: Map<String, Dependency>,
}

#[derive(Deserialize)]
struct Dependency {
    version: String,
}

fn main() {
    let j = r#"{
        "name": "demo",
        "dependencies": {
            "serde": {
                "version": 1
            }
        }
    }"#;

    // Some Deserializer.
    let jd = &mut serde_json::Deserializer::from_str(j);

    let result: Result<Package, _> = serde_path_to_error::deserialize(jd);
    match result {
        Ok(_) => panic!("expected a type error"),
        Err(err) => {
            let path = err.path().to_string();
            assert_eq!(path, "dependencies.serde.version");
        }
    }
}
```

<br>

#### License

<sup>
Licensed under either of <a href="LICENSE-APACHE">Apache License, Version
2.0</a> or <a href="LICENSE-MIT">MIT license</a> at your option.
</sup>

<br>

<sub>
Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in this crate by you, as defined in the Apache-2.0 license, shall
be dual licensed as above, without any additional terms or conditions.
</sub>

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:serde-path-to-error-kotlin:0.1.0-SNAPSHOT")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`dtolnay/path-to-error`](https://github.com/dtolnay/path-to-error). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the path-to-error authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`dtolnay/path-to-error`](https://github.com/dtolnay/path-to-error) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
