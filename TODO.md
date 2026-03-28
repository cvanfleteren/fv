- normalizeLineEndings() — Convert all line endings to "\n" (LF). Helpful when ingesting Windows/Mac text.
    - "a\r\nb\rc" → "a\nb\nc"
- stripDiacritics() — Remove accents/combining marks while keeping base letters (NFD + remove `\p{M}` + NFC).
    - "Café naïve" → "Cafe naive"
- stripControlChars() — Remove non-printable/control characters (e.g., `\p{Cc}` + BOM + zero-width `\u200B-\u200D`, `\u2060`, `\uFEFF`).
    - "A\u200BBC" → "ABC"
- lettersOnly() / lettersAndSpacesOnly() — Keep only letters (and optionally spaces), Unicode-aware.
    - "H3llo, 世界!" → "Hllo世界" (lettersOnly)
- removePunctuation() — Strip all Unicode punctuation `\p{P}`.
    - "Hi—there!" → "Hithere"

### Case and capitalization
- capitalize() — Uppercase first character, keep rest.
    - "hello world" → "Hello world"
- uncapitalize() — Lowercase first character, keep rest.
    - "Hello" → "hello"
- titleCase(Locale) — Capitalize first letter of each word, lowercase others (beware locale rules, e.g., Turkish).
    - "tHe quICK bRoWn" → "The Quick Brown"
- sentenceCase(Locale) — Capitalize first letter after sentence boundaries, lowercase the rest.
    - "hello. how ARE you?" → "Hello. How are you?"

### Unicode normalization and width
- normalizeUnicodeNFC()/NFKC()/NFD()/NFKD() — Explicit Unicode normalization forms.
    - Useful for canonical storage and comparison.
- toHalfWidth() / toFullWidth() — Convert between full-width and half-width characters (common in CJK text).

### Separator and symbol normalization
- normalizeDashes() — Convert all dash-like characters to ASCII hyphen-minus '-'.
    - "– — ― -" → "- - - -"
- normalizeQuotes() — Convert curly/smart quotes to straight quotes.
    - "“quote” ‘x’" → '"quote" \''x'\''
- collapseRepeated(char sep) — Collapse multiple occurrences of a given separator to a single one (without trimming).
    - collapseRepeated('-'): "a---b--c" → "a-b-c"
- normalizeSeparatorsTo(char sep) — Replace any of `[\s,;:/|]+` with a single given separator.
    - sep=',' : "a | b; c\td" → "a,b,c,d"

### Slug and identifier shaping
- slugify() — Lowercase, strip diacritics, remove punctuation, collapse whitespace to '-', trim.
    - "Crème Brûlée — Recipe!" → "creme-brulee-recipe"
- kebabCase() / snakeCase() / camelCase() / pascalCase() — Convert between naming styles by tokenizing on non-alphanumerics and case boundaries.

### Escaping and encoding
- escapeRegex() — Escape a literal string for safe use inside a regex.
    - "a+b(c)" → "a\+b\(c\)"
- urlEncode()/urlDecode() — Percent-encode/decode using UTF-8.
- htmlEscape()/htmlUnescape() — Convert between plain text and HTML-escaped forms (e.g., `&`, `<`, `>`).
- xmlEscape() — Similar to HTML but xml-focused set.

### Structural utilities
- ensurePrefix(String prefix) / ensureSuffix(String suffix) — Add when missing.
    - ensureSuffix("/"): "path" → "path/"
- removePrefix(String prefix) / removeSuffix(String suffix) — Remove if present.
- padLeft(int len, char ch) / padRight(int len, char ch) — Pad to a minimum width.
- truncate(int maxLen) — Cut at max length.
- truncateWithEllipsis(int maxLen) — Cut and append "…" or "..." if over length; avoid breaking surrogate pairs.
- substringBefore(String token) / substringAfter(String token) / substringBetween(String start, String end) — Safe variants (no exceptions, return empty string if not found).

### Content allow/deny lists
- keepChars(String allowed) — Inverse of removeCharacters: build a char class of allowed characters and strip everything else (escaped properly).
- asciiPrintableOnly() — Keep `0x20-0x7E` only.

### Domain-focused presets (composed from primitives)
- normalizeEmail() — trim → NFC → strip control → lowercase domain only.
- normalizePhone() — digits() → optional leading '+', drop leading zeros by locale rules (configurable).
- normalizeWhitespaceForIndexing() — collapseWhitespace() → trim() → stripDiacritics() → lowercase().

### API shape suggestions (consistent with existing patterns)
- Keep everything as `MappingRule<String, String>` in `StringTransformations`.
- Continue using a shared `nullSafe(Function<String,String>)` helper to enforce `cannot.be.null`.
- For locale-sensitive methods, provide both default `Locale.ROOT` and `Locale`-taking overloads.
- For configurable behaviors (e.g., separators, truncateWithEllipsis), prefer small, explicit parameters over global settings.

### Implementation notes and hints
- Unicode normalization: `java.text.Normalizer.normalize(s, Form.NFC)`. To strip diacritics: NFD then `replaceAll("\\p{M}+", "")`, then NFC.
- Control/zero-width: remove `\p{Cc}` and specific format chars: `[\u200B-\u200D\u2060\uFEFF]`.
- URL encode/decode: `URLEncoder.encode(s, StandardCharsets.UTF_8)` / `URLDecoder.decode(s, StandardCharsets.UTF_8)`; ensure they’re pure mappings (throwing `IllegalArgumentException` on bad encodings is standard Java behavior; you may want to surface it as validation error key if you prefer).
- HTML/XML escaping: consider lightweight in-house implementation or a small optional dependency (e.g., Apache Commons Text) guarded behind a module to avoid pulling large trees.
- Case conversions across scripts can be tricky; keep `Locale` overloads, and default to `Locale.ROOT`.
- Truncation and substring ops should be surrogate-pair aware to avoid cutting inside a code point: operate on code points or use `Character.isHighSurrogate` / `isLowSurrogate` checks.

### Testing ideas
- Include null-input invalidation for every new rule, as you did for existing ones.
- Provide examples covering: different Unicode scripts, combining characters, surrogate pairs (emoji), locale edge cases (Turkish İ/i), and mixed line endings (CR, LF, CRLF).
- Property-like tests: roundtrips for escape/unescape pairs, idempotency checks (e.g., applying `slugify` twice yields the same result), and compositional invariants (e.g., `normalizeLineEndings` followed by `removeNewlines` equals `removeNewlines` alone).

If you’d like, I can draft signatures and skeleton implementations for the top 5 you choose (e.g., `stripDiacritics`, `normalizeLineEndings`, `slugify`, `escapeRegex`, `truncateWithEllipsis`) along with unit tests mirroring your current style.