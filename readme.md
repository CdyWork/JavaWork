# 卡西欧风格高级计算器 (Java 25)

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://jdk.java.net/25/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

一个功能完整的高级科学计算器，模仿卡西欧 fx-991CN X 计算器，使用 Java 25 和 Swing 实现。

![Calculator Preview](https://via.placeholder.com/800x600/2d2d2d/ffffff?text=CASIO+fx-991CN+X)

---

## 📋 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [安装指南](#安装指南)
- [使用说明](#使用说明)
- [开发文档](#开发文档)
- [常见问题](#常见问题)

---

## ✨ 功能特性

### 1. 普通计算模式 🔢
- **基本运算**: 加、减、乘、除、取余、幂运算
- **科学函数**: 
  - 三角函数: sin, cos, tan
  - 对数函数: ln, log
  - 指数函数: eˣ, xʸ, x², x³
  - 根函数: √, ∛
  - 其他: |x|, x!, 1/x
- **特殊常数**: π (圆周率), e (自然对数底)
- **内存功能**: MC (清除), MR (读取), M+ (加), M- (减), MS (存储)
- **历史记录**: ANS 功能存储上次计算结果

### 2. 矩阵运算模式 📊
- **支持 3×3 矩阵**输入和计算
- **矩阵运算**:
  - 加法: A + B
  - 减法: A - B
  - 乘法: A × B
  - 行列式: det(A), det(B)
  - 逆矩阵: A⁻¹, B⁻¹
  - 转置: Aᵀ, Bᵀ
- **实时结果显示**

### 3. 方程求解模式 🔍
- **线性方程**: 2*x + 3 = 7
- **二次方程**: x^2 - 4*x + 3 = 0
- **三角方程**: sin(x) = 0.5
- **数值求解**: 在 [-100, 100] 范围内搜索近似解
- **精度**: 0.001 误差范围

### 4. 函数绘图模式 📈
- **函数可视化**: 输入任意数学函数绘制图形
- **预设函数**: 
  - 三角: sin(x), cos(x), tan(x)
  - 多项式: x², x³
  - 其他: sqrt(x), exp(x), log(x), abs(x)
- **绘图范围**: x ∈ [-10, 10]
- **交互式图表**: 基于 JFreeChart

### 5. 用户界面特性 🎨
- **卡西欧风格设计**: 经典绿色 LCD 显示屏
- **颜色编码按键**:
  - 🔘 灰色: 数字键 (0-9, .)
  - 🟠 橙色: 运算符 (+, -, ×, ÷)
  - 🔵 蓝色: 科学函数
  - 🔴 红色: 特殊功能 (AC, DEL)
  - 🟢 绿色: 等号
  - 🟣 紫色: 内存操作
- **实时状态显示**: 角度模式、内存值
- **响应式布局**: 适配不同屏幕尺寸

---

## 🛠 技术栈

### 核心技术
- **Java 25**: 最新 JDK 特性
- **Swing**: GUI 框架
- **AWT**: 图形绘制

### 依赖库
| 库名 | 版本 | 用途 |
|------|------|------|
| exp4j | 0.4.8 | 数学表达式解析和计算 |
| EJML | 0.43.1 | 高效矩阵运算 |
| JFreeChart | 1.5.4 | 函数图形绘制 |
| Apache Commons Math3 | 3.6.1 | 数值计算和方程求解 |

### 构建工具
- **Maven 3.9+**: 依赖管理和项目构建

---

## 📁 项目结构

```
casio-calculator/
├── pom.xml                                 # Maven 配置文件
├── README.md                               # 项目说明文档
├── LICENSE                                 # 许可证
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── calculator/
│       │           ├── CasioCalculator.java      # 主程序和 GUI
│       │           ├── CalculatorEngine.java     # 计算引擎
│       │           ├── MatrixCalculator.java     # 矩阵运算
│       │           └── GraphPlotter.java         # 图形绘制
│       └── resources/
│           └── icon.png                    # 应用图标（可选）
├── target/                                 # 编译输出目录
│   └── casio-calculator-1.0.0.jar         # 可执行 JAR
└── docs/                                   # 文档目录
    ├── screenshots/                        # 截图
    └── user-manual.md                      # 用户手册
```

### 核心类说明

#### 1. `CasioCalculator.java` (主类)
- **职责**: GUI 界面、事件处理、用户交互
- **主要方法**:
  - `initializeUI()`: 初始化用户界面
  - `createNormalPanel()`: 创建普通计算面板
  - `createMatrixPanel()`: 创建矩阵运算面板
  - `createEquationPanel()`: 创建方程求解面板
  - `createGraphPanel()`: 创建绘图面板
  - `handleButtonClick(String)`: 处理按钮点击事件

#### 2. `CalculatorEngine.java` (计算引擎)
- **职责**: 表达式计算、方程求解、内存管理
- **主要方法**:
  - `calculate(String)`: 计算数学表达式
  - `solveEquation(String)`: 求解方程
  - `preprocessExpression(String)`: 预处理表达式
  - `memoryAdd/Clear/Recall/Store()`: 内存操作

#### 3. `MatrixCalculator.java` (矩阵计算器)
- **职责**: 矩阵运算
- **主要方法**:
  - `performOperation(String, double[][], double[][])`: 执行矩阵运算
  - `matrixToString(SimpleMatrix)`: 矩阵转字符串

#### 4. `GraphPlotter.java` (图形绘制器)
- **职责**: 函数图形可视化
- **主要方法**:
  - 构造函数: 创建并显示函数图形窗口

---

## 🚀 安装指南

### 前置要求

1. **JDK 25** (必需)
2. **Maven 3.9+** (推荐) 或 Gradle
3. **Git** (可选，用于克隆项目)

### 方法一: 使用 Maven (推荐)

#### Step 1: 安装 JDK 25

```bash
# macOS (使用 Homebrew)
brew install openjdk@25

# Linux (Ubuntu/Debian)
wget https://download.java.net/java/GA/jdk25/...
sudo tar -xzf openjdk-25_linux-x64_bin.tar.gz -C /opt/
export JAVA_HOME=/opt/jdk-25
export PATH=$JAVA_HOME/bin:$PATH

# Windows
# 从 https://jdk.java.net/25/ 下载安装包
# 安装后配置环境变量 JAVA_HOME 和 PATH
```

验证安装：
```bash
java --version
# 输出应显示: openjdk 25...
```

#### Step 2: 安装 Maven

```bash
# macOS
brew install maven

# Linux
sudo apt install maven

# Windows (使用 Chocolatey)
choco install maven
```

验证安装：
```bash
mvn --version
# 输出应显示: Apache Maven 3.9.x
```

#### Step 3: 克隆或创建项目

```bash
# 克隆项目（如果使用 Git）
git clone https://github.com/yourusername/casio-calculator.git
cd casio-calculator

# 或手动创建目录结构
mkdir casio-calculator
cd casio-calculator
mkdir -p src/main/java/com/calculator
```

#### Step 4: 复制源代码

将以下文件放入对应位置：
- `pom.xml` → 项目根目录
- `*.java` 文件 → `src/main/java/com/calculator/`

#### Step 5: 构建项目

```bash
# 下载依赖并编译
mvn clean install

# 或仅编译
mvn compile
```

#### Step 6: 运行程序

```bash
# 方式 1: 使用 Maven exec 插件
mvn exec:java -Dexec.mainClass="com.calculator.CasioCalculator"

# 方式 2: 运行打包的 JAR
java -jar target/casio-calculator-1.0.0.jar
```

### 方法二: 使用 IDE (IntelliJ IDEA / Eclipse)

#### IntelliJ IDEA

1. **File** → **Open** → 选择项目根目录
2. IDEA 会自动识别 Maven 项目并下载依赖
3. 等待索引完成
4. 右键点击 `CasioCalculator.java` → **Run 'CasioCalculator.main()'**

#### Eclipse

1. **File** → **Import** → **Maven** → **Existing Maven Projects**
2. 选择项目根目录
3. 等待依赖下载完成
4. 右键点击 `CasioCalculator.java` → **Run As** → **Java Application**

### 方法三: 手动编译 (不推荐)

```bash
# 下载依赖 JAR 文件到 lib/ 目录
mkdir lib
cd lib
# 下载: exp4j-0.4.8.jar, ejml-all-0.43.1.jar, jfreechart-1.5.4.jar, commons-math3-3.6.1.jar

# 编译
cd ..
javac --release 25 -cp "lib/*" -d bin src/main/java/com/calculator/*.java

# 运行
java -cp "bin:lib/*" com.calculator.CasioCalculator
```

---

## 📖 使用说明

### 快速开始

1. **启动程序**后，默认进入"普通计算"模式
2. 绿色 LCD 显示屏显示当前输入
3. 使用鼠标点击按键输入表达式
4. 按 **=** 键计算结果

### 普通计算模式

#### 基本运算
```
示例 1: 加法
输入: 123 + 456
结果: 579

示例 2: 乘方
输入: 2 ^ 10
结果: 1024

示例 3: 混合运算
输入: (3 + 5) * 2 - 10 / 2
结果: 11
```

#### 科学函数
```
示例 1: 正弦
输入: sin(30)
结果: 0.5 (DEG 模式)

示例 2: 对数
输入: ln(e)
结果: 1

示例 3: 平方根
输入: √(144)
结果: 12
```

#### 内存操作
```
1. 输入 100, 按 MS (存储)
2. 输入 50, 按 M+ (加到内存)
3. 按 MR (读取内存) → 显示 150
4. 按 MC (清除内存)
```

### 矩阵运算模式

1. 切换到"矩阵运算"选项卡
2. 在"矩阵 A"和"矩阵 B"输入框中输入数值
3. 点击运算按钮（如 A + B）
4. 结果显示在主显示屏

#### 示例：矩阵乘法
```
矩阵 A:          矩阵 B:
[1  2  3]        [1  0  0]
[4  5  6]   ×    [0  1  0]
[7  8  9]        [0  0  1]

结果: A × B = A (单位矩阵)
```

### 方程求解模式

1. 切换到"方程求解"选项卡
2. 输入方程（包含等号）
3. 点击"求解方程"按钮

#### 支持的方程类型
```
线性方程: 2*x + 3 = 7        → x = 2
二次方程: x^2 - 4 = 0        → x ≈ 2 或 x ≈ -2
三角方程: sin(x) = 0.5       → x ≈ 30° (DEG)
```

### 函数绘图模式

1. 切换到"函数绘图"选项卡
2. 输入函数表达式（使用 x 作为变量）
3. 点击"绘制图形"或选择预设函数
4. 新窗口显示函数图像

#### 函数示例
```
sin(x)           - 正弦波
x^2              - 抛物线
exp(x)           - 指数增长
log(x)           - 对数曲线
abs(x)           - V 型图
x^3 - 3*x        - 三次函数
```

---

## 🔧 开发文档

### 添加新功能

#### 1. 添加新按钮

在 `CasioCalculator.java` 中：

```java
// 在 createNormalPanel() 方法中
String[][] newButtons = {
    {"新功能1", "新功能2", ...}
};

for (String[] row : newButtons) {
    for (String text : row) {
        buttonsPanel.add(createStyledButton(text, ButtonType.FUNCTION));
    }
}
```

#### 2. 添加新计算功能

在 `CalculatorEngine.java` 中：

```java
public String newFunction(String input) throws Exception {
    // 实现计算逻辑
    double result = ...;
    return formatResult(result);
}
```

#### 3. 添加新数学函数

在 `preprocessExpression()` 中注册：

```java
private String preprocessExpression(String expr) {
    expr = expr.replace("你的函数", "对应的exp4j函数");
    return expr;
}
```

### 自定义样式

修改按钮颜色：

```java
Color bgColor;
if (type == ButtonType.YOUR_TYPE) {
    bgColor = new Color(R, G, B); // RGB 值
}
```

修改显示屏颜色：

```java
display.setBackground(new Color(R, G, B));
display.setForeground(new Color(R, G, B));
```

### 扩展矩阵维度

在 `CasioCalculator.java` 中：

```java
// 修改矩阵大小
private JTextField[][] matrixAFields = new JTextField[N][N]; // N为新维度

// 在 createMatrixInput() 中更新循环
for (int i = 0; i < N; i++) {
    for (int j = 0; j < N; j++) {
        // ...
    }
}
```

### 调试技巧

#### 启用详细日志

```java
// 在 handleButtonClick() 中添加
System.out.println("Button clicked: " + text);
System.out.println("Current display: " + display.getText());
```

#### 捕获异常

```java
try {
    // 你的代码
} catch (Exception e) {
    e.printStackTrace(); // 打印堆栈跟踪
    display.setText("错误: " + e.getMessage());
}
```

---

## ❓ 常见问题

### Q1: 运行时报错 "找不到主类"
**A**: 确保：
1. Maven 编译成功: `mvn clean install`
2. 主类路径正确: `com.calculator.CasioCalculator`
3. 使用正确的 JDK 版本

### Q2: Maven 下载依赖失败
**A**: 尝试：
```bash
# 清理缓存
mvn clean

# 强制更新依赖
mvn clean install -U

# 使用国内镜像（在 settings.xml 中配置阿里云镜像）
<mirror>
    <id>aliyun</id>
    <url>https://maven.aliyun.com/repository/public</url>
    <mirrorOf>central</mirrorOf>
</mirror>
```

### Q3: 矩阵运算报错 "矩阵奇异"
**A**: 矩阵行列式为 0，不可逆。检查输入矩阵是否线性相关。

### Q4: 方程求解找不到解
**A**: 
1. 确保方程有实数解
2. 解可能超出搜索范围 [-100, 100]
3. 尝试调整搜索步长或范围

### Q5: 图形绘制窗口空白
**A**: 
1. 检查函数语法是否正确
2. 函数值可能超出显示范围
3. 确保使用 `x` 作为变量

### Q6: 在 macOS 上界面显示异常
**A**: 
```java
// 在 main() 方法开始添加
System.setProperty("apple.laf.useScreenMenuBar", "true");
System.setProperty("apple.awt.application.name", "CASIO Calculator");
```

### Q7: 如何打包为独立可执行文件？
**A**: 
```bash
# 使用 Maven Shade 插件打包
mvn clean package

# 生成的 JAR 位于 target/ 目录
java -jar target/casio-calculator-1.0.0.jar

# 或使用 jpackage (Java 14+)
jpackage --input target --name CasioCalculator \
         --main-jar casio-calculator-1.0.0.jar \
         --main-class com.calculator.CasioCalculator
```

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本项目
2. 创建特性分支: `git checkout -b feature/新功能`
3. 提交更改: `git commit -m '添加新功能'`
4. 推送到分支: `git push origin feature/新功能`
5. 提交 Pull Request

### 代码规范
- 使用 4 空格缩进
- 遵循 Java 命名约定
- 添加必要的注释
- 编写单元测试

---

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

---

## 👥 作者

- **开发者**: Your Name
- **邮箱**: your.email@example.com
- **GitHub**: [@yourusername](https://github.com/yourusername)

---

## 🙏 致谢

- [exp4j](https://www.objecthunter.net/exp4j/) - 表达式解析库
- [EJML](http://ejml.org/) - 矩阵运算库
- [JFreeChart](https://www.jfree.org/jfreechart/) - 图表绘制库
- [Apache Commons Math](https://commons.apache.org/proper/commons-math/) - 数学工具库

---

## 📚 相关资源

- [Java 25 文档](https://docs.oracle.com/en/java/javase/25/)
- [Swing 教程](https://docs.oracle.com/javase/tutorial/uiswing/)
- [Maven 指南](https://maven.apache.org/guides/)

---

**如有问题或建议，欢迎提交 Issue！** 🎉