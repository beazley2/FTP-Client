package psu.agilemethods.src;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.PrintStream;
import java.util.Vector;

/**
 * Implements a simple FTP client
 *
 * @version 0.1
 */
public class FTPClient{
    public static final String HOST = "ada.cs.pdx.edu";
    public static final int PORT = 22;
    public static final String FILE_TO_DL = "download.txt";
    public static final String FILE_TO_UL = "upload.txt";

  public static void main(String[] args) {
      String userName = null;
      String password = null;

      TextUI.start();

      //parse command line args
      for (String arg : args) {
          if (userName == null) {
              userName = arg;
          } else if (password == null) {
              password = arg;
          } else {
              usage("Erroneous command line argument: " + arg);
          }
      }

      //check for missing username
      if (userName == null) {
          usage("Missing command line arguments");
      } else if (password == null) {
          usage("Missing password");
      }

      JSch jsch = new JSch();
      JSch.setConfig("StrictHostKeyChecking", "no");
      Session session = null;
      try {
          session = jsch.getSession(userName, HOST, PORT);
      } catch (JSchException e) {
          e.printStackTrace();
      }

      if (session != null) {
          session.setPassword(password);
          try {
              session.connect();
              System.out.println("Connected");
          } catch (JSchException e) {
              e.printStackTrace();
          }
      } else {
          error ("Failed to create session");
      }


      System.exit(0);
  }

    private static void error( String message )
    {
        PrintStream err = System.err;
        err.println("** " + message);

        System.exit(1);
    }

    /**
     * Prints usage information for this program and exits
     * @param message An error message to print
     */
    private static void usage( String message )
    {
        PrintStream err = System.err;
        err.println("** " + message);
        err.println();
        err.println("usage: java Project4 username password");
        err.println("  username     User name on remote server");
        err.println("  password     Password on remote server");
        err.println();
        err.println("This simple program connects to an FTP server");
        err.println("Default server: " + HOST);
        err.println("Default port: " + PORT);
        err.println();

        System.exit(1);
    }

    static void lsLocal(String path) {
        File dir = new File(path);
        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            if (f.isDirectory()) {
                System.out.println("Directory:\t" + f.getName());
            }
            if (f.isFile()) {
                System.out.println("File:\t\t" + f.getName());
            }
        }
    }

    static void lsRemote(ChannelSftp ch) {
        try {
            Vector<ChannelSftp.LsEntry> currentDir = ch.ls(ch.pwd());
            for (ChannelSftp.LsEntry entry : currentDir) {
                System.out.println(entry.getLongname());
            }
        } catch (SftpException e) {
            System.err.println(e.getMessage());
        }
    }

    // "public" for accessibility from "FTPClientGetTest.java"
    // (absent a more modular structure for the project)
    public static void get (ChannelSftp sftpChannel, String sourceFilePath,
                            String destDirectoryPath) throws SftpException {
        System.out.println("Attempting to download \"" + sourceFilePath + "\" from server");

        try {
            sftpChannel.get(sourceFilePath, destDirectoryPath);
        }
        catch (SftpException e) {
            System.err.println(e.getMessage());
            throw e;
        }

        System.out.println("Download successful");

        lsLocal(".");
        pause();

    }
}








