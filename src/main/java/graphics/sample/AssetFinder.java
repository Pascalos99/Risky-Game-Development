package graphics.sample;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class AssetFinder {

    public static String assetsPath = "assets"+File.separator;

    @SuppressWarnings("deprecation")
	public static URL getResource(String filename) {
        try {
            return new File(assetsPath+filename).toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
