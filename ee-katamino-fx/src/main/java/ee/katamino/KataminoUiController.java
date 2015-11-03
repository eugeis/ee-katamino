/*
 * Copyright 2015-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ee.katamino;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * @author Eugen Eisler
 */
public class KataminoUiController implements Initializable {
	@FXML
	private ComboBox<String> type;
	@FXML
	private ComboBox<Integer> level;
	@FXML
	private Button solve;
	@FXML
	private Pane field;
	@FXML
	private ProgressBar progress;

	private SmallSlam smallSlam;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		smallSlam = new SmallSlam();
		List<String> types = new ArrayList<>(smallSlam.getAllTypes().keySet());
		types.sort(Comparator.<String>naturalOrder());
		type.getItems().addAll(types);
		type.getSelectionModel().selectFirst();

		level.getItems().addAll(3, 4, 5, 6, 7, 8);
		level.getSelectionModel().selectFirst();

		progress.setLayoutY(15);
	}

	@FXML
	protected void solve() {
		progress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		solve.setDisable(true);

		// Blue stroked polygon
		ObservableList<Node> children = field.getChildren();
		children.clear();

		Runnable solver =  new Runnable() {
			@Override
			public void run() {
				smallSlam.setLevel(level.getValue());
				smallSlam.setType(type.getValue());
				smallSlam.init();

				smallSlam.solve();
				smallSlam.draw();

				Platform.runLater(new Runnable() {
					@Override
					public void run() {

						progress.setProgress(0);

						int factor = 50;

						for (Figure figure : smallSlam.getFigures()) {
							for (Cell cell : figure.getCells()) {
								Polygon polygon = new Polygon();
								polygon.getPoints().add(Double.valueOf(cell.getH() * factor));
								polygon.getPoints().add(Double.valueOf(cell.getV() * factor));
								polygon.getPoints().add(Double.valueOf((cell.getH()) * factor));
								polygon.getPoints().add(Double.valueOf((cell.getV() + 1) * factor));
								polygon.getPoints().add(Double.valueOf((cell.getH() + 1) * factor));
								polygon.getPoints().add(Double.valueOf((cell.getV() + 1) * factor));
								polygon.getPoints().add(Double.valueOf((cell.getH() + 1) * factor));
								polygon.getPoints().add(Double.valueOf((cell.getV()) * factor));
								double[] color = figure.getColor();
								polygon.setFill(new Color(color[0], color[1], color[2], 1));
								children.add(polygon);
							}
						}
					}
				});

				solve.setDisable(false);
			}
		};

		Thread thread = new Thread(solver);
		thread.start();
	}
}