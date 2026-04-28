module sh.izzac.photoyoink {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.drew.metadata;
    requires atlantafx.base;
    
    opens sh.izzac.photoyoink to javafx.fxml;
    exports sh.izzac.photoyoink;
}
