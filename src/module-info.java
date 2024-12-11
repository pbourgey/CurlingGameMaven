module CurlingGameMaven {
	requires javafx.controls;
	requires opencv;
	requires java.desktop;
	
	opens application to javafx.graphics, javafx.fxml;
}
