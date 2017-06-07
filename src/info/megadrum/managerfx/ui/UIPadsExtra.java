package info.megadrum.managerfx.ui;

import javax.swing.event.EventListenerList;

import info.megadrum.managerfx.data.ConfigCustomName;
import info.megadrum.managerfx.utils.Constants;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class UIPadsExtra extends UIPanel {
	private TabPane			tabPane;
	private Tab 			tabCurves;
	private	Tab				tabCustomNames;
	private UICurves		uiCurves;
	private UICustomNames	uiCustomNames;

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
	
	public UIPadsExtra(String title) {
		super(title);
		tabPane = new TabPane();
        tabCurves = new Tab("Custom Curves");
        tabCurves.setClosable(false);
        tabCustomNames = new Tab("Custom Names");
        tabCustomNames.setClosable(false);

        uiCurves = new UICurves();
        uiCurves.addControlChangeEventListener(new ControlChangeEventListener() {
			
			@Override
			public void controlChangeEventOccurred(ControlChangeEvent evt, Integer parameter) {
				// TODO Auto-generated method stub
				fireControlChangeEvent(new ControlChangeEvent(this), parameter);
			}
		});
        tabCurves.setContent(uiCurves.getUI());
        
        uiCustomNames = new UICustomNames();
        uiCustomNames.addControlChangeEventListener(new ControlChangeEventListener() {
			
			@Override
			public void controlChangeEventOccurred(ControlChangeEvent evt, Integer parameter) {
				// TODO Auto-generated method stub
				fireControlChangeEvent(new ControlChangeEvent(this), parameter);
			}
		});
        tabCustomNames.setContent(uiCustomNames.getUI());
        tabPane.getTabs().addAll(tabCurves,tabCustomNames);
        vBoxAll = new VBox(1);
        vBoxAll.getChildren().add(tabPane);
		setDetached(false);
		titledPane.setMaxSize(340, 500);
	}

	public void respondToResize (Double h, Double w, Double fullHeight, Double controlH, Double controlW) {
		Double toolBarFontHeight = fullHeight*Constants.FX_TOOLBARS_FONT_SCALE;
		Double titledPaneFontHeight = fullHeight*Constants.FX_TITLEBARS_FONT_SCALE;
		Double tabsFontSize = fullHeight*Constants.FX_TABS_FONT_SCALE;
		Double tabHeaderPadding = -fullHeight*0.0005;
		Double tabHeaderHeight = fullHeight*0.013;
		if (titledPaneFontHeight > Constants.FX_TITLEBARS_FONT_MIN_SIZE) {
			titledPane.setFont(new Font(titledPaneFontHeight));
		} else {
			titledPane.setFont(new Font(Constants.FX_TITLEBARS_FONT_MIN_SIZE));
		}
		tabCurves.setStyle("-fx-font-size: " + tabsFontSize.toString() + "pt");
		tabCustomNames.setStyle("-fx-font-size: " + tabsFontSize.toString() + "pt");
		tabPane.setStyle("-fx-padding: " + tabHeaderPadding.toString() + "em 0.0em 0.0em 0.0em; -fx-tab-max-height:" + tabHeaderHeight.toString() + "pt;-fx-tab-min-height:" + tabHeaderHeight.toString() + "pt;");

		//titledPane.setMaxWidth(fullHeight*0.35);
		//uiCurves.respondToResize(h, w, (fullHeight > 600.0)?600:fullHeight, controlH, controlW);
		//uiCustomNames.respondToResize(h, w, (fullHeight > 600.0)?600:fullHeight, controlH, controlW);
		uiCurves.respondToResize(h, w, fullHeight, controlH, controlW);
		uiCustomNames.respondToResize(h, w, fullHeight, controlH, controlW);
	}
	
	public void setAllCustomNamesStatesUnknown() {
		uiCustomNames.setAllStateUnknown();
	}
	
	public void setCustomName(ConfigCustomName config, int id, Boolean setFromSysex) {
		uiCustomNames.setCustomName(config, id, setFromSysex);
	}
	
	public void getCustomName(ConfigCustomName config, int id) {
		uiCustomNames.getCustomName(config, id);
	}
	
	public void setMdCustomName(ConfigCustomName config, int id) {
		uiCustomNames.setMdCustomName(config, id);
	}

	public Button getCustomNamesButtonGetAll() {
		return uiCustomNames.getButtonGetAll();
	}

	public Button getCustomNamesButtonSendAll() {
		return uiCustomNames.getButtonSendAll();
	}

	public Button getCustomNamesButtonLoadAll() {
		return uiCustomNames.getButtonLoadAll();
	}

	public Button getCustomNamesButtonSaveAll() {
		return uiCustomNames.getButtonSaveAll();
	}

	public ComboBox<String> getComboBoxCustomNamesCount() {
		return uiCustomNames.getComboBoxCustomNamesCount();
	}
	
	public void setYvalues(int [] values, Boolean setFromSysex) {
		uiCurves.setYvalues(values, setFromSysex);
	}

	public void setMdYvalues(int [] values) {
		uiCurves.setMdYvalues(values);
	}

	public void getYvalues(int [] values) {
		uiCurves.getYvalues(values);
	}
	
	public void setCurveSysexReceived(Boolean received) {
		uiCurves.setSysexReceived(received);
	}
	
	public void testCurveSyncState() {
		uiCurves.testSyncState();
	}

	public Button getCurvesButtonGet() {
		return uiCurves.getButtonGet();
	}

	public Button getCurvesButtonSend() {
		return uiCurves.getButtonSend();
	}

	public Button getCurvesButtonGetAll() {
		return uiCurves.getButtonGetAll();
	}

	public Button getCurvesButtonSendAll() {
		return uiCurves.getButtonSendAll();
	}

	public Button getCurvesButtonLoad() {
		return uiCurves.getButtonLoad();
	}

	public Button getCurvesButtonSave() {
		return uiCurves.getButtonSave();
	}

	public ComboBox<String> getCurvesComboBox() {
		return uiCurves.getComboBoxCurve();
	}
	
	public Button getCurvesButtonFirst() {
		return uiCurves.getButtonFirst();
	}

	public Button getCurvesButtonPrev() {
		return uiCurves.getButtonPrev();
	}

	public Button getCurvesButtonNext() {
		return uiCurves.getButtonNext();
	}

	public Button getCurvesButtonLast() {
		return uiCurves.getButtonLast();
	}
}
