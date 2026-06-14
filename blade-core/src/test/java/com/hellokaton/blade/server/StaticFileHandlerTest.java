package com.hellokaton.blade.server;

import com.hellokaton.blade.Blade;
import com.hellokaton.blade.mvc.WebContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.time.DateTimeException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StaticFileHandler
 */
public class StaticFileHandlerTest {

    private static StaticFileHandler handler;

    @BeforeAll
    static void initBlade() {
        Blade blade = Blade.create();
        WebContext.init(blade, "/");
        handler = new StaticFileHandler(blade);
    }

    // ======================== format() date parsing ========================

    @Test
    void testFormat_validHttpDate() {
        Date date = handler.format("Mon, 01 Jan 2024 00:00:00 GMT",
                "EEE, dd MMM yyyy HH:mm:ss zzz");
        assertNotNull(date);
    }

    @Test
    void testFormat_differentPattern() {
        Date date = handler.format("2024-06-15 10:30:00",
                "yyyy-MM-dd HH:mm:ss");
        assertNotNull(date);
    }

    @Test
    void testFormat_simpleDate() {
        // The format() method uses LocalDateTime.parse which requires time components
        Date date = handler.format("2024-01-15 00:00:00",
                "yyyy-MM-dd HH:mm:ss");
        assertNotNull(date);
    }

    @Test
    void testFormat_invalidDate() {
        assertThrows(DateTimeException.class, () ->
                handler.format("not a date", "yyyy-MM-dd"));
    }

    @Test
    void testFormat_invalidPattern() {
        assertThrows(IllegalArgumentException.class, () ->
                handler.format("2024-01-01", "INVALID_PATTERN"));
    }

    // ======================== sanitizeUri (via reflection) ========================

    @Test
    void testSanitizeUri_validPath() throws Exception {
        Method sanitizeUri = StaticFileHandler.class.getDeclaredMethod("sanitizeUri", String.class);
        sanitizeUri.setAccessible(true);

        String result = (String) sanitizeUri.invoke(null, "/static/app.js");
        assertNotNull(result);
        assertTrue(result.contains("static"));
        assertTrue(result.contains("app.js"));
    }

    @Test
    void testSanitizeUri_emptyUri() throws Exception {
        Method sanitizeUri = StaticFileHandler.class.getDeclaredMethod("sanitizeUri", String.class);
        sanitizeUri.setAccessible(true);

        String result = (String) sanitizeUri.invoke(null, "");
        assertNull(result);
    }

    @Test
    void testSanitizeUri_noLeadingSlash() throws Exception {
        Method sanitizeUri = StaticFileHandler.class.getDeclaredMethod("sanitizeUri", String.class);
        sanitizeUri.setAccessible(true);

        String result = (String) sanitizeUri.invoke(null, "no-leading-slash");
        assertNull(result);
    }

    @Test
    void testSanitizeUri_pathTraversal() throws Exception {
        Method sanitizeUri = StaticFileHandler.class.getDeclaredMethod("sanitizeUri", String.class);
        sanitizeUri.setAccessible(true);

        // Path with /.. should be blocked
        String result = (String) sanitizeUri.invoke(null, "/../etc/passwd");
        assertNull(result);
    }

    @Test
    void testSanitizeUri_insecureChars() throws Exception {
        Method sanitizeUri = StaticFileHandler.class.getDeclaredMethod("sanitizeUri", String.class);
        sanitizeUri.setAccessible(true);

        assertNull(sanitizeUri.invoke(null, "/file<name"));
        assertNull(sanitizeUri.invoke(null, "/file>name"));
        assertNull(sanitizeUri.invoke(null, "/file&name"));
        assertNull(sanitizeUri.invoke(null, "/file\"name"));
    }

    @Test
    void testSanitizeUri_dotAtStart() throws Exception {
        Method sanitizeUri = StaticFileHandler.class.getDeclaredMethod("sanitizeUri", String.class);
        sanitizeUri.setAccessible(true);

        String result = (String) sanitizeUri.invoke(null, ".hidden");
        assertNull(result);
    }

    @Test
    void testSanitizeUri_dotAtEnd() throws Exception {
        Method sanitizeUri = StaticFileHandler.class.getDeclaredMethod("sanitizeUri", String.class);
        sanitizeUri.setAccessible(true);

        String result = (String) sanitizeUri.invoke(null, "/file.");
        assertNull(result);
    }

    // ======================== FileMeta ========================

    @Test
    void testFileMeta_buildByFile() throws IOException {
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();

        StaticFileHandler.FileMeta meta = StaticFileHandler.FileMeta.buildByFile(tempFile);
        assertNotNull(meta);
        assertEquals(tempFile.getName(), meta.name);
        assertFalse(meta.isDirectory);
        assertEquals(tempFile.length(), meta.length);
    }

    @Test
    void testFileMeta_buildByDirectory() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));

        StaticFileHandler.FileMeta meta = StaticFileHandler.FileMeta.buildByFile(tempDir);
        assertNotNull(meta);
        assertTrue(meta.isDirectory);
    }
}
