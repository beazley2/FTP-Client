package psu.agilemethods.src;


import com.jcraft.jsch.*;

import java.util.Vector;

import static psu.agilemethods.src.TextUI.*;

/**
 * Implements a simple FTP client
 *
 * @version 0.1
 */
public class FTPClient{

  public static final int PORT = 22;

  public static void main(String[] args) {
    String hostName = null;
    String userName = null;
    String password = null;

    start();


    try {
      JSch jsch = new JSch();
      if (args.length != 3) {
        cmd_error();
        System.exit(1);
      } else {
        hostName = args[0];
        userName = args[1];
        password = args[2];
      }

      JSch.setConfig("StrictHostKeyChecking", "no");
      Session session = jsch.getSession(userName, hostName, PORT);
      session.setPassword(password);

      session.connect();

      Channel channel = session.openChannel("sftp");
      channel.connect();

      ChannelSftp sftpChannel = (ChannelSftp) channel;

      String dirPath = "/u/" + userName + "/";
      Vector<ChannelSftp.LsEntry> currentDir = sftpChannel.ls(dirPath);
      for (ChannelSftp.LsEntry entry : currentDir) {
        System.out.println(entry.getLongname()); // + "/t" + entry.getAttrs().toString());
      }
      System.out.println(sftpChannel.pwd() + ">");

      channel.disconnect();

    } catch (JSchException | SftpException e) {
      System.err.println(e.getMessage());
    }




    System.exit(0);
  }


}






