/**
 * 
 */
package com.gmail.br45entei.util;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/** @author Brian_Entei */
public class ImageUtil {
	
	public static final Dimension getImageSizeFromFile(File file) throws IOException {
		if(file == null) {
			return null;
		}
		try(ImageInputStream in = ImageIO.createImageInputStream(file)) {
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
			if(readers.hasNext()) {
				ImageReader reader = readers.next();
				try {
					reader.setInput(in);
					return new Dimension(reader.getWidth(0), reader.getHeight(0));
				} finally {
					reader.dispose();
				}
			}
		}
		return null;
	}
	
}
