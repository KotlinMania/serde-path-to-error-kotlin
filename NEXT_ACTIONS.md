# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/7 (71.4%)
- **Function parity:** 16/148 matched (target 98) — 10.8%
- **Class/type parity:** 11/37 matched (target 28) — 29.7%
- **Combined symbol parity:** 27/185 matched (target 126) — 14.6%
- **Average inline-code cosine:** 0.33 (function body across 4 matched files)
- **Average documentation cosine:** 0.39 (doc text across 4 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 4 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. serde_path_to_error.wrap

- **Target:** `serdepathtoerror.Wrap`
- **Similarity:** 0.96
- **Dependents:** 1
- **Priority Score:** 1000300.4
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 2. serde_path_to_error.de

- **Target:** `serdepathtoerror.De`
- **Similarity:** 0.02
- **Dependents:** 0
- **Priority Score:** 767909.9
- **Functions:** 2/71 matched (target 36)
- **Missing functions:** `deserialize_any`, `deserialize_bool`, `deserialize_u8`, `deserialize_u16`, `deserialize_u32`, `deserialize_u64`, `deserialize_u128`, `deserialize_i8`, `deserialize_i16`, `deserialize_i32`, `deserialize_i64`, `deserialize_i128`, `deserialize_f32`, `deserialize_f64`, `deserialize_char`, `deserialize_str`, `deserialize_string`, `deserialize_bytes`, `deserialize_byte_buf`, `deserialize_option`, `deserialize_unit`, `deserialize_unit_struct`, `deserialize_newtype_struct`, `deserialize_seq`, `deserialize_tuple`, `deserialize_tuple_struct`, `deserialize_map`, `deserialize_struct`, `deserialize_enum`, `deserialize_ignored_any`, `deserialize_identifier`, `is_human_readable`, `expecting`, `visit_bool`, `visit_i8`, `visit_i16`, `visit_i32`, `visit_i64`, `visit_i128`, `visit_u8`, `visit_u16`, `visit_u32`, `visit_u64`, `visit_u128`, `visit_f32`, `visit_f64`, `visit_char`, `visit_str`, `visit_borrowed_str`, `visit_string`, `visit_unit`, `visit_none`, `visit_some`, `visit_newtype_struct`, `visit_seq`, `visit_map`, `visit_enum`, `visit_bytes`, `visit_borrowed_bytes`, `visit_byte_buf`, `variant_seed`, `unit_variant`, `newtype_variant_seed`, `tuple_variant`, `struct_variant`, `next_element_seed`, `size_hint`, `next_key_seed`, `next_value_seed`
- **Types:** 1/8 matched (target 3)
- **Missing types:** `Error`, `Value`, `Variant`, `CaptureKey`, `TrackedSeed`, `SeqAccess`, `MapAccess`

### 3. serde_path_to_error.ser

- **Target:** `serdepathtoerror.Ser`
- **Similarity:** 0.04
- **Dependents:** 0
- **Priority Score:** 515609.6
- **Functions:** 2/42 matched (target 36)
- **Missing functions:** `serialize_bool`, `serialize_i8`, `serialize_i16`, `serialize_i32`, `serialize_i64`, `serialize_i128`, `serialize_u8`, `serialize_u16`, `serialize_u32`, `serialize_u64`, `serialize_u128`, `serialize_f32`, `serialize_f64`, `serialize_char`, `serialize_str`, `serialize_bytes`, `serialize_none`, `serialize_some`, `serialize_unit`, `serialize_unit_struct`, `serialize_unit_variant`, `serialize_newtype_struct`, `serialize_newtype_variant`, `serialize_seq`, `serialize_tuple`, `serialize_tuple_struct`, `serialize_tuple_variant`, `serialize_map`, `serialize_struct`, `serialize_struct_variant`, `collect_str`, `is_human_readable`, `serialize_element`, `end`, `serialize_field`, `serialize_key`, `serialize_value`, `skip_field`, `collect_seq`, `collect_map`
- **Types:** 3/14 matched (target 5)
- **Missing types:** `Ok`, `Error`, `SerializeSeq`, `SerializeTuple`, `SerializeTupleStruct`, `SerializeTupleVariant`, `SerializeMap`, `SerializeStruct`, `SerializeStructVariant`, `TrackedValue`, `CaptureKey`

### 4. serde_path_to_error.path

- **Target:** `serdepathtoerror.Path`
- **Similarity:** 0.31
- **Dependents:** 0
- **Priority Score:** 81606.9
- **Functions:** 5/11 matched (target 13)
- **Missing functions:** `into_iter`, `next`, `size_hint`, `next_back`, `len`, `fmt`
- **Types:** 3/5 matched (target 14)
- **Missing types:** `Item`, `IntoIter`

### 5. serde_path_to_error.lib

- **Target:** `serdepathtoerror.Lib [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 31110.0
- **Functions:** 6/8 matched (target 11)
- **Missing functions:** `fmt`, `source`
- **Types:** 2/3 matched (target 4)
- **Missing types:** `Chain`

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

