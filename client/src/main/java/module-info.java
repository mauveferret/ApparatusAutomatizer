module client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;

    opens ru.mauveferret to javafx.fxml;
    exports ru.mauveferret;
}