package com.example;

import org.ejml.simple.SimpleMatrix;

public class MatrixCalculator {
    
    public static String performOperation(String operation, double[][] matrixA, double[][] matrixB) {
        try {
            SimpleMatrix A = new SimpleMatrix(matrixA);
            SimpleMatrix B = new SimpleMatrix(matrixB);
            
            SimpleMatrix result;
            
            if (operation.equals("A + B")) {
                result = A.plus(B);
                return matrixToString(result);
            } else if (operation.equals("A - B")) {
                result = A.minus(B);
                return matrixToString(result);
            } else if (operation.equals("A × B")) {
                result = A.mult(B);
                return matrixToString(result);
            } else if (operation.equals("det(A)")) {
                double det = A.determinant();
                return "det(A) = " + String.format("%.4f", det);
            } else if (operation.equals("A⁻¹")) {
                result = A.invert();
                return matrixToString(result);
            } else if (operation.equals("Aᵀ")) {
                result = A.transpose();
                return matrixToString(result);
            }
            
            return "未知操作";
            
        } catch (Exception e) {
            return "错误: " + e.getMessage();
        }
    }
    
    private static String matrixToString(SimpleMatrix matrix) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.numRows(); i++) {
            sb.append("[");
            for (int j = 0; j < matrix.numCols(); j++) {
                sb.append(String.format("%.2f", matrix.get(i, j)));
                if (j < matrix.numCols() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]\n");
        }
        return sb.toString();
    }
}