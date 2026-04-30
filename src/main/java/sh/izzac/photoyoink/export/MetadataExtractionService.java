package sh.izzac.photoyoink.export;

import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import sh.izzac.photoyoink.export.model.CameraExportModel;
import sh.izzac.photoyoink.export.model.PhotoExportModel;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class MetadataExtractionService {
    private static final DateTimeFormatter EXIF_DATETIME = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss");

    public record Extraction(PhotoExportModel model) {}

    public Extraction extract(File file, Metadata metadata) {
        String filepath = file.getAbsolutePath();
        BigInteger fileSize = BigInteger.valueOf(file.length());

        Optional<BigDecimal> lat = Optional.empty();
        Optional<BigDecimal> lon = Optional.empty();
        GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
        if (gps != null) {
            GeoLocation loc = gps.getGeoLocation();
            if (loc != null && !loc.isZero()) {
                lat = Optional.of(BigDecimal.valueOf(loc.getLatitude()));
                lon = Optional.of(BigDecimal.valueOf(loc.getLongitude()));
            }
        }

        Optional<Integer> width = Optional.empty();
        Optional<Integer> height = Optional.empty();

        ExifSubIFDDirectory exifSub = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (exifSub != null) {
            Integer w = exifSub.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
            Integer h = exifSub.getInteger(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
            width = Optional.ofNullable(w);
            height = Optional.ofNullable(h);
        }

        if (width.isEmpty() || height.isEmpty()) {
            JpegDirectory jpeg = metadata.getFirstDirectoryOfType(JpegDirectory.class);
            if (jpeg != null) {
                width = width.isPresent() ? width : Optional.ofNullable(jpeg.getInteger(JpegDirectory.TAG_IMAGE_WIDTH));
                height = height.isPresent() ? height : Optional.ofNullable(jpeg.getInteger(JpegDirectory.TAG_IMAGE_HEIGHT));
            }
        }

        if (width.isEmpty() || height.isEmpty()) {
            PngDirectory png = metadata.getFirstDirectoryOfType(PngDirectory.class);
            if (png != null) {
                width = width.isPresent() ? width : Optional.ofNullable(png.getInteger(PngDirectory.TAG_IMAGE_WIDTH));
                height = height.isPresent() ? height : Optional.ofNullable(png.getInteger(PngDirectory.TAG_IMAGE_HEIGHT));
            }
        }

        Optional<LocalDateTime> dateTimeTaken = Optional.empty();
        if (exifSub != null) {
            String s = exifSub.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (s != null && !s.isBlank()) {
                try { dateTimeTaken = Optional.of(LocalDateTime.parse(s.trim(), EXIF_DATETIME)); } catch (Exception ignored) {}
            }
        }
        if (dateTimeTaken.isEmpty()) {
            ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (ifd0 != null) {
                String s = ifd0.getString(ExifIFD0Directory.TAG_DATETIME);
                if (s != null && !s.isBlank()) {
                    try { dateTimeTaken = Optional.of(LocalDateTime.parse(s.trim(), EXIF_DATETIME)); } catch (Exception ignored) {}
                }
            }
        }
        if (dateTimeTaken.isEmpty()) {
            PngDirectory png = metadata.getFirstDirectoryOfType(PngDirectory.class);
            if (png != null) {
                String s = png.getString(PngDirectory.TAG_LAST_MODIFICATION_TIME);
                if (s != null && !s.isBlank()) {
                    try { dateTimeTaken = Optional.of(LocalDateTime.parse(s.trim(), EXIF_DATETIME)); } catch (Exception ignored) {}
                }
            }
        }

        Optional<CameraExportModel> camera = extractCamera(metadata);

        PhotoExportModel model = new PhotoExportModel(
                Optional.empty(), // CameraID null for now
                filepath,
                fileSize,
                lat,
                lon,
                width,
                height,
                dateTimeTaken,
                camera
        );
        return new Extraction(model);
    }

    private Optional<CameraExportModel> extractCamera(Metadata metadata) {
        Optional<String> make = Optional.empty();
        Optional<String> model = Optional.empty();
        Optional<String> serial = Optional.empty();

        ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (ifd0 != null) {
            make = Optional.ofNullable(ifd0.getString(ExifIFD0Directory.TAG_MAKE)).map(String::trim).filter(s -> !s.isBlank());
            model = Optional.ofNullable(ifd0.getString(ExifIFD0Directory.TAG_MODEL)).map(String::trim).filter(s -> !s.isBlank());
        }

        ExifSubIFDDirectory sub = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (sub != null) {
            serial = Optional.ofNullable(sub.getString(ExifSubIFDDirectory.TAG_BODY_SERIAL_NUMBER)).map(String::trim).filter(s -> !s.isBlank());
        }

        // Camera requires Brand + Model,only export if both are present
        if (make.isEmpty() || model.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new CameraExportModel(make, model, serial));
    }
}
