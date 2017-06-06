package info.megadrum.managerfx;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.OperationsException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import org.apache.commons.collections.functors.AndPredicate;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import info.megadrum.managerfx.data.ConfigFull;
import info.megadrum.managerfx.data.ConfigOptions;
import info.megadrum.managerfx.data.FileManager;
import info.megadrum.managerfx.midi.MidiController;
import info.megadrum.managerfx.midi.MidiEvent;
import info.megadrum.managerfx.midi.MidiEventListener;
import info.megadrum.managerfx.midi.MidiRescanEvent;
import info.megadrum.managerfx.midi.MidiRescanEventListener;
import info.megadrum.managerfx.ui.ControlChangeEvent;
import info.megadrum.managerfx.ui.ControlChangeEventListener;
import info.megadrum.managerfx.ui.SpinnerFast;
import info.megadrum.managerfx.ui.UIPadsExtra;
import info.megadrum.managerfx.ui.UIPanel;
import info.megadrum.managerfx.ui.UIGlobal;
import info.megadrum.managerfx.ui.UIGlobalMisc;
import info.megadrum.managerfx.ui.UIInput;
import info.megadrum.managerfx.ui.UIMisc;
import info.megadrum.managerfx.ui.UIOptions;
import info.megadrum.managerfx.ui.UIPad;
import info.megadrum.managerfx.ui.UIPedal;
import info.megadrum.managerfx.utils.Constants;
import info.megadrum.managerfx.utils.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Controller implements MidiRescanEventListener {
	private Stage window;
	private Scene scene1;
	private MenuBar mainMenuBar;
	private Menu mainMenu, viewMenu, aboutMenu;
	private Menu menuAllSettings, menuGlobalMisc, menuMisc, menuHiHat, menuAllPads,
				menuSelectedPad, menuCustomCurves, menuCustomNames;
	private MenuItem menuItemAllSettingsGet, menuItemAllSettingsSend, menuItemAllSettingsLoad,
				menuItemAllSettingsSave;
	private Menu menuLoadFromMdSlot, menuSaveToMdSlot;
	private ArrayList<MenuItem> allMenuItemsLoadFromSlot, allMenuItemsSaveSlot;
	private MenuItem menuItemGlobalMiscGet, menuItemGlobalMiscSend;
	private MenuItem menuItemMiscGet, menuItemMiscSend;
	private MenuItem menuItemHiHatGet, menuItemHiHatSend;
	private MenuItem menuItemAllPadsGet, menuItemAllPadsSend;
	private MenuItem menuItemSelectedPadGet, menuItemSelectedPadSend;
	private MenuItem menuItemCustomCurvesGet, menuItemCustomCurvesSend;
	private MenuItem menuItemCustomNamesGet, menuItemCustomNamesSend;
	private MenuItem firmwareUpgradeMenuItem, optionsMenuItem, exitMenuItem;
	
	private RadioMenuItem rbMiscHide;
	private RadioMenuItem rbMiscShow;
	private RadioMenuItem rbMiscDetach;
	private RadioMenuItem rbPedalHide;
	private RadioMenuItem rbPedalShow;
	private RadioMenuItem rbPedalDetach;
	private RadioMenuItem rbPadsHide;
	private RadioMenuItem rbPadsShow;
	private RadioMenuItem rbPadsDetach;
	private RadioMenuItem rbPadsExtraHide;
	private RadioMenuItem rbPadsExtraShow;
	private RadioMenuItem rbPadsExtraDetach;
	private RadioMenuItem rbMidiLogHide;
	private RadioMenuItem rbMidiLogShow;
	private RadioMenuItem rbMidiLogDetach;
	private UIOptions optionsWindow;
	private HBox hBoxUIviews;
	private UIGlobal uiGlobal;
	private UIGlobalMisc uiGlobalMisc;
	private UIMisc uiMisc;
	private Stage windowMisc;
	private UIPedal uiPedal;
	private Stage windowPedal;
	private UIPad uiPad;
	private Stage windowPad;
	private UIPadsExtra uiPadsExtra;
	private Stage windowPadsExtra;
	private Stage windowMidiLog;
	private ArrayList<UIPanel>	allPanels;
	private ArrayList<Stage> allWindows;
	//private ProgressBar tempProgressBar;
	private Timer 		timerResize;
	private TimerTask	timerTaskResize;
	private int			timerResizeDelay = 200;
	
	private MidiController midiController;
	private ConfigOptions configOptions;
	private ConfigFull configFull;
	private ConfigFull moduleConfigFull;
	private ConfigFull [] fullConfigs;
	private String [] configFileNames;
	private FileManager fileManager;
	private File file;
	private int padPair = 0;
	private int comboBoxInputChangedFromSet = 0;
	private int comboBoxFileChangedFromSet = 0;
	private int toggleButtonMidiChangedFromSet = 0;
	private int oldInputsCounts = 0;
	private Boolean sendNextAllSysexRequestsFlag = false;
	private Boolean sendSysexReadOnlyRequestFlag = false;
	private Boolean loadConfigAfterLoadSlot = false;
	private Boolean saveToSlotAfterSendAll = false;
	private int saveToSlot = 0;
	
	private int curvePointer = 0;

	private List<byte[]> sysexSendList;
	
	public Controller(Stage primaryStage) {
		window = primaryStage;
		window.setTitle("MegaDrumManagerFX");
		window.setOnCloseRequest(e-> {
			e.consume();
			closeProgram();
		});

		fileManager = new FileManager(window);
		initMidi();
		initConfigs();
		createMainMenuBar();
		uiGlobal = new UIGlobal();
		uiGlobal.getButtonGetAll().setOnAction(e-> sendAllSysexRequests());
		uiGlobal.getButtonSendAll().setOnAction(e-> sendAllSysex());
		uiGlobal.getButtonLoadAll().setOnAction(e-> load_all());
		uiGlobal.getButtonSaveAll().setOnAction(e-> save_all());
		uiGlobal.getComboBoxFile().getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
		    	if (comboBoxFileChangedFromSet > 0) {
		    		comboBoxFileChangedFromSet--;
		    	} else {
					configOptions.lastConfig = uiGlobal.getComboBoxFile().getSelectionModel().getSelectedIndex();
					fileManager.loadAllSilent(fullConfigs[configOptions.lastConfig], configOptions);
					loadAllFromConfigFull();
		    	}				
			}
        });
		uiGlobal.getButtonPrevFile().setOnAction(e-> {
			if (configOptions.lastConfig>0) {
				uiGlobal.getComboBoxFile().getSelectionModel().select(configOptions.lastConfig - 1);
			}			
		});
		uiGlobal.getButtonNextFile().setOnAction(e-> {
			if (configOptions.lastConfig<(Constants.CONFIGS_COUNT-1)) {
				uiGlobal.getComboBoxFile().getSelectionModel().select(configOptions.lastConfig + 1);
			}			
		});

		
		uiGlobalMisc = new UIGlobalMisc();
		uiGlobalMisc.getButtonGet().setOnAction(e-> sendSysexGlobalMiscRequest());
		uiGlobalMisc.getButtonSend().setOnAction(e-> sendSysexGlobalMisc());
		uiGlobalMisc.getCheckBoxLiveUpdates().selectedProperty().addListener(new ChangeListener<Boolean>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	configOptions.liveUpdates = newValue;
		    }
		});
		uiGlobalMisc.addControlChangeEventListener(new ControlChangeEventListener() {
			
			@Override
			public void controlChangeEventOccurred(ControlChangeEvent evt, Integer parameter) {
				// TODO Auto-generated method stub
				//System.out.println("aaaaaaaaaaaaaaaaaaaaa");
				controlsGlobalMiscChanged();
			}
		});
		uiGlobalMisc.getToggleButtonMidi().setOnAction(e-> openMidiPorts(uiGlobalMisc.getToggleButtonMidi().isSelected()));
		uiMisc = new UIMisc("Misc");
		uiMisc.getButtonGet().setOnAction(e-> sendSysexMiscRequest());
		uiMisc.getButtonSend().setOnAction(e-> sendSysexMisc());
		uiMisc.getButtonLoad().setOnAction(e-> loadSysexMisc());
		uiMisc.getButtonSave().setOnAction(e-> saveSysexMisc());		
		
		uiMisc.addControlChangeEventListener(new ControlChangeEventListener() {
			
			@Override
			public void controlChangeEventOccurred(ControlChangeEvent evt, Integer parameter) {
				// TODO Auto-generated method stub
				controlsMiscChanged();
			}
		});

		uiPedal = new UIPedal("HiHat Pedal");
		uiPedal.getButtonSend().setOnAction(e-> sendSysexPedal());
		uiPedal.getButtonGet().setOnAction(e-> sendSysexPedalRequest());
		uiPedal.getButtonLoad().setOnAction(e-> loadSysexPedal());
		uiPedal.getButtonSave().setOnAction(e-> saveSysexPedal());		
		uiPedal.addControlChangeEventListener(new ControlChangeEventListener() {
			
			@Override
			public void controlChangeEventOccurred(ControlChangeEvent evt, Integer parameter) {
				// TODO Auto-generated method stub
				controlsPedalChanged();
			}
		});
		uiPad = new UIPad("Pads");
		uiPad.addControlChangeEventListener(new ControlChangeEventListener() {
			
			@Override
			public void controlChangeEventOccurred(ControlChangeEvent evt, Integer parameter) {
				// TODO Auto-generated method stub
				Integer inputNumber = 0;
				if (padPair > 0) {
					inputNumber = ((padPair - 1)*2) +1; 
				}				
				System.out.printf("Input %s control change\n", (parameter == Constants.CONTROL_CHANGE_EVENT_LEFT_INPUT) ? "left" : "right");
				switch (parameter) {
				case Constants.CONTROL_CHANGE_EVENT_LEFT_INPUT:
					if (uiPad.isCopyPressed()) {
						System.out.printf("left copy pressed with valueId = %d\n", uiPad.getCopyPressedValueId());
						uiPad.resetCopyPressed();
						copyLeftInputValueToAllOthers();
						switchToSelectedPair(padPair);
					} else {
						controlsInputChanged(inputNumber, true);
					}
					break;
				case Constants.CONTROL_CHANGE_EVENT_RIGHT_INPUT:
					if (uiPad.isCopyPressed()) {
						System.out.printf("right copy pressed with valueId = %d\n", uiPad.getCopyPressedValueId());
						uiPad.resetCopyPressed();
						copyPadPairValueToAllOthers();
					} else {
						controlsInputChanged(inputNumber + 1, false);						
					}
					break;
				case Constants.CONTROL_CHANGE_EVENT_3RD_INPUT:
					if (padPair > 0) {
						if (uiPad.isCopyPressed()) {
							System.out.printf("3rd zone copy pressed with valueId = %d\n", uiPad.getCopyPressedValueId());
							uiPad.resetCopyPressed();
							copy3rdZoneValueToAllOthers();
						} else {
							controlsInputChanged(inputNumber + 1, false);						
							controls3rdChanged(padPair);
						}
					}
					break;
				default:
					break;
				}
				if (uiPad.isNameChanged()) {
					uiPad.resetNameChanged();
					updateComboBoxInput(true);
				}
			}
		});

		padPair = 0;
		uiPad.getButtonGet().setOnAction(e-> {
			if (padPair == 0) {
				sendSysexInputRequest(0);
			} else {
				sendSysexPairRequest(padPair);
			}
		});
		uiPad.getButtonSend().setOnAction(e-> {
			if (padPair == 0) {
				sendSysexInput(0, true);
			} else {
				sendSysexPair(padPair);
			}
		});
		uiPad.getButtonGetAll().setOnAction(e-> {
			sendAllInputsSysexRequests();
		});
		uiPad.getButtonSendAll().setOnAction(e-> {
			sendAllInputsSysex();
		});
		uiPad.getButtonLoad().setOnAction(e-> loadSysexPad());
		uiPad.getButtonSave().setOnAction(e-> saveSysexPad());		
		uiPad.getButtonPrev().setOnAction(e-> {
			if (padPair > 0) {
				switchToSelectedPair(padPair - 1);
			}
		});
		uiPad.getButtonNext().setOnAction(e-> {
			if (padPair < ((configFull.configGlobalMisc.inputs_count/2) - 1)) {
				switchToSelectedPair(padPair + 1);
			}
		});
		uiPad.getButtonFirst().setOnAction(e-> {
			switchToSelectedPair(0);
		});
		uiPad.getButtonLast().setOnAction(e-> {
			switchToSelectedPair((configFull.configGlobalMisc.inputs_count/2) - 1);
		});
		uiPad.getComboBoxInput().getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
		    	if (comboBoxInputChangedFromSet > 0) {
		    		comboBoxInputChangedFromSet--;
		        	//System.out.printf("changedFromSet reduced to %d for %s\n", changedFromSet, label.getText());
		    	} else {
		    		Integer newInValue = uiPad.getComboBoxInput().getSelectionModel().getSelectedIndex();
		        	//System.out.printf("Setting %s to %s\n", label.getText(), newValue);
		    		if (newInValue > -1) {
			    		switchToSelectedPair(newInValue);		    			
		    		}
		    	}				
			}
        });
		updateComboBoxInput(true);
		uiPad.setInputPair(0, configFull.configPads[0], configFull.configPos[0], null, null, null);
		
		uiPadsExtra = new UIPadsExtra("Pads Extra Settings");
		uiPadsExtra.addControlChangeEventListener(new ControlChangeEventListener() {
			
			@Override
			public void controlChangeEventOccurred(ControlChangeEvent evt, Integer parameter) {
				// TODO Auto-generated method stub
				if (parameter.intValue() == Constants.CONTROL_CHANGE_EVENT_CURVE) {
					controlsCurveChanged();
				} else {
					if (parameter.intValue() >= Constants.CUSTOM_NAME_CHANGE_TEXT_START) {
						if (parameter.intValue() < Constants.CUSTOM_NAME_CHANGE_GET_START) {
							// Custom Name changed
							controlsCustomNameChanged(parameter - Constants.CUSTOM_NAME_CHANGE_TEXT_START);
						} else if (parameter.intValue() < Constants.CUSTOM_NAME_CHANGE_SEND_START) {
							// Get button pressed
							sendSysexCustomNameRequest(parameter - Constants.CUSTOM_NAME_CHANGE_GET_START);
						} else {
							// Send button pressed
							sendSysexCustomName(parameter - Constants.CUSTOM_NAME_CHANGE_SEND_START);
						}
					}
				}
			}
		});
		uiPadsExtra.getComboBoxCustomNamesCount().getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				switch (uiPadsExtra.getComboBoxCustomNamesCount().getSelectionModel().getSelectedIndex()) {
				case 0:
					configFull.customNamesCount = 2;
					break;
				case 1:
					configFull.customNamesCount = 16;
					break;
				case 2:
				default:
					configFull.customNamesCount = 32;
					break;
				}
			}
		});
		uiPadsExtra.getCustomNamesButtonGetAll().setOnAction(e-> sendAllCustomNamesSysexRequests());
		uiPadsExtra.getCustomNamesButtonSendAll().setOnAction(e-> sendAllCustomNamesSysex());
		uiPadsExtra.getCustomNamesButtonLoadAll().setOnAction(e-> loadSysexAllCustomNames());
		uiPadsExtra.getCustomNamesButtonSaveAll().setOnAction(e-> saveSysexAllCustomNames());
		switch (configFull.customNamesCount) {
		case 32:
			uiPadsExtra.getComboBoxCustomNamesCount().getSelectionModel().select(2);
			break;
		case 16:
			uiPadsExtra.getComboBoxCustomNamesCount().getSelectionModel().select(1);
			break;
		case 2:
		default:
			uiPadsExtra.getComboBoxCustomNamesCount().getSelectionModel().select(0);
			break;
		}
		uiPadsExtra.getCurvesButtonGet().setOnAction(e-> sendSysexCurveRequest());
		uiPadsExtra.getCurvesButtonSend().setOnAction(e-> sendSysexCurve());
		uiPadsExtra.getCurvesButtonGetAll().setOnAction(e-> sendAllCurvesSysexRequests());
		uiPadsExtra.getCurvesButtonSendAll().setOnAction(e-> sendAllCurvesSysex());
		uiPadsExtra.getCurvesButtonLoad().setOnAction(e-> loadSysexCurve());
		uiPadsExtra.getCurvesButtonSave().setOnAction(e-> saveSysexCurve());
		uiPadsExtra.getCurvesComboBox().getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				int newCurve = uiPadsExtra.getCurvesComboBox().getSelectionModel().getSelectedIndex();
				if (newCurve != curvePointer) {
					switchToSelectedCurve(newCurve);
				}
			}
		});
		uiPadsExtra.getCurvesButtonFirst().setOnAction(e-> {
			switchToSelectedCurve(0);
		});
		uiPadsExtra.getCurvesButtonPrev().setOnAction(e-> {
			switchToSelectedCurve(curvePointer - 1);
		});
		uiPadsExtra.getCurvesButtonNext().setOnAction(e-> {
			switchToSelectedCurve(curvePointer + 1);
		});
		uiPadsExtra.getCurvesButtonLast().setOnAction(e-> {
			switchToSelectedCurve(Constants.CURVES_COUNT - 1);
		});
		
		VBox layout1VBox = new VBox();

		mainMenuBar.prefWidthProperty().bind(primaryStage.widthProperty());
		layout1VBox.getChildren().add(mainMenuBar);
		layout1VBox.getChildren().add(uiGlobal.getUI());
		layout1VBox.getChildren().add(uiGlobalMisc.getUI());
		
		hBoxUIviews = new HBox(5);
		Button button = new Button("b");
		//layout2HBox.getChildren().add(button);
		hBoxUIviews.getChildren().add(uiMisc.getUI());
		hBoxUIviews.getChildren().add(uiPedal.getUI());
		hBoxUIviews.getChildren().add(uiPad.getUI());
		hBoxUIviews.getChildren().add(uiPadsExtra.getUI());

		allPanels = new ArrayList<UIPanel>();
		allPanels.add(uiMisc);
		allPanels.add(uiPedal);
		allPanels.add(uiPad);
		allPanels.add(uiPadsExtra);
		
		allWindows = new ArrayList<Stage>();
		allWindows.add(null);
		allWindows.add(null);
		allWindows.add(null);
		allWindows.add(null);
