package com.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * GraphPlotter - 增强版函数绘图器
 * 
 * 新特性：
 * - 支持复合函数：sin(cos(x)), exp(x^2), log(abs(x)), sqrt(x^2+1) 等
 * - 支持更多数学函数：sqrt, abs, sin, cos, tan, log, ln, exp 等
 * - 自适应 Y 轴范围
 * - 可调整 X 轴范围和采样精度
 * - 更好的异常值处理
 */
public class GraphPlotter extends JFrame {
    
    private String function;
    private double xMin = -10;
    private double xMax = 10;
    private double step = 0.05;
    private ChartPanel chartPanel;
    
    public GraphPlotter(String function) {
        this.function = preprocessFunction(function);
        setTitle("函数图形: " + function);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initUI();
        plotFunction();
    }
    
    /**
     * 预处理函数字符串，使其兼容 exp4j 语法
     */
    private String preprocessFunction(String func) {
        if (func == null || func.trim().isEmpty()) {
            return "x";
        }
        
        String processed = func.trim();
        
        // 替换常见的数学符号
        processed = processed.replace("×", "*");
        processed = processed.replace("÷", "/");
        processed = processed.replace("π", "pi");
        processed = processed.replace("√", "sqrt");
        
        // 处理隐式乘法：2x -> 2*x, xsin -> x*sin
        processed = processed.replaceAll("(\\d)([a-zA-Z])", "$1*$2");
        processed = processed.replaceAll("([a-zA-Z])(\\d)", "$1*$2");
        processed = processed.replaceAll("\\)\\(", ")*(");
        processed = processed.replaceAll("(\\d)\\(", "$1*(");
        processed = processed.replaceAll("\\)(\\d)", ")*$1");
        
        // ln 转换为 log (exp4j 中 log 是自然对数)
        if (processed.contains("ln(")) {
            processed = processed.replace("ln(", "log(");
        }
        
        return processed;
    }
    
    /**
     * 初始化用户界面
     */
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 控制面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBackground(new Color(240, 240, 240));
        
        controlPanel.add(new JLabel("X 范围: "));
        JTextField xMinField = new JTextField(String.valueOf(xMin), 5);
        controlPanel.add(xMinField);
        
        controlPanel.add(new JLabel(" 到 "));
        JTextField xMaxField = new JTextField(String.valueOf(xMax), 5);
        controlPanel.add(xMaxField);
        
        controlPanel.add(new JLabel("  步长: "));
        JTextField stepField = new JTextField(String.valueOf(step), 5);
        controlPanel.add(stepField);
        
        JButton refreshBtn = new JButton("刷新图形");
        refreshBtn.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 12));
        refreshBtn.addActionListener((ActionEvent e) -> {
            try {
                xMin = Double.parseDouble(xMinField.getText());
                xMax = Double.parseDouble(xMaxField.getText());
                step = Double.parseDouble(stepField.getText());
                
                if (xMin >= xMax) {
                    JOptionPane.showMessageDialog(this, "X 最小值必须小于最大值！");
                    return;
                }
                if (step <= 0 || step > (xMax - xMin)) {
                    JOptionPane.showMessageDialog(this, "步长必须大于 0 且小于范围！");
                    return;
                }
                
                plotFunction();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的数字！");
            }
        });
        controlPanel.add(refreshBtn);
        
        // 预设按钮
        JPanel presetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        presetPanel.setBackground(new Color(240, 240, 240));
        presetPanel.add(new JLabel("快速设置: "));
        
        String[][] presets = {
            {"[-10, 10]", "-10", "10", "0.05"},
            {"[-5, 5]", "-5", "5", "0.02"},
            {"[0, 20]", "0", "20", "0.1"},
            {"[-π, π]", String.valueOf(-Math.PI), String.valueOf(Math.PI), "0.05"}
        };
        
        for (String[] preset : presets) {
            JButton btn = new JButton(preset[0]);
            btn.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 11));
            btn.addActionListener(e -> {
                xMinField.setText(preset[1]);
                xMaxField.setText(preset[2]);
                stepField.setText(preset[3]);
            });
            presetPanel.add(btn);
        }
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(presetPanel, BorderLayout.SOUTH);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // 图表面板占位
        JPanel chartContainer = new JPanel(new BorderLayout());
        mainPanel.add(chartContainer, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    /**
     * 绘制函数图形
     */
    private void plotFunction() {
        XYSeries series = new XYSeries(function);
        
        double yMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;
        int validPoints = 0;
        
        try {
            // 构建表达式（支持复合函数）
            ExpressionBuilder builder = new ExpressionBuilder(function)
                .variable("x");
            
            Expression exp = builder.build();
            
            // 采样计算
            for (double x = xMin; x <= xMax; x += step) {
                try {
                    exp.setVariable("x", x);
                    double y = exp.evaluate();
                    
                    // 过滤无效值
                    if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                        // 动态调整 Y 轴范围
                        if (Math.abs(y) < 10000) {  // 防止极端值
                            series.add(x, y);
                            yMin = Math.min(yMin, y);
                            yMax = Math.max(yMax, y);
                            validPoints++;
                        }
                    }
                } catch (Exception e) {
                    // 跳过单个点的计算错误（如 log(0), sqrt(-1) 等）
                }
            }
            
            if (validPoints == 0) {
                JOptionPane.showMessageDialog(this, 
                    "无法计算函数值，请检查函数定义或调整 X 范围！\n" +
                    "函数: " + function);
                return;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "函数解析错误: " + e.getMessage() + "\n\n" +
                "支持的函数格式示例：\n" +
                "- 复合函数: sin(cos(x)), exp(x^2), log(abs(x))\n" +
                "- 多项式: x^3 - 2*x^2 + x - 1\n" +
                "- 三角函数: sin(x), cos(x), tan(x)\n" +
                "- 指数对数: exp(x), log(x), sqrt(x)\n" +
                "- 其他: abs(x), x! (阶乘需用 factorial(x))");
            return;
        }
        
        // 创建数据集
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        
        // 创建图表
        JFreeChart chart = ChartFactory.createXYLineChart(
            "y = " + function,
            "x",
            "y",
            dataset
        );
        
        // 自定义图表样式
        chart.setBackgroundPaint(Color.WHITE);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(250, 250, 250));
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
        
        // 设置线条样式
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, new Color(0, 100, 200));
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(renderer);
        
        // 更新或创建图表面板
        if (chartPanel != null) {
            getContentPane().remove(chartPanel);
        }
        
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(880, 580));
        chartPanel.setMouseWheelEnabled(true);
        
        JPanel container = (JPanel) ((JPanel) getContentPane()).getComponent(1);
        container.removeAll();
        container.add(chartPanel, BorderLayout.CENTER);
        container.revalidate();
        container.repaint();
        
        // 显示统计信息
        String info = String.format("已绘制 %d 个点 | Y 范围: [%.3f, %.3f]", 
            validPoints, yMin, yMax);
        setTitle("函数图形: " + function + " - " + info);
    }
    
    // 测试主函数
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 测试复合函数
            String[] testFunctions = {
                "sin(cos(x))",
                "exp(x^2)",
                "log(abs(x))",
                "sqrt(x^2+1)",
                "sin(x)*cos(x)",
                "x^3-2*x^2+x-1"
            };
            
            GraphPlotter plotter = new GraphPlotter(testFunctions[0]);
            plotter.setVisible(true);
        });
    }
}