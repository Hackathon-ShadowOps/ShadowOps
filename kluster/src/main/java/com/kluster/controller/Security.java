package com.kluster.controller;

public class Security {
    public static String sanitizeSQL(String input) {
        if (input == null) {
            return null;
        }

        // Default sanitization: strip comments/terminators then escape single quotes
        String cleaned = stripCommentsAndSemicolons(input);
        return escapeSingleQuotes(cleaned);

    }

    /**
     * Escape single quotes by doubling them (SQL literal escaping).
     */
    public static String escapeSingleQuotes(String input) {
        if (input == null)
            return null;
        return input.replace("'", "''");
    }

    /**
     * Escape characters that are special in SQL LIKE patterns: '%', '_' and the
     * escape char '\\'.
     * Also escapes single quotes so the result can be placed in a SQL string
     * literal.
     */
    public static String escapeForLike(String input) {
        if (input == null)
            return null;
        // escape backslash first
        String s = input.replace("\\", "\\\\");
        s = s.replace("%", "\\%");
        s = s.replace("_", "\\_");
        return escapeSingleQuotes(s);
    }

    /**
     * Remove block comments (/* ... * /), line comments (-- ...), and semicolons.
     * Also strips control characters to reduce injection vectors.
     */
    public static String stripCommentsAndSemicolons(String input) {
        if (input == null)
            return null;
        // Remove block comments (DOTALL)
        String s = input.replaceAll("(?s)/\\*.*?\\*/", "");
        // Remove line comments starting with -- to end of line
        s = s.replaceAll("(?m)--.*$", "");
        // Remove semicolons which can terminate statements
        s = s.replace(";", "");
        // Remove ASCII control characters (0x00-0x1F, 0x7F)
        s = s.replaceAll("[\\x00-\\x1F\\x7F]", "");
        return s;
    }

    /**
     * Strip any character not in the whitelist (alphanumeric, space, dash,
     * underscore, dot, @).
     * Use when input must be constrained to a safe subset (e.g. identifiers,
     * emails, simple tokens).
     */
    public static String whitelistAlphanumeric(String input) {
        if (input == null)
            return null;
        return input.replaceAll("[^A-Za-z0-9 \\-_@.]", "");
    }

    /**
     * Replace dangerous SQL keywords with a placeholder to reduce the risk of
     * injection via keywords.
     * This is a lossy transformation and should be used with caution (may break
     * legitimate content).
     */
    public static String removeSQLKeywords(String input) {
        if (input == null)
            return null;
        String pattern = "(?i)\\b(drop|delete|insert|update|alter|create|truncate|exec|execute|union|select|replace|merge)\\b";
        return input.replaceAll(pattern, "[REMOVED]");
    }

    /**
     * Combined sanitizer useful for storing free-text user input safely in SQL
     * literals.
     * It strips comments/semicolons then escapes quotes.
     */
    public static String sanitizeForStore(String input) {
        if (input == null)
            return null;
        return escapeSingleQuotes(stripCommentsAndSemicolons(input));
    }

    /**
     * Return false if the input contains any SQL metacharacters or keywords, true
     * otherwise.
     * This can be used for simple validation of inputs that should not contain SQL.
     */
    public static boolean isSafeForSQL(String input) {
        if (input == null)
            return true; // Null is considered safe

        String pattern = "(?i)[;'\"\\\\]|\\b(drop|delete|insert|update|alter|create|truncate|exec|execute|union|select|replace|merge)\\b";
        return !input.matches(".*" + pattern + ".*");
    }

    /**
     * Validate that an identifier (e.g. table name, column name) contains only
     * allowed characters (alphanumeric, underscore) and does not start with a
     * digit.
     * This can be used to validate dynamic identifiers before including them in
     * SQL.
     */
    public static boolean isValidIdentifier(String input) {
        if (input == null)
            return false;
        return input.matches("^[A-Za-z_][A-Za-z0-9_]*$");
    }
}
