/**
 * Generated from "display"
 */
package net.sf.orcc.oj;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.Timer;

public class Actor_display extends JFrame implements IActor, ActionListener {

	private static Actor_display instance;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static int clip(int n) {
		if (n < 0) {
			return 0;
		} else if (n > 255) {
			return 255;
		} else {
			return n;
		}
	}

	public static void closeDisplay() {
		if (instance != null) {
			instance.timer.stop();
			instance.dispose();
		}
	}

	private static int convertYCbCrtoRGB(int y, int cb, int cr) {
		y = (76306 * (y - 16)) + 32768;
		int r = (y + (104597 * (cr - 128))) >> 16;
		int g = (y - ((25675 * (cb - 128)) + (53279 * (cr - 128)))) >> 16;
		int b = (y + (132201 * (cb - 128)) >> 16);

		r = clip(r);
		g = clip(g);
		b = clip(b);

		return (r << 16) | (g << 8) | b;
	}

	private BufferStrategy buffer;

	private Canvas canvas;

	private IntFifo fifo_B;

	private IntFifo fifo_HEIGHT;

	private IntFifo fifo_WIDTH;

	private int height;

	private BufferedImage image;

	private Timer timer;

	private int width;

	private int x;

	private int y;

	public Actor_display() {
		super("display");

		canvas = new Canvas();
		add(canvas);
		setVisible(true);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		timer = new Timer(1, this);
		//timer.start();

		instance = this;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (buffer != null) {
			Graphics graphics = buffer.getDrawGraphics();
			graphics.drawImage(image, 0, 0, null);
			buffer.show();
			graphics.dispose();
			
			timer.stop();
		}
	}

	@Override
	public void initialize() {
	}

	@Override
	public int schedule() {
		boolean res = true;
		int i = 0;

		while (res) {
			res = false;
			if (fifo_WIDTH.hasTokens(1) && fifo_HEIGHT.hasTokens(1)) {
				setVideoSize();
				res = true;
				i++;
			}

			if (fifo_B.hasTokens(384)) {
				writeMB();
				res = true;
				i++;
			}
		}

		return i;
	}

	@Override
	public void setFifo(String portName, IntFifo fifo) {
		if ("B".equals(portName)) {
			fifo_B = fifo;
		} else if ("WIDTH".equals(portName)) {
			fifo_WIDTH = fifo;
		} else if ("HEIGHT".equals(portName)) {
			fifo_HEIGHT = fifo;
		} else {
			String msg = "unknown port \"" + portName + "\"";
			throw new IllegalArgumentException(msg);
		}
	}

	private void setVideoSize() {
		int[] width = new int[1];
		int[] height = new int[1];

		fifo_WIDTH.get(width);
		fifo_HEIGHT.get(height);

		int newWidth = width[0] << 4;
		int newHeight = height[0] << 4;

		if (newWidth != this.width || newHeight != this.height) {
			this.width = newWidth;
			this.height = newHeight;

			canvas.setSize(this.width, this.height);
			pack();

			canvas.createBufferStrategy(2);
			buffer = canvas.getBufferStrategy();

			image = new BufferedImage(this.width, this.height,
					BufferedImage.TYPE_INT_RGB);
		}
	}

	private void writeMB() {
		int[] mb = new int[384];
		fifo_B.get(mb);

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				int index = 8 * j + i;

				int u = mb[256 + index];
				int v = mb[320 + index];

				for (int ym = 0; ym < 2; ym++) {
					for (int xm = 0; xm < 2; xm++) {
						int y = mb[64 * (xm + 2 * ym) + index];

						int rgb = convertYCbCrtoRGB(y, u, v);

						image.setRGB(x + i + xm * 8, this.y + j + ym * 8, rgb);
					}
				}
			}
		}

		x += 16;
		if (x == width) {
			x = 0;
			y += 16;
		}

		if (y == height) {
			x = 0;
			y = 0;
			timer.start();
		}
	}

}
