package psu.agilemethods.src;

import com.jcraft.jsch.*;
import org.junit.*;
import psu.agilemethods.test.FTPTestSetup;

import java.util.ArrayList;
import java.util.Vector;

import static junit.framework.Assert.fail;

/**
 *
 * @author Brad
 * @version 8/2/15
 */
public class FTPClientTest {
  static ChannelSftp c;
  static ArrayList<String> fileNames;
  static Vector<ChannelSftp.LsEntry> fileList;
  static final String PATH = "ftp-testing";
  static final String LOCAL_PATH = "C:\\Users\\Brad\\IdeaProjects\\FTPClient\\ftp-test\\upload.txt";
  static final String FILE = "upload.txt";

  static public void refreshFileList() {
    try {
      fileList = c.ls(c.pwd());
      fileNames = new ArrayList<>();
      for (ChannelSftp.LsEntry file : fileList) {
        fileNames.add(file.getFilename());
      }
    } catch (SftpException e) {
      System.err.println(e.getMessage());
    }

  }

  @BeforeClass
  static public void init() {
    JSch jsch = new JSch();

    FTPTestSetup setup = new FTPTestSetup();
    String password = setup.getPassword();
    String user = setup.getUsername();
    String host = "ada.cs.pdx.edu";

    int port = 22;

    try {
      JSch.setConfig("StrictHostKeyChecking", "no");
      Session session = jsch.getSession(user, host, port);
      session.setPassword(password);

      session.connect();

      Channel channel = session.openChannel("sftp");
      channel.connect();

      c = (ChannelSftp) channel;
      c.rmdir(PATH);
    } catch ( JSchException | SftpException e) {
      e.getMessage();
    }
  }

  @Test
  public void mkdirSuccessful() {
    try {
      String curPath = c.pwd();
      c.mkdir(PATH);
      c.cd(PATH);
      Assert.assertEquals(c.pwd(), curPath + "/" + PATH);
    } catch (SftpException e) {
      fail("Directory already exists or failed to create.");
    }

  }

  @Test
  public void rmSuccessful() {
    try {
      c.put(LOCAL_PATH, FILE);
      refreshFileList();
      Assert.assertTrue("upload.txt exists remotely", fileNames.contains(FILE));
      c.rm(FILE);
      refreshFileList();
      Assert.assertFalse("upload.txt no longer exists", fileNames.contains(FILE));
    } catch (SftpException e) {
      e.getMessage();
    }
  }

}
