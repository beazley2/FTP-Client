package psu.agilemethods.test;

import com.jcraft.jsch.*;
import junit.framework.Assert;
import org.junit.*;
import psu.agilemethods.src.FTPClient;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by gaber on 7/31/15.
 */


public class FTPClientTest {
    static ChannelSftp c;
    public static final String XFER_FILE = "xfer.txt";
    public static final String XFER_FILE2 = "xfer2.txt";
    public static final String XFER_FILE3 = "xfer3.txt";
    FTPClient client = new FTPClient();
    static File dir = new File(".");
    static File[] fileList;
    static ArrayList<String> localFileNames;
    static ArrayList<String> hostFileNames;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    static public void refreshFileList() {
        fileList = dir.listFiles();
        localFileNames = new ArrayList();
        for (File f : fileList) {
            localFileNames.add(f.getName());
        }
    }

    @Before
    public void setUpFiles() {
        //ensure there is a file on the server to get
        try {
            Writer writer = new FileWriter(XFER_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            c.put(XFER_FILE);
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    static public void initChannelandHostLs() {
        JSch jsch = new JSch();
        JSch.setConfig("StrictHostKeyChecking", "no");


        FTPTestSetup setup = new FTPTestSetup();
        String password = setup.getPassword();
        String user = setup.getUsername();
        String host = "ada.cs.pdx.edu";
        int port = 22;

        try {
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            c = (ChannelSftp) channel;
            assert (channel.isConnected());
        } catch (JSchException e) {
            e.printStackTrace();
        }
        try {
            Vector<ChannelSftp.LsEntry> currentDir = c.ls(c.pwd());
            hostFileNames = new ArrayList();
            for (ChannelSftp.LsEntry entry : currentDir) {
                hostFileNames.add(entry.getFilename());
            }
        } catch (SftpException e) {
        }
    }

    @Before
    public void refresh() {
        refreshFileList();
    }


    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
        System.setErr(null);
    }

    @Test
    public void targetFilePresent() {
        Assert.assertTrue("xfer.txt absent on host",
            hostFileNames.contains(XFER_FILE));
    }

    @Test
    public void targetFileAbsent() {
        try {
            client.get(c, "fu.bar", ".");
        } catch (SftpException e) {
            Assert.assertEquals("2: No such file", e.toString());
        }
    }

    @Test
    public void testParseCmdGetFailsWithNoParams() {
        client.parseCmd("get", c);
        assertThat(outContent.toString(), containsString("Source and destination path must be specified"));
    }

    @Test
    public void testParseCmdGetPassesWithParams() {
        String cmdString = "get " + XFER_FILE + " .";
        client.parseCmd(cmdString, c);
        assertThat(outContent.toString(), containsString("Download successful"));
    }

    @Test
    public void testParseCmdPutFailsWithNoParams() {
        client.parseCmd("put", c);
        assertThat(outContent.toString(), containsString("Source and destination must be specified"));
    }

    @Test
    public void testParseCmdPutPassesWithParams() {
        String cmdString = "put " + XFER_FILE + " .";
        client.parseCmd(cmdString, c);
        assertThat(outContent.toString(), containsString("Upload successful"));
    }

    @Test
    public void testParseCmdPutPassesWithDuplicateName() {
        String cmdString = "put " + XFER_FILE + " .";
        client.parseCmd(cmdString, c);
        client.parseCmd(cmdString, c);
        assertThat(outContent.toString(), containsString("Upload successful"));
    }

    @Test
    public void testParseCmdPutFailsWithNonpresentFile() {
        String cmdString = "put idontexist.txt .";
        client.parseCmd(cmdString, c);
        assertThat(outContent.toString(), containsString("Source file idontexist.txt not found"));
    }

    @Test
    public void testParseCmdPutWithTwoFilesPasses() {
        String cmdString = "put " + XFER_FILE2 + " . " + XFER_FILE3 + " . ";
        client.parseCmd(cmdString, c);
        assertThat(outContent.toString(), containsString("Upload successful"));
        assertThat("See if XFER_FILE2 was found", (!outContent.toString().contains("Source file " + XFER_FILE2 + "not found")));
        assertThat("See if XFER_FILE3 was found", (!outContent.toString().contains("Source file " + XFER_FILE3 + "not found")));
    }



    @Test
    public void testParseCmdDeletePassesWithParams() {
        String cmdString = "rm " + XFER_FILE;
        client.parseCmd(cmdString, c);
        assertThat(outContent.toString(), containsString(XFER_FILE + " deleted."));
    }

    @Test
    public void testParseCmdDeleteFailsWithNoParams() {
        client.parseCmd("rm", c);
        assertThat(outContent.toString(), containsString("Source must be specified"));
    }

    @Test
    public void testParseCmdDeleteFailsWithFileNotFound() {
        String cmdString = "rm " + "Notfound.txt" + ".";
        client.parseCmd(cmdString, c);
        assertThat(outContent.toString(), containsString("Notfound.txt. does not exist on remote server"));
    }

    @Test
    public void testParseCmdcdFailsWithInvalidDirectory() {
        String cmdString = "cd idontexist";
        client.parseCmd(cmdString, c);
        assertThat(outContent.toString(), containsString("No such directory"));
    }

    @Test
    public void testParseCmdlcdFailsWithInvalidDirectory() {
        String cmdString = "lcd idontexist";
        client.parseCmd(cmdString, c);
        assertThat(outContent.toString(), containsString("No such directory"));
    }
}
