package com.pressassociation;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.sun.xml.internal.rngom.ast.builder.BuildException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.logging.Level;

public abstract class AbstractSshMessage {
    private static final double ONE_SECOND = 1000.0;
    private static final byte[] ACK = {0};

    private Session session;
    private boolean verbose;

    protected LogListener listener = new LogListener() {
        public void log(Level level, String message) { /* NOOP */ }
    };


    /**
     * Constructor for AbstractSshMessage
     *
     * @param session the ssh session to use
     */
    public AbstractSshMessage(Session session) {
        this(session, false);
    }

    /**
     * Constructor for AbstractSshMessage
     *
     * @param session the ssh session to use
     * @param verbose if true do verbose logging
     */
    public AbstractSshMessage(Session session, boolean verbose) {
        this.session = session;
        this.verbose = verbose;
    }

    /**
     * Open an ssh channel.
     *
     * @param command the command to use
     * @return the channel
     * @throws JSchException on error
     */
    protected Channel openExecChannel(String command) throws JSchException {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        return channel;
    }

    /**
     * Send an ack.
     *
     * @param out the output stream to use
     * @throws IOException on error
     */
    protected void sendAck(OutputStream out) throws IOException {
        out.write(ACK);
        out.flush();
    }

    /**
     * Reads the response, throws a BuildException if the response
     * indicates an error.
     *
     * @param in the input stream to use
     * @throws IOException    on I/O error
     * @throws BuildException on other errors
     */
    protected void waitForAck(InputStream in) throws IOException {
        int b = in.read();

        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,

        if (b == -1) {
            // didn't receive any response
            throw new SSHException("No response from server");
        } else if (b != 0) {
            StringBuilder sb = new StringBuilder();

            int c = in.read();
            while (c > 0 && c != '\n') {
                sb.append((char) c);
                c = in.read();
            }

            if (b == 1) {
                throw new SSHException("server indicated an error: " + sb.toString());
            } else if (b == 2) {
                throw new SSHException("server indicated a fatal error: " + sb.toString());
            } else {
                throw new SSHException("unknown response, code " + b + " message: " + sb.toString());
            }
        }
    }

    /**
     * Carry out the transfer.
     *
     * @throws IOException   on I/O errors
     * @throws JSchException on ssh errors
     */
    public abstract void execute() throws IOException, JSchException;

    /**
     * Set a log listener.
     *
     * @param aListener the log listener
     */
    public void setLogListener(LogListener aListener) {
        listener = aListener;
    }

    /**
     * Log a message to the log listener.
     *
     * @param message the message to log
     */
    protected void log(String message) {
        log(Level.FINE, message);
    }

    /**
     * Log a message to the log listener.
     *
     * @param message the message to log
     */
    protected void log(Level level, String message) {
        listener.log(level, message);
    }

    /**
     * Log transfer stats to the log listener.
     *
     * @param timeStarted the time started
     * @param timeEnded   the finishing time
     * @param totalLength the total length
     */
    protected void logStats(long timeStarted, long timeEnded, long totalLength) {
        double duration = (timeEnded - timeStarted) / ONE_SECOND;
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(1);
        listener.log(Level.INFO, "File transfer time: " + format.format(duration)
                + " Average Rate: " + format.format(totalLength / duration)
                + " B/s");
    }

    /**
     * Is the verbose attribute set.
     *
     * @return true if the verbose attribute is set
     */
    protected final boolean getVerbose() {
        return verbose;
    }

}
