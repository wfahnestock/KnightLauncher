package com.luuqui.launcher;

import com.luuqui.util.FileUtil;
import com.luuqui.util.ImageUtil;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static com.luuqui.launcher.Log.log;

public class Cache {

  public static String CACHE_PATH = LauncherGlobals.USER_DIR + File.separator + "KnightLauncher" + File.separator + "cache" + File.separator;

  public static void setup() {
    // Stores all cache resources.
    FileUtil.createDir(CACHE_PATH);
  }

  public static BufferedImage fetchImage(String url, int width, int height) {
    String localPath = CACHE_PATH + url.split("https://")[1];
    BufferedImage bufferedImage = null;
    URL localURL;

    if(FileUtil.fileExists(localPath)) {
      log.info("Cache: Loading image from cache", "localPath", localPath);
      try {
        localURL = new File(localPath).toURI().toURL();
        bufferedImage = ImageUtil.toBufferedImage(ImageUtil.getImageFromURL(localURL, width, height));
      } catch (MalformedURLException e) {
        log.error(e);
      }
    } else {
      bufferedImage = ImageUtil.toBufferedImage(ImageUtil.getImageFromURL(url, width, height));
      log.info("Cache: Saving image to cache", "localPath", localPath);
      File cacheFile = new File(localPath);
      cacheFile.getParentFile().mkdirs();
      try {
        ImageIO.write(bufferedImage, FilenameUtils.getExtension(localPath), cacheFile);
      } catch (IOException e) {
        log.error(e);
      }
    }

    return bufferedImage;
  }

}
