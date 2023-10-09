package test;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//from  w  w  w.j a va 2 s.  co m
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Test {

  public static void main(String[] args) {
    JFrame mainFrame = new JFrame();
    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    MyCanvas sadraGraphics = new MyCanvas();
    sadraGraphics.setPreferredSize((new Dimension(640, 480)));
    mainFrame.getContentPane().add(sadraGraphics);
    mainFrame.pack();
    mainFrame.setVisible(true);
  }
}

class MyCanvas extends JPanel {
  int x1 = 0;
  int y1 = 50;
  int x2 = 0;
  int y2 = 200;

  public MyCanvas() {
    Timer timer = new Timer(30, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        x1 += 2;
        x2 += 2;
        repaint();
      }
    });
    timer.start();
  }

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawLine(x1, y1, x2, y2);

  }
}