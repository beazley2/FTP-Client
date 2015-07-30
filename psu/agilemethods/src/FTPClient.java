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
    String userName = "null";
    String password = "null";

    TextUI.start();


    try {
      JSch jsch = new JSch();

      JSch.setConfig("StrictHostKeyChecking", "no");
      Session session = jsch.getSession(userName, HOST, PORT);
      session.setPassword(password);

      session.connect();

      Channel channel = session.openChannel("sftp");
      channel.connect();

      ChannelSftp sftpChannel = (ChannelSftp) channel;


      upload(sftpChannel, FILE_TO_UL);
      delete(sftpChannel, FILE_TO_UL);

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

  // used to upload a file to the server
  static void upload(ChannelSftp sftpChannel, String file) throws SftpException {

    System.out.println("Attempting to upload" + file + " to server");
    try {
      sftpChannel.put(file, file);
    } catch (SftpException e) {
      System.err.println(e.getMessage());
    }
    System.out.println("Upload successful");
    lsRemote(sftpChannel);
    pause();
  }

  // used to delete a file from the server
  static void delete(ChannelSftp sftpChannel, String file) throws SftpException {
    System.out.println("Deleting " + file + " from remote");
    try {
      sftpChannel.rm(file);
    } catch (SftpException e) {
      System.err.println(e.getMessage());
    }
    lsRemote(sftpChannel);
    pause();
  }

  static void pause() {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}






