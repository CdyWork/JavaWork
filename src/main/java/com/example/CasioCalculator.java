package com.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class CasioCalculator extends JFrame {

    // ⭐ 静态初始化块 - 在类加载时立即执行，设置中文字体
    static {
        setupWindowsChineseFont();
    }

    private JTextArea display;
    private JTabbedPane tabbedPane;
    private CalculatorEngine engine;
    private JLabel statusLabel;
    private JTextField[][] matrixAFields;
    private JTextField[][] matrixBFields;

    public CasioCalculator() {
        engine = new CalculatorEngine();
        matrixAFields = new JTextField[3][3];
        matrixBFields = new JTextField[3][3];
        initializeUI();
    }

    /**
     * 设置 Windows 系统中文字体（关键修复）
     */
    private static void setupWindowsChineseFont() {
        try {
            // 设置系统编码为 UTF-8
            System.setProperty("file.encoding", "UTF-8");

            // Windows 推荐字体
            String fontName = "Microsoft YaHei UI";  // 微软雅黑 UI

            // 创建字体对象
            Font plainFont = new Font(fontName, Font.PLAIN, 13);
            Font boldFont = new Font(fontName, Font.BOLD, 14);
            Font buttonFont = new Font(fontName, Font.BOLD, 13);

            // ⭐ 特别设置：显示屏使用支持中文的等宽字体
            // 创建字体映射，让 TextArea 使用 Consolas 显示数字，但后备字体用微软雅黑显示中文
            Font textAreaFont = new Font("Consolas", Font.BOLD, 20);

            // 设置所有 Swing 组件的默认字体
            UIManager.put("Button.font", buttonFont);
            UIManager.put("Label.font", plainFont);
            UIManager.put("TextField.font", plainFont);
            UIManager.put("TextArea.font", textAreaFont);  // 显示屏字体
            UIManager.put("TabbedPane.font", boldFont);
            UIManager.put("TitledBorder.font", boldFont);
            UIManager.put("Menu.font", plainFont);
            UIManager.put("MenuItem.font", plainFont);
            UIManager.put("PopupMenu.font", plainFont);
            UIManager.put("ToolTip.font", plainFont);
            UIManager.put("Table.font", plainFont);
            UIManager.put("TableHeader.font", boldFont);
            UIManager.put("ComboBox.font", plainFont);
            UIManager.put("List.font", plainFont);
            UIManager.put("Panel.font", plainFont);
            UIManager.put("CheckBox.font", plainFont);
            UIManager.put("RadioButton.font", plainFont);

            System.out.println("✓ Windows 中文字体设置成功: " + fontName);
            System.out.println("✓ 显示屏字体: Consolas (数字) + " + fontName + " (中文)");
            System.out.println("✓ 文件编码: " + System.getProperty("file.encoding"));

        } catch (Exception e) {
            System.err.println("✗ 字体设置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        setTitle("Calculator  by CDY");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(40, 40, 40));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        mainPanel.add(createTopBar(), BorderLayout.NORTH);
        mainPanel.add(createDisplay(), BorderLayout.CENTER);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 14));
        tabbedPane.addTab("普通计算", createNormalPanel());
        tabbedPane.addTab("矩阵运算", createMatrixPanel());
        tabbedPane.addTab("方程求解", createEquationPanel());
        tabbedPane.addTab("函数绘图", createGraphPanel());

        mainPanel.add(tabbedPane, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private JPanel createTopBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(20, 50, 100));
        panel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel brand = new JLabel("demo-test");
        brand.setFont(new Font("Arial", Font.BOLD, 20));
        brand.setForeground(Color.WHITE);

        statusLabel = new JLabel("DEG | M: 0");
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(150, 200, 255));

        panel.add(brand, BorderLayout.WEST);
        panel.add(statusLabel, BorderLayout.EAST);

        return panel;
    }

    private JScrollPane createDisplay() {
        display = new JTextArea(4, 40);

        // ⭐ 关键修复：创建复合字体 - Consolas 显示数字，微软雅黑显示中文
        // 方案1：使用字体后备机制（推荐）
        Font displayFont = createDisplayFont();
        display.setFont(displayFont);

        display.setBackground(new Color(180, 255, 180));
        display.setForeground(new Color(0, 60, 0));
        display.setEditable(true);
        display.setText("0");
        display.setLineWrap(true);
        display.setMargin(new Insets(10, 15, 10, 15));

        JScrollPane scrollPane = new JScrollPane(display);
        scrollPane.setBorder(new LineBorder(new Color(0, 100, 0), 4));

        return scrollPane;
    }

    /**
     * 创建显示屏字体 - 支持数字和中文
     */
    private Font createDisplayFont() {
        // 方案1：直接使用微软雅黑（简单有效）
        // 微软雅黑也能很好地显示数字，只是不是等宽字体
        return new Font("Microsoft YaHei UI", Font.BOLD, 20);

    }

    private JPanel createNormalPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel buttonsPanel = new JPanel(new GridLayout(8, 5, 6, 6));
        buttonsPanel.setBackground(new Color(40, 40, 40));

        String[][] allButtons = {
                {"sin", "cos", "tan", "√", "x²"},
                {"ln", "log", "eˣ", "|x|", "x!"},
                {"(", ")", "π", "e", "^"},
                {"AC", "DEL", "ANS", "÷", "%"},
                {"7", "8", "9", "×", "1/x"},
                {"4", "5", "6", "-", "x³"},
                {"1", "2", "3", "+", "∛"},
                {"0", ".", "(−)", "=", "EXP"}
        };

        for (String[] row : allButtons) {
            for (String text : row) {
                ButtonType type = determineButtonType(text);
                buttonsPanel.add(createStyledButton(text, type));
            }
        }

        panel.add(buttonsPanel, BorderLayout.CENTER);

        JPanel memoryPanel = new JPanel(new GridLayout(1, 5, 6, 6));
        memoryPanel.setBackground(new Color(40, 40, 40));
        String[] memButtons = {"MC", "MR", "M+", "M-", "MS"};
        for (String text : memButtons) {
            memoryPanel.add(createStyledButton(text, ButtonType.MEMORY));
        }
        panel.add(memoryPanel, BorderLayout.SOUTH);

        return panel;
    }

    private ButtonType determineButtonType(String text) {
        if (text.matches("[0-9.]")) {
            return ButtonType.NUMBER;
        }
        if (text.equals("=")) {
            return ButtonType.EQUALS;
        }
        if ("+-×÷%".contains(text)) {
            return ButtonType.OPERATOR;
        }
        if (text.equals("AC") || text.equals("DEL")) {
            return ButtonType.SPECIAL;
        }
        return ButtonType.FUNCTION;
    }

    private JPanel createMatrixPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel matricesPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        matricesPanel.setBackground(new Color(40, 40, 40));

        matricesPanel.add(createMatrixInput("矩阵 A", matrixAFields));
        matricesPanel.add(createMatrixInput("矩阵 B", matrixBFields));

        JPanel opsPanel = new JPanel(new GridLayout(2, 3, 6, 6));
        opsPanel.setBackground(new Color(40, 40, 40));
        opsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        String[] operations = {"A + B", "A - B", "A × B", "det(A)", "A⁻¹", "Aᵀ"};

        for (String op : operations) {
            JButton btn = createStyledButton(op, ButtonType.OPERATOR);
            btn.addActionListener(e -> performMatrixOperation(op));
            opsPanel.add(btn);
        }

        panel.add(matricesPanel, BorderLayout.CENTER);
        panel.add(opsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMatrixInput(String title, JTextField[][] fields) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(new Color(55, 55, 55));
        panel.setBorder(new TitledBorder(
                new LineBorder(Color.GRAY, 2),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Microsoft YaHei UI", Font.BOLD, 14),
                Color.WHITE
        ));

        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 6, 6));
        gridPanel.setBackground(new Color(55, 55, 55));
        gridPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                fields[i][j] = new JTextField("0");
                fields[i][j].setFont(new Font("Consolas", Font.BOLD, 16));
                fields[i][j].setHorizontalAlignment(JTextField.CENTER);
                fields[i][j].setBackground(new Color(70, 70, 70));
                fields[i][j].setForeground(Color.WHITE);
                fields[i][j].setCaretColor(Color.WHITE);
                gridPanel.add(fields[i][j]);
            }
        }

        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createEquationPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.setBackground(new Color(55, 55, 55));
        inputPanel.setBorder(new TitledBorder(
                new LineBorder(Color.GRAY, 2),
                "输入方程",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Microsoft YaHei UI", Font.BOLD, 14),
                Color.WHITE
        ));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                inputPanel.getBorder(),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JTextField equationField = new JTextField();
        equationField.setFont(new Font("Consolas", Font.PLAIN, 18));
        equationField.setBackground(new Color(70, 70, 70));
        equationField.setForeground(Color.WHITE);
        equationField.setCaretColor(Color.WHITE);

        JLabel hint = new JLabel("例: 2*x + 3 = 7 或 x^2 - 4 = 0");
        hint.setFont(new Font("Microsoft YaHei UI", Font.ITALIC, 12));
        hint.setForeground(Color.LIGHT_GRAY);

        inputPanel.add(equationField, BorderLayout.CENTER);
        inputPanel.add(hint, BorderLayout.SOUTH);

        JButton solveBtn = createStyledButton("求解方程", ButtonType.EQUALS);
        solveBtn.setPreferredSize(new Dimension(200, 55));
        solveBtn.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        solveBtn.addActionListener(e -> solveEquation(equationField.getText()));

        panel.add(inputPanel, BorderLayout.CENTER);
        panel.add(solveBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createGraphPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(40, 40, 40));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel inputPanel = new JPanel(new BorderLayout(8, 8));
        inputPanel.setBackground(new Color(55, 55, 55));
        inputPanel.setBorder(new TitledBorder(
                new LineBorder(Color.GRAY, 2),
                "函数表达式 y = f(x)",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Microsoft YaHei UI", Font.BOLD, 14),
                Color.WHITE
        ));
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                inputPanel.getBorder(),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JTextField funcField = new JTextField("sin(x)");
        funcField.setFont(new Font("Consolas", Font.PLAIN, 18));
        funcField.setBackground(new Color(70, 70, 70));
        funcField.setForeground(Color.WHITE);
        funcField.setCaretColor(Color.WHITE);

        inputPanel.add(funcField, BorderLayout.CENTER);

        JPanel quickPanel = new JPanel(new GridLayout(3, 3, 6, 6));
        quickPanel.setBackground(new Color(40, 40, 40));
        quickPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        String[] quickFuncs = {
                "sin(x)", "cos(x)", "tan(x)",
                "x^2", "x^3", "sqrt(x)",
                "exp(x)", "log(x)", "abs(x)"
        };

        for (String func : quickFuncs) {
            JButton btn = createStyledButton(func, ButtonType.FUNCTION);
            btn.addActionListener(e -> funcField.setText(func));
            quickPanel.add(btn);
        }

        JButton plotBtn = createStyledButton("绘制图形", ButtonType.EQUALS);
        plotBtn.setPreferredSize(new Dimension(200, 55));
        plotBtn.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        plotBtn.addActionListener(e -> plotGraph(funcField.getText()));

        JPanel topPanel = new JPanel(new BorderLayout(12, 12));
        topPanel.setBackground(new Color(40, 40, 40));
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(quickPanel, BorderLayout.CENTER);
        topPanel.add(plotBtn, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);

        return panel;
    }

    private JButton createStyledButton(String text, ButtonType type) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 13));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        Color bgColor;
        if (type == ButtonType.NUMBER) {
            bgColor = new Color(75, 75, 75);
        } else if (type == ButtonType.OPERATOR) {
            bgColor = new Color(200, 100, 40);
        } else if (type == ButtonType.FUNCTION) {
            bgColor = new Color(40, 100, 180);
        } else if (type == ButtonType.SPECIAL) {
            bgColor = new Color(180, 40, 40);
        } else if (type == ButtonType.EQUALS) {
            bgColor = new Color(40, 150, 40);
        } else {
            bgColor = new Color(120, 40, 150);
        }

        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setBorder(new EmptyBorder(8, 12, 8, 12));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        button.addActionListener(e -> handleButtonClick(text));

        return button;
    }

    private void handleButtonClick(String text) {
        String current = display.getText().trim();

        if (text.equals("AC")) {
            display.setText("0");
            statusLabel.setText("DEG | M: " + engine.getMemory());
        } else if (text.equals("DEL")) {
            if (current.length() > 1) {
                display.setText(current.substring(0, current.length() - 1));
            } else {
                display.setText("0");
            }
        } else if (text.equals("=")) {
            try {
                String result = engine.calculate(current);
                display.setText(result);
                statusLabel.setText("DEG | M: " + engine.getMemory());
            } catch (Exception ex) {
                display.setText("错误: " + ex.getMessage());
            }
        } else if (text.equals("ANS")) {
            if (current.equals("0")) {
                display.setText(engine.getLastAnswer());
            } else {
                display.setText(current + engine.getLastAnswer());
            }
        } else if (text.equals("MC")) {
            engine.memoryClear();
            statusLabel.setText("DEG | M: 0");
        } else if (text.equals("MR")) {
            display.setText(String.valueOf(engine.memoryRecall()));
        } else if (text.equals("M+") || text.equals("M-") || text.equals("MS")) {
            try {
                double val = Double.parseDouble(current);
                if (text.equals("M+")) {
                    engine.memoryAdd(val);
                } else if (text.equals("M-")) {
                    engine.memorySubtract(val);
                } else {
                    engine.memoryStore(val);
                }
                statusLabel.setText("DEG | M: " + engine.getMemory());
            } catch (NumberFormatException ex) {
                display.setText("错误: 无效数字");
            }
        } else {
            if (current.equals("0") || current.startsWith("错误")) {
                display.setText(text);
            } else {
                display.setText(current + text);
            }
        }
    }

    private void performMatrixOperation(String operation) {
        try {
            double[][] matrixA = readMatrix(matrixAFields);
            double[][] matrixB = readMatrix(matrixBFields);

            String result = MatrixCalculator.performOperation(operation, matrixA, matrixB);
            display.setText(result);
        } catch (Exception ex) {
            display.setText("矩阵错误: " + ex.getMessage());
        }
    }

    private double[][] readMatrix(JTextField[][] fields) throws NumberFormatException {
        double[][] matrix = new double[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                matrix[i][j] = Double.parseDouble(fields[i][j].getText().trim());
            }
        }
        return matrix;
    }

    private void solveEquation(String equation) {
        try {
            String result = engine.solveEquation(equation);
            display.setText(result);
        } catch (Exception ex) {
            display.setText("方程错误: " + ex.getMessage());
        }
    }

    private void plotGraph(String function) {
        try {
            GraphPlotter plotter = new GraphPlotter(function);
            plotter.setVisible(true);
        } catch (Exception ex) {
            display.setText("绘图错误: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Windows 系统外观
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            CasioCalculator calculator = new CasioCalculator();
            calculator.setVisible(true);
        });
    }

    enum ButtonType {
        NUMBER, OPERATOR, FUNCTION, SPECIAL, EQUALS, MEMORY
    }
}