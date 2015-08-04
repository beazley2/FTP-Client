package psu.agilemethods.src;


import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Arrays;

import static psu.agilemethods.src.TextUI.*;

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
  public static BufferedReader br;

  public static void main(String[] args) {
    br = new BufferedReader(new InputStreamReader(System.in));
    String userName = null;
    String password = null;
    Boolean quit = false;

    start();


    try {
      JSch jsch = new JSch();
      if (args.length != 2) {
        cmdError();
        System.exit(1);
      } else {
        userName = args[0];
        password = args[1];
      }

      JSch.setConfig("StrictHostKeyChecking", "no");
      Session session = jsch.getSession(userName, HOST, PORT);
      session.setPassword(password);

      session.connect();

      Channel channel = session.openChannel("sftp");
      channel.connect();

      ChannelSftp sftpChannel = (ChannelSftp) channel;

      System.out.println("Connected to " + HOST);

      while (!quit) {
        ArrayList<String> commands = getCommand(sftpChannel);
        quit = execCommand(sftpChannel, commands);
      }

      channel.disconnect();

    } catch (JSchException e) {
      System.err.println(e.getMessage());
    }




    System.exit(0);
  }

  private static Boolean execCommand(ChannelSftp sftpChannel, ArrayList<String> commands) {
    boolean quit = false;
    int size = commands.size();
    if (!commands.isEmpty()) {
      String cmd = commands.get(0).toLowerCase();
      switch (cmd) {
        case "mkdir":
          if (size < 2) {
            missingCommandArguments(cmd, "mkdir <path> - path can be complete or relative to current directory.");
          } else {
            mkdir(sftpChannel, commands.get(1));
          }
          break;
        case "ls":
          lsRemote(sftpChannel);
          break;
        case "dir":
          lsLocal(sftpChannel.lpwd());
          break;
        case "rm":
          if (size < 2) {
            missingCommandArguments(cmd, "rm <filepath> - file path can be complete or relative to current directory.");
          } else {
            rmRemote(sftpChannel, commands.get(1));
          }
          break;
        case "quit":
          quit = true;
          break;
        default:
          unknownCommand(cmd);
        }
      }
    return quit;
  }

  private static ArrayList<String> getCommand(ChannelSftp sftpChannel) {
    String delimiters = "[ ]+";
    String cmds = "";
    try {
      System.out.print(sftpChannel.pwd() + ">");
    } catch (SftpException e) {
      System.err.println(e.getMessage());
    }
    try {
      cmds = br.readLine();
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
    String[] split = cmds.split(delimiters);
    return new ArrayList<>(Arrays.asList(split));
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

  static void lsLocal(String path) {
    File dir = new File(path);
    File[] fileList = dir.listFiles();
    if (fileList != null) {
      for (File f : fileList) {
        if (f.isDirectory()) {
          System.out.println("Directory:\t" + f.getName());
        }
        if (f.isFile()) {
          System.out.println("File:\t\t" + f.getName());
        }
      }
    }
  }

  static void mkdir(ChannelSftp sftpChannel, String path) {
    try {
      sftpChannel.mkdir(path);
      System.out.println("Directory " + path + " created.");
    } catch (SftpException e) {
      System.out.println("Directory already exists.");
    }
  }

  static void rmRemote(ChannelSftp sftpChannel, String path) {
    Vector<ChannelSftp.LsEntry> files;
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
        fileName = paths[paths.length-1];
      }

      for (ChannelSftp.LsEntry file : files) {
        if (file.getFilename().equals(fileName)){
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
      System.out.println("File not found. No files deleted.");
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }

  }

  static void pause() {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}






