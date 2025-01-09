module CurlingGameMaven {
	requires javafx.controls;
	requires opencv;
	requires java.desktop;
	requires javafx.fxml;

	opens ui to javafx.graphics, javafx.fxml;
	opens application to javafx.fxml, javafx.graphics;
}
