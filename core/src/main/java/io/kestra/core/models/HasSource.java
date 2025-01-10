package io.kestra.core.models;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Interface that can be implemented by Kestra's resource attached to an original source code.
 */
public interface HasSource {

    /**
     * Gets the source of this Kestra's resource.
     * <p>
     * This method should return a valid and parseable in YAML object.
     *
     * @return  the string source.
     */
    String source();

    /**
     * Static helper method for constructing a ZIP file containing the given sources.
     *
     * @param sources      the sources to zip.
     * @param zipEntryName the function used for constructing the ZIP entry name.
     * @param <T>          type of the source.
     * @return a byte array representation of the ZIP file.
     * @throws IOException if an error happen while zipping sources.
     */
    static <T extends HasSource> byte[] asZipFile(final List<? extends T> sources,
                                                  final Function<T, String> zipEntryName) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ZipOutputStream archive = new ZipOutputStream(bos)) {

            for (var source : sources) {
                var zipEntry = new ZipEntry(zipEntryName.apply(source));
                archive.putNextEntry(zipEntry);
                archive.write(source.source().getBytes());
                archive.closeEntry();
            }
            archive.finish();
            return bos.toByteArray();
        }
    }
}
