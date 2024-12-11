module CurlingGameMaven {
	requires javafx.controls;
	requires opencv;
	
	opens application to javafx.graphics, javafx.fxml;
}
