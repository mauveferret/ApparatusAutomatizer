module client {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.jfoenix;

    opens ru.mauveferret.Controllers to javafx.fxml;
    exports ru.mauveferret;
}