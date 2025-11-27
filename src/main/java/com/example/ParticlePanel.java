package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticlePanel extends JPanel implements MouseMotionListener, MouseListener {

    private static class Particle {
        double x, y;
        double vx, vy;
        int life;
        int maxLife;
        Color color;
        double size;
        double angle;
        double angleSpeed;

        Particle(double x, double y) {
            this.x = x;
            this.y = y;
            this.vx = (Math.random() - 0.5) * 12;  // 速度加倍
            this.vy = (Math.random() - 0.5) * 12;
            this.maxLife = 20 + (int) (Math.random() * 25);  // 寿命减半
            this.life = maxLife;
            this.size = 3 + Math.random() * 5;
            this.angle = Math.random() * Math.PI * 2;
            this.angleSpeed = (Math.random() - 0.5) * 0.4;  // 旋转更快

            // 赛博朋克配色：霓虹蓝、紫、粉、青
            int colorChoice = (int) (Math.random() * 4);
            switch (colorChoice) {
                case 0: this.color = new Color(0, 255, 255); break;    // 青色
                case 1: this.color = new Color(255, 0, 255); break;    // 洋红
                case 2: this.color = new Color(0, 150, 255); break;    // 霓虹蓝
                default: this.color = new Color(255, 20, 147); break;  // 粉红
            }
        }
    }

    private final List<Particle> particles = new ArrayList<>();
    private int mouseX = -1000, mouseY = -1000;
    private JFrame parentFrame;
    private final Random random = new Random();

    // 追踪轨迹点
    private final List<Point> trail = new ArrayList<>();
    private static final int MAX_TRAIL = 15;

    public ParticlePanel() {
        setOpaque(false);
        addMouseMotionListener(this);
        addMouseListener(this);

        Timer timer = new Timer(16, e -> {
            updateParticles();
            repaint();
        });
        timer.start();
    }

    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
    }

    private void updateParticles() {
        // 更新鼠标轨迹
        trail.add(new Point(mouseX, mouseY));
        if (trail.size() > MAX_TRAIL) {
            trail.remove(0);
        }

        // 生成粒子（更多数量，更快节奏）
        for (int i = 0; i < 8; i++) {
            double offsetX = (Math.random() - 0.5) * 40;
            double offsetY = (Math.random() - 0.5) * 40;
            particles.add(new Particle(mouseX + offsetX, mouseY + offsetY));
        }

        // 更新粒子位置
        particles.removeIf(p -> {
            p.x += p.vx;
            p.y += p.vy;

            // 引力效果（更强的吸引力）
            double dx = mouseX - p.x;
            double dy = mouseY - p.y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 0) {
                p.vx += (dx / dist) * 0.4;  // 引力加强
                p.vy += (dy / dist) * 0.4;
            }

            // 阻尼减小（保持更高速度）
            p.vx *= 0.9;
            p.vy *= 0.9;
            p.angle += p.angleSpeed;

            p.life -= 2;  // 消失速度加倍
            return p.life <= 0;
        });

        // 限制粒子数量（性能优化）
        while (particles.size() > 400) {  // 增加粒子数量上限
            particles.remove(0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 绘制鼠标轨迹光晕
        if (trail.size() > 1) {
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < trail.size() - 1; i++) {
                Point p1 = trail.get(i);
                Point p2 = trail.get(i + 1);
                int alpha = (int) (255 * (i / (float) trail.size()));
                g2.setColor(new Color(0, 255, 255, alpha / 2));
                g2.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
            }
        }

        // 2. 绘制粒子之间的连线（赛博朋克网格效果）
        g2.setStroke(new BasicStroke(1));
        for (int i = 0; i < particles.size(); i++) {
            Particle p1 = particles.get(i);
            for (int j = i + 1; j < particles.size(); j++) {
                Particle p2 = particles.get(j);
                double dx = p1.x - p2.x;
                double dy = p1.y - p2.y;
                double dist = Math.sqrt(dx * dx + dy * dy);

                // 只连接距离小于80的粒子
                if (dist < 80) {
                    int alpha = (int) (100 * (1 - dist / 80));
                    g2.setColor(new Color(0, 255, 255, alpha));
                    g2.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
                }
            }
        }

        // 3. 绘制粒子本体（带光晕效果）
        for (Particle p : particles) {
            float lifeRatio = p.life / (float) p.maxLife;
            int alpha = (int) (255 * lifeRatio);

            // 外层光晕（更大、更透明）
            int glowSize = (int) (p.size * 4);
            RadialGradientPaint glowPaint = new RadialGradientPaint(
                    (float) p.x, (float) p.y, glowSize,
                    new float[]{0f, 0.5f, 1f},
                    new Color[]{
                            new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha / 3),
                            new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha / 6),
                            new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), 0)
                    }
            );
            g2.setPaint(glowPaint);
            g2.fillOval(
                    (int) (p.x - glowSize),
                    (int) (p.y - glowSize),
                    glowSize * 2,
                    glowSize * 2
            );

            // 内层粒子核心（实心、高亮）
            g2.setColor(new Color(
                    p.color.getRed(),
                    p.color.getGreen(),
                    p.color.getBlue(),
                    Math.min(255, alpha + 50)
            ));
            g2.fillOval(
                    (int) (p.x - p.size / 2),
                    (int) (p.y - p.size / 2),
                    (int) p.size,
                    (int) p.size
            );

            // 高光点（白色小点）
            g2.setColor(new Color(255, 255, 255, alpha));
            g2.fillOval(
                    (int) (p.x - 1),
                    (int) (p.y - 1),
                    2, 2
            );
        }

        // 4. 绘制鼠标周围的脉冲圆环
        int pulseRadius = (int) (30 + 10 * Math.sin(System.currentTimeMillis() / 200.0));  // 频率加快
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(0, 255, 255, 150));
        g2.drawOval(mouseX - pulseRadius, mouseY - pulseRadius, pulseRadius * 2, pulseRadius * 2);

        g2.setColor(new Color(255, 0, 255, 100));
        int pulseRadius2 = (int) (20 + 8 * Math.sin(System.currentTimeMillis() / 80.0));  // 频率更快
        g2.drawOval(mouseX - pulseRadius2, mouseY - pulseRadius2, pulseRadius2 * 2, pulseRadius2 * 2);
    }

    private void dispatchEventToUnderlyingComponent(MouseEvent e) {
        if (parentFrame != null) {
            Component contentPane = parentFrame.getContentPane();
            Point pointInContentPane = SwingUtilities.convertPoint(this, e.getPoint(), contentPane);

            Component targetComponent = SwingUtilities.getDeepestComponentAt(
                    contentPane,
                    pointInContentPane.x,
                    pointInContentPane.y
            );

            if (targetComponent != null) {
                MouseEvent convertedEvent = SwingUtilities.convertMouseEvent(
                        this, e, targetComponent
                );
                targetComponent.dispatchEvent(convertedEvent);
            }
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        dispatchEventToUnderlyingComponent(e);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(e);
        dispatchEventToUnderlyingComponent(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}