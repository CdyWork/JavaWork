package com.example;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CasioCalculator - 增强版 UI
 *
 * 特性：
 *  - 动态矩阵输入：用户可指定行列数（NxM）生成 JTextField 网格；
 *    支持从网格读取矩阵或直接粘贴多行文本解析矩阵字符串（parseMatrixFromString）。
 *  - 方程面板：多行输入，支持分号或换行分隔方程组，自动识别并求解多元线性方程组或单方程。
 *  - 增强函数绘图：支持复合函数快捷输入和常用函数模板
 *  - 保留普通计算、函数绘图模块（调用 CalculatorEngine 与 GraphPlotter）。
 *
 * 注意：需要与增强版 CalculatorEngine 和 GraphPlotter 一并使用。
 */
public class CasioCalculator extends JFrame {

    static {
        setupWindowsChineseFont();
    }

    private final CalculatorEngine engine;

    // 显示与状态
    private JTextArea display;
    private JLabel statusLabel;

    // 动态矩阵 A/B 的容器（使用 List 以支持动态大小）
    private JPanel matrixAContainer;
    private JPanel matrixBContainer;
    private List<List<JTextField>> matrixAFields = new ArrayList<>();
    private List<List<JTextField>> matrixBFields = new ArrayList<>();
    private JTextField matrixARowsField;
    private JTextField matrixAColsField;
    private JTextField matrixBRowsField;
    private JTextField matrixBColsField;
    private JTextArea matrixATextArea; // 支持粘贴文本解析矩阵
    private JTextArea matrixBTextArea;

    // 方程组输入（多行）
    private JTextArea equationsTextArea;

    public CasioCalculator() {
        super("Calculator by CDY - Dynamic Matrix & Multivariate Solver");
        engine = new CalculatorEngine();
        initializeUI();
    }

    private static void setupWindowsChineseFont() {
        try {
            // 不尝试在运行时修改 JVM file.encoding（不会生效），仅设置 Swing 字体
            String fontName = "Microsoft YaHei UI";
            Font plainFont = new Font(fontName, Font.PLAIN, 13);
            Font boldFont = new Font(fontName, Font.BOLD, 14);
            Font displayFont = new Font("Consolas", Font.BOLD, 20);

            UIManager.put("Button.font", plainFont);
            UIManager.put("Label.font", plainFont);
            UIManager.put("TextField.font", plainFont);
            UIManager.put("TextArea.font", displayFont);
            UIManager.put("TabbedPane.font", boldFont);
            UIManager.put("TitledBorder.font", boldFont);

            System.out.println("Fonts set: " + fontName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 820);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(new EmptyBorder(12, 12, 12, 12));
        main.setBackground(new Color(36, 36, 36));

        main.add(createTopBar(), BorderLayout.NORTH);
        main.add(createDisplayPane(), BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("普通计算", createNormalPanel());
        tabs.addTab("矩阵运算 (动态大小)", createMatrixPanel());
        tabs.addTab("方程求解", createEquationPanel());
        tabs.addTab("函数绘图", createGraphPanel());

        main.add(tabs, BorderLayout.SOUTH);

        setContentPane(main);
    }

    private JPanel createTopBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(18, 80, 140));
        p.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel title = new JLabel("CDY Calculator");
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        statusLabel = new JLabel("DEG | M: 0");
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(200, 230, 255));

