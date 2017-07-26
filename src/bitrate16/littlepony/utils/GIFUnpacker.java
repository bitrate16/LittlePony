package bitrate16.littlepony.utils;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GIFUnpacker {
	public static BufferedImage[] unpackGIF(File giffile) throws IOException {
		ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
		ImageInputStream ciis = ImageIO.createImageInputStream(giffile);
		reader.setInput(ciis, false);

		int noi = reader.getNumImages(true);
		BufferedImage[] frames = new BufferedImage[noi];

		for (int i = 0; i < noi; i++) {
			frames[i] = reader.read(i);
		}

		return frames;
	}

	public static BufferedImage[] unpackGIFSameSize(File giffile) throws IOException {
		String[] imageatt = new String[] { "imageLeftPosition", "imageTopPosition", "imageWidth", "imageHeight" };

		ImageReader reader = (ImageReader) ImageIO.getImageReadersByFormatName("gif").next();
		ImageInputStream ciis = ImageIO.createImageInputStream(giffile);
		reader.setInput(ciis, false);

		int noi = reader.getNumImages(true);
		BufferedImage master = null;
		BufferedImage[] frames = new BufferedImage[noi];

		for (int i = 0; i < noi; i++) {
			BufferedImage image = reader.read(i);
			IIOMetadata metadata = reader.getImageMetadata(i);

			Node tree = metadata.getAsTree("javax_imageio_gif_image_1.0");
			NodeList children = tree.getChildNodes();

			for (int j = 0; j < children.getLength(); j++) {
				Node nodeItem = children.item(j);

				if (nodeItem.getNodeName().equals("ImageDescriptor")) {
					Map<String, Integer> imageAttr = new HashMap<String, Integer>();

					for (int k = 0; k < imageatt.length; k++) {
						NamedNodeMap attr = nodeItem.getAttributes();
						Node attnode = attr.getNamedItem(imageatt[k]);
						imageAttr.put(imageatt[k], Integer.valueOf(attnode.getNodeValue()));
					}
					if (i == 0)
						master = new BufferedImage(imageAttr.get("imageLeftPosition") + imageAttr.get("imageWidth"),
								imageAttr.get("imageTopPosition") + imageAttr.get("imageHeight"),
								BufferedImage.TYPE_INT_ARGB);
					else
						master = new BufferedImage(frames[0].getWidth(), frames[0].getHeight(),
								BufferedImage.TYPE_INT_ARGB);

					// System.out.println(imageAttr.get("imageLeftPosition") +
					// ", " + imageAttr.get("imageTopPosition")
					// + ". " + imageAttr.get("imageWidth") + ", " +
					// imageAttr.get("imageHeight"));

					master.getGraphics().drawImage(image, imageAttr.get("imageLeftPosition"),
							imageAttr.get("imageTopPosition"), null);
				}
			}
			frames[i] = master;
		}

		return frames;
	}

	public static Dimension getResolution(File imagefile) {
		try {
			ImageInputStream in = ImageIO.createImageInputStream(imagefile);
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if (readers.hasNext()) {
				ImageReader reader = readers.next();
				try {
					reader.setInput(in);
					return new Dimension(reader.getWidth(0), reader.getHeight(0));
				} finally {
					reader.dispose();
				}
			}
		} catch (Exception e) {}
		return null;
	}

	public static BufferedImage[] flip(BufferedImage[] images, boolean horisontal, boolean vertical) {
		BufferedImage[] flip = new BufferedImage[images.length];
		for (int i = 0; i < images.length; i++) {
			AffineTransform tx = new AffineTransform();
			tx.scale(horisontal ? -1 : 1, vertical ? -1 : 1);
			tx.translate(horisontal ? -images[i].getWidth(null) : 0, vertical ? -images[i].getHeight(null) : 0);
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			flip[i] = op.filter(images[i], null);
		}
		return flip;
	}
}
