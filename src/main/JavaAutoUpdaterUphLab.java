package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author irvin
 */
public class JavaAutoUpdaterUphLab {

    private static final String COMPLETE_FILE = "complete.uph";
    private static final String VERSION_FILE = "version.uph";
    private static final String CURRENT_DIST_FOLDER = "dist1";
    private static final String NEW_DIST_FOLDER = "dist1-new";
    private static final String NEW_ZIP_FILE = "dist1-new.zip";
    
    public static void main(String[] args) {
        if (doesFileExist(new File(COMPLETE_FILE))) {
            deleteFileDir(new File(CURRENT_DIST_FOLDER));
            renameFileDir(new File(NEW_DIST_FOLDER), new File(CURRENT_DIST_FOLDER));
            deleteFileDir(new File(COMPLETE_FILE));
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("javaw", "-jar", "D:\\lab-client\\dist1\\java-UPH-computer-lab-client.jar");
            Process p = pb.start();
        } catch (IOException ex) {
            System.out.println(ex);
        }
        
        new Thread(() -> {
            String localVersion = readFirstLine(VERSION_FILE);
            downloadUsingNIO("https://dl.dropboxusercontent.com/s/kgilyuakdfl2cin/version.uph?dl=0", new File(VERSION_FILE));
            String cloudVersion = readFirstLine(VERSION_FILE);
            if (!localVersion.equals(cloudVersion)) {
                System.out.println("different version!");
                downloadUsingNIO("https://dl.dropboxusercontent.com/s/w5ho1hozvh7br14/dist1-new.zip?dl=0", new File(NEW_ZIP_FILE));
                try {
                    UnzipUtility.unzip(NEW_ZIP_FILE, NEW_DIST_FOLDER);
                    makeFile(new File(COMPLETE_FILE));
                } catch (IOException ex) {
                    Logger.getLogger(JavaAutoUpdaterUphLab.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            deleteFileDir(new File(NEW_ZIP_FILE));
        }).start();
    }

    private static void downloadUsingNIO(String urlStr, File file) {
        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        try {
            URL url = new URL(urlStr);
            rbc = Channels.newChannel(url.openStream());
            fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            try {
                if (rbc != null) {
                    rbc.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    private static String readFirstLine(String path) {
        String line = null;
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        File file = null;

        try {
            file = new File(path);
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            return (line = bufferedReader.readLine().trim());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static boolean doesFileExist(File file) {
        return file.exists();
    }

    private static void makeFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(JavaAutoUpdaterUphLab.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void deleteFileDir(File file) {
        if (file.isDirectory() && file.listFiles() != null) {
            for (File childFile : file.listFiles()) {
                deleteFileDir(childFile);
            }
        }
        file.delete();
    }

    private static void renameFileDir(File from, File to) {
        from.renameTo(to);
    }
}
