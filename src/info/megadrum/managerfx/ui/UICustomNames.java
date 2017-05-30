package info.megadrum.managerfx.ui;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.event.EventListenerList;

import info.megadrum.managerfx.utils.Constants;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class UICustomNames {
	private ScrollPane	scrollPane;
	private VBox		vBox;
	private ToolBar		toolBarTop;
	private GridPane	gridPane;
	private Button 		buttonGetAll;
	private Button 		buttonSendAll;
	private Button 		buttonLoadAll;
	private Button 		buttonSaveAll;
	
	private Label		labelCustomNamesCount;
	private ComboBox<String>	comboBoxCustomNamesCount;
	private ArrayList<Label> allLabels;
	private ArrayList<TextField> allTextFields;
	private ArrayList<Button> allGetButtons;
	private ArrayList<Button> allSendButtons;
	private int []		allSyncStates;

	protected EventListenerList listenerList = new EventListenerList();
	
	public void addControlChangeEventListener(ControlChangeEventListener listener) {
		listenerList.add(ControlChangeEventListener.class, listener);
	}
	public void removeControlChangeEventListener(ControlChangeEventListener listener) {
		listenerList.remove(ControlChangeEventListener.class, listener);
	}
	protected void fireControlChangeEvent(ControlChangeEvent evt, Integer parameter) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i = i+2) {
			if (listeners[i] == ControlChangeEventListener.class) {
				((ControlChangeEventListener) listeners[i+1]).controlChangeEventOccurred(evt, parameter);
			}
		}
	}

	public UICustomNames() {
		labelCustomNamesCount = new Label("Custom Names:");
		comboBoxCustomNamesCount = new ComboBox<String>();
		comboBoxCustomNamesCount.getItems().clear();
		comboBoxCustomNamesCount.getItems().addAll(Arrays.asList("2","16","32"));
		comboBoxCustomNamesCount.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				Integer start = 2;
				switch (comboBoxCustomNamesCount.getSelectionModel().getSelectedIndex()) {
				case 0:
					start = 2;
					break;
				case 1:
					start = 16;
					break;
				case 3:
				default:
					start = 32;
						break;
				}
				Boolean visible;
				for (int i = 0; i < Constants.CUSTOM_NAMES_MAX;i++) {
					if (i < start.intValue()) {
						visible = true;
					} else {
						visible = false;
					}
					allLabels.get(i).setVisible(visible);
					allTextFields.get(i).setVisible(visible);
					allGetButtons.get(i).setVisible(visible);
					allSendButtons.get(i).setVisible(visible);
				}
			}
        });
		toolBarTop = new ToolBar();
		buttonGetAll = new Button("GetAll");
		buttonSendAll = new Button("SendAll");
		buttonLoadAll = new Button("LoadAll");
		buttonSaveAll = new Button("SaveAll");
		toolBarTop.getItems().addAll(labelCustomNamesCount, comboBoxCustomNamesCount, buttonGetAll,buttonSendAll, new Separator(),buttonLoadAll,buttonSaveAll);

		vBox = new VBox(1);
		vBox.setStyle("-fx-padding: 0.0em 0.0em 0.2em 0.0em");
		vBox.getChildren().addAll(toolBarTop);
		allLabels = new ArrayList<Label>();
		allTextFields = new ArrayList<TextField>();
		allGetButtons = new ArrayList<Button>();
		allSendButtons = new ArrayList<Button>();
		allSyncStates = new int[Constants.CUSTOM_NAMES_MAX];
		gridPane = new GridPane();
		gridPane.getColumnConstraints().add(new ColumnConstraints(4));
		gridPane.getColumnConstraints().add(new ColumnConstraints(16));
		gridPane.getColumnConstraints().add(new ColumnConstraints(110));
		gridPane.getColumnConstraints().add(new ColumnConstraints(1));
		gridPane.getColumnConstraints().add(new ColumnConstraints(30));
		gridPane.getColumnConstraints().add(new ColumnConstraints(1));
		gridPane.getColumnConstraints().add(new ColumnConstraints(30));
		
		for (int i = 0; i < Constants.CUSTOM_NAMES_MAX; i++) {
			final Integer iFinal = i;
			allLabels.add(new Label(Integer.toString(i + 1) + ":"));
			GridPane.setConstraints(allLabels.get(i), 1, i);
			GridPane.setHalignment(allLabels.get(i), HPos.CENTER);
			GridPane.setValignment(allLabels.get(i), VPos.CENTER);
			gridPane.getChildren().add(allLabels.get(i));

			allTextFields.add(new TextField());
			GridPane.setConstraints(allTextFields.get(i), 2, i);
			GridPane.setHalignment(allTextFields.get(i), HPos.CENTER);
			GridPane.setValignment(allTextFields.get(i), VPos.CENTER);
			gridPane.getChildren().add(allTextFields.get(i));
			allTextFields.get(i).setMinWidth(100);
			allTextFields.get(i).setMaxWidth(100);
			allTextFields.get(i).textProperty().addListener(new ChangeListener<String>() {
		        @Override
		        public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
		            if (allTextFields.get(iFinal).getText().length() > 8) {
						int pos = allTextFields.get(iFinal).getCaretPosition();
						System.out.printf("Pos = %d\n", pos);
						
//		                String s = allTextFields.get(iFinal).getText().substring(0, 8);
//		                allTextFields.get(iFinal).setText(s);
						String text = allTextFields.get(iFinal).getText();
						text = text.trim();
						text += "        ";
						text = text.substring(0, 8);
						text = text.trim();
						allTextFields.get(iFinal).setText(text);
						Platform.runLater( new Runnable() {
						    @Override
						    public void run() {
								allTextFields.get(iFinal).positionCaret(pos + 1);
						    }
						});

		            }
		        }
		    });

			allGetButtons.add(new Button("Get"));
			GridPane.setConstraints(allGetButtons.get(i), 4, i);
			GridPane.setHalignment(allGetButtons.get(i), HPos.CENTER);
			GridPane.setValignment(allGetButtons.get(i), VPos.CENTER);
			gridPane.getChildren().add(allGetButtons.get(i));

			allSendButtons.add(new Button("Send"));
			GridPane.setConstraints(allSendButtons.get(i), 6, i);
			GridPane.setHalignment(allSendButtons.get(i), HPos.CENTER);
			GridPane.setValignment(allSendButtons.get(i), VPos.CENTER);
			gridPane.getChildren().add(allSendButtons.get(i));

			setSyncState(Constants.SYNC_STATE_UNKNOWN, i);
		}
		scrollPane = new ScrollPane();
		scrollPane.setContent(gridPane);
		//scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		//scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPane.setMaxHeight(400);
		vBox.getChildren().add(scrollPane);
		comboBoxCustomNamesCount.getSelectionModel().select(0);

	}

	public Node getUI() {
		return (Node) vBox;
	}

	public void respondToResize (Double h, Double w, Double fullHeight, Double controlH, Double controlW) {
		//vBox.setStyle(null);
		
		Double toolBarFontHeight = fullHeight*Constants.FX_TITLEBARS_FONT_SCALE;
		if (toolBarFontHeight > Constants.FX_TITLEBARS_FONT_MIN_SIZE) {
			//System.out.printf("ToolBar font size = %f\n",fontHeight);
			toolBarTop.setStyle("-fx-font-size: " + toolBarFontHeight.toString() + "pt");			
		} else {
			toolBarTop.setStyle("-fx-font-size: " + Constants.FX_TITLEBARS_FONT_MIN_SIZE.toString() + "pt");			
		}
		toolBarTop.setStyle("-fx-padding: 0.0em 0.0em 0.2em 0.0em");
		//comboBoxCustomNamesCount.setMinWidth(controlH*4);
		//comboBoxCustomNamesCount.setMaxWidth(controlH*4);
		//labelCustomNamesCount.setFont(new Font(controlH*0.4));
	}


	public Button getButtonGetAll() {
		return buttonGetAll;
	}

	public Button getButtonSendAll() {
		return buttonSendAll;
	}

	public Button getButtonLoadAll() {
		return buttonLoadAll;
	}

	public Button getButtonSaveAll() {
		return buttonSaveAll;
	}


	public ComboBox<String> getComboBoxCurve() {
		return comboBoxCustomNamesCount;
	}
	
	
	public void setSyncState(int state, Integer namePointer) {
		allSyncStates[namePointer] = state;
		Color color;
		// TODO Auto-generated method stub
		switch (state) {
		case Constants.SYNC_STATE_UNKNOWN:
			color = Constants.SYNC_STATE_UNKNOWN_COLOR;
			break;
		case Constants.SYNC_STATE_SYNCED:
			color = Constants.SYNC_STATE_SYNCED_COLOR;
			break;
		case Constants.SYNC_STATE_NOT_SYNCED:
			color = Constants.SYNC_STATE_NOT_SYNCED_COLOR;
			break;
		default:
			color = Constants.SYNC_STATE_SYNCED_COLOR;
			break;
		}
		//labelCustomNamesCount.setTextFill(color);
	}

}
