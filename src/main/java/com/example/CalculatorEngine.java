package com.example;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.ejml.simple.SimpleMatrix;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculatorEngine {

    private double memory = 0;
    private String lastAnswer = "0";

    public CalculatorEngine() {}

    /* ------------------ 普通表达式计算 ------------------ */

    public String calculate(String expression) throws Exception {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("表达式不能为空");
        }
        expression = preprocessExpression(expression);
        try {
            Expression exp = new ExpressionBuilder(expression).build();
            double result = exp.evaluate();

            if (Double.isNaN(result)) throw new ArithmeticException("结果未定义");
            if (Double.isInfinite(result)) throw new ArithmeticException("结果为无穷大");

            lastAnswer = formatResult(result);
            return lastAnswer;
        } catch (ArithmeticException e) {
            throw new Exception("算术错误: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("计算错误: " + e.getMessage());
        }
    }

    private String preprocessExpression(String expr) {
        if (expr == null) return "";

        expr = expr.replace("×", "*");
        expr = expr.replace("÷", "/");
        expr = expr.replace("π", String.valueOf(Math.PI));
        expr = expr.replace("(", "(").replace(")", ")");
        expr = expr.replace("(−)", "(-1)");
        expr = expr.replace(" ", "");

        expr = expr.replaceAll("(?<![a-zA-Z])e(?![a-zA-Z0-9])", String.valueOf(Math.E));

        return expr;
    }

    /* ------------------ 方程组求解 ------------------ */

    public String solveEquation(String input) {
        if (input == null || input.trim().isEmpty()) return "方程不能为空";

        String trimmed = input.trim();
        boolean looksLikeSystem = trimmed.contains(";") || trimmed.split("[\\r\\n]+").length > 1;
        int eqCount = countChar(trimmed, '=');
        if (eqCount > 1) looksLikeSystem = true;

        try {
            if (looksLikeSystem) {
                String[] eqs = Arrays.stream(trimmed.split("[;\\n\\r]+"))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toArray(String[]::new);

                // 修复问题2：改进线性方程组检测
                if (isLinearSystem(eqs)) {
                    Map<String, Double> sol = LinearSolver.solveLinearSystemFromStrings(eqs);
                    StringBuilder sb = new StringBuilder();
                    sb.append("线性方程组解：\n");
                    sol.forEach((k, v) -> sb.append(String.format("%s = %s%n", k, formatResult(v))));
                    lastAnswer = sb.toString();
                    return sb.toString();
                } else {
                    Map<String, Double> sol = NonlinearSolver.solveNonlinearSystem(eqs);
                    StringBuilder sb = new StringBuilder();
                    sb.append("非线性方程组数值解：\n");
                    sol.forEach((k, v) -> sb.append(String.format("%s ≈ %s%n", k, formatResult(v))));
                    sb.append("\n(数值解，可能存在误差)");
                    lastAnswer = sb.toString();
                    return sb.toString();
                }
            } else {
                if (!trimmed.contains("=")) return "方程必须包含等号 (=)";

                String[] parts = trimmed.split("=");
                if (parts.length != 2) return "方程格式错误";

                String left = parts[0].trim();
                String right = parts[1].trim();

                String exprTemplate = "(" + left + ")-(" + right + ")";
                Double root = findRootForSingleVariable(exprTemplate, "x", -1000, 1000);
                if (root == null) {
                    return "在搜索区间内未找到实根";
                } else {
                    String out = "解: x ≈ " + formatResult(root);
                    lastAnswer = out;
                    return out;
                }
            }
        } catch (IllegalArgumentException iae) {
            return "解析错误: " + iae.getMessage();
        } catch (Exception ex) {
            return "求解失败: " + ex.getMessage();
        }
    }

    /**
     * 修复问题2：改进的线性方程组检测
     */
    private boolean isLinearSystem(String[] equations) {
        for (String eq : equations) {
            if (eq == null || eq.trim().isEmpty()) continue;
            String[] sides = eq.split("=");
            if (sides.length != 2) continue;

            String lhs = sides[0].trim();

            // 检测非线性特征
            // 1. 幂运算
            if (lhs.contains("^")) return false;

            // 2. 变量相乘 (如 x*y)
            if (lhs.matches(".*[a-zA-Z]\\s*\\*\\s*[a-zA-Z].*")) return false;

            // 3. 三角函数和其他非线性函数
            if (lhs.contains("sin") || lhs.contains("cos") || lhs.contains("tan") ||
                    lhs.contains("exp") || lhs.contains("log") || lhs.contains("sqrt")) {
                return false;
            }
        }
        return true;
    }

    private int countChar(String s, char c) {
        int cnt = 0;
        for (char ch : s.toCharArray()) if (ch == c) cnt++;
        return cnt;
    }

    private Double findRootForSingleVariable(String expressionTemplate, String varName, double min, double max) {
        int steps = 400;
        double step = (max - min) / steps;
        Double prevX = null;
        Double prevY = null;

        for (int i = 0; i <= steps; i++) {
            double x = min + i * step;
            double y;
            try {
                Expression exp = new ExpressionBuilder(expressionTemplate)
                        .variable(varName)
                        .build()
                        .setVariable(varName, x);
                y = exp.evaluate();
                if (Double.isNaN(y) || Double.isInfinite(y)) {
                    prevX = x;
                    prevY = y;
                    continue;
                }
            } catch (Exception e) {
                return null;
            }

            if (prevY != null && !Double.isNaN(prevY) && !Double.isInfinite(prevY)) {
                if (prevY == 0.0) return prevX;
                if (y == 0.0) return x;
                if (prevY * y < 0.0) {
                    double a = prevX, b = x;
                    double fa = prevY, fb = y;
                    for (int iter = 0; iter < 60; iter++) {
                        double mid = 0.5 * (a + b);
                        double fmid;
                        try {
                            Expression emid = new ExpressionBuilder(expressionTemplate)
                                    .variable(varName)
                                    .build()
                                    .setVariable(varName, mid);
                            fmid = emid.evaluate();
                        } catch (Exception e) {
                            break;
                        }
                        if (Double.isNaN(fmid) || Double.isInfinite(fmid)) break;
                        if (Math.abs(fmid) < 1e-10) return mid;
                        if (fa * fmid <= 0) {
                            b = mid;
                            fb = fmid;
                        } else {
                            a = mid;
                            fa = fmid;
                        }
                    }
                    return 0.5 * (a + b);
                }
            }
            prevX = x;
            prevY = y;
        }
        return null;
    }

    /* ------------------ 非线性方程组求解器 ------------------ */

    public static class NonlinearSolver {

        private static final double EPSILON = 1e-8;
        private static final int MAX_ITERATIONS = 100;
        private static final double DELTA = 1e-6;

        public static Map<String, Double> solveNonlinearSystem(String[] equations) {
            List<String> variables = extractVariables(equations);
            int n = variables.size();

            if (n == 0) throw new IllegalArgumentException("未检测到未知数");
            if (equations.length != n) {
                throw new IllegalArgumentException(
                        String.format("方程数 (%d) 与未知数数 (%d) 不一致", equations.length, n));
            }

            List<String> functions = new ArrayList<>();
            for (String eq : equations) {
                String[] parts = eq.split("=");
                if (parts.length != 2) throw new IllegalArgumentException("方程格式错误: " + eq);
                functions.add("(" + parts[0].trim() + ")-(" + parts[1].trim() + ")");
            }

            double[][] initialGuesses = {
                    {1.0, 1.0, 1.0},
                    {0.5, 0.5, 0.5},
                    {2.0, 2.0, 2.0},
                    {-1.0, -1.0, -1.0},
                    {0.1, 0.1, 0.1},
                    {5.0, 5.0, 5.0},
                    {3.0, 4.0, 5.0},
                    {-2.0, 3.0, -1.0}
            };

            Exception lastException = null;

            for (double[] guess : initialGuesses) {
                try {
                    double[] x0 = new double[n];
                    for (int i = 0; i < n; i++) {
                        x0[i] = i < guess.length ? guess[i] : 1.0;
                    }

                    double[] solution = newtonRaphson(functions, variables, x0);

                    if (verifySolution(functions, variables, solution)) {
                        Map<String, Double> result = new LinkedHashMap<>();
                        for (int i = 0; i < n; i++) {
                            result.put(variables.get(i), solution[i]);
                        }
                        return result;
                    }
                } catch (Exception e) {
                    lastException = e;
                }
            }

            if (lastException != null) {
                throw new RuntimeException("所有初始值尝试均失败，最后错误: " + lastException.getMessage());
            } else {
                throw new RuntimeException("无法找到有效的数值解");
            }
        }

        private static boolean verifySolution(List<String> functions,
                                              List<String> variables,
                                              double[] x) {
            double[] F = evaluateFunctions(functions, variables, x);
            double residual = 0;
            for (double v : F) {
                if (Double.isNaN(v) || Double.isInfinite(v)) return false;
                residual += v * v;
            }
            return Math.sqrt(residual) < 1e-6;
        }

        private static List<String> extractVariables(String[] equations) {
            LinkedHashSet<String> varSet = new LinkedHashSet<>();
            Pattern varPattern = Pattern.compile("\\b([a-zA-Z]\\w*)\\b");

            for (String eq : equations) {
                Matcher m = varPattern.matcher(eq);
                while (m.find()) {
                    String token = m.group(1);
                    if (!isFunctionName(token)) {
                        varSet.add(token);
                    }
                }
            }
            return new ArrayList<>(varSet);
        }

        private static boolean isFunctionName(String token) {
            String[] funcs = {"sin", "cos", "tan", "exp", "log", "sqrt", "abs",
                    "asin", "acos", "atan", "sinh", "cosh", "tanh"};
            for (String f : funcs) {
                if (token.equals(f)) return true;
            }
            return false;
        }

        /**
         * 修复问题5：增强数值稳定性检测
         */
        private static double[] newtonRaphson(List<String> functions,
                                              List<String> variables,
                                              double[] x0) {
            int n = x0.length;
            double[] x = Arrays.copyOf(x0, n);

            for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
                double[] F = evaluateFunctions(functions, variables, x);

                double norm = 0;
                for (double v : F) {
                    if (Double.isNaN(v) || Double.isInfinite(v)) {
                        throw new RuntimeException("函数值包含 NaN 或无穷大");
                    }
                    norm += v * v;
                }
                norm = Math.sqrt(norm);

                if (norm < EPSILON) {
                    System.out.println("收敛于迭代 " + iter + "，残差: " + norm);
                    return x;
                }

                double[][] J = computeJacobian(functions, variables, x);
                double det = computeDeterminant(J);
                if (Math.abs(det) < 1e-12) {
                    throw new RuntimeException("雅可比矩阵接近奇异 (det=" + det + ")");
                }

                double[] delta;
                try {
                    SimpleMatrix Jmatrix = new SimpleMatrix(J);
                    SimpleMatrix Fvector = new SimpleMatrix(n, 1, true, F);
                    SimpleMatrix deltaMatrix = Jmatrix.solve(Fvector.negative());
                    delta = new double[n];
                    for (int i = 0; i < n; i++) {
                        delta[i] = deltaMatrix.get(i, 0);
                        if (Double.isNaN(delta[i]) || Double.isInfinite(delta[i])) {
                            throw new RuntimeException("求解步长包含 NaN 或无穷大");
                        }
                    }
                } catch (RuntimeException re) {
                    throw re;
                } catch (Exception e) {
                    throw new RuntimeException("无法求解线性系统: " + e.getMessage());
                }

                // ============================================
                // 【关键修改】发散检测：检查步长而不是单个元素
                // ============================================
                double maxDelta = 0;
                for (int i = 0; i < n; i++) {
                    maxDelta = Math.max(maxDelta, Math.abs(delta[i]));
                }

                if (maxDelta > 1e6) {
                    // 抛出异常让外层尝试下一组初始值，不提示用户
                    throw new RuntimeException("迭代发散（最大步长: " + maxDelta + "）");
                }
                // ============================================

                double alpha = 1.0;
                double[] xNew = new double[n];

                for (int backtrack = 0; backtrack < 10; backtrack++) {
                    for (int i = 0; i < n; i++) {
                        xNew[i] = x[i] + alpha * delta[i];
                    }

                    double[] FNew = evaluateFunctions(functions, variables, xNew);
                    double normNew = 0;
                    boolean valid = true;
                    for (double v : FNew) {
                        if (Double.isNaN(v) || Double.isInfinite(v)) {
                            valid = false;
                            break;
                        }
                        normNew += v * v;
                    }
                    normNew = Math.sqrt(normNew);

                    if (valid && normNew < norm * 1.1) {
                        break;
                    }

                    alpha *= 0.5;
                }

                for (int i = 0; i < n; i++) {
                    x[i] = xNew[i];
                }
            }

            throw new RuntimeException("牛顿法未收敛（达到最大迭代次数 " + MAX_ITERATIONS + "）");
        }

        private static double computeDeterminant(double[][] matrix) {
            try {
                SimpleMatrix m = new SimpleMatrix(matrix);
                return m.determinant();
            } catch (Exception e) {
                return 0.0;
            }
        }

        private static double[] evaluateFunctions(List<String> functions,
                                                  List<String> variables,
                                                  double[] x) {
            double[] result = new double[functions.size()];
            for (int i = 0; i < functions.size(); i++) {
                result[i] = evaluateFunction(functions.get(i), variables, x);
            }
            return result;
        }

        private static double evaluateFunction(String function,
                                               List<String> variables,
                                               double[] x) {
            try {
                ExpressionBuilder builder = new ExpressionBuilder(function);
                for (String var : variables) {
                    builder.variable(var);
                }
                Expression exp = builder.build();
                for (int i = 0; i < variables.size(); i++) {
                    exp.setVariable(variables.get(i), x[i]);
                }
                return exp.evaluate();
            } catch (Exception e) {
                throw new RuntimeException("函数求值错误: " + function + " -> " + e.getMessage());
            }
        }

        private static double[][] computeJacobian(List<String> functions,
                                                  List<String> variables,
                                                  double[] x) {
            int m = functions.size();
            int n = x.length;
            double[][] J = new double[m][n];

            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    double[] xPlus = Arrays.copyOf(x, n);
                    double[] xMinus = Arrays.copyOf(x, n);
                    xPlus[j] += DELTA;
                    xMinus[j] -= DELTA;

                    double fPlus = evaluateFunction(functions.get(i), variables, xPlus);
                    double fMinus = evaluateFunction(functions.get(i), variables, xMinus);

                    J[i][j] = (fPlus - fMinus) / (2 * DELTA);
                }
            }

            return J;
        }
    }

    /* ------------------ 矩阵解析与运算 ------------------ */

    public static double[][] parseMatrixFromString(String text) {
        if (text == null) throw new IllegalArgumentException("矩阵输入为空");
        String[] rows = Arrays.stream(text.split("[;\\n\\r]+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (rows.length == 0) throw new IllegalArgumentException("无法解析矩阵：无行");

        List<double[]> data = new ArrayList<>();
        int cols = -1;
        for (String row : rows) {
            String[] parts = row.split("[,\\s]+");
            List<Double> nums = new ArrayList<>();
            for (String p : parts) {
                if (p == null || p.trim().isEmpty()) continue;
                try {
                    nums.add(Double.parseDouble(p.trim()));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("矩阵元素格式错误: " + p);
                }
            }
            if (nums.isEmpty()) continue;
            if (cols == -1) cols = nums.size();
            if (nums.size() != cols) throw new IllegalArgumentException("矩阵每行列数不一致");
            double[] arr = nums.stream().mapToDouble(Double::doubleValue).toArray();
            data.add(arr);
        }
        if (data.isEmpty()) throw new IllegalArgumentException("矩阵无有效数据");

        double[][] mat = new double[data.size()][cols];
        for (int i = 0; i < data.size(); i++) mat[i] = data.get(i);
        return mat;
    }

    /**
     * 修复问题3：添加注释说明每个case都有return，无需break
     */
    public static String performMatrixOperation(String operation, double[][] Aarray, double[][] Barray) {
        try {
            SimpleMatrix A = new SimpleMatrix(Aarray);
            SimpleMatrix B = (Barray == null) ? null : new SimpleMatrix(Barray);

            switch (operation) {
                case "A + B":
                    if (B == null) return "需要矩阵 B";
                    if (A.numRows() != B.numRows() || A.numCols() != B.numCols())
                        return "矩阵维度不匹配";
                    return matrixToString(A.plus(B));
                // 每个case都有return，无需break

                case "A - B":
                    if (B == null) return "需要矩阵 B";
                    if (A.numRows() != B.numRows() || A.numCols() != B.numCols())
                        return "矩阵维度不匹配";
                    return matrixToString(A.minus(B));

                case "A * B":
                case "A × B":
                    if (B == null) return "需要矩阵 B";
                    if (A.numCols() != B.numRows()) return "矩阵维度不兼容，无法相乘";
                    return matrixToString(A.mult(B));

                case "det(A)":
                case "det":
                    if (A.numRows() != A.numCols()) return "行列式仅对方阵定义";
                    return String.format("det(A) = %s", formatResult(A.determinant()));

                case "A^-1":
                case "A⁻¹":
                    if (A.numRows() != A.numCols()) return "仅方阵有逆矩阵";
                    if (Math.abs(A.determinant()) < 1e-10) return "矩阵奇异，无逆矩阵";
                    SimpleMatrix inv = A.invert();
                    return matrixToString(inv);

                case "A^T":
                case "Aᵀ":
                    return matrixToString(A.transpose());

                case "Parse A from Text":
                case "Parse B from Text":
                    return "请在文本框中输入矩阵后点击其他运算按钮";

                default:
                    return "未知矩阵操作: " + operation;
            }
        } catch (ArithmeticException e) {
            return "算术错误: " + e.getMessage();
        } catch (IllegalArgumentException e) {
            return "参数错误: " + e.getMessage();
        } catch (Exception e) {
            return "矩阵运算错误: " + e.getMessage();
        }
    }

    private static String matrixToString(SimpleMatrix m) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.numRows(); i++) {
            sb.append("[");
            for (int j = 0; j < m.numCols(); j++) {
                sb.append(formatResult(m.get(i, j)));
                if (j < m.numCols() - 1) sb.append(", ");
            }
            sb.append("]\n");
        }
        return sb.toString();
    }

    /* ------------------ 线性方程组求解器（保留） ------------------ */

    public static class LinearSolver {

        private static final Pattern TERM_PATTERN = Pattern.compile("([+-]?\\s*(?:\\d+(?:\\.\\d+)?|\\.\\d+)?\\s*)([a-zA-Z]\\w*)?");

        public static Map<String, Double> solveLinearSystemFromStrings(String[] equations) {
            LinearSystem sys = parseLinearSystem(equations);
            double[] x = solveByGaussian(sys.A, sys.b);
            Map<String, Double> result = new LinkedHashMap<>();
            for (int i = 0; i < sys.variables.size(); ++i) {
                result.put(sys.variables.get(i), x[i]);
            }
            return result;
        }

        public static class LinearSystem {
            public final List<String> variables;
            public final double[][] A;
            public final double[] b;

            public LinearSystem(List<String> variables, double[][] A, double[] b) {
                this.variables = variables;
                this.A = A;
                this.b = b;
            }
        }

        public static LinearSystem parseLinearSystem(String[] equations) {
            if (equations == null || equations.length == 0)
                throw new IllegalArgumentException("方程组为空");

            LinkedHashSet<String> varSet = new LinkedHashSet<>();
            List<Map<String, Double>> coeffList = new ArrayList<>();
            List<String> rhsExprs = new ArrayList<>();

            for (String rawEq : equations) {
                if (rawEq == null) continue;
                String eq = rawEq.trim();
                if (eq.isEmpty()) continue;

                String[] sides = eq.split("=");
                if (sides.length != 2) throw new IllegalArgumentException("每条方程必须包含且只包含一个 '=': " + eq);
                String lhs = sides[0].trim();
                String rhs = sides[1].trim();

                String lhsNorm = lhs.replaceAll("\\s+", "");
                if (!lhsNorm.startsWith("+") && !lhsNorm.startsWith("-")) lhsNorm = "+" + lhsNorm;

                Map<String, Double> coeffs = new HashMap<>();
                Matcher m = TERM_PATTERN.matcher(lhsNorm);
                int matchPos = 0;
                while (m.find()) {
                    if (m.start() == m.end()) continue;
                    if (m.start() != matchPos) {
                        matchPos = m.end();
                        continue;
                    }
                    matchPos = m.end();

                    String coeffStr = m.group(1).replaceAll("\\s+", "");
                    String var = m.group(2);

                    if (var == null || var.isEmpty()) {
                        if (!coeffStr.isEmpty() && !coeffStr.equals("+") && !coeffStr.equals("-")) {
                            double val = Double.parseDouble(coeffStr);
                            coeffs.put("__CONST__", coeffs.getOrDefault("__CONST__", 0.0) + val);
                        } else if (coeffStr.equals("+")) {
                            coeffs.put("__CONST__", coeffs.getOrDefault("__CONST__", 0.0) + 1.0);
                        } else if (coeffStr.equals("-")) {
                            coeffs.put("__CONST__", coeffs.getOrDefault("__CONST__", 0.0) - 1.0);
                        }
                    } else {
                        double coeff;
                        if (coeffStr.equals("+") || coeffStr.equals("")) coeff = 1.0;
                        else if (coeffStr.equals("-")) coeff = -1.0;
                        else coeff = Double.parseDouble(coeffStr);
                        coeffs.put(var, coeffs.getOrDefault(var, 0.0) + coeff);
                        varSet.add(var);
                    }
                }

                coeffList.add(coeffs);
                rhsExprs.add(rhs);
            }

            int nEq = coeffList.size();
            List<String> variables = new ArrayList<>(varSet);
            int nVar = variables.size();

            if (nVar == 0) throw new IllegalArgumentException("未检测到未知数");

            if (nEq != nVar) {
                throw new IllegalArgumentException(String.format("方程数 (%d) 与未知数数 (%d) 不一致", nEq, nVar));
            }

            double[][] A = new double[nEq][nVar];
            double[] b = new double[nEq];

            for (int i = 0; i < nEq; ++i) {
                Map<String, Double> coeffs = coeffList.get(i);
                for (int j = 0; j < nVar; ++j) {
                    String var = variables.get(j);
                    A[i][j] = coeffs.getOrDefault(var, 0.0);
                }
                double rhsVal = evaluateExpression(rhsExprs.get(i));
                double lhsConst = coeffs.getOrDefault("__CONST__", 0.0);
                b[i] = rhsVal - lhsConst;
            }

            return new LinearSystem(variables, A, b);
        }

        private static double evaluateExpression(String expr) {
            if (expr == null || expr.trim().isEmpty()) return 0.0;
            String cleaned = expr.replace("×", "*").replace("÷", "/").replace("π", String.valueOf(Math.PI));
            try {
                Expression e = new ExpressionBuilder(cleaned).build();
                double v = e.evaluate();
                if (Double.isFinite(v)) return v;
            } catch (Exception ignored) {}
            try {
                return simpleEvaluate(cleaned);
            } catch (Exception ex) {
                throw new IllegalArgumentException("无法解析表达式: " + expr);
            }
        }

        private static double simpleEvaluate(String expr) {
            String s = expr.replaceAll("\\s+", "");
            if (s.startsWith("+")) s = s.substring(1);

            while (s.contains("(")) {
                int close = s.indexOf(')');
                if (close == -1) throw new IllegalArgumentException("括号不匹配");
                int open = s.lastIndexOf('(', close);
                if (open == -1) throw new IllegalArgumentException("括号不匹配");
                String inner = s.substring(open + 1, close);
                double val = simpleEvaluate(inner);
                s = s.substring(0, open) + Double.toString(val) + s.substring(close + 1);
            }

            List<String> tokens = new ArrayList<>();
            Matcher m = Pattern.compile("[+-]?\\d+(?:\\.\\d+)?|[+\\-*/]").matcher(s);
            while (m.find()) tokens.add(m.group());

            List<String> afterMul = new ArrayList<>();
            int idx = 0;
            while (idx < tokens.size()) {
                String t = tokens.get(idx);
                if (t.equals("*") || t.equals("/")) {
                    if (afterMul.isEmpty()) throw new IllegalArgumentException("运算符位置错误");
                    String left = afterMul.remove(afterMul.size() - 1);
                    if (idx + 1 >= tokens.size()) throw new IllegalArgumentException("运算符后缺少操作数");
                    String right = tokens.get(++idx);
                    double l = Double.parseDouble(left);
                    double r = Double.parseDouble(right);
                    if (t.equals("/") && Math.abs(r) < 1e-15) throw new ArithmeticException("除零错误");
                    double res = t.equals("*") ? l * r : l / r;
                    afterMul.add(Double.toString(res));
                    idx++;
                } else {
                    afterMul.add(t);
                    idx++;
                }
            }
            double total = 0.0;
            double sign = 1.0;
            for (String token : afterMul) {
                if (token.equals("+")) sign = 1.0;
                else if (token.equals("-")) sign = -1.0;
                else total += sign * Double.parseDouble(token);
            }
            return total;
        }

        public static double[] solveByGaussian(double[][] Aorig, double[] borig) {
            int n = borig.length;
            if (Aorig.length != n) throw new IllegalArgumentException("A 的行数必须等于 b 的长度");

            double[][] A = new double[n][n];
            double[] b = new double[n];
            for (int i = 0; i < n; ++i) {
                if (Aorig[i].length != n) throw new IllegalArgumentException("矩阵 A 必须是方阵");
                System.arraycopy(Aorig[i], 0, A[i], 0, n);
                b[i] = borig[i];
            }

            for (int k = 0; k < n; ++k) {
                int maxRow = k;
                double maxVal = Math.abs(A[k][k]);
                for (int i = k + 1; i < n; ++i) {
                    double v = Math.abs(A[i][k]);
                    if (v > maxVal) { maxVal = v; maxRow = i; }
                }
                if (Math.abs(maxVal) < 1e-14) throw new IllegalArgumentException("矩阵可能是奇异的或接近奇异");
                if (maxRow != k) {
                    double[] tmp = A[k]; A[k] = A[maxRow]; A[maxRow] = tmp;
                    double tt = b[k]; b[k] = b[maxRow]; b[maxRow] = tt;
                }
                for (int i = k + 1; i < n; ++i) {
                    double factor = A[i][k] / A[k][k];
                    A[i][k] = 0.0;
                    for (int j = k + 1; j < n; ++j) A[i][j] -= factor * A[k][j];
                    b[i] -= factor * b[k];
                }
            }

            double[] x = new double[n];
            for (int i = n - 1; i >= 0; --i) {
                double s = b[i];
                for (int j = i + 1; j < n; ++j) s -= A[i][j] * x[j];
                if (Math.abs(A[i][i]) < 1e-15) throw new ArithmeticException("除零错误：矩阵奇异");
                x[i] = s / A[i][i];
            }
            return x;
        }
    }

    /* ------------------ 辅助函数 ------------------ */

    private static String formatResult(double result) {
        if (Math.abs(result - Math.round(result)) < 1e-10) {
            return String.valueOf((long) Math.round(result));
        }
        String s = String.format(Locale.ROOT, "%.8f", result).replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }

    public void memoryClear() { memory = 0; }
    public double memoryRecall() { return memory; }
    public void memoryAdd(double value) { memory += value; }
    public void memorySubtract(double value) { memory -= value; }
    public void memoryStore(double value) { memory = value; }
    public double getMemory() { return memory; }
    public String getLastAnswer() { return lastAnswer; }
}