        p.add(title, BorderLayout.WEST);
        p.add(statusLabel, BorderLayout.EAST);
        return p;
    }

    private JScrollPane createDisplayPane() {
        display = new JTextArea(4, 60);
        display.setText("0");
        display.setEditable(true);
        display.setLineWrap(true);
        display.setWrapStyleWord(true);
        display.setMargin(new Insets(8, 12, 8, 12));
        display.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 20));
        display.setBackground(new Color(230, 250, 230));
        display.setForeground(new Color(10, 50, 10));

        JScrollPane sp = new JScrollPane(display);
        sp.setBorder(new LineBorder(new Color(0, 120, 0), 4));
        sp.setPreferredSize(new Dimension(1000, 120));
        return sp;
    }

    /* ------------------ 普通计算面板 ------------------ */
    private JPanel createNormalPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setPreferredSize(new Dimension(1000, 360));
        panel.setBackground(new Color(36, 36, 36));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.setBackground(new Color(36, 36, 36));

        JPanel buttons = new JPanel(new GridLayout(8, 5, 6, 6));
        buttons.setBackground(new Color(36, 36, 36));

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
            for (String t : row) {
                buttons.add(createStyledButton(t));
            }
        }

        center.add(buttons, BorderLayout.CENTER);

        JPanel memPanel = new JPanel(new GridLayout(1, 5, 6, 6));
        memPanel.setBackground(new Color(36, 36, 36));
        String[] mems = {"MC", "MR", "M+", "M-", "MS"};
        for (String m : mems) memPanel.add(createStyledButton(m));
        center.add(memPanel, BorderLayout.SOUTH);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(80, 80, 80));
        btn.setForeground(Color.BLACK);
        btn.setBorder(new EmptyBorder(8, 12, 8, 12));
        btn.addActionListener(e -> handleButtonClick(text));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(btn.getBackground().brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(80, 80, 80)); }
        });
        return btn;
    }

    private void handleButtonClick(String text) {
        String cur = display.getText();
        if (text.equals("AC")) {
            display.setText("0");
            statusLabel.setText("DEG | M: " + engine.getMemory());
        } else if (text.equals("DEL")) {
            if (cur.length() <= 1) display.setText("0");
            else display.setText(cur.substring(0, cur.length() - 1));
        } else if (text.equals("=")) {
            try {
                String res = engine.calculate(cur);
                display.setText(res);
                statusLabel.setText("DEG | M: " + engine.getMemory());
            } catch (Exception ex) {
                display.setText("错误: " + ex.getMessage());
            }
        } else if (text.equals("ANS")) {
            display.setText(engine.getLastAnswer());
        } else if (text.equals("MC")) {
            engine.memoryClear();
            statusLabel.setText("DEG | M: " + engine.getMemory());
        } else if (text.equals("MR")) {
            display.setText(String.valueOf(engine.memoryRecall()));
        } else if (text.equals("M+")) {
            try { engine.memoryAdd(Double.parseDouble(display.getText())); statusLabel.setText("DEG | M: " + engine.getMemory()); }
            catch (Exception ex) { display.setText("错误: 无效数字"); }
        } else if (text.equals("M-")) {
            try { engine.memorySubtract(Double.parseDouble(display.getText())); statusLabel.setText("DEG | M: " + engine.getMemory()); }
            catch (Exception ex) { display.setText("错误: 无效数字"); }
        } else if (text.equals("MS")) {
            try { engine.memoryStore(Double.parseDouble(display.getText())); statusLabel.setText("DEG | M: " + engine.getMemory()); }
            catch (Exception ex) { display.setText("错误: 无效数字"); }
        } else {
            if (cur.equals("0") || cur.startsWith("错误")) display.setText(text);
            else display.setText(cur + text);
        }
    }

    /* ------------------ 矩阵面板（动态大小） ------------------ */

    private JPanel createMatrixPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(36, 36, 36));
        panel.setPreferredSize(new Dimension(1000, 380));

        JPanel top = new JPanel(new GridLayout(1, 2, 12, 12));
        top.setBackground(new Color(36, 36, 36));

        // 左侧：矩阵 A 控件
        JPanel left = new JPanel(new BorderLayout(6, 6));
        left.setBackground(new Color(48, 48, 48));
        left.setBorder(new TitledBorder(new LineBorder(Color.GRAY, 2), "矩阵 A", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Microsoft YaHei UI", Font.BOLD, 14), Color.WHITE));
        left.add(createMatrixControlPanel(true), BorderLayout.NORTH);
        matrixAContainer = new JPanel();
        matrixAContainer.setBackground(new Color(55, 55, 55));
        left.add(new JScrollPane(matrixAContainer), BorderLayout.CENTER);

        // 右侧：矩阵 B 控件
        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.setBackground(new Color(48, 48, 48));
        right.setBorder(new TitledBorder(new LineBorder(Color.GRAY, 2), "矩阵 B", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Microsoft YaHei UI", Font.BOLD, 14), Color.WHITE));
        right.add(createMatrixControlPanel(false), BorderLayout.NORTH);
        matrixBContainer = new JPanel();
        matrixBContainer.setBackground(new Color(55, 55, 55));
        right.add(new JScrollPane(matrixBContainer), BorderLayout.CENTER);

        top.add(left);
        top.add(right);

        // 操作按钮区
        JPanel ops = new JPanel(new GridLayout(2, 4, 8, 8));
        ops.setBackground(new Color(36, 36, 36));
        String[] opsList = {"A + B", "A - B", "A * B", "det(A)", "A^-1", "A^T", "Parse A from Text", "Parse B from Text"};
        for (String op : opsList) {
            JButton btn = new JButton(op);
            btn.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
            btn.addActionListener(e -> handleMatrixAction(op));
            ops.add(btn);
        }

        panel.add(top, BorderLayout.CENTER);
        panel.add(ops, BorderLayout.SOUTH);

        // 初始化默认矩阵（3x3）
        buildMatrixGrid(true, 3, 3);
        buildMatrixGrid(false, 3, 3);

        return panel;
    }

    private JPanel createMatrixControlPanel(boolean isA) {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBackground(new Color(55, 55, 55));

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        inputRow.setBackground(new Color(55, 55, 55));
        inputRow.add(new JLabel("行:"));
        JTextField rowsField = new JTextField("3", 3);
        inputRow.add(rowsField);
        inputRow.add(new JLabel("列:"));
        JTextField colsField = new JTextField("3", 3);
        inputRow.add(colsField);

        JButton genBtn = new JButton("生成矩阵");
        genBtn.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        genBtn.addActionListener(e -> {
            int r = parsePositiveInt(rowsField.getText(), 3);
            int c = parsePositiveInt(colsField.getText(), 3);
            buildMatrixGrid(isA, r, c);
        });
        inputRow.add(genBtn);

        JTextArea textArea = new JTextArea(3, 20);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane taScroll = new JScrollPane(textArea);
        taScroll.setPreferredSize(new Dimension(260, 70));

        p.add(inputRow, BorderLayout.NORTH);
        p.add(taScroll, BorderLayout.CENTER);

        if (isA) {
            matrixARowsField = rowsField;
            matrixAColsField = colsField;
            matrixATextArea = textArea;
        } else {
            matrixBRowsField = rowsField;
            matrixBColsField = colsField;
            matrixBTextArea = textArea;
        }

        return p;
    }

    private int parsePositiveInt(String s, int defaultVal) {
        try {
            int v = Integer.parseInt(s.trim());
            return Math.max(1, Math.min(v, 200));
        } catch (Exception ex) {
            return defaultVal;
        }
    }

    private void buildMatrixGrid(boolean isA, int rows, int cols) {
        JPanel container = isA ? matrixAContainer : matrixBContainer;
        container.removeAll();
        container.setLayout(new BorderLayout());

        JPanel grid = new JPanel(new GridLayout(rows, cols, 6, 6));
        grid.setBackground(new Color(70, 70, 70));
        List<List<JTextField>> targetList = new ArrayList<>();

        for (int i = 0; i < rows; i++) {
            List<JTextField> rowList = new ArrayList<>();
            for (int j = 0; j < cols; j++) {
                JTextField tf = new JTextField("0");
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(new Font("Consolas", Font.PLAIN, 13));
                tf.setBackground(new Color(60, 60, 60));
                tf.setForeground(Color.WHITE);
                grid.add(tf);
                rowList.add(tf);
            }
            targetList.add(rowList);
        }

        container.add(grid, BorderLayout.CENTER);
        container.revalidate();
        container.repaint();

        if (isA) {
            matrixAFields = targetList;
            matrixARowsField.setText(String.valueOf(rows));
            matrixAColsField.setText(String.valueOf(cols));
        } else {
            matrixBFields = targetList;
            matrixBRowsField.setText(String.valueOf(rows));
            matrixBColsField.setText(String.valueOf(cols));
        }
    }

    private void handleMatrixAction(String op) {
        try {
            double[][] A = null;
            double[][] B = null;
            if (matrixATextArea != null && !matrixATextArea.getText().trim().isEmpty()) {
                try {
                    A = CalculatorEngine.parseMatrixFromString(matrixATextArea.getText());
                } catch (Exception ex) {
                    A = readMatrixFromGrid(matrixAFields);
                }
            } else {
                A = readMatrixFromGrid(matrixAFields);
            }

            if (op.contains("B") || op.contains("*")) {
                if (matrixBTextArea != null && !matrixBTextArea.getText().trim().isEmpty()) {
                    try {
                        B = CalculatorEngine.parseMatrixFromString(matrixBTextArea.getText());
                    } catch (Exception ex) {
                        B = readMatrixFromGrid(matrixBFields);
                    }
                } else {
                    B = readMatrixFromGrid(matrixBFields);
                }
            }

            String result = CalculatorEngine.performMatrixOperation(op, A, B);
            display.setText(result);

        } catch (NumberFormatException nfe) {
            display.setText("矩阵输入错误：请确保所有单元为数字");
        } catch (Exception ex) {
            display.setText("矩阵运算错误: " + ex.getMessage());
        }
    }

    private double[][] readMatrixFromGrid(List<List<JTextField>> gridFields) {
        if (gridFields == null || gridFields.size() == 0) throw new IllegalArgumentException("矩阵未初始化");
        int rows = gridFields.size();
        int cols = gridFields.get(0).size();
        double[][] m = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            List<JTextField> row = gridFields.get(i);
            if (row.size() != cols) throw new IllegalArgumentException("矩阵列数不一致");
            for (int j = 0; j < cols; j++) {
                String s = row.get(j).getText().trim();
                if (s.isEmpty()) s = "0";
                m[i][j] = Double.parseDouble(s);
            }
        }
        return m;
    }

    /* ------------------ 方程求解面板 ------------------ */

    private JPanel createEquationPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(new Color(36, 36, 36));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setPreferredSize(new Dimension(1000, 320));

        JPanel inputPane = new JPanel(new BorderLayout(6, 6));
        inputPane.setBackground(new Color(55, 55, 55));
        inputPane.setBorder(new TitledBorder(new LineBorder(Color.GRAY, 2), "输入方程（多行或用 ; 分隔）",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Microsoft YaHei UI", Font.BOLD, 14), Color.WHITE));

        equationsTextArea = new JTextArea(6, 60);
        equationsTextArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        equationsTextArea.setLineWrap(true);
        equationsTextArea.setWrapStyleWord(true);
        equationsTextArea.setText("例：\nx + 2y - z = 3;\n3x - y + 4z = 1;\n-2x + 5y + 2z = 7");

        inputPane.add(new JScrollPane(equationsTextArea), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(new Color(36, 36, 36));
        JButton solveBtn = new JButton("求解");
        solveBtn.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 14));
        solveBtn.setPreferredSize(new Dimension(140, 44));
        solveBtn.addActionListener(e -> solveEquationsAction());
        south.add(solveBtn);

        p.add(inputPane, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);

        return p;
    }

    private void solveEquationsAction() {
        String input = equationsTextArea.getText();
        if (input == null || input.trim().isEmpty()) {
            display.setText("方程输入为空");
            return;
        }
        String out = engine.solveEquation(input);
        display.setText(out);
    }

    /* ------------------ 函数绘图 ------------------ */

    private JPanel createGraphPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setBackground(new Color(36, 36, 36));
        p.setPreferredSize(new Dimension(1000, 360));

        JPanel input = new JPanel(new BorderLayout(6, 6));
        input.setBackground(new Color(55, 55, 55));
        input.setBorder(new TitledBorder(new LineBorder(Color.GRAY, 2), "函数 y = f(x)",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Microsoft YaHei UI", Font.BOLD, 14), Color.WHITE));

        JTextField funcField = new JTextField("sin(x)");
        funcField.setFont(new Font("Consolas", Font.PLAIN, 16));
        funcField.setBackground(new Color(70, 70, 70));
        funcField.setForeground(Color.WHITE);
        funcField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 100), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        input.add(funcField, BorderLayout.CENTER);

        JPanel quickPanel = new JPanel(new BorderLayout(8, 8));
        quickPanel.setBackground(new Color(36, 36, 36));

        JPanel basicFuncs = new JPanel(new GridLayout(2, 5, 6, 6));
        basicFuncs.setBackground(new Color(36, 36, 36));
        basicFuncs.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
            "基础函数",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Microsoft YaHei UI", Font.PLAIN, 11),
            new Color(180, 180, 180)
        ));
        
        String[] basicList = {
            "sin(x)", "cos(x)", "tan(x)", "x^2", "x^3",
            "sqrt(x)", "exp(x)", "log(x)", "abs(x)", "1/x"
        };
        for (String q : basicList) {
            JButton b = createQuickFunctionButton(q, funcField);
            basicFuncs.add(b);
        }

        JPanel compositeFuncs = new JPanel(new GridLayout(2, 5, 6, 6));
        compositeFuncs.setBackground(new Color(36, 36, 36));
        compositeFuncs.setBorder(new TitledBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
            "复合函数",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Microsoft YaHei UI", Font.PLAIN, 11),
            new Color(180, 180, 180)
        ));
        
        String[] compositeList = {
            "sin(cos(x))", "cos(sin(x))", "exp(-x^2)", "log(abs(x))", "sqrt(x^2+1)",
            "sin(x)*cos(x)", "x*exp(-x)", "sin(x)/x", "tan(x^2)", "(x^2-1)/(x^2+1)"
        };
        for (String q : compositeList) {
            JButton b = createQuickFunctionButton(q, funcField);
            compositeFuncs.add(b);
        }

        quickPanel.add(basicFuncs, BorderLayout.NORTH);
        quickPanel.add(compositeFuncs, BorderLayout.CENTER);

        JButton plotBtn = new JButton("绘制图形");
        plotBtn.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        plotBtn.setPreferredSize(new Dimension(180, 50));
        plotBtn.setBackground(new Color(0, 120, 215));
        plotBtn.setForeground(Color.BLACK);
        plotBtn.setFocusPainted(false);
        plotBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        plotBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                plotBtn.setBackground(new Color(0, 140, 255));
            }
            public void mouseExited(MouseEvent e) {
                plotBtn.setBackground(new Color(0, 120, 215));
            }
        });
        plotBtn.addActionListener(e -> {
            String f = funcField.getText();
            if (f == null || f.trim().isEmpty()) {
                display.setText("函数输入为空");
                return;
            }
            try {
                GraphPlotter gp = new GraphPlotter(f);
                gp.setVisible(true);
                display.setText("已打开绘图窗口: y = " + f);
            } catch (Exception ex) {
                display.setText("绘图错误: " + ex.getMessage());
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        south.setBackground(new Color(36, 36, 36));
        
        JButton helpBtn = new JButton("帮助");
        helpBtn.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 12));
        helpBtn.setFocusPainted(false);
        helpBtn.addActionListener(e -> showFunctionHelp());
        south.add(helpBtn);
        
        south.add(plotBtn);

        p.add(input, BorderLayout.NORTH);
        p.add(quickPanel, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);

        return p;
    }

    private JButton createQuickFunctionButton(String funcText, JTextField targetField) {
        JButton b = new JButton(funcText);
        b.setFont(new Font("Consolas", Font.PLAIN, 11));
        b.setBackground(new Color(70, 70, 70));
        b.setForeground(new Color(20, 20, 220));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> targetField.setText(funcText));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(90, 90, 90));
            }
            public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(70, 70, 70));
            }
        });
        return b;
    }

    private void showFunctionHelp() {
        String helpText = 
            "═══════════════════════════════════════\n" +
            "              函数绘图帮助\n" +
            "═══════════════════════════════════════\n\n" +
            "【基础函数】\n" +
            "  • 三角函数: sin(x), cos(x), tan(x)\n" +
            "  • 指数对数: exp(x), log(x), sqrt(x)\n" +
            "  • 幂函数: x^2, x^3, x^n\n" +
            "  • 其他: abs(x), 1/x\n\n" +
            "【复合函数】\n" +
            "  • 嵌套函数: sin(cos(x)), log(abs(x))\n" +
            "  • 四则运算: sin(x)*cos(x), x*exp(-x)\n" +
            "  • 分式: sin(x)/x, (x^2-1)/(x^2+1)\n" +
            "  • 多项式: x^3 - 2*x^2 + x - 1\n\n" +
            "【常用示例】\n" +
            "  • 高斯函数: exp(-x^2)\n" +
            "  • 双曲正弦: (exp(x)-exp(-x))/2\n" +
            "  • 洛伦兹函数: 1/(1+x^2)\n" +
            "  • 阻尼振荡: exp(-x)*sin(5*x)\n\n" +
            "【运算符】\n" +
            "  + - * / ^  (加减乘除乘方)\n" +
            "  支持括号嵌套\n\n" +
            "【提示】\n" +
            "  • 绘图窗口可调整 X 范围和步长\n" +
            "  • 使用鼠标滚轮可缩放图形\n" +
            "  • 自动处理无效值（如 log(0)）\n" +
            "═══════════════════════════════════════";
        
        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 13));
        textArea.setBackground(new Color(250, 250, 250));
        textArea.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(480, 520));
        
        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "函数绘图帮助",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    /* ------------------ 启动 ------------------ */

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            CasioCalculator app = new CasioCalculator();
            app.setVisible(true);
        });
    }
}