package sh.izzac.photoyoink.export.model;

import java.util.Optional;

public record CameraExportModel(
        Optional<String> brand,
        Optional<String> model,
        Optional<String> serialNumber
) {
    public CameraExportModel {
        brand = brand == null ? Optional.empty() : brand;
        model = model == null ? Optional.empty() : model;
        serialNumber = serialNumber == null ? Optional.empty() : serialNumber;
    }
}

