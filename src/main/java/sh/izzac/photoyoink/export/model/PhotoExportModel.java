package sh.izzac.photoyoink.export.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

public record PhotoExportModel(
        Optional<Integer> cameraId,
        String filepath,
        BigInteger fileSize,
        Optional<BigDecimal> latitude,
        Optional<BigDecimal> longitude,
        Optional<Integer> imageWidth,
        Optional<Integer> imageHeight,
        Optional<LocalDateTime> dateTimeTaken,
        Optional<CameraExportModel> camera
) {
    public PhotoExportModel {
        if (filepath == null || filepath.isBlank()) {
            throw new IllegalArgumentException("filepath must be non-empty");
        }
        cameraId = cameraId == null ? Optional.empty() : cameraId;
        latitude = latitude == null ? Optional.empty() : latitude;
        longitude = longitude == null ? Optional.empty() : longitude;
        imageWidth = imageWidth == null ? Optional.empty() : imageWidth;
        imageHeight = imageHeight == null ? Optional.empty() : imageHeight;
        dateTimeTaken = dateTimeTaken == null ? Optional.empty() : dateTimeTaken;
        camera = camera == null ? Optional.empty() : camera;
    }
}
