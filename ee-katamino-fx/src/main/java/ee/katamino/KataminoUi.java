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

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Eugen Eisler
 */
public class KataminoUi extends Application {
    Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Katamino Solver");
        stage = primaryStage;
        replaceSceneContent("/view.fxml");
        primaryStage.show();
    }

    private Parent replaceSceneContent(String fxml) throws Exception {
        URL location = getClass().getResource(fxml);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(location);
        fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
        Parent page = (Parent) fxmlLoader.load(location.openStream());
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(page, 500, 400);
            stage.setScene(scene);
        } else {
            scene.setRoot(page);
        }
        stage.sizeToScene();
        return page;
    }

    /**
     * Java main for when running without JavaFX launcher
     */
    public static void main(String[] args) {
        launch(args);
    }
}
