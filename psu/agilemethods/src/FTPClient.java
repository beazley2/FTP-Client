package psu.agilemethods.src;


import com.jcraft.jsch.*;

import java.io.File;
import java.util.Vector;

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

  public static void main(String[] args) {
    String userName = null;
    String password = null;

    start();


    try {
      JSch jsch = new JSch();
      if (args.length != 2) {
        cmd_error();
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

      System.out.println("Showing current directory: ");
      System.out.println("Current directory: " + sftpChannel.pwd());
      pause();

      System.out.println("Showing contents on current directory");
      lsRemote(sftpChannel);
      pause();

      System.out.println("Making new remote directory: ftp-test ");
      mkdir(sftpChannel, "ftp-test");
      pause();

      System.out.println("Changing to newly created directory: ");
      sftpChannel.cd("ftp-test");
      System.out.println("Current directory: " + sftpChannel.pwd());
      pause();

      System.out.println("Showing current local directory: ");
      System.out.println("Local directory: " + sftpChannel.lpwd());
      pause();

      System.out.println("Making new local directory: ftp-test");
      String newLocalDirPath = "ftp-test";
      if (new File(newLocalDirPath).mkdir()) {
        System.out.println("Directory created.");
      } else {
        System.out.println("Directory already exists.");
      }
      pause();

      System.out.println("Changing to newly created local directory: ");
      sftpChannel.lcd("ftp-test");
      System.out.println("Local directory: " + sftpChannel.lpwd());
      pause();

      System.out.println("Showing contents of local directory: ");
      lsLocal(sftpChannel.lpwd());
      pause();

      System.out.println("Attempting to download download.txt from server");
      try {
        sftpChannel.get(FILE_TO_DL, FILE_TO_DL);
      } catch (SftpException e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
      System.out.println("Download Successful.");
      lsLocal(sftpChannel.lpwd());
      pause();

      System.out.println("Attempting to upload upload.txt to server");
      try {
        sftpChannel.put(FILE_TO_UL, FILE_TO_UL);
      } catch (SftpException e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
      System.out.println("Upload successful");
      lsRemote(sftpChannel);
      pause();

      System.out.println("Renaming upload.txt to uploaded.txt");
      sftpChannel.rename("upload.txt", "uploaded.txt");
      lsRemote(sftpChannel);
      pause();

      System.out.println("Deleting uploaded.txt from remote");
      rmRemote(sftpChannel, "uploaded.txt");
      lsRemote(sftpChannel);
      pause();



      channel.disconnect();

    } catch (JSchException | SftpException e) {
      System.err.println(e.getMessage());
    }




    System.exit(0);
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
    for (File f : fileList) {
      if (f.isDirectory()) {
        System.out.println("Directory:\t" + f.getName());
      }
      if (f.isFile()) {
        System.out.println("File:\t\t" + f.getName());
      }
    }
  }

  static void mkdir(ChannelSftp sftpChannel, String path) {
    try {
      sftpChannel.mkdir(path);
      System.out.println("Directory " + path + "created.");
    } catch (SftpException e) {
      System.out.println("Directory already exists.");
    }
  }

  static void rmRemote(ChannelSftp sftpChannel, String path) {
    try {
      System.out.println("Deleting " + path);
      sftpChannel.rm(path);
    } catch (SftpException e) {
      System.out.println("File not found. No files deleted.");
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






