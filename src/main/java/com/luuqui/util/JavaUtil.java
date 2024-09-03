package com.luuqui.util;

import com.luuqui.launcher.LauncherGlobals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Properties;

import static com.luuqui.launcher.setting.Log.log;

public class JavaUtil {

  public static String getJVMVersionOutput(String path) {
    String output;
    if (SystemUtil.isWindows()) {
      output = ProcessUtil.runAndCapture(new String[]{ "cmd.exe", "/C", path, "-version" })[1];
    } else {
      output = ProcessUtil.runAndCapture(new String[]{ "/bin/bash", "-c", path + " -version" })[1];
    }
    return output;
  }

  public static int getJVMArch(String path) {
    String output = getJVMVersionOutput(path);
    if (SystemUtil.isWindows()) {
      // We got no output, so we can't do any checks.
      if(output.isEmpty()) return 0;

      // Matches a 64-bit '-version' output.
      if(output.contains("64-Bit Server VM")) return 64;
    } else {
      // We got no output, so we can't do any checks.
      if(output.isEmpty()) return 0;

      // Matches a 64-bit.
      if(output.contains("64-Bit") || output.contains("PE32+")) return 64;
    }

    // No results matched. We assume it's 32-bit.
    return 32;
  }

  public static String getGameJVMData() {
    String path = getGameJavaDirPath() + "/release";
    String version = "";
    String osArch = "";

    if(FileUtil.fileExists(path)) {
      Properties releaseFile = new Properties();
      try {
        releaseFile.load(Files.newInputStream(new File(path).toPath()));
      } catch (IOException e) {
        log.error(e);
      }

      version = releaseFile.getProperty("JAVA_VERSION");
      osArch = releaseFile.getProperty("OS_ARCH");
    } else {
      String output = getJVMVersionOutput(getGameJVMExePath());
      version = output.split("\"")[1];
      osArch = String.valueOf(getJVMArch(getGameJVMExePath()));
    }

    if(version.isEmpty() || osArch.isEmpty()) {
      return "Unknown Java VM";
    }

    return (version + ", " + osArch).replace("\"", "");
  }

  public static String getReadableGameJVMData() {
    String rawJavaVMData = getGameJVMData();

    if(rawJavaVMData.contains("Unknown")) {
      return "Unknown, probably 32-bit";
    }

    String javaMajorVersion = "unknown";
    String javaMinorVersion = "unknown";
    String javaArch = "unknown";

    try {
      javaMajorVersion = rawJavaVMData.split("\\.")[1];
      javaMinorVersion = rawJavaVMData.split("_")[1].split(",")[0];
      javaArch = JavaUtil.getJVMArch(JavaUtil.getGameJVMExePath()) == 64 ? "64-bit" : "32-bit";
    } catch (Exception e) {
      log.error(e);
    }

    if(javaMajorVersion.equalsIgnoreCase("unknown")
      || javaMinorVersion.equalsIgnoreCase("unknown")
      || javaArch.equalsIgnoreCase("unknown")) {
      return "Unknown, probably 32-bit";
    }

    return "Java " + javaMajorVersion + " (" + javaMinorVersion + "), " + javaArch;
  }

  public static String getGameJavaDirPath() {
    File javaVMDir = new File(LauncherGlobals.USER_DIR, "/java_vm");
    if (javaVMDir.exists() && javaVMDir.isDirectory()) {
     return javaVMDir.getAbsolutePath();
    }
    File javaDir = new File(LauncherGlobals.USER_DIR, "/java");
    if (javaDir.exists() && javaDir.isDirectory()) {
      return javaDir.getAbsolutePath();
    }
    return "";
  }

  public static String getGameJVMExePath() {
    String javaDir = getGameJavaDirPath();
    if (FileUtil.fileExists(javaDir + "/bin/java.exe")) {
      return javaDir + "/bin/java.exe";
    }
    if (FileUtil.fileExists(javaDir + "/bin/java")) {
      return javaDir + "/bin/java";
    }
    log.error("Cannot locate local java executable");
    return "java";
  }

  public static String getJavaVMCommandLineSeparator() {
    return SystemUtil.isWindows() ? ";" : ":";
  }

  public static synchronized void loadLibrary(File jar) {
    try {
      // We are using reflection here to circumvent encapsulation; addURL is not public
      URLClassLoader loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      URL url = jar.toURI().toURL();

      // Disallow if already loaded
      for (URL it : Arrays.asList(loader.getURLs())) {
        if (it.equals(url)) {
          return;
        }
      }

      Method method = java.net.URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      method.setAccessible(true);
      method.invoke(loader, url);
    } catch (final NoSuchMethodException |
                   IllegalAccessException |
                   MalformedURLException |
                   InvocationTargetException e) {
      log.error(e);
    }
  }

  public static void addToLibraryPath(String... path) {
    String cmdLineSeparator = getJavaVMCommandLineSeparator();

    for(String p : path) {
      System.setProperty("java.library.path", p + cmdLineSeparator + System.getProperty("java.library.path"));
    }

    try {
      Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
      fieldSysPath.setAccessible(true);
      fieldSysPath.set(null, null);
    } catch (Exception e) {
      log.error(e);
    }
  }

}
