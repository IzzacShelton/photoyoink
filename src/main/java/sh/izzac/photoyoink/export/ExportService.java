package sh.izzac.photoyoink.export;

import com.drew.metadata.Metadata;
import sh.izzac.photoyoink.export.model.CameraExportModel;
import sh.izzac.photoyoink.export.model.PhotoExportModel;

import java.io.File;
import java.util.Optional;

public final class ExportService {
    private final MetadataExtractionService extractionService = new MetadataExtractionService();

    public ExportResult export(File file, Metadata metadata) {
        MetadataExtractionService.Extraction extraction = extractionService.extract(file, metadata);
        PhotoExportModel model = extraction.model();

        String photoTuple = TupleFormat.renderTuple(ExportRegistry.PHOTO, model);

        String cameraTuple = "";
        Optional<CameraExportModel> camera = model.camera();
        if (camera.isPresent()) {
            cameraTuple = TupleFormat.renderTuple(ExportRegistry.CAMERA, camera.get());
        }

        return new ExportResult(photoTuple, cameraTuple);
    }
}

