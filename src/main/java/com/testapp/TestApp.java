package com.testapp;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.stream.Collectors;

public class TestApp {

    private static final int BUTTONS_PER_COLUMN = 10;
    private static final int MAX_VALUE = 1000;
    private static final int MORE_BUTTONS_VALUE = 30;

    private static final String UPDATE_BUTTON_MSG = "Exception occurred while button's updating: ";
    private static final String INCORRECT_NUMBER_MSG = "Please enter a number greater than 0";
    private static final String INVALID_NUMBER_MSG = "Please enter a valid integer number";
    private static final String MORE_BUTTONS_MSG = "Please select a value smaller or equal to " + MORE_BUTTONS_VALUE;

    private final JFrame frame;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;

    private JPanel buttonsPanel;
    private int numberForSortScreen;
    private boolean sortScreenInitialized = false;

    private List<Integer> numbers;
    private final List<JButton> allButtons = new ArrayList<>();

    private volatile boolean sorting = false;
    private boolean ascendingOrder = true;

    private JButton startButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TestApp::new);
    }

    public TestApp() {
        frame = new JFrame("SPA: Intro & Sort");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(createIntroScreen(), "Intro");
        cardPanel.add(createSortScreen(), "Sort");

        frame.add(cardPanel);
        frame.setVisible(true);
    }

    private JPanel createIntroScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("How many numbers to display?");
        JTextField textField = new JTextField();
        JButton enterButton = new JButton("Enter");

        textField.setMaximumSize(new Dimension(200, 30));
        enterButton.setMaximumSize(new Dimension(100, 30));

        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        textField.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterButton.addActionListener(e -> {
            final String text = textField.getText();
            try {
                numberForSortScreen = Integer.parseInt(text.trim());
                if (numberForSortScreen > 0) {
                    sortScreenInitialized = false;
                    showSortScreen();
                } else {
                    JOptionPane.showMessageDialog(frame, INCORRECT_NUMBER_MSG);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, INVALID_NUMBER_MSG);
            }
        });

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(textField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(enterButton);

        return panel;
    }

    private JPanel createSortScreen() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

        JScrollPane scrollPane = new JScrollPane(buttonsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        startButton = new JButton("Start");
        startButton.addActionListener(e -> startSorting());
        JButton resetButton = new JButton("Back");
        resetButton.addActionListener(e -> {
            sorting = false;
            ascendingOrder = true;
            cardLayout.show(cardPanel, "Intro");
        });

        rightPanel.add(startButton);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(resetButton);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        return mainPanel;
    }

    private void showSortScreen() {
        cardLayout.show(cardPanel, "Sort");
        if (!sortScreenInitialized) {
            generateButtons(numberForSortScreen);
        }
    }

    private void generateButtons(int buttonCount) {
        clearButtons();
        numbers = initRandomNumbers(buttonCount);
        createButtons(buttonCount);
        sortScreenInitialized = true;
    }

    private static List<Integer> initRandomNumbers(int totalButtons) {
        Random random = new Random();
        int[] arr = new int[totalButtons];
        boolean hasSmall = false;

        for (int i = 0; i < totalButtons; i++) {
            arr[i] = random.nextInt(MAX_VALUE) + 1;
            if (arr[i] <= 30) {
                hasSmall = true;
            }
        }

        if (!hasSmall) {
            arr[random.nextInt(totalButtons)] = random.nextInt(30) + 1;
        }

        return Arrays.stream(arr)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void createButtons(int totalButtons) {
        allButtons.clear();
        buttonsPanel.removeAll();

        int columns = (int) Math.ceil((double) totalButtons / BUTTONS_PER_COLUMN);

        int index = 0;
        for (int col = 0; col < columns; col++) {
            JPanel columnPanel = new JPanel();
            columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));
            columnPanel.setAlignmentY(Component.TOP_ALIGNMENT);

            for (int row = 0; row < BUTTONS_PER_COLUMN; row++) {
                if (index >= totalButtons) {
                    break;
                }

                JButton button = createNewButton(index, columnPanel);
                allButtons.add(button);
                index++;
            }

            buttonsPanel.add(columnPanel);
            buttonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        }

        buttonsPanel.revalidate();
        buttonsPanel.repaint();
    }

    private JButton createNewButton(int index, JPanel columnPanel) {
        JButton button = new JButton(String.valueOf(numbers.get(index)));
        Dimension fixedSize = new Dimension(80, 30);
        button.setPreferredSize(fixedSize);
        button.setMaximumSize(fixedSize);
        button.setMinimumSize(fixedSize);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        button.addActionListener(e -> {
            int count = Integer.parseInt(button.getText());
            if (count <= MORE_BUTTONS_VALUE) {
                generateButtons(count);
                ascendingOrder = true;
            } else {
                JOptionPane.showMessageDialog(frame, MORE_BUTTONS_MSG);
            }
        });

        columnPanel.add(button);
        columnPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        return button;
    }

    private void clearButtons() {
        buttonsPanel.removeAll();
        allButtons.clear();
        buttonsPanel.revalidate();
        buttonsPanel.repaint();
    }

    private void updateButtons(List<Integer> arr) {
        for (int i = 0; i < arr.size(); i++) {
            allButtons.get(i).setText(String.valueOf(arr.get(i)));
        }
    }

    private void startSorting() {
        if (sorting) {
            return;
        }

        sorting = true;
        startButton.setEnabled(false);
        startButton.setBackground(Color.GRAY);

        new Thread(() -> {
            Consumer<List<Integer>> stepCallback = step -> {
                if (!sorting) {
                    return;
                }
                try {
                    SwingUtilities.invokeAndWait(() -> updateButtons(step));
                    Thread.sleep(300);
                } catch (Exception ex) {
                    showMessageError(ex);
                }
            };

            BooleanSupplier running = () -> sorting;

            if (ascendingOrder) {
                IterativeQuickSort.quickSort(numbers, stepCallback, running, (a, b) -> Integer.compare(a, b));
            } else {
                IterativeQuickSort.quickSort(numbers, stepCallback, running, (a, b) -> Integer.compare(b, a));
            }

            ascendingOrder = !ascendingOrder;

            SwingUtilities.invokeLater(() -> {
                sorting = false;
                startButton.setEnabled(true);
                startButton.setBackground(UIManager.getColor("Button.background"));
            });
        }).start();
    }

    private void showMessageError(Exception ex) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                        frame,
                        UPDATE_BUTTON_MSG + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                )
        );
    }

    private static final class IterativeQuickSort {

        public static void quickSort(List<Integer> arr, Consumer<List<Integer>> stepCallback, BooleanSupplier running, IntBinaryOperator compare) {
            Stack<int[]> stack = new Stack<>();
            stack.push(new int[]{0, arr.size() - 1});

            while (!stack.isEmpty() && running.getAsBoolean()) {
                int[] range = stack.pop();
                int low = range[0];
                int high = range[1];

                if (low < high) {
                    int pivotIndex = partition(arr, low, high, compare);

                    stack.push(new int[]{low, pivotIndex - 1});
                    stack.push(new int[]{pivotIndex + 1, high});

                    stepCallback.accept(arr);
                }
            }
        }

        private static int partition(List<Integer> arr, int low, int high, IntBinaryOperator compare) {
            int pivot = arr.get(high);
            int i = low - 1;

            for (int j = low; j < high; j++) {
                if (compare.applyAsInt(arr.get(j), pivot) <= 0) {
                    i++;
                    Collections.swap(arr, i, j);
                }
            }
            Collections.swap(arr, i + 1, high);
            return i + 1;
        }
    }
}
