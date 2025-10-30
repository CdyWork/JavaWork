package com.example;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class CalculatorEngine {
    private double memory = 0;
    private String lastAnswer = "0";
    
    public String calculate(String expression) throws Exception {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("表达式不能为空");
        }
        
        expression = preprocessExpression(expression);
        
        try {
            Expression exp = new ExpressionBuilder(expression).build();
            double result = exp.evaluate();
            
            if (Double.isNaN(result)) {
                throw new ArithmeticException("结果未定义");
            }
            if (Double.isInfinite(result)) {
                throw new ArithmeticException("结果为无穷大");
            }
            
            lastAnswer = formatResult(result);
            return lastAnswer;
            
        } catch (Exception e) {
            throw new Exception("计算错误: " + e.getMessage());
        }
    }
    
    private String preprocessExpression(String expr) {
        expr = expr.replace("×", "*");
        expr = expr.replace("÷", "/");
        expr = expr.replace("π", String.valueOf(Math.PI));
        expr = expr.replace("e", String.valueOf(Math.E));
        expr = expr.replace("(−)", "(-1)");
        return expr;
    }
    
    public String solveEquation(String equation) throws Exception {
        if (!equation.contains("=")) {
            throw new IllegalArgumentException("方程必须包含等号");
        }
        
        String[] parts = equation.split("=");
        if (parts.length != 2) {
            throw new IllegalArgumentException("方程格式错误");
        }
        
        String left = parts[0].trim();
        String right = parts[1].trim();
        
        // 简单数值求解
        for (double x = -100; x <= 100; x += 0.1) {
            try {
                String leftExpr = left.replace("x", "(" + x + ")");
                String rightExpr = right.replace("x", "(" + x + ")");
                
                Expression expLeft = new ExpressionBuilder(leftExpr).build();
                Expression expRight = new ExpressionBuilder(rightExpr).build();
                
                double valLeft = expLeft.evaluate();
                double valRight = expRight.evaluate();
                
                if (Math.abs(valLeft - valRight) < 0.001) {
                    return "解: x ≈ " + formatResult(x);
                }
            } catch (Exception ignored) {
            }
        }
        
        return "在范围 [-100, 100] 内未找到解";
    }
    
    private String formatResult(double result) {
        if (Math.abs(result - Math.round(result)) < 1e-10) {
            return String.valueOf((long) Math.round(result));
        }
        return String.format("%.6f", result).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
    
    public void memoryClear() {
        memory = 0;
    }
    
    public double memoryRecall() {
        return memory;
    }
    
    public void memoryAdd(double value) {
        memory += value;
    }
    
    public void memorySubtract(double value) {
        memory -= value;
    }
    
    public void memoryStore(double value) {
        memory = value;
    }
    
    public double getMemory() {
        return memory;
    }
    
    public String getLastAnswer() {
        return lastAnswer;
    }
}
