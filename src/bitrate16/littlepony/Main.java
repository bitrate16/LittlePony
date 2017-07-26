package bitrate16.littlepony;

import java.io.File;
import java.io.IOException;

public class Main {
	public static final String FILES_DIR = "res";

	public static void main(String[] args) throws IOException {
		// PonyPanel pony = new PonyPanel();
		// pony.setScale(2);
		// pony.setMinDist(4);
		// pony.setMinFlipInterval(2000);
		// pony.setSource(GIFUnpacker.unpackGIFSameSize(new File(FILE)));
		// pony.setDelayFrames(50);
		// pony.setFreeze(false);
		// pony.setDebug(false);
		// TransparentFrame ponyFrame = new TransparentFrame(pony);

		File[] files = new File(FILES_DIR).listFiles();
		AnimatedPony[] ponies = new AnimatedPony[files.length];

		for (int i = 0; i < files.length; i++)
			ponies[i] = new AnimatedPony(files[i].getPath());
	}
}
