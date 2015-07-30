package psu.agilemethods.src

import com.jcraft.jsch.*;

import java.io.File;
import java.util.Vector;

/**
 * Created by domshyra on 7/30/15.
 */
class FTPClientTest extends groovy.util.GroovyTestCase {

    public static final String HOST = "ada.cs.pdx.edu";
    public static final int PORT = 22;
    public static final String FILE_TO_UL = "upload.txt";

    void testUpload(ChannelSftp sftpChannel, String file) {
        upload(sftpChannel, file);
//        assertEquals("value should be true", true, val);
    }

    void testUploadFileNotFound(ChannelSftp sftpChannel) {
        upload(sftpChannel, "upload2.txt");
        shouldFail(1);
//        assertEquals("value should be true", true, val);
    }

    void testUploadFileExistsAlready() {
//        assertEquals("value should be true", true, val);
    }

    void testDelete() {

    }

    void testDeleteFileNotFound() {

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


            testUpload(sftpChannel, FILE_TO_UL);
            testUploadFileNotFound(sftpChannel);
//            delete(sftpChannel, FILE_TO_UL);

            channel.disconnect();

        } catch (JSchException | SftpException e) {
            System.err.println(e.getMessage());
        }

        System.exit(0);
    }
}
