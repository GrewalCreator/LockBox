module com.lock {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires org.yaml.snakeyaml;

    opens com.lock to javafx.fxml, org.hibernate.orm.core;  // Opening com.lock for reflection-based access
    opens com.lock.model to org.hibernate.orm.core;  // Opening com.lock.model for Hibernate entity scanning
    exports com.lock;
}
