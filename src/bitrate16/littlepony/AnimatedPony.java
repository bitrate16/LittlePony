package bitrate16.littlepony;

import java.io.File;
import java.io.IOException;

import bitrate16.littlepony.utils.GIFUnpacker;

public class AnimatedPony {
	private PonyPanel			pony;
	private TransparentFrame	ponyFrame;

	public AnimatedPony(String sourcegif) throws IOException {
		this(new File(sourcegif));
	}

	public AnimatedPony(File sourcegif) throws IOException {
		pony = new PonyPanel();
		pony.setScale(2);
		pony.setMinDist(4);
		pony.setMinFlipInterval(2000);
		pony.setSource(GIFUnpacker.unpackGIFSameSize(sourcegif));
		pony.setDelayFrames(50);
		pony.setFreeze(false);
		pony.setDebug(false);
		ponyFrame = new TransparentFrame(pony);
	}

	public PonyPanel getPanel() {
		return pony;
	}

	public TransparentFrame getFrame() {
		return ponyFrame;
	}

	public void dispose() {
		pony.dispose();
		ponyFrame.dispose();
	}
}
