package psu.agilemethods.test;

import com.jcraft.jsch.*;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import psu.agilemethods.src.FTPClient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by gaber on 7/31/15.
 */


public class FTPClientTest {
    static ChannelSftp c;
    public static final String UL_FILE = "ul.txt";
    public static final String DL_FILE = "dl.txt";
    FTPClient client = new FTPClient();
    static File dir = new File(".");
    static File [] fileList;
    static ArrayList<String> localFileNames;
    static ArrayList<String> hostFileNames;

    static public void refreshFileList() {
        fileList = dir.listFiles();
        localFileNames = new ArrayList();
        for (File f : fileList)
        {
            localFileNames.add(f.getName());
        }
    }

    @BeforeClass
    static public void initChannelandHostLs() {
        JSch jsch=new JSch();
        JSch.setConfig("StrictHostKeyChecking", "no");


        FTPTestSetup setup = new FTPTestSetup();
        String password = setup.getPassword();
        String user= setup.getUsername();
        String host="ada.cs.pdx.edu";
        int port=22;

        try {
            Session session=jsch.getSession(user, host, port);
            session.setPassword(password);
            session.connect();

            Channel channel=session.openChannel("sftp");
            channel.connect();
            c=(ChannelSftp)channel;
            assert(channel.isConnected());
        }
        catch (JSchException e) {
            e.printStackTrace();
        }
        try {
            Vector<ChannelSftp.LsEntry> currentDir = c.ls(c.pwd());
            hostFileNames = new ArrayList();
            for (ChannelSftp.LsEntry entry : currentDir) {
                hostFileNames.add(entry.getFilename());
            }
        } catch (SftpException e) {}

        //ensure there is a file on the server to get
        try {
            Writer writer = new FileWriter(UL_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            c.put(UL_FILE);
            c.rename(UL_FILE, DL_FILE);
        } catch (SftpException e) {
            e.printStackTrace();
        }

    }

    @Before
    public void refresh() {
        refreshFileList();
    }

    //Error: files still being used by another process
    /*@AfterClass
    public static void tearDown() throws Exception {
        Path path1 = Paths.get("ul.txt");
        Files.deleteIfExists(path1);
        Files.deleteIfExists(Paths.get("dl.txt"));
    }*/

    @Test
    public void targetFilePresent() {
        Assert.assertTrue("dl.txt absent on host",
                hostFileNames.contains(DL_FILE));
    }

    @Test
    public void fileNotLocalAndConfirmTransfer() {
        Assert.assertFalse("dl.txt present locally",
                localFileNames.contains(DL_FILE));

        try {
            client.get(c, DL_FILE, ".");
        } catch (SftpException e) {}

        refreshFileList();

        Assert.assertTrue("xfer.txt absent (not transferred)",
                localFileNames.contains(DL_FILE));
    }

    @Test
    public void targetFileAbsent() {
        try {
            client.get(c, "fu.bar", ".");
        } catch (SftpException e){
            Assert.assertEquals("2: No such file", e.toString());
        }
    }


}
