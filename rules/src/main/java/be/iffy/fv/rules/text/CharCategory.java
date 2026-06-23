package be.iffy.fv.rules.text;

/**
 * Character categories for use with {@link StringOps#keep(CharCategory...)} and {@link StringOps#strip(CharCategory...)}.
 * <p>
 * By convention, unprefixed names are Unicode-aware; {@code ASCII_} prefixed names are restricted to ASCII.
 */
public enum CharCategory {

    /** ASCII digits 0–9 only. Unicode digits (e.g. Arabic-Indic) are excluded. */
    ASCII_DIGITS("0-9"),

    /** ASCII letters A–Z and a–z only. Accented or non-Latin letters are excluded. */
    ASCII_LETTERS("A-Za-z"),

    /** ASCII whitespace: space, tab, {@code \n}, {@code \r}, form feed, vertical tab ({@code \s}). */
    ASCII_WHITESPACE("\\s"),

    /** Decimal digit characters from any Unicode script (Unicode category {@code Nd}, e.g. Arabic-Indic digits). */
    DIGITS("\\p{Nd}"),

    /** Letter characters from any Unicode script (Unicode category {@code L}). */
    LETTERS("\\p{L}"),

    /** Combining marks / diacritics (Unicode category {@code M}).
     *  Useful with {@code strip(MARKS)} after NFD normalization to remove accents composably. */
    MARKS("\\p{M}"),

    /** ASCII punctuation: {@code !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~} (POSIX {@code Punct} class). */
    ASCII_PUNCTUATION("\\p{Punct}"),

    /** Punctuation from any Unicode script (Unicode category {@code P}).
     *  Superset of {@link #ASCII_PUNCTUATION} — includes e.g. guillemets «», ellipsis …, etc. */
    PUNCTUATION("\\p{P}"),

    /** Literal space character U+0020 only. */
    SPACE(" "),

    /** All Unicode whitespace characters (Unicode {@code White_Space} property). Superset of {@link #ASCII_WHITESPACE}. */
    WHITESPACE("\\p{IsWhite_Space}");

    final String fragment;

    CharCategory(String fragment) {
        this.fragment = fragment;
    }
}
