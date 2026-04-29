package sh.izzac.photoyoink.export;

import sh.izzac.photoyoink.export.model.CameraExportModel;
import sh.izzac.photoyoink.export.model.PhotoExportModel;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class ExportRegistry {
    private ExportRegistry() {}

    public static final TupleFormat.TableSpec<PhotoExportModel> PHOTO = new TupleFormat.TableSpec<>(
            "Photo",
            List.of(
                    new TupleFormat.ColumnSpec<>("CameraID", PhotoExportModel::cameraId),
                    new TupleFormat.ColumnSpec<>("Filepath", m -> Optional.of(m.filepath())),
                    new TupleFormat.ColumnSpec<>("Latitude", m -> m.latitude().map(bd -> bd.setScale(6, java.math.RoundingMode.HALF_UP))),
                    new TupleFormat.ColumnSpec<>("Longitude", m -> m.longitude().map(bd -> bd.setScale(6, java.math.RoundingMode.HALF_UP))),
                    new TupleFormat.ColumnSpec<>("ImageWidth", PhotoExportModel::imageWidth),
                    new TupleFormat.ColumnSpec<>("ImageHeight", PhotoExportModel::imageHeight)
            )
    );

    public static final TupleFormat.TableSpec<CameraExportModel> CAMERA = new TupleFormat.TableSpec<>(
            "Camera",
            List.of(
                    new TupleFormat.ColumnSpec<>("Brand", CameraExportModel::brand),
                    new TupleFormat.ColumnSpec<>("Model", CameraExportModel::model),
                    new TupleFormat.ColumnSpec<>("SerialNumber", CameraExportModel::serialNumber)
            )
    );

    public record ExportTarget(String label, Function<ExportResult, Optional<String>> tupleText) {}

    public static List<ExportTarget> exportTargets() {
        return List.of(
                new ExportTarget("Photo", r -> 
                        Optional.ofNullable(r.photoTuple())
                        .filter(s -> !s.isBlank())
                        .map(s -> s.replaceAll("[\r\n\t]", " "))),
                new ExportTarget("Camera", r -> 
                        Optional.ofNullable(r.cameraTuple())
                        .filter(s -> !s.isBlank())
                        .map(s -> s.replaceAll("[\r\n\t]", " ")))
        );
    }
}

