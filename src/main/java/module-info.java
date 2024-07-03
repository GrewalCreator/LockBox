module lockbox {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.lock to javafx.fxml;

    exports com.lock;
}

