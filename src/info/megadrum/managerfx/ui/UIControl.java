package info.megadrum.managerfx.ui;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class UIControl extends Control implements UIControlInterface {
	
	@Override
	protected Skin<?> createDefaultSkin() {
		// TODO Auto-generated method stub
		return super.createDefaultSkin();
	}

	private GridPane layout;
	private Label label;
	private Pane rootPane;
	private int intValue = 0;
	private boolean booleanValue = false;

	public UIControl() {
		init("Unknown");
	}
	
	public UIControl(String labelText) {
		init(labelText);
	}
	
	private void init(String labelText) {
		
		rootPane = new Pane();
		rootPane.setMaxHeight(30);
		rootPane.setMaxWidth(240);
		
		layout = new GridPane();
		
		rootPane.getChildren().add(layout);
		layout.setPadding(new Insets(2, 2, 2, 2));
		layout.setHgap(5);
		
		label = new Label();
		label.setText(labelText);
		GridPane.setConstraints(label, 0, 0);
		GridPane.setHalignment(label, HPos.RIGHT);
		layout.getChildren().add(label);		
		layout.getColumnConstraints().add(new ColumnConstraints(120)); // column 0 is 120 wide
		layout.getColumnConstraints().add(new ColumnConstraints(120)); // column 1 is 120 wide
	}
	public Node getUI(){
		return (Node)layout;
	}
	
	public void initControl(Control uiControl) {
		GridPane.setConstraints(uiControl, 1, 0);
		GridPane.setHalignment(uiControl, HPos.LEFT);
		layout.getChildren().add(uiControl);
	}
	
	public void setTextLabel(String text) {
		label.setText(text);
	}
	@Override
	public void setSyncState(int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSyncOrNotSync(boolean synced) {
		// TODO Auto-generated method stub
		
	}

}