/*		windowMisc = new Stage();
		windowMisc.setTitle("Misc");
		allWindows.add(windowMisc);
		windowPedal = new Stage();
		windowMisc.setTitle("Misc");
		allWindows.add(windowMisc);
*/		
		layout1VBox.getChildren().add(hBoxUIviews);
		//layout1VBox.setPadding(new Insets(5, 5, 5, 5));
		layout1VBox.setStyle("-fx-border-width: 2px; -fx-padding: 2.0 2.0 2.0 2.0; -fx-border-color: #2e8b57");
		//scene1 = new Scene(layout1, 300,500);
		scene1 = new Scene(layout1VBox);
		scene1.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

		optionsWindow = new UIOptions(this, configOptions);
		optionsWindow.addMidiRescanEventListener(this);
		
		window.setScene(scene1);
		window.setMinWidth(1000);
		//window.sizeToScene();
		//scene1.widthProperty().addListener((obs, oldVal, newVal) -> {
		//	respondToResize(scene1);
		//});

		scene1.heightProperty().addListener((obs, oldVal, newVal) -> {
			respondToResize(scene1);
		});
		loadConfig();
		window.show();
	}

	private boolean resizeFromResize = false;
	public void respondToResize(Scene sc) {
		if (resizeFromResize) {
			resizeFromResize = false;
		} else {
			if (timerResize != null) {
				timerResize.cancel();
			}
			timerResize = new Timer();
			timerTaskResize = new TimerTask() {
				
				@Override
				public void run() {
					Platform.runLater(new Runnable() {
						
						@Override
						public void run() {
							//resizeFromResize = true;
							Double mainMenuBarHeight = mainMenuBar.getHeight();
							Double globalBarHeight = uiGlobal.getUI().layoutBoundsProperty().getValue().getHeight();
							Double globalMiscBarHeight = uiGlobalMisc.getUI().layoutBoundsProperty().getValue().getHeight();
							Double height = sc.getHeight() - mainMenuBarHeight - globalBarHeight - globalMiscBarHeight;
							Double width = height*2;
							Double controlH, controlW;
							controlH= height *0.035 *0.8;
							controlW= controlH *8;
							uiMisc.respondToResize(height, width, height, controlH, controlW);
							uiPedal.respondToResize(height, width, height, controlH, controlW);
							uiPad.respondToResize(height, width, height, controlH, controlW);
							uiPadsExtra.respondToResize(height, width, height, controlH, controlW);							
						}
					});
				}
			};
			timerResizeDelay = 50;
			timerResize.schedule(timerTaskResize, timerResizeDelay);
		}
	}

	public void XrespondToResize(Scene sc) {
		if (resizeFromResize) {
			resizeFromResize = false;
		} else {
			//resizeFromResize = true;
			Double mainMenuBarHeight = mainMenuBar.getHeight();
			Double globalBarHeight = uiGlobal.getUI().layoutBoundsProperty().getValue().getHeight();
			Double globalMiscBarHeight = uiGlobalMisc.getUI().layoutBoundsProperty().getValue().getHeight();
			Double height = sc.getHeight() - mainMenuBarHeight - globalBarHeight - globalMiscBarHeight;
			Double width = height*2;
			Double controlH, controlW;
			controlH= height *0.035 *0.8;
			controlW= controlH *8;
			uiMisc.respondToResize(height, width, height, controlH, controlW);
			uiPedal.respondToResize(height, width, height, controlH, controlW);
			uiPad.respondToResize(height, width, height, controlH, controlW);
			uiPadsExtra.respondToResize(height, width, height, controlH, controlW);
		}
	}

	private void reCreateSlotsMenuItems() {
		allMenuItemsLoadFromSlot.clear();
		for (int i = 0; i < configFull.configNamesCount; i++) {
			final int iFinal = i;
			if (configFull.configGlobalMisc.config_names_en) {
				allMenuItemsLoadFromSlot.add(new MenuItem(Integer.toString(i + 1) + " " + configFull.configConfigNames[i].name));				
			} else {
				allMenuItemsLoadFromSlot.add(new MenuItem(Integer.toString(i + 1)));
			}
			allMenuItemsLoadFromSlot.get(i).setOnAction(e-> {
				sendSysexLoadFromSlotRequest(iFinal);
			});
		}
		menuLoadFromMdSlot.getItems().clear();
		menuLoadFromMdSlot.getItems().addAll(allMenuItemsLoadFromSlot);
		allMenuItemsSaveSlot.clear();
		for (int i = 0; i < configFull.configNamesCount; i++) {
			final int iFinal = i;
			if (configFull.configGlobalMisc.config_names_en) {
				allMenuItemsSaveSlot.add(new MenuItem(Integer.toString(i + 1) + " " + configFull.configConfigNames[i].name));				
			} else {
				allMenuItemsSaveSlot.add(new MenuItem(Integer.toString(i + 1)));
			}
			allMenuItemsSaveSlot.get(i).setOnAction(e-> {
				sendSysexSaveToSlotRequest(iFinal);
			});
		}
		menuSaveToMdSlot.getItems().clear();
		menuSaveToMdSlot.getItems().addAll(allMenuItemsSaveSlot);		
	}
	
	private void createMainMenuBar() {
		mainMenuBar = new MenuBar();
		mainMenuBar.setStyle("-fx-font-size: 10 pt");
		mainMenu = new Menu("Main");
		viewMenu = new Menu("View");
		aboutMenu = new Menu("About");
		
		mainMenuBar.getMenus().addAll(mainMenu,viewMenu,aboutMenu);

		menuAllSettings = new Menu("All Settings");
		menuItemAllSettingsGet = new MenuItem("Get from MD");
		menuItemAllSettingsGet.setOnAction(e-> sendAllSysexRequests());
		menuItemAllSettingsSend = new MenuItem("Send to MD");
		menuItemAllSettingsSend.setOnAction(e-> sendAllSysex());
		menuItemAllSettingsLoad = new MenuItem("Load from file");
		menuItemAllSettingsLoad.setOnAction(e-> load_all());
		menuItemAllSettingsSave = new MenuItem("Save to file");
		menuItemAllSettingsSave.setOnAction(e-> save_all());
		menuLoadFromMdSlot = new Menu("Load from MD Slot:");
		allMenuItemsLoadFromSlot = new ArrayList<MenuItem>();
		menuSaveToMdSlot = new Menu("Save to MD Slot:");
		allMenuItemsSaveSlot = new ArrayList<MenuItem>();
		reCreateSlotsMenuItems();
		menuAllSettings.getItems().addAll(menuItemAllSettingsGet, menuItemAllSettingsSend, menuItemAllSettingsLoad,
				menuItemAllSettingsSave, menuLoadFromMdSlot, menuSaveToMdSlot);

		menuGlobalMisc = new Menu("Global Misc Settings");
		menuItemGlobalMiscGet = new MenuItem("Get from MD");
		menuItemGlobalMiscGet.setOnAction(e-> sendSysexGlobalMiscRequest());
		menuItemGlobalMiscSend = new MenuItem("Send to MD");
		menuItemGlobalMiscSend.setOnAction(e-> sendSysexGlobalMisc());
		menuGlobalMisc.getItems().addAll(menuItemGlobalMiscGet, menuItemGlobalMiscSend);

		menuMisc = new Menu("Misc Settings");
		menuItemMiscGet = new MenuItem("Get from MD");
		menuItemMiscGet.setOnAction(e-> sendSysexMiscRequest());
		menuItemMiscSend = new MenuItem("Send to MD");
		menuItemMiscSend.setOnAction(e-> sendSysexMisc());
		menuMisc.getItems().addAll(menuItemMiscGet, menuItemMiscSend);

		menuHiHat = new Menu("HiHat Pedal Settings");
		menuItemHiHatGet = new MenuItem("Get from MD");
		menuItemHiHatGet.setOnAction(e-> sendSysexPedalRequest());
		menuItemHiHatSend = new MenuItem("Send to MD");
		menuItemHiHatSend.setOnAction(e-> sendSysexPedal());
		menuHiHat.getItems().addAll(menuItemHiHatGet, menuItemHiHatSend);
		
		menuAllPads = new Menu("All Pads Settings");
		menuItemAllPadsGet = new MenuItem("Get from MD");
		menuItemAllPadsGet.setOnAction(e-> sendAllInputsSysexRequests());
		menuItemAllPadsSend = new MenuItem("Send to MD");
		menuItemAllPadsSend.setOnAction(e-> sendAllInputsSysex());
		menuAllPads.getItems().addAll(menuItemAllPadsGet, menuItemAllPadsSend);

		menuSelectedPad = new Menu("Selected Pad Settings");
		menuItemSelectedPadGet = new MenuItem("Get from MD");
		menuItemSelectedPadGet.setOnAction(e-> {
			if (padPair == 0) {
				sendSysexInputRequest(0);
			} else {
				sendSysexPairRequest(padPair);
			}
		});
		menuItemSelectedPadSend = new MenuItem("Send to MD");
		menuItemSelectedPadSend.setOnAction(e-> {
			if (padPair == 0) {
				sendSysexInput(0, true);
			} else {
				sendSysexPair(padPair);
			}
		});
		menuSelectedPad.getItems().addAll(menuItemSelectedPadGet, menuItemSelectedPadSend);

		menuCustomCurves = new Menu("All Custom Curves");
		menuItemCustomCurvesGet = new MenuItem("Get from MD");
		menuItemCustomCurvesGet.setOnAction(e-> sendAllCurvesSysexRequests());
		menuItemCustomCurvesSend = new MenuItem("Send to MD");
		menuItemCustomCurvesSend.setOnAction(e-> sendAllCurvesSysex());
		menuCustomCurves.getItems().addAll(menuItemCustomCurvesGet, menuItemCustomCurvesSend);

		menuCustomNames = new Menu("All Custom Names");
		menuItemCustomNamesGet = new MenuItem("Get from MD");
		menuItemCustomNamesGet.setOnAction(e-> sendAllCustomNamesSysexRequests());
		menuItemCustomNamesSend = new MenuItem("Send to MD");
		menuItemCustomNamesSend.setOnAction(e-> sendAllCustomNamesSysex());
		menuCustomNames.getItems().addAll(menuItemCustomNamesGet, menuItemCustomNamesSend);

		firmwareUpgradeMenuItem = new MenuItem("Firmware Upgrade");
		optionsMenuItem = new MenuItem("Options");
		optionsMenuItem.setOnAction(e-> { 
			showOptionsWindow();
		});
		exitMenuItem = new MenuItem("Exit");
		exitMenuItem.setOnAction(e-> closeProgram());
		
		mainMenu.getItems().addAll(menuAllSettings, menuGlobalMisc, menuMisc,
				menuHiHat,menuAllPads,menuSelectedPad,menuCustomCurves, menuCustomNames,
				new SeparatorMenuItem(), firmwareUpgradeMenuItem, new SeparatorMenuItem(), optionsMenuItem,
				new SeparatorMenuItem(),exitMenuItem
				);
		
		Menu menuViewMisc = new Menu("Misc");
		
		ToggleGroup tgMisc = new ToggleGroup();
		rbMiscHide = new RadioMenuItem("Hide");
		rbMiscHide.setToggleGroup(tgMisc);
		rbMiscHide.setOnAction(e-> {
			//hBoxUIviews.getChildren().remove(uiMisc.getUI());
			uiMisc.setViewState(Constants.PANEL_HIDE);
			showPanels();
			});
		rbMiscShow = new RadioMenuItem("Show");
		rbMiscShow.setToggleGroup(tgMisc);
		rbMiscShow.setOnAction(e-> {
			uiMisc.setViewState(Constants.PANEL_SHOW);
			showPanels();
			});
		rbMiscDetach = new RadioMenuItem("Detach");
		rbMiscDetach.setToggleGroup(tgMisc);
		rbMiscDetach.setOnAction(e-> {
			uiMisc.setViewState(Constants.PANEL_DETACH);
			showPanels();
			});
		menuViewMisc.getItems().addAll(rbMiscHide, rbMiscShow, rbMiscDetach);
		rbMiscShow.setSelected(true);
		viewMenu.getItems().add(menuViewMisc);
		
		Menu menuViewPedal = new Menu("Pedal");
		
		ToggleGroup tgPedal = new ToggleGroup();
		rbPedalHide = new RadioMenuItem("Hide");
		rbPedalHide.setToggleGroup(tgPedal);
		rbPedalHide.setOnAction(e-> {
			//hBoxUIviews.getChildren().remove(uiMisc.getUI());
			uiPedal.setViewState(Constants.PANEL_HIDE);
			showPanels();
			});
		rbPedalShow = new RadioMenuItem("Show");
		rbPedalShow.setToggleGroup(tgPedal);
		rbPedalShow.setOnAction(e-> {
			uiPedal.setViewState(Constants.PANEL_SHOW);
			showPanels();
			});
		rbPedalDetach = new RadioMenuItem("Detach");
		rbPedalDetach.setToggleGroup(tgPedal);
		rbPedalDetach.setOnAction(e-> {
			uiPedal.setViewState(Constants.PANEL_DETACH);
			showPanels();
			});
		menuViewPedal.getItems().addAll(rbPedalHide, rbPedalShow, rbPedalDetach);
		rbPedalShow.setSelected(true);
		viewMenu.getItems().add(menuViewPedal);

		Menu menuViewPads = new Menu("Pads");
		
		ToggleGroup tgPads = new ToggleGroup();
		rbPadsHide = new RadioMenuItem("Hide");
		rbPadsHide.setToggleGroup(tgPads);
		rbPadsHide.setOnAction(e-> {
			//hBoxUIviews.getChildren().remove(uiMisc.getUI());
			uiPad.setViewState(Constants.PANEL_HIDE);
			showPanels();
			});
		rbPadsShow = new RadioMenuItem("Show");
		rbPadsShow.setToggleGroup(tgPads);
		rbPadsShow.setOnAction(e-> {
			uiPad.setViewState(Constants.PANEL_SHOW);
			showPanels();
			});
		rbPadsDetach = new RadioMenuItem("Detach");
		rbPadsDetach.setToggleGroup(tgPads);
		rbPadsDetach.setOnAction(e-> {
			uiPad.setViewState(Constants.PANEL_DETACH);
			showPanels();
			});
		menuViewPads.getItems().addAll(rbPadsHide, rbPadsShow, rbPadsDetach);
		rbPadsShow.setSelected(true);
		viewMenu.getItems().add(menuViewPads);

		Menu menuViewPadsExtra = new Menu("PadsExtra");
		
		ToggleGroup tgPadsExtra = new ToggleGroup();
		rbPadsExtraHide = new RadioMenuItem("Hide");
		rbPadsExtraHide.setToggleGroup(tgPadsExtra);
		rbPadsExtraHide.setOnAction(e-> {
			//hBoxUIviews.getChildren().remove(uiMisc.getUI());
			uiPadsExtra.setViewState(Constants.PANEL_HIDE);
			showPanels();
			});
		rbPadsExtraShow = new RadioMenuItem("Show");
		rbPadsExtraShow.setToggleGroup(tgPadsExtra);
		rbPadsExtraShow.setOnAction(e-> {
			uiPadsExtra.setViewState(Constants.PANEL_SHOW);
			showPanels();
			});
		rbPadsExtraDetach = new RadioMenuItem("Detach");
		rbPadsExtraDetach.setToggleGroup(tgPadsExtra);
		rbPadsExtraDetach.setOnAction(e-> {
			uiPadsExtra.setViewState(Constants.PANEL_DETACH);
			showPanels();
			});
		menuViewPadsExtra.getItems().addAll(rbPadsExtraHide, rbPadsExtraShow, rbPadsExtraDetach);
		rbPadsExtraShow.setSelected(true);
		viewMenu.getItems().add(menuViewPadsExtra);

		Menu menuViewMidiLog = new Menu("MidiLog");
		
		ToggleGroup tgMidiLog = new ToggleGroup();
		rbMidiLogHide = new RadioMenuItem("Hide");
		rbMidiLogHide.setToggleGroup(tgMidiLog);
		rbMidiLogHide.setOnAction(e-> {
			//hBoxUIviews.getChildren().remove(uiMisc.getUI());
			//uiPad.setViewState(Constants.PANEL_HIDE);
			showPanels();
			});
		rbMidiLogShow = new RadioMenuItem("Show");
		rbMidiLogShow.setToggleGroup(tgMidiLog);
		rbMidiLogShow.setOnAction(e-> {
			//uiPad.setViewState(Constants.PANEL_SHOW);
			showPanels();
			});
		rbMidiLogDetach = new RadioMenuItem("Detach");
		rbMidiLogDetach.setToggleGroup(tgMidiLog);
		rbMidiLogDetach.setOnAction(e-> {
			//uiPad.setViewState(Constants.PANEL_DETACH);
			showPanels();
			});
		menuViewMidiLog.getItems().addAll(rbMidiLogHide, rbMidiLogShow, rbMidiLogDetach);
		rbMidiLogShow.setSelected(true);
		viewMenu.getItems().add(menuViewMidiLog);

	}

	private void showPanels() {
		hBoxUIviews.getChildren().clear();
		for (int i = 0; i < allPanels.size(); i++) {
			if (allPanels.get(i).getViewState() == Constants.PANEL_SHOW) {
				if (allWindows.get(i) != null) {
					if (allWindows.get(i).isShowing()) {
						allWindows.get(i).close();
					}
				}
				hBoxUIviews.getChildren().add(allPanels.get(i).getUI());
			}
			if (allPanels.get(i).getViewState() == Constants.PANEL_DETACH) {
				hBoxUIviews.getChildren().remove(allPanels.get(i).getUI());
				if (allWindows.get(i) == null) {
					Stage window = new Stage();
					Scene scene = new Scene(allPanels.get(i).getTopLayout());
					window.setScene(scene);
					allWindows.add(i, window);
				}
				allWindows.get(i).show();
			}
			if (allPanels.get(i).getViewState() == Constants.PANEL_HIDE) {
				if (allWindows.get(i) != null) {
					if (allWindows.get(i).isShowing()) {
						allWindows.get(i).close();
					}
				}
			}
		}
	}
	
	private void loadConfig() {
		//copyAllToConfigFull();
		configOptions  = fileManager.loadLastOptions(configOptions);
		System.out.println("ToDo!!");
		//showChangeNotificationIfNeeded();
		uiGlobal.getComboBoxFile().getItems().clear();
		uiGlobal.getComboBoxFile().getItems().addAll(configOptions.configFileNames);
		uiGlobal.getComboBoxFile().getSelectionModel().select(configOptions.lastConfig);
		//showMidiWarningIfNeeded();
		//if (configOptions.autoOpenPorts) {
		//	midiController.;
		//	tglbtnMidi.setSelected(midi_handler.isMidiOpen());
		//}
		//midiController.chainId = configOptions.chainId;
		//comboBox_inputsCount.setSelectedIndex((fullConfigs[configOptions.lastConfig].configGlobalMisc.inputs_count - Constants.MIN_INPUTS)/2);
		//updateInputsCountControls();
		//updateGlobalMiscControls();
		window.setX(configOptions.mainWindowPosition.getX());
		window.setY(configOptions.mainWindowPosition.getY());
		window.setWidth(configOptions.mainWindowSize.getX());
		//System.out.printf("Setting height to %f\n", configOptions.mainWindowSize.getY());
		window.setHeight(configOptions.mainWindowSize.getY());
		//System.out.printf("Height after setting is %f\n", window.getHeight());
		
		for (int i = 0;i<Constants.PANELS_COUNT;i++) {
			//framesDetached[i].setLocation(configOptions.framesPositions[i]);
			//viewMenus[i].setConfigOptions(configOptions);
		}
		uiGlobalMisc.getCheckBoxLiveUpdates().setSelected(configOptions.liveUpdates);
		//checkBoxAutoResize.setSelected(configOptions.autoResize);
	}

	private void closeProgram() {
		System.out.println("Exiting\n");
		midiController.closeAllPorts();
		Point2D point2d = new Point2D(window.getX(), window.getY());
		configOptions.mainWindowPosition = point2d;
		point2d = new Point2D(window.getWidth(), window.getHeight());
		configOptions.mainWindowSize = point2d;
		for (int i = 0;i<Constants.PANELS_COUNT;i++) {
			//configOptions.framesPositions[i] = framesDetached[i].getLocation(); 
		}
		if (configOptions.saveOnExit) {
			fileManager.saveLastOptions(configOptions);
		}
		window.close();
		System.exit(0);
	}
	
	private void copyLeftInputValueToAllOthers() {
		int maxInputs = configFull.configGlobalMisc.inputs_count - 1;
		int valueId = uiPad.getCopyPressedValueId();
		int currentInput = 0;
		if (padPair > 0) {
			currentInput = (padPair*2) - 1;
		}
		int value = configFull.configPads[currentInput].getValueById(valueId);
		for (int i = 0; i < maxInputs; i++) {
			if (i != currentInput) {
				if (((i&1) > 0) || (valueId != Constants.INPUT_VALUE_ID_TYPE)) {
					if ((valueId >= Constants.INPUT_VALUE_ID_POS_LEVEL) && (valueId <= Constants.INPUT_VALUE_ID_POS_HIGH)) {
						value = configFull.configPos[currentInput].getValueById(valueId);
						configFull.configPos[i].setValueById(valueId, value);					
					} else {
						configFull.configPads[i].setValueById(valueId, value);					
					}
				}
			}
		}
	}
	
	private void copyPadPairValueToAllOthers() {
		int maxPair = (configFull.configGlobalMisc.inputs_count/2) - 1;
		int valueId = uiPad.getCopyPressedValueId();
		int currentPair = padPair - 1;
		int valueLeft = configFull.configPads[currentPair*2  + 1].getValueById(valueId);
		int valueRight = configFull.configPads[currentPair*2 + 2].getValueById(valueId);
		//ui
		for (int i = 0; i < maxPair; i++) {
			if (i != currentPair) {
				if ((valueId >= Constants.INPUT_VALUE_ID_POS_LEVEL) && (valueId <= Constants.INPUT_VALUE_ID_POS_HIGH)) {
					valueLeft = configFull.configPos[currentPair*2 + 1].getValueById(valueId);
					valueRight = configFull.configPos[currentPair*2 + 2].getValueById(valueId);
					configFull.configPos[i*2 + 1].setValueById(valueId, valueLeft);			
					configFull.configPos[i*2 + 2].setValueById(valueId, valueRight);			
				} else {
					configFull.configPads[i*2 + 1].setValueById(valueId, valueLeft);					
					configFull.configPads[i*2 + 2].setValueById(valueId, valueRight);					
				}
			}
		}
	}
	
	private void copy3rdZoneValueToAllOthers() {
		int max3rd = (configFull.configGlobalMisc.inputs_count/2) - 1;
		int valueId = uiPad.getCopyPressedValueId();
		int current3rd = padPair - 1;
		int value = configFull.config3rds[current3rd].getValueById(valueId);
		for (int i = 0; i < max3rd; i++) {
			if (i != current3rd) {
				configFull.config3rds[i].setValueById(valueId, value);
			}
		}
	}
	
	private void sendSysex() {
		midiController.sendSysexConfigsTaskRecreate();
		uiGlobal.getProgressBarSysex().setVisible(true);
		midiController.addSendSysexConfigsTaskSucceedEventHandler(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				// TODO Auto-generated method stub
				//System.out.println("SendSysexConfigsTask succeeded");
				uiGlobal.getProgressBarSysex().progressProperty().unbind();
				uiGlobal.getProgressBarSysex().setProgress(1.0);
				uiGlobal.getProgressBarSysex().setVisible(false);
				if (sendSysexReadOnlyRequestFlag) {
					sendSysexReadOnlyRequestFlag = false;
					sendSysexReadOnlyRequest();
				}
				if (saveToSlotAfterSendAll) {
					saveToSlotAfterSendAll = false;
					sendSysexSaveToSlotOnlyRequest(saveToSlot);
				}
			}
		});
		midiController.sendSysexConfigs(sysexSendList, uiGlobal.getProgressBarSysex(), 10, 50);		
	}
	
	private void sendSysexRequest() {
		midiController.sendSysexRequestsTaskRecreate();
		uiGlobal.getProgressBarSysex().setVisible(true);
		midiController.addSendSysexRequestsTaskSucceedEventHandler(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent event) {
				// TODO Auto-generated method stub
				//System.out.println("SendSysexRequestsTask succeeded");
				uiGlobal.getProgressBarSysex().progressProperty().unbind();
				uiGlobal.getProgressBarSysex().setProgress(1.0);
				uiGlobal.getProgressBarSysex().setVisible(false);
				if (sendNextAllSysexRequestsFlag) {
					sendNextAllSysexRequestsFlag = false;
					sendNextAllSysexRequests();
				}
				if (loadConfigAfterLoadSlot) {
					loadConfigAfterLoadSlot = false;
					sendAllSysexRequests();
				}
			}
		});
		midiController.sendSysexRequests(sysexSendList, uiGlobal.getProgressBarSysex(), 10, 50);		
	}

	private void sendSysexReadOnlyRequest() {
		sysexSendList.clear();
		byte [] typeAndId;
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_VERSION;
		sysexSendList.add(typeAndId);
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_MCU_TYPE;
		sysexSendList.add(typeAndId);
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_CONFIG_COUNT;
		sysexSendList.add(typeAndId);
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_CONFIG_CURRENT;
		sysexSendList.add(typeAndId);
		sendSysexRequest();		
	}
	
	private void sendSysexGlobalMisc() {
		sysexSendList.clear();
		byte [] sysex = new byte[Constants.MD_SYSEX_GLOBAL_MISC_SIZE];
		Utils.copyConfigGlobalMiscToSysex(configFull.configGlobalMisc, sysex, configOptions.chainId);
		sysexSendList.add(sysex);
		sendSysexReadOnlyRequestFlag = true;
		sendSysex();
	}
	
	private void controlsGlobalMiscChanged() {
		uiGlobalMisc.setConfigFromControls(configFull.configGlobalMisc);
		updateComboBoxInput(false);
		if (configOptions.liveUpdates) {
			sendSysexGlobalMisc();
		}
	}

	private void sendSysexGlobalMiscRequest() {
		sysexSendList.clear();
		byte [] typeAndId;
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_GLOBAL_MISC;
		sysexSendList.add(typeAndId);
		sendSysexRequest();
	}
	
	private void sendSysexCustomName(int id) {
		sysexSendList.clear();
		byte [] sysex = new byte[Constants.MD_SYSEX_CUSTOM_NAME_SIZE];
		Utils.copyConfigCustomNameToSysex(configFull.configCustomNames[id], sysex, configOptions.chainId, id);
		sysexSendList.add(sysex);
		sendSysex();
	}

	private void controlsCustomNameChanged(int id) {
		uiPadsExtra.getCustomName(configFull.configCustomNames[id], id);
		if (configOptions.liveUpdates) {
			sendSysexCustomName(id);
		}
	}
	
	private void sendSysexCustomNameRequest(int id) {
		sysexSendList.clear();
		byte [] typeAndId;
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_CUSTOM_NAME;
		typeAndId[1] = (byte)id;
		sysexSendList.add(typeAndId);
		sendSysexRequest();
	}

	private void sendAllCustomNamesSysex() {
		sysexSendList.clear();
		byte [] sysex;
		byte i;
		for (i = 0; i < configFull.customNamesCount; i++) {
			sysex = new byte[Constants.MD_SYSEX_CUSTOM_NAME_SIZE];	
			Utils.copyConfigCustomNameToSysex(configFull.configCustomNames[i], sysex, configOptions.chainId, i);
			sysexSendList.add(sysex);
			
		}
		sendSysex();
	}
	
	private void sendAllCustomNamesSysexRequests() {
		sysexSendList.clear();
		byte [] typeAndId;
		byte i;
		for (i = 0; i < configFull.customNamesCount; i++) {
			typeAndId = new byte[2];
			typeAndId[0] = Constants.MD_SYSEX_CUSTOM_NAME;
			typeAndId[1] = i;
			sysexSendList.add(typeAndId);
		}
		sendSysexRequest();
	}


	private void sendSysexCurve() {
		sysexSendList.clear();
		byte [] sysex = new byte[Constants.MD_SYSEX_CURVE_SIZE];
		Utils.copyConfigCurveToSysex(configFull.configCurves[curvePointer], sysex, configOptions.chainId, curvePointer);
		sysexSendList.add(sysex);
		sendSysex();
	}
	
	private void sendSysexLoadFromSlotRequest(int slot) {
		setAllStatesUnknown();
		sysexSendList.clear();
		byte [] typeAndId;
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_CONFIG_LOAD;
		typeAndId[1] = (byte)slot;
		sysexSendList.add(typeAndId);
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_GLOBAL_MISC;
		sysexSendList.add(typeAndId);
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_VERSION;
		sysexSendList.add(typeAndId);
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_MCU_TYPE;
		sysexSendList.add(typeAndId);
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_CONFIG_COUNT;
		sysexSendList.add(typeAndId);
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_CONFIG_CURRENT;
		sysexSendList.add(typeAndId);
		sendSysexRequest();
		//loadConfigAfterLoadSlot = configOptions.liveUpdates;
		loadConfigAfterLoadSlot = true;
		System.out.println("Load from slot to do");
	}
	
	private void sendSysexSaveToSlotOnlyRequest(int slot) {
		sysexSendList.clear();
		byte [] typeAndId;
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_CONFIG_SAVE;
		typeAndId[1] = (byte)slot;
		sysexSendList.add(typeAndId);
		sendSysexRequest();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						sysexSendList.clear();
						byte [] typeAndIdFinal = new byte[2];
						typeAndIdFinal[0] = Constants.MD_SYSEX_CONFIG_CURRENT;
						sysexSendList.add(typeAndIdFinal);
						sendSysexRequest();				
					}
				});
			}
		}, 2000);
		System.out.println("Save slot to do");
	}
	
	private void sendSysexConfigName(int id) {
		byte [] sysex = new byte[Constants.MD_SYSEX_CONFIG_NAME_SIZE];
		Utils.copyConfigConfigNameToSysex(configFull.configConfigNames[id], sysex, configOptions.chainId, id);
		System.out.printf("Config name to save in slot = %s\n", configFull.configConfigNames[id].name);
		sysexSendList.clear();
		sysexSendList.add(sysex);
		sendSysex();
	}

	private void sendSysexSaveToSlotRequest(int slot) {
		System.out.printf("Saving to slot %d\n", slot + 1);
		configFull.configConfigNames[slot].name = (uiGlobalMisc.getTextFieldSlotName().getText() + "            ").substring(0, 12);
		sendSysexConfigName(slot);
		//Utils.copyConfigConfigNameToSysex(null, null, i, i);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Platform.runLater(new Runnable() {
					
					@Override
					public void run() {
						saveToSlot = slot;
						saveToSlotAfterSendAll = true;
						sendAllSysex();
					}
				});
			}
		}, 200);
	}
	
	private void controlsCurveChanged() {
		uiPadsExtra.getYvalues(configFull.configCurves[curvePointer].yValues);
		if (configOptions.liveUpdates) {
			sendSysexCurve();
		}
	}

	private void sendSysexCurveRequest() {
		sysexSendList.clear();
		byte [] typeAndId;
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_CURVE;
		typeAndId[1] = (byte)curvePointer;
		sysexSendList.add(typeAndId);
		sendSysexRequest();
	}

	private void sendAllCurvesSysex() {
		sysexSendList.clear();
		byte [] sysex;
		byte i;
		for (i = 0; i < Constants.CURVES_COUNT; i++) {
			sysex = new byte[Constants.MD_SYSEX_CURVE_SIZE];	
			Utils.copyConfigCurveToSysex(configFull.configCurves[i], sysex, configOptions.chainId, i);
			sysexSendList.add(sysex);
			
		}
		sendSysex();
	}
	
	private void sendAllCurvesSysexRequests() {
		sysexSendList.clear();
		byte [] typeAndId;
		byte i;
		for (i = 0; i < Constants.CURVES_COUNT; i++) {
			typeAndId = new byte[2];
			typeAndId[0] = Constants.MD_SYSEX_CURVE;
			typeAndId[1] = i;
			sysexSendList.add(typeAndId);
		}
		sendSysexRequest();
	}

	private void sendSysexMisc() {
		sysexSendList.clear();
		byte [] sysex = new byte[Constants.MD_SYSEX_MISC_SIZE];
		Utils.copyConfigMiscToSysex(configFull.configMisc, sysex, configOptions.chainId);
		sysexSendList.add(sysex);
		sendSysex();
	}

	private void controlsMiscChanged() {
		uiMisc.setConfigFromControls(configFull.configMisc);
		if (configOptions.liveUpdates) {
			sendSysexMisc();
		}
	}
	
	private void sendSysexMiscRequest() {
		sysexSendList.clear();
		byte [] typeAndId;
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_MISC;
		sysexSendList.add(typeAndId);
		sendSysexRequest();
	}

	private void sendSysexPedal() {
		sysexSendList.clear();
		byte [] sysex = new byte[Constants.MD_SYSEX_PEDAL_SIZE];
		Utils.copyConfigPedalToSysex(configFull.configPedal, sysex, configOptions.chainId);
		sysexSendList.add(sysex);
		sendSysex();
	}

	private void controlsPedalChanged() {
		uiPedal.setConfigFromControls(configFull.configPedal);
		if (configOptions.liveUpdates) {
			sendSysexPedal();
		}
	}
	
	private void sendSysexPedalRequest() {
		sysexSendList.clear();
		byte [] typeAndId;
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_PEDAL;
		sysexSendList.add(typeAndId);
		sendSysexRequest();
	}
	
	private void sendSysexInput(Integer input, Boolean leftInput) {
		sysexSendList.clear();
		byte [] sysex = new byte[Constants.MD_SYSEX_PAD_SIZE];	
		Utils.copyConfigPadToSysex(configFull.configPads[input], sysex, configOptions.chainId, input);
		sysexSendList.add(sysex);
		
		sysex = new byte[Constants.MD_SYSEX_POS_SIZE];	
		Utils.copyConfigPosToSysex(configFull.configPos[input], sysex, configOptions.chainId, input);
		sysexSendList.add(sysex);
		sendSysex();
	}

	private void sendSysexPair(Integer pair) {
		sysexSendList.clear();
		byte [] sysex = new byte[Constants.MD_SYSEX_PAD_SIZE];	
		Utils.copyConfigPadToSysex(configFull.configPads[(pair*2) - 1], sysex, configOptions.chainId, (pair*2) - 1);
		sysexSendList.add(sysex);
		
		sysex = new byte[Constants.MD_SYSEX_PAD_SIZE];	
		Utils.copyConfigPadToSysex(configFull.configPads[(pair*2)], sysex, configOptions.chainId, (pair*2));
		sysexSendList.add(sysex);

		sysex = new byte[Constants.MD_SYSEX_POS_SIZE];	
		Utils.copyConfigPosToSysex(configFull.configPos[(pair*2) - 1], sysex, configOptions.chainId, (pair*2) - 1);
		sysexSendList.add(sysex);
		
		sysex = new byte[Constants.MD_SYSEX_POS_SIZE];	
		Utils.copyConfigPosToSysex(configFull.configPos[(pair*2)], sysex, configOptions.chainId, (pair*2));
		sysexSendList.add(sysex);

		sysex = new byte[Constants.MD_SYSEX_3RD_SIZE];
		Utils.copyConfig3rdToSysex(configFull.config3rds[pair - 1], sysex, configOptions.chainId, pair - 1);
		sysexSendList.add(sysex);

		sendSysex();
	}

	private void controlsInputChanged(Integer input, Boolean leftInput) {
		uiPad.setConfigFromControlsPad(configFull.configPads[input], leftInput);
		// Needs implementation for Positional on Atmega644 and Atmega1284
		uiPad.setConfigPosFromControlsPad(configFull.configPos[input], leftInput);
		if (configOptions.liveUpdates) {
			sendSysexInput(input, leftInput);
		}
	}
	
	private void sendSysex3rd(Integer pair) {
		byte [] sysex = new byte[Constants.MD_SYSEX_3RD_SIZE];
		Utils.copyConfig3rdToSysex(configFull.config3rds[pair - 1], sysex, configOptions.chainId, pair - 1);
		sysexSendList.clear();
		sysexSendList.add(sysex);
		sendSysex();
	}
	
	private void controls3rdChanged(Integer pair) {
		uiPad.setConfig3rdFromControlsPad(configFull.config3rds[pair - 1]);
		if (configOptions.liveUpdates) {
			sendSysex3rd(pair);
		}
	}
	
	private void sendSysexInputRequest(Integer input) {
		sysexSendList.clear();
		byte [] typeAndId;
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_PAD;
		typeAndId[1] = input.byteValue();
		sysexSendList.add(typeAndId);
		
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_POS;
		typeAndId[1] = input.byteValue();
		sysexSendList.add(typeAndId);
		sendSysexRequest();
	}

	private void sendSysexPairRequest(Integer pair) {
		sysexSendList.clear();
		byte [] typeAndId;
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_PAD;
		typeAndId[1] = (byte)((pair*2) - 1);
		sysexSendList.add(typeAndId);

		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_PAD;
		typeAndId[1] = (byte)(pair*2);
		sysexSendList.add(typeAndId);

		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_POS;
		typeAndId[1] = (byte)((pair*2) - 1);
		sysexSendList.add(typeAndId);

		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_POS;
		typeAndId[1] = (byte)(pair*2);
		sysexSendList.add(typeAndId);

		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_3RD;
		typeAndId[1] = (byte)(pair - 1);
		sysexSendList.add(typeAndId);
		sendSysexRequest();
	}

	private void sendAllInputsSysex() {
		sysexSendList.clear();
		byte [] sysex;
		byte i;
		for (i = 0; i < (configFull.configGlobalMisc.inputs_count - 1); i++) {
			sysex = new byte[Constants.MD_SYSEX_PAD_SIZE];	
			Utils.copyConfigPadToSysex(configFull.configPads[i], sysex, configOptions.chainId, i);
			sysexSendList.add(sysex);
			
			sysex = new byte[Constants.MD_SYSEX_POS_SIZE];	
			Utils.copyConfigPosToSysex(configFull.configPos[i], sysex, configOptions.chainId, i);
			sysexSendList.add(sysex);

			if ((i&1) > 0) {
				sysex = new byte[Constants.MD_SYSEX_3RD_SIZE];
				Utils.copyConfig3rdToSysex(configFull.config3rds[(i-1)/2], sysex, configOptions.chainId, ((i-1)/2));
				sysexSendList.add(sysex);
			}
		}
		sendSysex();
	}
	
	private void sendAllInputsSysexRequests() {
		sysexSendList.clear();
		byte [] typeAndId;
		byte i;
		for (i = 0; i < (configFull.configGlobalMisc.inputs_count - 1); i++) {
			typeAndId = new byte[2];
			typeAndId[0] = Constants.MD_SYSEX_PAD;
			typeAndId[1] = i;
			sysexSendList.add(typeAndId);
			typeAndId = new byte[2];
			typeAndId[0] = Constants.MD_SYSEX_POS;
			typeAndId[1] = i;
			sysexSendList.add(typeAndId);
			if ((i&1) > 0) {
				typeAndId = new byte[2];
				typeAndId[0] = Constants.MD_SYSEX_3RD;
				typeAndId[1] = (byte)((i-1)/2);
				sysexSendList.add(typeAndId);
				
			}
		}
		sendSysexRequest();
	}
	
	private void sendAllSysex() {
		sysexSendList.clear();
		byte [] sysex;
		byte i;
		sysex = new byte[Constants.MD_SYSEX_GLOBAL_MISC_SIZE];
		Utils.copyConfigGlobalMiscToSysex(configFull.configGlobalMisc, sysex, configOptions.chainId);
		sysexSendList.add(sysex);
		sysex = new byte[Constants.MD_SYSEX_MISC_SIZE];
		Utils.copyConfigMiscToSysex(configFull.configMisc, sysex, configOptions.chainId);
		sysexSendList.add(sysex);		
		sysex = new byte[Constants.MD_SYSEX_PEDAL_SIZE];
		Utils.copyConfigPedalToSysex(configFull.configPedal, sysex, configOptions.chainId);
		sysexSendList.add(sysex);
		for (i = 0; i < (configFull.configGlobalMisc.inputs_count - 1); i++) {
			sysex = new byte[Constants.MD_SYSEX_PAD_SIZE];	
			Utils.copyConfigPadToSysex(configFull.configPads[i], sysex, configOptions.chainId, i);
			sysexSendList.add(sysex);
			
			sysex = new byte[Constants.MD_SYSEX_POS_SIZE];	
			Utils.copyConfigPosToSysex(configFull.configPos[i], sysex, configOptions.chainId, i);
			sysexSendList.add(sysex);

			if ((i&1) > 0) {
				sysex = new byte[Constants.MD_SYSEX_3RD_SIZE];
				Utils.copyConfig3rdToSysex(configFull.config3rds[(i-1)/2], sysex, configOptions.chainId, ((i-1)/2));
				sysexSendList.add(sysex);
			}
		}
		for (i = 0; i < Constants.CURVES_COUNT; i++) {
			sysex = new byte[Constants.MD_SYSEX_CURVE_SIZE];	
			Utils.copyConfigCurveToSysex(configFull.configCurves[i], sysex, configOptions.chainId, i);
			sysexSendList.add(sysex);
			
		}
		for (i = 0; i < configFull.customNamesCount; i++) {
			sysex = new byte[Constants.MD_SYSEX_CUSTOM_NAME_SIZE];	
			Utils.copyConfigCustomNameToSysex(configFull.configCustomNames[i], sysex, configOptions.chainId, i);
			sysexSendList.add(sysex);
			
		}
		for (i = 0; i < configFull.configNamesCount; i++) {
			sysex = new byte[Constants.MD_SYSEX_CONFIG_NAME_SIZE];	
			Utils.copyConfigConfigNameToSysex(configFull.configConfigNames[i], sysex, configOptions.chainId, i);
			sysexSendList.add(sysex);
		}
		sendSysexReadOnlyRequestFlag = true;
		sendSysex();
	}
	
	private void sendAllSysexRequests() {
		byte [] typeAndId;
		byte i;
		sysexSendList.clear();
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_GLOBAL_MISC;
		sysexSendList.add(typeAndId);
		sendNextAllSysexRequestsFlag = true;
		sendSysexRequest();
	}
	
	private void sendNextAllSysexRequests() {
		byte [] typeAndId;
		byte i;
		sysexSendList.clear();
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_MISC;
		sysexSendList.add(typeAndId);
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_PEDAL;
		sysexSendList.add(typeAndId);
		for (i = 0; i < (configFull.configGlobalMisc.inputs_count - 1); i++) {
			typeAndId = new byte[2];
			typeAndId[0] = Constants.MD_SYSEX_PAD;
			typeAndId[1] = i;
			sysexSendList.add(typeAndId);
			typeAndId = new byte[2];
			typeAndId[0] = Constants.MD_SYSEX_POS;
			typeAndId[1] = i;
			sysexSendList.add(typeAndId);
			if ((i&1) > 0) {
				typeAndId = new byte[2];
				typeAndId[0] = Constants.MD_SYSEX_3RD;
				typeAndId[1] = (byte)((i-1)/2);
				sysexSendList.add(typeAndId);				
			}
		}
		for (i = 0; i < Constants.CURVES_COUNT; i++) {
			typeAndId = new byte[2];
			typeAndId[0] = Constants.MD_SYSEX_CURVE;
			typeAndId[1] = i;
			sysexSendList.add(typeAndId);
		}
		for (i = 0; i < configFull.customNamesCount; i++) {
			typeAndId = new byte[2];
			typeAndId[0] = Constants.MD_SYSEX_CUSTOM_NAME;
			typeAndId[1] = i;
			sysexSendList.add(typeAndId);
		}
		if (configFull.configGlobalMisc.config_names_en) {
			for (i = 0; i < configFull.configNamesCount; i++) {
				typeAndId = new byte[2];
				typeAndId[0] = Constants.MD_SYSEX_CONFIG_NAME;
				typeAndId[1] = i;
				sysexSendList.add(typeAndId);
			}
		}
		typeAndId = new byte[2];
		typeAndId[0] = Constants.MD_SYSEX_CONFIG_CURRENT;
		sysexSendList.add(typeAndId);
		sendSysexRequest();
	}
	
	private void showOptionsWindow() {
		optionsUpdatePorts();
		optionsWindow.show();
		if (optionsWindow.getClosedWithOk()) {
			System.out.println("Closed with ok");
			openMidiPorts(true);
		}
	}
	
	private void setAllStatesUnknown() {
		uiGlobalMisc.setAllStatesUnknown();
		uiMisc.setAllStateUnknown();
		uiPedal.setAllStateUnknown();
		for (int i = 0; i < Constants.MAX_INPUTS; i++ ) {
			moduleConfigFull.configPads[i].sysexReceived = false;
			moduleConfigFull.configPos[i].sysexReceived = false;
			if ((i&1) > 0) {
				moduleConfigFull.config3rds[(i-1)/2].sysexReceived = false;
			}
		}
		uiPad.setAllStatesUnknown(false, false, false);
		uiPadsExtra.setCurveSysexReceived(false);
		uiPadsExtra.testCurveSyncState();
		for (int i = 0; i < Constants.CURVES_COUNT; i++) {
			moduleConfigFull.configCurves[i].sysexReceived = false;
		}
		uiPadsExtra.setAllCustomNamesStatesUnknown();
		
	}
	private void openMidiPorts(Boolean toOpen) {
		if (toOpen) {
			if (midiController.isMidiOpen()) {
				midiController.closeAllPorts();
			}
			if (configOptions.useThruPort) {
				midiController.openMidi(configOptions.MidiInName, configOptions.MidiOutName, configOptions.MidiThruName);				
			} else {
				midiController.openMidi(configOptions.MidiInName, configOptions.MidiOutName, "");
			}
		} else {
			midiController.closeAllPorts();
		}
		if (midiController.isMidiOpen()) {
			uiGlobalMisc.getToggleButtonMidi().setSelected(true);
			uiGlobalMisc.getToggleButtonMidi().setText("Close MIDI");
			sendSysexReadOnlyRequest();
		} else {
			uiGlobalMisc.getToggleButtonMidi().setSelected(false);
			uiGlobalMisc.getToggleButtonMidi().setText("Open MIDI");
			uiGlobalMisc.setAllStatesUnknown();
			setAllStatesUnknown();
		}
	}
	
	private void initMidi() {
		midiController = new MidiController();
		sysexSendList = new ArrayList<>();
		midiController.addMidiEventListener(new MidiEventListener() {
			@Override
			public void midiEventOccurred(MidiEvent evt) {
				// TODO Auto-generated method stub
/*				if (!upgradeDialog.isVisible()) {
					sendSysexEnabled = false;
					midiController.getMidi();
					if (midiController.sysexReceived) {
						midiController.sysexReceived = false;
						if (compareSysexToConfigIsOn) {
							compareSysexToConfig(midiController.bufferIn);
						} else {
							decodeSysex(midiController.bufferIn);						
						}
					} else if (midiController.bufferIn != null) {
						decodeShortMidi(midiController.bufferIn);
					}
					midiController.bufferIn = null;
				}
*/				
			}

			@Override
			public void midiEventOccurredWithBuffer(MidiEvent evt, byte[] buffer) {
				// TODO Auto-generated method stub
				//System.out.println("Received MidiEvent with buffer");
				processSysex(buffer);
			}
		});		
	}
	
	private void processSysex(byte [] sysex) {
		if (sysex.length >= 5) {
			//System.out.printf("Sysex received type = %d\n", sysex[3]);			
			byte pointer = sysex[4];
	    	switch (sysex[3]) {
			case Constants.MD_SYSEX_3RD:
				//System.out.printf("Sysex 3rd pointer = %d\n", pointer);
				if (pointer < configFull.config3rds.length) {
					Utils.copySysexToConfig3rd(sysex, configFull.config3rds[pointer]);
					Utils.copySysexToConfig3rd(sysex, moduleConfigFull.config3rds[pointer]);
					moduleConfigFull.config3rds[pointer].syncState = Constants.SYNC_STATE_RECEIVED;
					moduleConfigFull.config3rds[pointer].sysexReceived = true;
					if ((pointer + 1) == padPair) {
						uiPad.setControlsFromConfig3rd(configFull.config3rds[pointer], true);					
					}
				}
				break;
			case Constants.MD_SYSEX_CONFIG_COUNT:
				if (sysex.length >= Constants.MD_SYSEX_CONFIG_COUNT_SIZE) {
					int b;
					b = (int)sysex[4];
					uiGlobalMisc.setConfigsCount(b);
					configFull.configNamesCount = b;
					configFull.configCountSysexReceived = true;
					reCreateSlotsMenuItems();
					
					//TODO
/*
					setSysexOk();
					popupMenuSaveToSlot.removeAll();
					mntmSaveToMd.removeAll();
					popupMenuLoadFromSlot.removeAll();
					mntmLoadFromMd.removeAll();
					for (int i = 0; i < b; i++) {
						popupMenuSaveToSlot.add(popupMenuItemsSaveToSlot[i]);
						mntmSaveToMd.add(menuItemsSaveToSlot[i]);
						popupMenuLoadFromSlot.add(popupMenuItemsLoadFromSlot[i]);
						mntmLoadFromMd.add(menuItemsLoadFromSlot[i]);
					}
*/
				}
				break;
			case Constants.MD_SYSEX_CONFIG_CURRENT:
				if (sysex.length >= Constants.MD_SYSEX_CONFIG_CURRENT_SIZE) {
					int b;
					b = (int)sysex[4];
					uiGlobalMisc.setConfigCurrent(b);
					configFull.configCurrentSysexReceived = true;
					uiGlobalMisc.getTextFieldSlotName().setText(configFull.configConfigNames[pointer].name.trim());
					//TODO
					//setSysexOk();
				}
				break;
			case Constants.MD_SYSEX_CONFIG_NAME:
				Utils.copySysexToConfigConfigName(sysex, configFull.configConfigNames[pointer]);
				Utils.copySysexToConfigConfigName(sysex, moduleConfigFull.configConfigNames[pointer]);
				configFull.configConfigNames[pointer].sysexReceived = true;
			    //System.out.printf("sysexReceived for ConfigName id %d set to true\n", buffer[4]);
				reCreateSlotsMenuItems();
				if (configFull.configCurrent == pointer) {
					//uiGlobalMisc.geTextFieldSlotName().setText(configFull.configConfigNames[pointer].name.trim());
				}
				break;
			case Constants.MD_SYSEX_CURVE:
				Utils.copySysexToConfigCurve(sysex, configFull.configCurves[pointer]);
				Utils.copySysexToConfigCurve(sysex, moduleConfigFull.configCurves[pointer]);
				moduleConfigFull.configCurves[pointer].syncState = Constants.SYNC_STATE_RECEIVED;
				moduleConfigFull.configCurves[pointer].sysexReceived = true;
				if (pointer == curvePointer) {
					uiPadsExtra.setYvalues(configFull.configCurves[pointer].yValues, true);					
				}
				break;
			case Constants.MD_SYSEX_CUSTOM_NAME:
				Utils.copySysexToConfigCustomName(sysex, configFull.configCustomNames[pointer]);
				Utils.copySysexToConfigCustomName(sysex, moduleConfigFull.configCustomNames[pointer]);
				moduleConfigFull.configCustomNames[pointer].syncState = Constants.SYNC_STATE_RECEIVED;
				moduleConfigFull.configCustomNames[pointer].sysexReceived = true;
				uiPadsExtra.setCustomName(configFull.configCustomNames[pointer], pointer, true);
				break;
			case Constants.MD_SYSEX_GLOBAL_MISC:
				Utils.copySysexToConfigGlobalMisc(sysex, configFull.configGlobalMisc);
				Utils.copySysexToConfigGlobalMisc(sysex, moduleConfigFull.configGlobalMisc);
				moduleConfigFull.configGlobalMisc.syncState = Constants.SYNC_STATE_RECEIVED;
				moduleConfigFull.configGlobalMisc.sysexReceived = true;
				uiGlobalMisc.setControlsFromConfig(configFull.configGlobalMisc, true);
				updateComboBoxInput(false);
				break;
			case Constants.MD_SYSEX_MCU_TYPE:
				if (sysex.length >= Constants.MD_SYSEX_MCU_TYPE_SIZE) {
					configOptions.mcuType = (int)(sysex[4]<<4);
					configOptions.mcuType |= (int)sysex[5];
					if (configOptions.mcuType < Constants.MCU_TYPES.length ) {
						uiGlobalMisc.setMcu(configOptions.mcuType);								
					}
					//TODO
					//setSysexOk();
				}
				break;
			case Constants.MD_SYSEX_MISC:
				Utils.copySysexToConfigMisc(sysex, configFull.configMisc);
				Utils.copySysexToConfigMisc(sysex, moduleConfigFull.configMisc);
				moduleConfigFull.configMisc.syncState = Constants.SYNC_STATE_RECEIVED;
				moduleConfigFull.configMisc.sysexReceived = true;
				uiMisc.setControlsFromConfig(configFull.configMisc, true);
				break;
			case Constants.MD_SYSEX_PAD:
				//System.out.printf("Sysex received wiht pad pointer = %d\n", pointer);
				Utils.copySysexToConfigPad(sysex, configFull.configPads[pointer - 1]);
				Utils.copySysexToConfigPad(sysex, moduleConfigFull.configPads[pointer - 1]);
				moduleConfigFull.configPads[pointer - 1].syncState = Constants.SYNC_STATE_RECEIVED;
				moduleConfigFull.configPads[pointer - 1].sysexReceived = true;
				if ((pointer - 1) == 0) {
					if (padPair == 0) {
						uiPad.setControlsFromConfigPad(configFull.configPads[pointer - 1], true, true);					
					}
				} else {
					if ((((pointer - 2)/2) + 1) == padPair) {
						uiPad.setControlsFromConfigPad(configFull.configPads[pointer - 1], (pointer&1) == 0, true);
					}
				}
				break;
			case Constants.MD_SYSEX_PEDAL:
				Utils.copySysexToConfigPedal(sysex, configFull.configPedal);
				Utils.copySysexToConfigPedal(sysex, moduleConfigFull.configPedal);
				moduleConfigFull.configPedal.syncState = Constants.SYNC_STATE_RECEIVED;
				moduleConfigFull.configPedal.sysexReceived = true;
				uiPedal.setControlsFromConfig(configFull.configPedal, true);
				break;
			case Constants.MD_SYSEX_POS:
				//System.out.printf("Sysex pos pointer = %d\n", pointer);
				Utils.copySysexToConfigPos(sysex, configFull.configPos[pointer]);
				Utils.copySysexToConfigPos(sysex, moduleConfigFull.configPos[pointer]);
				moduleConfigFull.configPos[pointer].syncState = Constants.SYNC_STATE_RECEIVED;
				moduleConfigFull.configPos[pointer].sysexReceived = true;
				if (pointer == 0) {
					if (padPair == 0) {
						uiPad.setControlsFromConfigPos(configFull.configPos[pointer], true, true);					
					}
				} else {
					if ((((pointer - 1)/2) + 1) == padPair) {
						uiPad.setControlsFromConfigPos(configFull.configPos[pointer], ((pointer+1)&1) > 0, true);
					}
				}
				break;
			case Constants.MD_SYSEX_VERSION:
				int ver = 0;
				if (sysex.length >= Constants.MD_SYSEX_VERSION_SIZE) {
					int b;
					for (int i=0;i<4;i++) {
						b = (int)(sysex[i*2 + 4]<<4);
						b |= (int)sysex[i*2 + 5];
						ver += b<<(8*i);
					}
					configOptions.version = ver;
					uiGlobalMisc.setVersion(ver);
					//TODO
/*
					setSysexOk();
					if (ver < Constants.MD_MINIMUM_VERSION) {
						if (!versionWarningAlreadyShown) {
							versionWarningAlreadyShown = true;
							lblVersion.setBackground(Color.RED);
							Timer warning_timer = new Timer();
							warning_timer.schedule(new TimerTask() {
								
								@Override
								public void run() {
									JOptionPane.showMessageDialog(null,
										    "<html><font size=5>"+Constants.WARNING_VERSION+"</font></html>",
										    "Warning",
										    JOptionPane.WARNING_MESSAGE);
								}
							}, 200);
						}
					} else {
						lblVersion.setBackground(Color.GREEN);
					}
*/
				}
				break;
			default:
				break;
			}			
		}
	}
	private void initConfigs() {
		fullConfigs = new ConfigFull[Constants.CONFIGS_COUNT];
		configFileNames = new String[Constants.CONFIGS_COUNT];
		for (Integer i = 0;i < Constants.CONFIGS_COUNT;i++) {
			fullConfigs[i] = new ConfigFull();
			configFileNames[i] = new String();
		}
		configOptions = new ConfigOptions();
		configFull = new ConfigFull(); 
		moduleConfigFull = new ConfigFull();
	}

	private void optionsUpdatePorts() {
		optionsWindow.setMidiInList(Arrays.asList(midiController.getMidiInList()));
		optionsWindow.setMidiOutList(Arrays.asList(midiController.getMidiOutList()));
		optionsWindow.setMidiThruList(Arrays.asList(midiController.getMidiOutList()));
		optionsWindow.updateControls();
	}
	
	@Override
	public void midiRescanEventOccurred(MidiRescanEvent evt) {
		// TODO Auto-generated method stub
		System.out.println("Midi Rescan Event occured");
		optionsUpdatePorts();
		
	}

	private String getInputName(int input) {
		String result = Integer.valueOf(input + 1).toString() + " ";
		int totalCustomNames = Constants.CUSTOM_PADS_NAMES_LIST.length;
		int namePointer;
		namePointer = configFull.configPads[input].name;
		if (namePointer == 0) {
			result = result + Constants.PADS_NAMES_LIST[input];
		} else {
			if (namePointer < (totalCustomNames + 1 )) {
				result = result + (Constants.CUSTOM_PADS_NAMES_LIST[namePointer - 1]);
			} else {
				result = result + configFull.configCustomNames[namePointer - totalCustomNames + 1];
			}
			if ((input & 1) > 0) {
				result += "h";
			} else {
				result += "r";
			}
		}
		return result;
	}
	
	private void updateComboBoxInput(Boolean nameChaned) {
		if ((oldInputsCounts != configFull.configGlobalMisc.inputs_count) || nameChaned ) {
			oldInputsCounts = configFull.configGlobalMisc.inputs_count;
			List<String> list;
			String name;
			list = new ArrayList<>();
			int inputPointer;
			for (int i = 0; i < ((configFull.configGlobalMisc.inputs_count/2)); i++) {
				if (i == 0) {
					list.add(getInputName(i));
				} else {
					inputPointer = (i*2) - 1;
					name = getInputName(inputPointer);
					name = name + "/";
					inputPointer++;
					name = name + getInputName(inputPointer);
					list.add(name);
				}
			}
			//comboBoxInputChangedFromSet = 1;
			uiPad.getComboBoxInput().getItems().clear();
			uiPad.getComboBoxInput().getItems().addAll(list);
			if ((configFull.configGlobalMisc.inputs_count)/2 > padPair) {
				uiPad.getComboBoxInput().getSelectionModel().select(padPair);				
			} else {
				switchToSelectedPair(0);
			}
		}
	}
	
	private void switchToSelectedPair(Integer newPadPair) {
		if (padPair != newPadPair) {
			if (padPair == 0) {
				uiPad.setConfigFromControlsPad(configFull.configPads[0], true);			
				uiPad.setConfigPosFromControlsPad(configFull.configPos[0], true);			
			} else {
				uiPad.setConfigFromControlsPad(configFull.configPads[((padPair-1)*2) + 1], true);
				uiPad.setConfigPosFromControlsPad(configFull.configPos[((padPair-1)*2) + 1], true);
				uiPad.setConfigFromControlsPad(configFull.configPads[((padPair-1)*2) + 2], false);
				uiPad.setConfigPosFromControlsPad(configFull.configPos[((padPair-1)*2) + 2], false);
				uiPad.setConfig3rdFromControlsPad(configFull.config3rds[padPair - 1]);
			}		
			padPair = newPadPair;
		}
		if (padPair == 0) {
			uiPad.setAllStatesUnknown(moduleConfigFull.configPads[0].sysexReceived, false, false);
			if (moduleConfigFull.configPads[0].sysexReceived) {
				uiPad.setMdValuesPad(moduleConfigFull.configPads[0], configFull.configPos[0], true);
			}
			uiPad.setInputPair(padPair, configFull.configPads[0], configFull.configPos[0], null, null, null);
		} else {
			uiPad.setAllStatesUnknown(moduleConfigFull.configPads[((padPair-1)*2) + 1].sysexReceived, moduleConfigFull.configPads[((padPair-1)*2) + 2].sysexReceived, moduleConfigFull.config3rds[padPair - 1].sysexReceived);
			if (moduleConfigFull.configPads[((padPair-1)*2) + 1].sysexReceived) {
				uiPad.setMdValuesPad(moduleConfigFull.configPads[((padPair-1)*2) + 1], configFull.configPos[((padPair-1)*2) + 1], true);				
			}
			if (moduleConfigFull.configPads[((padPair-1)*2) + 2].sysexReceived) {
				uiPad.setMdValuesPad(moduleConfigFull.configPads[((padPair-1)*2) + 2], configFull.configPos[((padPair-1)*2) + 2], false);				
			}
			if (moduleConfigFull.config3rds[padPair - 1].sysexReceived) {
				uiPad.setMdValues3rd(moduleConfigFull.config3rds[padPair - 1]);
			}
			uiPad.setInputPair(padPair, configFull.configPads[((padPair-1)*2) + 1], configFull.configPos[((padPair-1)*2) + 1], configFull.configPads[((padPair-1)*2) + 2], configFull.configPos[((padPair-1)*2) + 2], configFull.config3rds[padPair - 1]);
		}
		//comboBoxInputChangedFromSet = 1;
		uiPad.getComboBoxInput().getSelectionModel().select(padPair);
	}
	
	private void switchToSelectedCurve(Integer curve) {
		if ((curve > -1) && (curve < Constants.CURVES_COUNT)) {
			//uiPadsExtra.getYvalues(configFull.configCurves[curvePointer].yValues);
			int [] t = configFull.configCurves[curvePointer].yValues;
			//System.out.printf("Y values at %d: %d %d %d %d %d %d %d %d %d\n", curvePointer, t[0],t[1],t[2],t[3],t[4],t[5],t[6],t[7],t[8] );
			curvePointer = curve;
			if (moduleConfigFull.configCurves[curvePointer].sysexReceived) {
				uiPadsExtra.setMdYvalues(moduleConfigFull.configCurves[curvePointer].yValues);
				uiPadsExtra.setCurveSysexReceived(true);
			} else {
				uiPadsExtra.setCurveSysexReceived(false);				
			}
			uiPadsExtra.getCurvesComboBox().getSelectionModel().select(curvePointer);
			uiPadsExtra.setYvalues(configFull.configCurves[curvePointer].yValues, false);
			uiPadsExtra.testCurveSyncState();
		}
	}

	private void loadAllFromConfigFull() {
		byte [] sysex = new byte[256];

		Utils.copyConfigGlobalMiscToSysex(fullConfigs[configOptions.lastConfig].configGlobalMisc, sysex, configOptions.chainId);
		Utils.copySysexToConfigGlobalMisc(sysex, configFull.configGlobalMisc);

		Utils.copyConfigMiscToSysex(fullConfigs[configOptions.lastConfig].configMisc, sysex, configOptions.chainId);
		Utils.copySysexToConfigMisc(sysex, configFull.configMisc);

		Utils.copyConfigPedalToSysex(fullConfigs[configOptions.lastConfig].configPedal, sysex, configOptions.chainId);
		Utils.copySysexToConfigPedal(sysex, configFull.configPedal);		

		for (int i=0; i < (Constants.MAX_INPUTS - 1); i++) {
			Utils.copyConfigPadToSysex(fullConfigs[configOptions.lastConfig].configPads[i], sysex, configOptions.chainId, i);
			Utils.copySysexToConfigPad(sysex, configFull.configPads[i]);
			configFull.configPads[i].altNote_linked = fullConfigs[configOptions.lastConfig].configPads[i].altNote_linked;
			configFull.configPads[i].pressrollNote_linked = fullConfigs[configOptions.lastConfig].configPads[i].pressrollNote_linked;
			Utils.copyConfigPosToSysex(fullConfigs[configOptions.lastConfig].configPos[i], sysex, configOptions.chainId, i);
			Utils.copySysexToConfigPos(sysex, configFull.configPos[i]);
		}
		for (int i=0; i < ((Constants.MAX_INPUTS/2) - 1); i++) {
			Utils.copyConfig3rdToSysex(fullConfigs[configOptions.lastConfig].config3rds[i], sysex, configOptions.chainId, i);
			Utils.copySysexToConfig3rd(sysex, configFull.config3rds[i]);
			configFull.config3rds[i].altNote_linked = fullConfigs[configOptions.lastConfig].config3rds[i].altNote_linked;
			configFull.config3rds[i].pressrollNote_linked = fullConfigs[configOptions.lastConfig].config3rds[i].pressrollNote_linked;
		}				
		
		for (int i=0; i < (Constants.CURVES_COUNT); i++) {
			Utils.copyConfigCurveToSysex(fullConfigs[configOptions.lastConfig].configCurves[i], sysex, configOptions.chainId, i);
			Utils.copySysexToConfigCurve(sysex, configFull.configCurves[i]);					
		}
		for (int i=0; i < (Constants.CUSTOM_NAMES_MAX); i++) {
			Utils.copyConfigCustomNameToSysex(fullConfigs[configOptions.lastConfig].configCustomNames[i], sysex, configOptions.chainId, i);
			Utils.copySysexToConfigCustomName(sysex, configFull.configCustomNames[i]);					
		}
		switchToSelectedPair(padPair);
	}
	
	private void load_all() {
		fileManager.load_all(fullConfigs[configOptions.lastConfig], configOptions);
		loadAllFromConfigFull();
	}

	private void loadSysexMisc() {
		byte [] sysex = new byte[Constants.MD_SYSEX_MISC_SIZE];
		Utils.copyConfigMiscToSysex(configFull.configMisc, sysex, configOptions.chainId);
		fileManager.loadSysex(sysex, configOptions);
		Utils.copySysexToConfigMisc(sysex, configFull.configMisc);
		uiMisc.setControlsFromConfig(configFull.configMisc, false);		
	}
	
	private void saveSysexMisc() {
		byte [] sysex = new byte[Constants.MD_SYSEX_MISC_SIZE];
		Utils.copyConfigMiscToSysex(configFull.configMisc, sysex, configOptions.chainId);
		fileManager.saveSysex(sysex, configOptions);		
	}
	
	private void loadSysexPedal() {
		byte [] sysex = new byte[Constants.MD_SYSEX_PEDAL_SIZE];
		Utils.copyConfigPedalToSysex(configFull.configPedal, sysex, configOptions.chainId);
		fileManager.loadSysex(sysex, configOptions);
		Utils.copySysexToConfigPedal(sysex, configFull.configPedal);
		uiPedal.setControlsFromConfig(configFull.configPedal, false);		
	}
	
	private void saveSysexPedal() {
		byte [] sysex = new byte[Constants.MD_SYSEX_PEDAL_SIZE];
		Utils.copyConfigPedalToSysex(configFull.configPedal, sysex, configOptions.chainId);
		fileManager.saveSysex(sysex, configOptions);		
	}
	
	private void loadSysexPad() {
		byte [] sysex = new byte[Constants.MD_SYSEX_PAD_SIZE];
		byte [] sysex3rd = new byte[Constants.MD_SYSEX_3RD_SIZE];
		byte [] sysexPos = new byte[Constants.MD_SYSEX_POS_SIZE];
		if (padPair > 0 ) {
			int leftInput = (padPair - 1)*2 + 1;
			int rightInput = leftInput + 1;
			byte [] sysexPad = new byte[Constants.MD_SYSEX_PAD_SIZE*2 + Constants.MD_SYSEX_3RD_SIZE + Constants.MD_SYSEX_POS_SIZE*2];
			Utils.copyConfigPadToSysex(configFull.configPads[leftInput], sysex, configOptions.chainId, leftInput);
			for (int i = 0; i<sysex.length;i++) {
				sysexPad[i] = sysex[i];
			}
			Utils.copyConfigPadToSysex(configFull.configPads[rightInput], sysex, configOptions.chainId, rightInput);
			for (int i = 0; i<sysex.length;i++) {
				sysexPad[Constants.MD_SYSEX_PAD_SIZE + i] = sysex[i];
			}
			Utils.copyConfig3rdToSysex(configFull.config3rds[padPair - 1], sysex3rd, configOptions.chainId, padPair - 1);
			for (int i = 0; i<sysex3rd.length;i++) {
				sysexPad[Constants.MD_SYSEX_PAD_SIZE*2 + i] = sysex3rd[i];
			}
			Utils.copyConfigPosToSysex(configFull.configPos[leftInput], sysexPos, configOptions.chainId, leftInput);
			for (int i = 0; i<sysexPos.length;i++) {
				sysexPad[Constants.MD_SYSEX_PAD_SIZE*2 + Constants.MD_SYSEX_3RD_SIZE + i] = sysexPos[i];
			}
			Utils.copyConfigPosToSysex(configFull.configPos[rightInput], sysexPos, configOptions.chainId, rightInput);
			for (int i = 0; i<sysexPos.length;i++) {
				sysexPad[Constants.MD_SYSEX_PAD_SIZE*2 + Constants.MD_SYSEX_3RD_SIZE + Constants.MD_SYSEX_POS_SIZE + i] = sysexPos[i];
			}
			fileManager.loadSysex(sysexPad, configOptions);
			for (int i = 0; i<sysex.length;i++) {
				sysex[i] = sysexPad[i];
			}
			Utils.copySysexToConfigPad(sysex, configFull.configPads[leftInput]);
			for (int i = 0; i<sysex.length;i++) {
				sysex[i] = sysexPad[Constants.MD_SYSEX_PAD_SIZE + i];
			}
			Utils.copySysexToConfigPad(sysex, configFull.configPads[rightInput]);
			for (int i = 0; i<sysex3rd.length;i++) {
				sysex3rd[i] = sysexPad[Constants.MD_SYSEX_PAD_SIZE*2 + i];
			}
			Utils.copySysexToConfig3rd(sysex3rd, configFull.config3rds[padPair - 1]);
			for (int i = 0; i<sysexPos.length;i++) {
				sysexPos[i] = sysexPad[Constants.MD_SYSEX_PAD_SIZE*2 + Constants.MD_SYSEX_3RD_SIZE + i];
			}
			Utils.copySysexToConfigPos(sysexPos, configFull.configPos[leftInput]);
			for (int i = 0; i<sysexPos.length;i++) {
				sysexPos[i] = sysexPad[Constants.MD_SYSEX_PAD_SIZE*2 + Constants.MD_SYSEX_3RD_SIZE + Constants.MD_SYSEX_POS_SIZE + i];
			}
			Utils.copySysexToConfigPos(sysexPos, configFull.configPos[rightInput]);
			uiPad.setControlsFromConfigPad(configFull.configPads[leftInput], true, false);
			uiPad.setControlsFromConfigPad(configFull.configPads[rightInput], false, false);
			uiPad.setControlsFromConfigPos(configFull.configPos[leftInput], true, false);
			uiPad.setControlsFromConfigPos(configFull.configPos[rightInput], false, false);
			uiPad.setControlsFromConfig3rd(configFull.config3rds[padPair - 1], false);
		} else {
			byte [] sysexPad = new byte[Constants.MD_SYSEX_PAD_SIZE + Constants.MD_SYSEX_POS_SIZE];
			Utils.copyConfigPadToSysex(configFull.configPads[0], sysex, configOptions.chainId, 0);
			System.arraycopy(sysex, 0, sysexPad, 0, sysex.length);
			Utils.copyConfigPosToSysex(configFull.configPos[0], sysexPos, configOptions.chainId, 0);
			System.arraycopy(sysexPos, 0, sysexPad, Constants.MD_SYSEX_PAD_SIZE, sysexPos.length);
			fileManager.loadSysex(sysexPad, configOptions);					
			for (int i = 0; i<sysex.length;i++) {
				sysex[i] = sysexPad[i];
			}
			Utils.copySysexToConfigPad(sysex, configFull.configPads[0]);
			for (int i = 0; i<sysexPos.length;i++) {
				sysexPos[i] = sysexPad[Constants.MD_SYSEX_PAD_SIZE + i];
			}
			Utils.copySysexToConfigPos(sysexPos, configFull.configPos[0]);
			uiPad.setControlsFromConfigPad(configFull.configPads[0], true, false);
			uiPad.setControlsFromConfigPos(configFull.configPos[0], true, false);
		}
	}
	
	private void saveSysexPad() {
		byte [] sysex = new byte[Constants.MD_SYSEX_PAD_SIZE];
		byte [] sysex3rd = new byte[Constants.MD_SYSEX_3RD_SIZE];
		byte [] sysexPos = new byte[Constants.MD_SYSEX_POS_SIZE];
		if (padPair > 0 ) {
			int leftInput = (padPair - 1)*2 + 1;
			int rightInput = leftInput + 1;
			byte [] sysexPad = new byte[Constants.MD_SYSEX_PAD_SIZE*2 + Constants.MD_SYSEX_3RD_SIZE + Constants.MD_SYSEX_POS_SIZE*2];
			Utils.copyConfigPadToSysex(configFull.configPads[leftInput], sysex, configOptions.chainId, leftInput);
			for (int i = 0; i<sysex.length;i++) {
				sysexPad[i] = sysex[i];
			}
			Utils.copyConfigPadToSysex(configFull.configPads[rightInput], sysex, configOptions.chainId, rightInput);
			for (int i = 0; i<sysex.length;i++) {
				sysexPad[Constants.MD_SYSEX_PAD_SIZE + i] = sysex[i];
			}
			Utils.copyConfig3rdToSysex(configFull.config3rds[padPair - 1], sysex3rd, configOptions.chainId, padPair - 1);
			for (int i = 0; i<sysex3rd.length;i++) {
				sysexPad[Constants.MD_SYSEX_PAD_SIZE*2 + i] = sysex3rd[i];
			}
			Utils.copyConfigPosToSysex(configFull.configPos[leftInput], sysexPos, configOptions.chainId, leftInput);
			for (int i = 0; i<sysexPos.length;i++) {
				sysexPad[Constants.MD_SYSEX_PAD_SIZE*2 + Constants.MD_SYSEX_3RD_SIZE + i] = sysexPos[i];
			}
			Utils.copyConfigPosToSysex(configFull.configPos[rightInput], sysexPos, configOptions.chainId, rightInput);
			for (int i = 0; i<sysexPos.length;i++) {
				sysexPad[Constants.MD_SYSEX_PAD_SIZE*2 + Constants.MD_SYSEX_3RD_SIZE + Constants.MD_SYSEX_POS_SIZE + i] = sysexPos[i];
			}
			fileManager.saveSysex(sysexPad, configOptions);
		} else {
			byte [] sysexPad = new byte[Constants.MD_SYSEX_PAD_SIZE + Constants.MD_SYSEX_POS_SIZE];
			Utils.copyConfigPadToSysex(configFull.configPads[0], sysex, configOptions.chainId, 0);
			for (int i = 0; i<sysex.length;i++) {
				sysexPad[i] = sysex[i];
			}					
			Utils.copyConfigPosToSysex(configFull.configPos[0], sysexPos, configOptions.chainId, 0);
			for (int i = 0; i<sysexPos.length;i++) {
				sysexPad[i + Constants.MD_SYSEX_PAD_SIZE] = sysexPos[i];
			}
			fileManager.saveSysex(sysexPad, configOptions);
		}
	}

	private void loadSysexCurve() {
		byte [] sysex = new byte[Constants.MD_SYSEX_CURVE_SIZE];
		Utils.copyConfigCurveToSysex(configFull.configCurves[curvePointer], sysex, configOptions.chainId, curvePointer);
		fileManager.loadSysex(sysex, configOptions);
		Utils.copySysexToConfigCurve(sysex, configFull.configCurves[curvePointer]);
		uiPadsExtra.setYvalues(configFull.configCurves[curvePointer].yValues, false);
	}
	
	private void saveSysexCurve() {
		byte [] sysex = new byte[Constants.MD_SYSEX_CURVE_SIZE];
		Utils.copyConfigCurveToSysex(configFull.configCurves[curvePointer], sysex, configOptions.chainId, curvePointer);
		fileManager.saveSysex(sysex, configOptions);
	}
	
	private void loadSysexAllCustomNames() {
		byte [] sysex;
		byte [] sysexAll = new byte[Constants.MD_SYSEX_CUSTOM_NAME_SIZE * configFull.customNamesCount];
		fileManager.loadSysex(sysexAll, configOptions);					
		for (int i = 0; i < configFull.customNamesCount; i++) {
			sysex = new byte[Constants.MD_SYSEX_CUSTOM_NAME_SIZE];
			System.arraycopy(sysexAll, i*Constants.MD_SYSEX_CUSTOM_NAME_SIZE, sysex, 0, sysex.length);
			Utils.copySysexToConfigCustomName(sysex, configFull.configCustomNames[i]);
			uiPadsExtra.setCustomName(configFull.configCustomNames[i], i, false);
		}

	}
	
	private void saveSysexAllCustomNames() {
		byte [] sysex;
		byte [] sysexAll = new byte[Constants.MD_SYSEX_CUSTOM_NAME_SIZE * configFull.customNamesCount];
		for (int i = 0; i < configFull.customNamesCount; i++) {
			sysex = new byte[Constants.MD_SYSEX_CUSTOM_NAME_SIZE];
			Utils.copyConfigCustomNameToSysex(configFull.configCustomNames[i], sysex, configOptions.chainId, i);
			System.arraycopy(sysex, 0, sysexAll, i*Constants.MD_SYSEX_CUSTOM_NAME_SIZE, sysex.length);
		}
		fileManager.saveSysex(sysexAll, configOptions);
	}
		
	private void save_all() {
		fileManager.save_all(configFull, configOptions);
		System.out.println("Saved all config");
		//updateGlobalMiscControls();
	}
	

}
