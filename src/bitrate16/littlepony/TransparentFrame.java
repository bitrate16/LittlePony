package bitrate16.littlepony;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Transparent Frame class created default JFrame with transparency preset. This
 * window it click-through and fully transparent
 * 
 * @author bitrate16
 *
 */
public class TransparentFrame extends JFrame {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates new Transparent Frame with given panel inside
	 * 
	 * @param panel
	 */
	public TransparentFrame(JPanel panel) {
		setUndecorated(true);
		setBackground(new Color(0, 0, 0, 0));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(panel);
		setAlwaysOnTop(true);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}
