package com.pressassociation;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.*;
import java.util.logging.Level;

public class ScpToMessage extends AbstractSshMessage {

    private static final int BUFFER_SIZE = 1024;

    private File localFile;
    private String remotePath;

    /**
     * Constructor for a local file to remote.
     *
     * @param session     the scp session to use
     * @param aLocalFile  the local file
     * @param aRemotePath the remote path
     * @param verbose     if true do verbose logging
     */
    public ScpToMessage(Session session, File aLocalFile, String aRemotePath, boolean verbose) {
        super(session, verbose);
        this.localFile = aLocalFile;
        this.remotePath = aRemotePath;
    }

    /**
     * Constructor for ScpToMessage.
     *
     * @param session     the scp session to use
     * @param aLocalFile  the local file
     * @param aRemotePath the remote path
     */
    public ScpToMessage(Session session, File aLocalFile, String aRemotePath) {
        this(session, aLocalFile, aRemotePath, false);
    }

    /**
     * Carry out the transfer.
     *
     * @throws IOException   on i/o errors
     * @throws JSchException on errors detected by scp
     */
    public void execute() throws IOException, JSchException {
        if (localFile != null) {
            doSingleTransfer();
        }
    }

    private void doSingleTransfer() throws IOException, JSchException {
        String cmd = "scp -t " + remotePath;
        Channel channel = openExecChannel(cmd);
        try {

            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            waitForAck(in);
            sendFileToRemote(localFile, in, out);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    private void sendFileToRemote(File localFile, InputStream in, OutputStream out) throws IOException {
        // send "C0644 filesize filename", where filename should not include '/'
        long filesize = localFile.length();
        String command = "C0644 " + filesize + " ";
        command += localFile.getName();
        command += "\n";

        out.write(command.getBytes());
        out.flush();

        waitForAck(in);

        // send a content of lfile
        FileInputStream fis = new FileInputStream(localFile);
        byte[] buf = new byte[BUFFER_SIZE];
        long startTime = System.currentTimeMillis();
        long totalLength = 0;

        try {
            if (getVerbose()) {
                log(Level.INFO, "Sending: " + localFile.getName() + " : " + localFile.length());
            }
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) {
                    break;
                }
                out.write(buf, 0, len);
                totalLength += len;
            }
            out.flush();
            sendAck(out);
            waitForAck(in);
        } finally {
            if (getVerbose()) {
                long endTime = System.currentTimeMillis();
                logStats(startTime, endTime, totalLength);
            }
            fis.close();
        }
    }

    /**
     * Get the local file
     *
     * @return the local file
     */
    public File getLocalFile() {
        return localFile;
    }

    /**
     * Get the remote path
     *
     * @return the remote path
     */
    public String getRemotePath() {
        return remotePath;
    }
}
