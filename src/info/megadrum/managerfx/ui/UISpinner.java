package info.megadrum.managerfx.ui;

import info.megadrum.managerfx.data.ConfigOptions;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;


public class UISpinner extends UIControl {
	private Spinner<Integer> uispinner;
	private Integer minValue;
	private Integer maxValue;
	private Integer initValue;
	private Integer currentValue;
	private Integer step;
	private HBox layout;
	private SpinnerValueFactory<Integer> valueFactory;

	public UISpinner(Boolean showCopyButton) {
		super(showCopyButton);
		init();
	}
	
	public UISpinner(Integer min, Integer max, Integer initial, Integer s, Boolean showCopyButton) {
		super(showCopyButton);
		init(min, max, initial, s);
	}
	
	public UISpinner(String labelText, Boolean showCopyButton) {
		super(labelText, showCopyButton);
		init();
	}

	public UISpinner(String labelText, Integer min, Integer max, Integer initial, Integer s, Boolean showCopyButton) {
		super(labelText, showCopyButton);
		init(min, max, initial, s);
	}

	private void init() {
		init(0,100,0,1);
	}
	
	private void init(Integer min, Integer max, Integer initial, Integer s) {
		minValue = min;
		maxValue = max;
		initValue = initial;
		currentValue = initValue;
		step = s;
		valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, initValue, step);

		uispinner = new Spinner<Integer>();
		uispinner.setValueFactory(valueFactory);
		uispinner.setEditable(true);
		//uispinner.getEditor().setStyle("-fx-text-fill: black; -fx-alignment: CENTER_RIGHT;"
		//		);    
		//uispinner.set
		uispinner.getEditor().textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// Spinner number validation
	            if (!newValue.matches("\\d*")) {
	            	uispinner.getEditor().setText(currentValue.toString());
	            } else {
					if (newValue.matches("")) {
						uispinner.getEditor().setText(currentValue.toString());
					} else {
						if (currentValue != Integer.valueOf(newValue)) {
							System.out.printf("%s: new value = %d, old value = %d\n",label.getText(),Integer.valueOf(newValue),currentValue );
							currentValue = Integer.valueOf(newValue);
							intValue = currentValue;
							resizeFont();
						}
					}
	            }
				
			}
	    });
		
		uispinner.getEditor().setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                    uispinner.increment(1);
                    break;
                case DOWN:
                    uispinner.decrement(1);
                    break;
			default:
				break;
            }
        });
		
	    IncrementHandler handler = new IncrementHandler();
	    uispinner.addEventFilter(MouseEvent.MOUSE_PRESSED, handler);
	    uispinner.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> {
	        if (evt.getButton() == MouseButton.PRIMARY) {
	            handler.stop();
	        }
	    });
	    
	    layout = new HBox();
	    layout.setAlignment(Pos.CENTER_LEFT);
	    layout.getChildren().addAll(uispinner);
		initControl(layout);
	}

    class IncrementHandler implements EventHandler<MouseEvent> {

        private Spinner<?> spinner;
        private boolean increment;
        private long startTimestamp;

        private static final long DELAY = 1000l * 1000L * 750L; // 0.75 sec
        private Node button;

        private final AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (now - startTimestamp >= DELAY) {
                    // trigger updates every frame once the initial delay is over
                    if (increment) {
                        spinner.increment();
                    } else {
                        spinner.decrement();
                    }
                }
            }
        };

        @Override
        public void handle(MouseEvent event) {
            if (event.getButton() == MouseButton.PRIMARY) {
                Spinner<?> source = (Spinner<?>) event.getSource();
                Node node = event.getPickResult().getIntersectedNode();

                Boolean increment = null;
                // find which kind of button was pressed and if one was pressed
                while (increment == null && node != source) {
                    if (node.getStyleClass().contains("increment-arrow-button")) {
                        increment = Boolean.TRUE;
                    } else if (node.getStyleClass().contains("decrement-arrow-button")) {
                        increment = Boolean.FALSE;
                    } else {
                        node = node.getParent();
                    }
                }
                if (increment != null) {
                    event.consume();
                    source.requestFocus();
                    spinner = source;
                    this.increment = increment;

                    // timestamp to calculate the delay
                    startTimestamp = System.nanoTime();

                    button = node;

                    // update for css styling
                    node.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), true);

                    // first value update
                    timer.handle(startTimestamp + DELAY);

                    // trigger timer for more updates later
                    timer.start();
                }
            }
        }
        public void stop() {
            timer.stop();
            if (button != null) {
            	button.pseudoClassStateChanged(PseudoClass.getPseudoClass("pressed"), false);
            	button = null;
            	spinner = null;
            }
        }

    }
    
    private void resizeFont() {
		Double we = uispinner.getEditor().getWidth();
		Integer l = valueFactory.getValue().toString().length();
		Double ll = (4/(4 + l.doubleValue()))*1.8;
		we = we*ll;
		//uispinner.getEditor().setFont(new Font(h*0.4));
		uispinner.getEditor().setFont(new Font(we*0.25));    	
    }
    
    @Override
    public void respondToResize(Double h, Double w) {
    	super.respondToResize(h, w);
		uispinner.setMinHeight(h);
		uispinner.setMaxHeight(h);
		//uispinner.setMaxWidth(h*2 + 30.0);
		//uispinner.setMinWidth(h*2 + 30.0);
		uispinner.setMaxWidth(w*0.30);
		uispinner.setMinWidth(w*0.30);
		// Spinner buttons width seems to be fixed and not adjustable
		//uispinner.setStyle("-fx-body-color: ladder(#444, yellow 0%, red 100%)");
		resizeFont();
    }
/*
    @Override
	public void setControlMinWidth(Double w) {
    	// don't change spinner control width so override setControlMinWidth here
	}
*/
    public void uiCtlSetValue(Integer n) {
    	valueFactory.setValue(n);
    }
    
    public Integer uiCtlGetValue() {
    	return valueFactory.getValue();
    }
}
