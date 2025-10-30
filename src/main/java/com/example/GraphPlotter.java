package com.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import javax.swing.*;
import java.awt.*;

public class GraphPlotter extends JFrame {
    
    public GraphPlotter(String function) {
        setTitle("函数图形: " + function);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        XYSeries series = new XYSeries(function);
        
        try {
            for (double x = -10; x <= 10; x += 0.1) {
                Expression exp = new ExpressionBuilder(function)
                    .variable("x")
                    .build()
                    .setVariable("x", x);
                
                double y = exp.evaluate();
                
                if (!Double.isNaN(y) && !Double.isInfinite(y) && Math.abs(y) < 1000) {
                    series.add(x, y);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "函数解析错误: " + e.getMessage());
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
            "y = " + function,
            "x",
            "y",
            dataset
        );
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));
        setContentPane(chartPanel);
    }
}