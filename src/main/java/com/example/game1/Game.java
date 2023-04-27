package com.example.game1;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.Random;

public class Game extends Application {

    private static final int ROWS = 3;
    private static final int COLS = 5;
    private static final int PAYLINES = 15;
    private static final int SPIN_COST = 1;

    private final String[] SYMBOLS = {"H1", "H2", "H3", "H4", "L1", "L2", "L3", "L4", "L5", "L6"};
    private final double[] PAYOUTS_3OAK = {10, 8, 6, 5, 1, 1, 1, 0.5, 0.5, 0.5};
    private final double[] PAYOUTS_4OAK = {40, 32, 24, 20, 4, 4, 4, 2, 2, 2};
    private final double[] PAYOUTS_5OAK = {160, 128, 96, 80, 16, 16, 16, 8, 8, 8};

    private int numSpins;
    private int numWins;
    private double totalBet;
    private double totalWins;

    private final Label[] spinResults = new Label[ROWS * COLS];
    private final Label[] symbolCounts = new Label[SYMBOLS.length * 3];
    private Label spinLabel, winLabel, totalWinLabel, totalBetLabel, rtpLabel;

    @Override
    public void start(Stage stage) {

        Label balanceLabel = new Label("Balance: $10000");
        Label spinCostLabel = new Label("Spin cost: $" + SPIN_COST);

        TextField betInput = new TextField();
        betInput.setPromptText("Enter bet amount");

        Button spinButton = new Button("Spin");
        spinButton.setOnAction(e -> spin(betInput, balanceLabel, spinCostLabel));


        GridPane slotPane = new GridPane();
        slotPane.setHgap(10);
        slotPane.setVgap(10);
        slotPane.setAlignment(Pos.CENTER);

        spinLabel = new Label("# Spins: 0");
        winLabel = new Label("# Winning Spins: 0");
        totalWinLabel = new Label("Total Wins: 0");
        totalBetLabel = new Label("Total Bet: 0");
        rtpLabel = new Label("RTP: 0%");
        HBox infoBox = new HBox(10, spinLabel, winLabel, totalWinLabel, totalBetLabel, rtpLabel);
        infoBox.setAlignment(Pos.CENTER);
        slotPane.add(infoBox, 0, 4, 5, 3);

        for (int i = 0; i < ROWS * COLS; i++) {
            spinResults[i] = new Label();
            slotPane.add(spinResults[i], i % COLS, i / COLS);
        }

        GridPane symbolPane = new GridPane();
        symbolPane.setHgap(10);
        symbolPane.setVgap(10);
        symbolPane.setAlignment(Pos.CENTER);

        for (int i = 0; i < SYMBOLS.length; i++) {
            symbolPane.add(new Label(SYMBOLS[i]), i, 0);
            symbolCounts[i * 3] = new Label("3 of a kind: 0");
            symbolPane.add(symbolCounts[i * 3], i, 1);
            symbolCounts[i * 3 + 1] = new Label("4 of a kind: 0");
            symbolPane.add(symbolCounts[i * 3 + 1], i, 2);
            symbolCounts[i * 3 + 2] = new Label("5 of a kind: 0");
            symbolPane.add(symbolCounts[i * 3 + 2], i, 3);
        }

        HBox bottomPane = new HBox(10);
        bottomPane.setAlignment(Pos.CENTER);
        bottomPane.getChildren().addAll(betInput, spinButton, balanceLabel, spinCostLabel);

        GridPane root = new GridPane();
        root.setHgap(20);
        root.setVgap(20);
        root.setAlignment(Pos.CENTER);
        root.add(slotPane, 0, 0);
        root.add(symbolPane, 0, 1);
        root.add(bottomPane, 0, 2);

        Scene scene = new Scene(root, 800, 600);

        stage.setScene(scene);
        stage.setTitle("Slot Machine");
        stage.show();
    }

    private void spin(TextField betInput, Label balanceLabel, Label spinCostLabel) {
        if (betInput.getText().isEmpty()) {
            return;
        }

        double betAmount = Double.parseDouble(betInput.getText());

        if (betAmount <= 0 || betAmount > Double.parseDouble(balanceLabel.getText().substring(10))) {
            return;
        }

        numSpins++;

        totalBet += betAmount;
        totalBetLabel.setText("Total Bet: " + totalBet);

        balanceLabel.setText("Balance: $" + String.format("%.2f", Double.parseDouble(balanceLabel.getText().substring(10)) - betAmount));
        spinCostLabel.setText("Spin cost: $" + SPIN_COST);

        int[][] board = new int[ROWS][COLS];
        Random random = new Random();

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = random.nextInt(SYMBOLS.length);
                spinResults[i * COLS + j].setText(SYMBOLS[board[i][j]]);
            }
        }

        checkPaylines(board, betAmount);

        double win = 0;
        if (win > 0) {
            numWins++;
        }
        totalWins += win;

        spinLabel.setText("# Spins: " + numSpins);
        winLabel.setText("# Winning Spins: " + numWins);
        totalWinLabel.setText("Total Wins: " + totalWins);
        rtpLabel.setText("RTP: " + String.format("%.2f", totalWins / totalBet * 100) + "%");
    }

    private void checkPaylines(int[][] board, double betAmount) {
        for (int i = 0; i < PAYLINES; i++) {
            int[] line = getLine(board, i);

            int symbol = line[0];
            int count = 1;

            for (int j = 1; j < line.length; j++) {
                if (line[j] == symbol) {
                    count++;
                } else {
                    if (count >= 3) {
                        payOut(symbol, count, betAmount);
                    }
                    symbol = line[j];
                    count = 1;
                }
            }

            if (count >= 3) {
                payOut(symbol, count, betAmount);
            }
        }
    }

    private int[] getLine(int[][] board, int lineNum) {
        int[] line = new int[COLS];

        for (int i = 0; i < COLS; i++) {
            line[i] = board[lineNum % ROWS][(i + lineNum / ROWS) % COLS];
        }

        return line;
    }


    private void payOut(int symbol, int count, double betAmount) {


        int index = symbol;
        double payout;

        if (count == 3) {
            payout = PAYOUTS_3OAK[index];
            int currentCount = Integer.parseInt(symbolCounts[index * 3].getText().split(": ")[1]);
            symbolCounts[index * 3].setText("3 of a kind: " + (currentCount + 1));

        } else if (count == 4) {
            payout = PAYOUTS_4OAK[index];
            int currentCount = Integer.parseInt(symbolCounts[index * 3 + 1].getText().split(": ")[1]);
            symbolCounts[index * 3 + 1].setText("4 of a kind: " + (currentCount + 1));

        } else {
            payout = PAYOUTS_5OAK[index];
            int currentCount = Integer.parseInt(symbolCounts[index * 3 + 2].getText().split(": ")[1]);
            symbolCounts[index * 3 + 2].setText("5 of a kind: " + (currentCount + 1));

        }
        numWins++;
        totalWins += payout * betAmount;
    }


    public static void main(String[] args) {
        launch();
    }
}

