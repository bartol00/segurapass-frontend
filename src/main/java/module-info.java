module com.example.passwordmanagerclient {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    requires static lombok;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires javafx.graphics;
    requires org.bouncycastle.provider;
    requires org.json;
    requires java.desktop;
    requires segurapass.sdk;

    opens com.example.passwordmanagerclient to javafx.fxml;
    opens com.example.passwordmanagerclient.controller.authorization to javafx.fxml;
    opens com.example.passwordmanagerclient.controller.credentials to javafx.fxml;
    opens com.example.passwordmanagerclient.controller.deletion to javafx.fxml;
    opens com.example.passwordmanagerclient.controller.versions to javafx.fxml;

    exports com.example.passwordmanagerclient;
    exports com.example.passwordmanagerclient.controller.authorization;
    exports com.example.passwordmanagerclient.controller.deletion;
    exports com.example.passwordmanagerclient.controller.credentials;
    exports com.example.passwordmanagerclient.controller.versions;
}
