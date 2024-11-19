module lockbox {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    opens com.lock to javafx.fxml, org.hibernate.orm.core;  // Opening package for reflection-based access
    exports com.lock;
}
