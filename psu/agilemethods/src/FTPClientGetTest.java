package psu.agilemethods.src;


import com.jcraft.jsch.*;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by gaber on 7/31/15.
 */


public class FTPClientGetTest {
    static ChannelSftp c;
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

        String user= "eschott";
        String host="ada.cs.pdx.edu";
        int port=22;

        try {
            Session session=jsch.getSession(user, host, port);
        //requires setting user specific info for login
            UserInfo ui = new SampleSftp();
            session.setUserInfo(ui);

            session.setPassword("viP#R450490");
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
    }

    @Before
    public void refresh() {
        refreshFileList();
    }

    @Test
    public void targetFilePresent() {
        Assert.assertTrue("xfer.txt absent on host",
                hostFileNames.contains("xfer.txt"));
    }

    @Test
    public void fileNotLocalAndConfirmTransfer() {
        Assert.assertFalse("xfer.txt present locally",
                localFileNames.contains("xfer.txt"));

        try {
            client.get(c, "xfer.txt", ".");
        } catch (SftpException e) {}

        refreshFileList();

        Assert.assertTrue("xfer.txt absent (not transferred)",
                localFileNames.contains("xfer.txt"));
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
