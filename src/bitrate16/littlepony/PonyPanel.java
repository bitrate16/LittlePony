package bitrate16.littlepony;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.Timer;

import bitrate16.littlepony.utils.GIFUnpacker;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PonyPanel extends JPanel implements MouseListener, ActionListener, MouseMotionListener {
	private static final long	serialVersionUID	= 1L;
	private int					delayFrames			= 1000;
	// Current frame index
	private int					frame				= 0;
	private int					scale				= 1;
	private BufferedImage[]		source				= null;
	private BufferedImage[]		flippedSource		= null;
	private Point				initialClick;
	private Timer				renderTimer;
	private boolean				debug				= false;
	// 0 = stay
	// 1 = left
	// 2 = top-left
	// 3 = top
	// 4 = top-right
	// 5 = right
	// 6 = bottom-right
	// 7 = bottom
	// 8 = bottom-left
	private int					direction			= 0;
	private Timer				movementTimer;
	private boolean				freeze				= true;
	private int					movementDelay		= 60;
	Random						random				= new Random();
	private int					MIN_DIST			= 0;
	private long				lastFlipTime;
	private int					MIN_FLIP_INTERVAL	= 1000;

	public PonyPanel() {
		setOpaque(false);
		setLayout(new GridBagLayout());
		addMouseListener(this);
		addMouseMotionListener(this);
		setDelayFrames(delayFrames);
		movementTimer = new Timer(movementDelay, this);
		movementTimer.start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setColor(getBackground());
		int fWidth = source[frame].getWidth() * scale;
		int fHeight = source[frame].getHeight() * scale;
		g2d.setColor(Color.BLUE);
		if (debug)
			g2d.fillRect(0, 0, source[frame].getWidth() * scale, source[frame].getHeight() * scale);
		if (source != null && frame < source.length)
			if (direction >= 4 && direction <= 7)
				g2d.drawImage(flippedSource[frame], 0, 0, fWidth, fHeight, null);
			else
				g2d.drawImage(source[frame], 0, 0, fWidth, fHeight, null);
		g2d.setColor(Color.RED);
		if (debug)
			g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		if (source.length == 0) {
			g2d.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
			g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		}

		g2d.dispose();

		frame++;
		if (frame >= source.length)
			frame = 0;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		try {
			if (arg0.getSource() == renderTimer)
				repaint();
			else if (arg0.getSource() == movementTimer) {
				if (!freeze) {
					if (random.nextBoolean()	&& random.nextBoolean() && random.nextBoolean()
						&& lastFlipTime + MIN_FLIP_INTERVAL < System.currentTimeMillis()) {
						if (random.nextBoolean()	&& random.nextBoolean() && random.nextBoolean()
							&& random.nextBoolean())
							direction = 0;
						else
							direction = random.nextInt(8) + 1;
						lastFlipTime = System.currentTimeMillis();
					}

					if (direction == 1) {
						moveSelf(-MIN_DIST, 0);
						if (!checkBounds()) {
							moveSelfTo(0, getSelfY());
							direction = 5;
						}
					} else if (direction == 3) {
						moveSelf(0, -MIN_DIST);
						if (!checkBounds()) {
							moveSelfTo(getSelfX(), 0);
							direction = 7;
						}
					} else if (direction == 5) {
						moveSelf(MIN_DIST, 0);
						if (!checkBounds()) {
							moveSelfTo(getResourceWidth() - getWidth(), getSelfY());
							direction = 1;
						}
					} else if (direction == 7) {
						moveSelf(0, MIN_DIST);
						if (!checkBounds()) {
							moveSelfTo(getSelfX(), getResourceHeight() - getHeight());
							direction = 3;
						}
					}

					else if (direction == 2) {
						moveSelf(-MIN_DIST, -MIN_DIST);
						if (!checkLeftBounds() && !checkTopBounds()) {
							moveSelfTo(0, 0);
							direction = 6;
						} else if (!checkLeftBounds()) {
							moveSelfTo(0, getSelfY());
							direction = 4;
						} else if (!checkTopBounds()) {
							moveSelfTo(getSelfX(), 0);
							direction = 8;
						}
					} else if (direction == 4) {
						moveSelf(MIN_DIST, -MIN_DIST);
						if (!checkRightBounds() && !checkTopBounds()) {
							moveSelfTo(getResourceWidth() - getWidth(), 0);
							direction = 8;
						} else if (!checkRightBounds()) {
							moveSelfTo(getResourceWidth() - getWidth(), getSelfY());
							direction = 2;
						} else if (!checkTopBounds()) {
							moveSelfTo(getSelfX(), 0);
							direction = 6;
						}
					} else if (direction == 6) {
						moveSelf(MIN_DIST, MIN_DIST);
						if (!checkRightBounds() && !checkBottomBounds()) {
							moveSelfTo(getResourceWidth() - getWidth(), getResourceHeight() - getHeight());
							direction = 2;
						} else if (!checkRightBounds()) {
							moveSelfTo(getResourceWidth() - getWidth(), getSelfY());
							direction = 8;
						} else if (!checkBottomBounds()) {
							moveSelfTo(getSelfX(), getResourceHeight() - getHeight());
							direction = 4;
						}
					} else if (direction == 8) {
						moveSelf(-MIN_DIST, MIN_DIST);
						if (!checkLeftBounds() && !checkBottomBounds()) {
							moveSelfTo(0, getResourceHeight() - getHeight());
							direction = 4;
						} else if (!checkLeftBounds()) {
							moveSelfTo(0, getSelfY());
							direction = 6;
						} else if (!checkBottomBounds()) {
							moveSelfTo(getSelfX(), getResourceHeight() - getHeight());
							direction = 2;
						}
					}
				}
			}
		} catch (Exception e) {}
	}

	private void moveSelf(int dx, int dy) {
		JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);

		int thisX = parent.getLocation().x;
		int thisY = parent.getLocation().y;

		parent.setLocation(thisX + dx, thisY + dy);
	}

	private void moveSelfTo(int x, int y) {
		JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);

		parent.setLocation(x, y);
	}

	private int getSelfX() {
		return SwingUtilities.getWindowAncestor(this).getLocation().x;
	}

	private int getSelfY() {
		return SwingUtilities.getWindowAncestor(this).getLocation().y;
	}

	private int getResourceWidth() {
		return (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	}

	private int getResourceHeight() {
		return (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	}

	private boolean checkBounds() {
		return checkBoundsX() && checkBoundsY();
	}

	private boolean checkBoundsX() {
		return getSelfX() >= 0 && getSelfX() + getWidth() <= getResourceWidth();
	}

	private boolean checkBoundsY() {
		return getSelfY() >= 0 && getSelfY() + getHeight() <= getResourceHeight();
	}

	private boolean checkLeftBounds() {
		return getSelfX() >= 0;
	}

	private boolean checkRightBounds() {
		return getSelfX() + getWidth() <= getResourceWidth();
	}

	private boolean checkTopBounds() {
		return getSelfY() >= 0;
	}

	private boolean checkBottomBounds() {
		return getSelfY() + getHeight() <= getResourceHeight();
	}

	/**
	 * Set's source array of images, automatically generates flipped copy
	 * 
	 * @param source
	 */
	public void setSource(BufferedImage[] source) {
		this.source = source;
		frame = 0;
		if (source != null && source.length > 0) {
			setPreferredSize(new Dimension(source[0].getWidth() * scale, source[10].getWidth() * scale));
			this.flippedSource = GIFUnpacker.flip(source, true, false);
		}
	}

	/**
	 * Set's delay between animation frames
	 * 
	 * @param delayFrames
	 */
	public void setDelayFrames(int delayFrames) {
		this.delayFrames = delayFrames;
		if (this.renderTimer != null)
			this.renderTimer.stop();
		this.renderTimer = new Timer(delayFrames, this);
		this.renderTimer.start();
	}

	/**
	 * Set's animation glyph scale
	 * 
	 * @param scale
	 */
	public void setScale(int scale) {
		this.scale = scale <= 0 ? 1 : scale;
		if (source != null && source.length > 0)
			setPreferredSize(new Dimension(source[0].getWidth() * scale, source[10].getWidth() * scale));
	}

	/**
	 * Set size from dimension, assuming dimension is resolution of the original
	 * image
	 * 
	 * @param size
	 */
	public void setNewSize(Dimension size) {
		if (size == null)
			return;
		setPreferredSize(new Dimension((int) (size.getWidth() * scale), (int) (size.getHeight() * scale)));
	}

	/**
	 * Enable debug render
	 * 
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Freeze animation types change, freeze movement
	 * 
	 * @param freeze
	 */
	public void setFreeze(boolean freeze) {
		this.freeze = freeze;
	}

	/**
	 * Set coordinate change per one movement tick
	 * 
	 * @param m
	 */
	public void setMinDist(int m) {
		this.MIN_DIST = m;
	}

	/**
	 * Set minimal time interval between movement animation changes
	 * 
	 * @param mfi
	 */
	public void setMinFlipInterval(int mfi) {
		this.MIN_FLIP_INTERVAL = mfi;
	}

	/**
	 * 
	 * 0 = stay
	 *
	 * 1 = left
	 * 
	 * 2 = top-left
	 * 
	 * 3 = top
	 * 
	 * 4 = top-right
	 * 
	 * 5 = right
	 * 
	 * 6 = bottom-right
	 * 
	 * 7 = bottom
	 * 
	 * 8 = bottom-left
	 * 
	 * @param dir
	 */
	public void setDirection(int dir) {
		this.direction = dir;
	}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		initialClick = e.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {
		JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
		int thisX = parent.getLocation().x;
		int thisY = parent.getLocation().y;

		int xMoved = (thisX + e.getX()) - (thisX + initialClick.x);
		int yMoved = (thisY + e.getY()) - (thisY + initialClick.y);

		int X = thisX + xMoved;
		int Y = thisY + yMoved;
		parent.setLocation(X, Y);
	}

	@Override
	public void mouseMoved(MouseEvent e) {}

	public void dispose() {
		renderTimer.stop();
		movementTimer.stop();
		source = null;
		flippedSource = null;
	}
}
