package psu.agilemethods.src;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * Implements a simple FTP client
 *
 * @version 0.1
 */
public class FTPClient{
  public static final String HOST = "ada.cs.pdx.edu";
  public static final int PORT = 22;
  static ChannelSftp c;

  public static void main(String[] args) {
    String userName = null;
    String password = null;

    TextUI.start();
    try {
      userName = TextUI.getUsername();
      password = TextUI.getPassword();
    } catch (IOException e) {
      e.printStackTrace();
    }

    //check for missing username
    if (userName == null) {
      usage("Missing command line arguments");
    } else if (password == null) {
      usage("Missing password");
    }

    //establish connection
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
        Channel channel = session.openChannel("sftp");
        channel.connect();
        c = (ChannelSftp) channel;
        System.out.println("Connected");
      } catch (JSchException e) {
        System.err.println("Failed to create session");
        System.exit(1);
      }
    } else {
      error("Failed to create session");
    }


    //enter console mode

    String cmd = "";
    while (!cmd.equals("exit")) {
      try {
        cmd = TextUI.getCommand();
      } catch (IOException e) {
        e.printStackTrace();
      }
      parseCmd(cmd, c);
    }

    if (session != null) {
      session.disconnect();
    }
    System.exit(0);
  }

  public static void parseCmd(String cmdIn, ChannelSftp c) {
    String[] cmd = cmdIn.split(" ");
    ArrayList<String> cmdArgs = new ArrayList<>(Arrays.asList(cmd));
    Iterator itr = cmdArgs.iterator();
    while (itr.hasNext()) {
      String arg = (String) itr.next();
      switch (arg) {
        case "get":
          try {
            String srcPath = (String) itr.next();
            String destPath = (String) itr.next();
            try {
              get(c, srcPath, destPath);
            } catch (SftpException e) {
              e.printStackTrace();
            }
          } catch (NoSuchElementException e) {
            usage("Source and destination path must be specified");
          }
        case "put":
          try {
            String file = (String) itr.next();
            try {
              upload(c, file);
            } catch (SftpException e) {
              e.printStackTrace();
            }
          } catch (NoSuchElementException e) {
            usage("Source must be specified");
          }
        case "rm":
          try {
            String file = (String) itr.next();
            try {
              delete(c, file);
            } catch (SftpException e) {
              e.printStackTrace();
            }
          } catch (NoSuchElementException e) {
            usage("Source must be specified");
          }
          break;
        case "exit":
          break;
        default:
          usage("That option is not recognized. Please view README");
          break;
      }
    }
  }

  private static void error(String message) {
    PrintStream err = System.err;
    err.println("** " + message);

    System.exit(1);
    }

  /**
   * Prints usage information for this program and exits
   *
   * @param message An error message to print
   */
  private static void usage(String message) {
    PrintStream err = System.err;
    err.println("** " + message);
    err.println();
    err.println("usage:");
    err.println("get [source] [destination]     : gets file from server");
    err.println("put [source]                   : puts file on server");
    err.println("rm  [source]                   : removes file from server");
    err.println("exit                           : exits from ftp console");
    err.println();
    err.println("This simple program connects to an FTP server");
    err.println("Default server: " + HOST);
    err.println("Default port: " + PORT);
    err.println();

    //System.exit(1);
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

  // "public" for accessibility from "FTPClientTest.java"
  // (absent a more modular structure for the project)
  public static void get(ChannelSftp sftpChannel, String sourceFilePath,
                         String destDirectoryPath) throws SftpException {
    System.out.println("Attempting to download \"" + sourceFilePath + "\" from server");

    try {
      sftpChannel.get(sourceFilePath, destDirectoryPath);
    } catch (SftpException e) {
      //System.err.println(e.getMessage());
      throw e;
    }

    System.out.println("Download successful");

    lsLocal(".");

  }

  // used to upload a file to the server
  public static void upload(ChannelSftp sftpChannel, String file) throws SftpException {

    System.out.println("Attempting to upload " + file + " to server");
    try {
      sftpChannel.put(file, file);
    } catch (SftpException e) {
      System.err.println(e.getMessage());
      throw e;
    }
    System.out.println("Upload successful");
    lsRemote(sftpChannel);
  }

  // used to delete a file from the server
  public static void delete(ChannelSftp sftpChannel, String file) throws SftpException {
    System.out.println("Deleting " + file + " from remote");
    try {
      sftpChannel.rm(file);
    } catch (SftpException e) {
      System.err.println("File does not exist on remote server");
      throw e;
    }
    System.out.println("Delete successful");
    lsRemote(sftpChannel);
  }

}








