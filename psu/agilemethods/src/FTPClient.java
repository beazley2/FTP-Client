package psu.agilemethods.src;

import com.jcraft.jsch.*;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static psu.agilemethods.src.TextUI.*;

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
    Boolean quit = false;

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
        System.out.println("Connected to " + HOST);
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
        String curPath = c.pwd();
        cmd = TextUI.getCommand(curPath);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (SftpException e) {
        System.out.println(e.getMessage());
      }
      parseCmd(cmd, c);
    }

    if (session != null) {
      session.disconnect();
    }
    System.exit(0);
  }

  public static void parseCmd(String cmdIn, ChannelSftp c) {
    String[] cmd = cmdIn.split("[ ]+");
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
              rmRemote(c, file);
            } catch (SftpException e) {
              System.out.println(e.getMessage());
            }
          } catch (NoSuchElementException e) {
            usage("Source must be specified");
          }
          break;
        case "mkdir":
          try {
            String path = (String) itr.next();
            try {
              mkdir(c, path);
            } catch (SftpException e) {
              System.out.println("Failed to create new directory.");
            }
          } catch (NoSuchElementException e) {
            usage("New directory path must be specified.");
          }
          break;
        case "ls":
          lsRemote(c);
          break;
        case "dir":
          lsLocal(c.lpwd());
          break;
        case "chmod":
            try {
              int permissions = Integer.parseInt((String) itr.next());
              String path = (String) itr.next();
              try {
                chmod(c, permissions, path);
              } catch (SftpException e) {
                System.out.println(e.getMessage());
              }
            } catch (NumberFormatException e) {
              System.out.println("Second argument must be an integer representing the desired permission changes.");
            } catch (NoSuchElementException e) {
              usage("Permissions and file path must be specified.");
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
    err.println("mkdir [path]                   : creates new directory on server");
    err.println("chmod [permissions] [path]     : changes file permission on the server");
    err.println("exit                           : exits from ftp console");
    err.println();
    err.println("This program connects to an FTP server");
    err.println("Default server: " + HOST);
    err.println("Default port: " + PORT);
    err.println();

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

  static void chmod(ChannelSftp sftpChannel, int permissions, String path) throws SftpException {
    try {
      sftpChannel.chmod(permissions, path);
      System.out.println("Permissions changed.");
    } catch (SftpException e) {
      throw e;
    }
  }

  static void mkdir(ChannelSftp sftpChannel, String path) throws SftpException {
    try {
      sftpChannel.mkdir(path);
      System.out.println("Directory " + path + " created.");
    } catch (SftpException e) {
      throw e;
    }
  }

  static void rmRemote(ChannelSftp sftpChannel, String path) throws SftpException {
    Vector<ChannelSftp.LsEntry> files;
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    String fileName;
    String dirPath = "";
    String[] paths = path.split("/");
    boolean exists = false;


    try {
      if (paths.length == 1) {
        files = sftpChannel.ls(sftpChannel.pwd());
        fileName = path;
      } else {
        for (int i = 0; i < paths.length - 1; i++) {
          dirPath += paths[i] + "/";
        }
        files = sftpChannel.ls(dirPath);
        fileName = paths[paths.length - 1];
      }

      for (ChannelSftp.LsEntry file : files) {
        if (file.getFilename().equals(fileName)) {
          exists = true;
          break;
        }
      }

      if (exists) {
        System.out.println("Deleting " + path);
        System.out.println("Are you sure? (y/n)");
        String confirm = br.readLine();
        String[] confirmSplit = confirm.split("[ ]+");
        confirm = confirmSplit[0].toLowerCase();
        if (confirm.equals("y") || confirm.equals("yes")) {
          sftpChannel.rm(path);
          System.out.println(fileName + " deleted.");
        } else {
          System.out.println("Deletion cancelled.");
        }
      } else {
        System.out.println("File not found.");
      }
    } catch (SftpException e) {
      throw e;
    } catch (IOException e) {
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

}








