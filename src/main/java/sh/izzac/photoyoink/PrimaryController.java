package sh.izzac.photoyoink;

import java.io.IOException;
import javafx.fxml.FXML;
import com.drew.metadata.Metadata;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import java.io.File;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import atlantafx.base.theme.CupertinoDark;
import javafx.application.Application;

public class PrimaryController {
    private record MetaRow(String name, String value) {};
    
    @FXML private TreeTableView<MetaRow> metadataTree;
    @FXML private TreeTableColumn<MetaRow, String> fieldColumn;
    @FXML private TreeTableColumn<MetaRow, String> valueColumn;
    @FXML private ContextMenu treeContextMenu;
    @FXML private MenuItem expandItem, collapseItem;
    @FXML private ImageView imageView;
    
    @FXML 
    public void initialize() {
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
        // initialize metadata view table stuff
        fieldColumn.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getValue().name()));
        valueColumn.setCellValueFactory(p ->
            new SimpleStringProperty(p.getValue().getValue().value()));
        metadataTree.setShowRoot(false);
        
        // handler for context menu
        EventHandler<ActionEvent> toggleAction = event -> {
            boolean expand = event.getSource() == expandItem;
            setExpanded(metadataTree.getRoot(), expand);
        };

        expandItem.setOnAction(toggleAction);
        collapseItem.setOnAction(toggleAction);

        metadataTree.setContextMenu(treeContextMenu);
    }
    
    private void setExpanded(TreeItem<MetaRow> item, boolean expand) {
        if (item == null) 
            return;
        if (item != metadataTree.getRoot())
            item.setExpanded(expand);
        for (TreeItem<MetaRow> child : item.getChildren())
            setExpanded(child, expand);
    }

    private void populateMetadata(Metadata metadata) {
        TreeItem<MetaRow> root = new TreeItem<>(null);
        for (Directory d : metadata.getDirectories()) {
            TreeItem<MetaRow> dirItem = new TreeItem<>(new MetaRow(d.getName(), ""));
            dirItem.setExpanded(false);
            for (Tag t : d.getTags()) {
                String desc = t.getDescription();
                dirItem.getChildren().add(
                    new TreeItem<>(new MetaRow(t.getTagName(), desc != null ? desc : ""))
                );
            }
            root.getChildren().add(dirItem);
        }
        metadataTree.setRoot(root);
    }
    
    
    @FXML
    private void pickPhoto() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*."
                    + "png", "*.gif", "*.bmp", "*.tiff")
        );
        
        Stage stage = (Stage) imageView.getScene().getWindow();
        File f = fileChooser.showOpenDialog(stage);

        if (f != null) {
            // 
            Image img = new Image(f.toURI().toString());
            imageView.setImage(img);
            try {
                Metadata metadata = ImageMetadataReader.readMetadata(f);
                
                // populate the TreeTableView
                populateMetadata(metadata);
                
                for (Directory d : metadata.getDirectories()) {
                    // directories are like categories of metadata from the image
                    System.out.printf("%s:\n", d.getName());
                    for (Tag t : d.getTags()) {
                        System.out.printf("-> %s (%s): %s\n", t.getTagName(), t.getTagTypeHex(), d.getObject(t.getTagType()));
                    }
                }
            } catch (ImageProcessingException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }
}
