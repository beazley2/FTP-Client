package psu.agilemethods.src;

import com.jcraft.jsch.*;

import java.io.*;
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

      // In bash etc. hide password input w/ readPassword method
      // (System.console() returns null when running in IDE)
      Console console = System.console();
      if (console == null) {
        password = TextUI.getPassword();
      }
      else {
        char passwordArray[] = console.readPassword("Password: ");
        password = new String(passwordArray);
      }

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

  /**
   * Parses command line args
   * @param cmdIn string entered on command line
   * @param c channel connection with FTP server
   */
  public static void parseCmd(String cmdIn, ChannelSftp c) {
    String[] cmd = cmdIn.split("[ ]+");
    ArrayList<String> cmdArgs = new ArrayList<>(Arrays.asList(cmd));
    Iterator itr = cmdArgs.iterator();
    if (itr.hasNext()) {
      String arg = (String) itr.next();
      switch (arg) {
        case "help":case "readme":
          usage("README file for FTP Client");
          break;
        case "cd":
          try {
            String path = (String) itr.next();
            try {
              cd(c, path);
            } catch (SftpException e) {
              System.out.println("No such directory");
            }
          } catch (NoSuchElementException e) {
            usage("Desired path must be specified");
          }
          break;
        case "get":
          String srcPath;
          String destPath;
          do {
            try {
              srcPath = (String) itr.next();
              if (itr.hasNext()) {
                destPath = (String) itr.next();
                try {
                  get(c, srcPath, destPath);
                } catch (SftpException e) {
                  System.out.println(e.getMessage());
                }
              }
              else {
                System.out.println("Source path (" + srcPath +
                        ") given without matching destination - no download.");
              }
            } catch (NoSuchElementException e) {
              usage("Source and destination path must be specified (in pairs to 'get' multiple)");
            }
          } while (itr.hasNext());
          break;
        case "put":
          try {
            String source;
            String dest;
            do {
              source = (String) itr.next();
              dest = (String) itr.next();
              try {
                upload(c, source, dest);
              } catch (SftpException e) {
                System.out.println("Source file " + source + " not found");
              }
            } while (itr.hasNext());
          } catch (NoSuchElementException e) {
            usage("Source and destination must be specified");
          }
          break;
        case "rm":
          try {
            String file;
            do {
              file = (String) itr.next();
              try {
                rmRemote(c, file);
              } catch (SftpException e) {
                System.out.println(e.getMessage());
              }
            } while (itr.hasNext());
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
              String pString = (String) itr.next();
              if (pString.contains("8") || pString.contains("9")) {
                System.out.println("Second argument must be in Octal form 000 - 777 representing the desired permission changes. Cannot use 8 or 9");
              } else {
                int permissions = Integer.parseInt(pString, 8);
                if (permissions >= 000 && permissions <= 511) {
                  String path = (String) itr.next();
                  try {
                    chmod(c, permissions, path);
                  } catch (SftpException e) {
                    System.out.println(e.getMessage());
                  }
                } else {
                  System.out.println("Permissions must be octal 000 - 777 only.");
                }
              }
            } catch (NumberFormatException e) {
                System.out.println("Second argument must be in Octal form 000 - 777 representing the desired permission changes.");
            } catch (NoSuchElementException e) {
              usage("Permissions and file path must be specified.");
            }
          break;
        case "pwd":
          try {
            pwd(c);
          } catch (SftpException e) {
            System.out.println(e.getMessage());
          }
          break;
        case "lpwd":
          lpwd(c);
          break;
        case "lcd":
          try {
            String path = (String) itr.next();
            lcd(c, path);
          } catch (NoSuchElementException e) {
            System.out.println("Path must be specified.");
          } catch (SftpException e) {
            System.out.println(e.getMessage());
          }
          break;
        case "mv":
          String oldFile = (String) itr.next();
          String newFile = (String) itr.next();
          rename(c, oldFile, newFile);
          break;
        case "exit":
          break;
        default:
          usage("That option is not recognized. Please view README");
          break;
      }
    } else {
      System.out.println("No command entered.");
    }

  }

  /**
   * Convenient formatting for error messages
   * @param message
   */
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
    PrintStream err = System.out;
    err.println("** " + message);
    err.println();
    err.println("usage:");
    err.println("help                           : displays this README");
    err.println("pwd                            : shows current remote directory");
    err.println("lpwd                           : shows current local directory");
    err.println("ls                             : list contents of remote directory");
    err.println("dir                            : list contents of local directory");
    err.println("cd [path]                      : change remote directory");
    err.println("lcd [path]                     : change local directory");
    err.println("mkdir [path]                   : creates new directory on server");
    err.println("mv [oldFilePath] [newFilePath] : renames file on server");
    err.println("get [source] [destination]     : gets file from server");
    err.println("put [source] [destination]     : puts file on server (wild cards are permitted)");
    err.println("put [[source] [destination]]*  : puts multiple files on server");
    err.println("rm [file]                      : removes file from server (wild cards are permtited)");
    err.println("rm {file]*                     : removes multiple files from server");
    err.println("chmod [permissions] [path]     : changes file permission on the server");
    err.println("exit                           : exits from ftp console");
    err.println();
    err.println("This program connects to an FTP server");
    err.println("Default server: " + HOST);
    err.println("Default port: " + PORT);
    err.println();

  }

  /**
   * Lists contents of local directory
   * @param path
   */
  static void lsLocal(String path) {
    File dir = new File(path);
    File[] fileList = dir.listFiles();
    System.out.println("Displaying contents of " + path);
    for (File f : fileList) {
      if (f.isDirectory()) {
        System.out.println("Directory:\t" + f.getName());
      }
      if (f.isFile()) {
        System.out.println("File:\t\t" + f.getName());
      }
    }
  }

  /**
   * Lists contents of remote directory
   * @param ch
   */
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

  /**
   * Changes permissions on remote file
   * @param sftpChannel
   * @param permissions
   * @param path
   * @throws SftpException
   */
  static void chmod(ChannelSftp sftpChannel, int permissions, String path) throws SftpException {
    try {
      sftpChannel.chmod(permissions, path);
      System.out.println("Permissions changed.");
    } catch (SftpException e) {
      throw e;
    }
  }

  /**
   * Creates a file on remote directory
   * @param sftpChannel
   * @param path
   * @throws SftpException
   */
  static void mkdir(ChannelSftp sftpChannel, String path) throws SftpException {
    try {
      sftpChannel.mkdir(path);
      System.out.println("Directory " + path + " created.");
    } catch (SftpException e) {
      throw e;
    }
  }

  /**
   * renames a file on the remote server
   * @param sftpChannel
   * @param oldFileName
   * @param newFileName
   */
  static void rename(ChannelSftp sftpChannel, String oldFileName, String newFileName) {
    try {
      sftpChannel.rename(oldFileName, newFileName);
      System.out.println("file renamed");
    } catch (SftpException e) {
      error("Unable to rename file: " + e.getMessage());
    }
  }

  /**
   * Removes a file from remote directory
   * @param sftpChannel
   * @param path
   * @throws SftpException
   */
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

      if (!fileName.contains("*")) {
        for (ChannelSftp.LsEntry file : files) {
          if (file.getFilename().equals(fileName)) {
            exists = true;
            break;
          }
        }
      }

      if (exists || fileName.contains("*")) {
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
        System.out.println(fileName + " does not exist on remote server");

      }
    } catch (SftpException | IOException e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * gets a file from the FTP server
   * @param sftpChannel
   * @param sourceFilePath
   * @param destDirectoryPath
   * @throws SftpException
   */
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

  }

  /**
   * Uploads file to remote ftp server
   * @param sftpChannel
   * @param sourceFilePath
   * @param destDirectoryPath
   * @throws SftpException
   */
  public static void upload(ChannelSftp sftpChannel, String sourceFilePath,
                            String destDirectoryPath) throws SftpException {

    System.out.println("Attempting to upload " + sourceFilePath + " to server");
    try {
      sftpChannel.put(sourceFilePath, destDirectoryPath);
    } catch (SftpException e) {
      throw e;
    }
    System.out.println("Upload successful");
    lsRemote(sftpChannel);
  }

  /**
   * Changes working directory on remote server
   * @param c
   * @param path
   * @throws SftpException
   */
  public static void cd(ChannelSftp c, String path) throws SftpException {
    try {
      c.cd(path);
    } catch (SftpException e) {
      throw e;
    }
  }

  /**
   * Displays remote working directory
   * @param c
   * @throws SftpException
   */
  public static void pwd(ChannelSftp c) throws SftpException {
    try {
      System.out.println("Current remote directory: " + c.pwd());
    } catch (SftpException e) {
      throw e;
    }
  }

  /**
   * Changes local working directory
   * @param c
   * @param path
   * @throws SftpException
   */
  public static void lcd(ChannelSftp c, String path) throws SftpException {
    try {
      c.lcd(path);
    } catch (SftpException e) {
      throw e;
    }
  }

  /**
   * Displays local working directory
   * @param c
   */
  public static void lpwd(ChannelSftp c)  {
    System.out.println("Current local directory: " + c.lpwd());
  }
}








