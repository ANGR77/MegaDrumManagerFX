package info.megadrum.managerfx.ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class UICheckBox extends UIControl{
	
	private CheckBox checkBox;
	private HBox layout;

	public UICheckBox(Boolean showCopyButton) {
		super(showCopyButton);
		init();
	}
	
	public UICheckBox(String labelText, Boolean showCopyButton) {
		super(labelText, showCopyButton);
		init();
	}
	
	private void init () {
		checkBox = new CheckBox();
		checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	//checkBox.setSelected(!newValue);
				//System.out.printf("%s: new value = %s, old value = %s\n",label.getText(),(newValue ? "true" : "false"),(oldValue ? "true" : "false") );
		    	booleanValue = newValue;
		    }
		});
		layout = new HBox();
		layout.setAlignment(Pos.CENTER_LEFT);
		layout.getChildren().addAll(checkBox);
		//layout.setStyle("-fx-background-color: red");
		//checkBox.setPadding(new Insets(50, 50, 50, 50));
		initControl(layout);		
	}
	
    @Override
    public void respondToResize(Double h, Double w) {
    	super.respondToResize(h, w);
    	// Standard checkBox is not resizeable
    	//checkBox.setMinHeight(h);
    	//checkBox.setMaxHeight(h);
    }

}