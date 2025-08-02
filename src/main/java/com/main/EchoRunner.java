package com.main;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class EchoRunner extends Application {
    private final List<Integer> sequence = new ArrayList<>();
    private final List<Button> buttons = new ArrayList<>();
    private final Random random = new Random();
    private int currentStep = 0;
    private int level = 1;
    private boolean playerTurn = false;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("🎵 Echo Runner");
        showStartScreen();
    }

    private void showStartScreen() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle
                ("-fx-background-color: linear-gradient(to bottom, #1f4037, #99f2c8);");

        Label title = new Label("🎵 Echo Runner");
        title.setStyle
                ("-fx-font-size: 36px; -fx-text-fill: white; -fx-font-weight: bold;");

        Button startBtn = new Button("▶ Start Game");
        startBtn.setStyle
                ("-fx-font-size: 20px; -fx-background-color: #00c853; -fx-text-fill: white; -fx-background-radius: 15;");
        startBtn.setOnAction(e -> showGameScreen());

        layout.getChildren().addAll(title, startBtn);

        Scene scene = new Scene(layout, 450, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void showGameScreen() {
        sequence.clear();
        buttons.clear();
        currentStep = 0;
        level = 1;

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: linear-gradient(to bottom, #2b5876, #4e4376);");

        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
        String[] sounds = {
                Objects.requireNonNull(getClass().getResource("/sound1.wav")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/sound2.wav")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/sound3.wav")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/sound4.wav")).toExternalForm()
        };

        for (int i = 0; i < 4; i++) {
            int index = i;
            Button btn = new Button();
            btn.setMinSize(180, 180);
            btn.setStyle("-fx-background-color: " + toRgb(colors[i]) + "; -fx-background-radius: 20;");
            AudioClip sound = new AudioClip(sounds[i]);

            btn.setOnAction(e -> {
                if (playerTurn) {
                    sound.play();
                    flash(btn);
                    if (index == sequence.get(currentStep)) {
                        currentStep++;
                        if (currentStep == sequence.size()) {
                            level++;
                            nextRound();
                        }
                    } else {
                        showGameOver();
                    }
                }
            });

            buttons.add(btn);
            grid.add(btn, i % 2, i / 2);
        }

        Scene scene = new Scene(grid, 450, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
        nextRound();
    }


    private void nextRound() {
        playerTurn = false;
        sequence.add(random.nextInt(4));

        new Thread(() -> {
            try {
                Thread.sleep(600);
                for (int idx : sequence) {
                    javafx.application.Platform.runLater(() -> flash(buttons.get(idx)));


                    javafx.scene.media.Media sound = new javafx.scene.media.Media(
                            Objects.requireNonNull(getClass()
                                    .getResource("/sound" + (idx + 1) + ".wav")).toExternalForm());
                    javafx.scene.media.MediaPlayer player = new javafx.scene.media.MediaPlayer(sound);

                    final Object lock = new Object();
                    player.setOnEndOfMedia(() -> {
                        synchronized (lock) {
                            lock.notify();
                        }
                    });

                    player.play();

                    synchronized (lock) {
                        lock.wait();
                    }

                    Thread.sleep(200);
                }
                currentStep = 0;
                playerTurn = true;
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void flash(Button btn) {
        String original = btn.getStyle();
        btn.setStyle("-fx-background-color: white; -fx-background-radius: 20;");
        new Thread(() -> {
            try {
                Thread.sleep(250);
                javafx.application.Platform.runLater(() -> btn.setStyle(original));
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    private void showGameOver() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle
                ("-fx-background-color: linear-gradient(to bottom, #6a11cb, #2575fc);");

        Label label = new Label("💀 GAME OVER!\nYour Score: " + (level - 1));
        label.setStyle
                ("-fx-font-size: 24px; -fx-text-fill: white; -fx-font-weight: bold;");

        Button restartBtn = new Button("🔁 Restart");
        restartBtn.setStyle
                ("-fx-font-size: 18px; -fx-background-color: #00c853; -fx-text-fill: white; -fx-background-radius: 15;");
        restartBtn.setMinWidth(200);
        restartBtn.setOnAction(e -> showGameScreen());

        Button exitBtn = new Button("🚪 Exit");
        exitBtn.setStyle
                ("-fx-font-size: 18px; -fx-background-color: #d50000; -fx-text-fill: white; -fx-background-radius: 15;");
        exitBtn.setMinWidth(200);
        exitBtn.setOnAction(e -> System.exit(0));

        layout.getChildren().addAll(label, restartBtn, exitBtn);

        Scene scene = new Scene(layout, 450, 500);
        primaryStage.setScene(scene);
    }

    private String toRgb(Color color) {
        return String.format("rgb(%d,%d,%d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
