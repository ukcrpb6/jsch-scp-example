package com.pressassociation;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class Scp extends SSHBase {

    private String remoteDirectory = ".";

    public Scp(String host) {
        this(host, DEFAULT_SSH_PORT);
    }

    public Scp(String host, int port) {
        super(host, port);
    }

    /**
     * Configure remote target directory.
     *
     * @param remoteDirectory destination directory
     */
    public void setRemoteDirectory(String remoteDirectory) {
        this.remoteDirectory = checkNotNull(remoteDirectory);
    }

    /**
     * Upload file using SCP connection.
     *
     * @param file File to upload to destination
     * @throws IOException
     * @throws JSchException
     */
    public void upload(File file) throws IOException, JSchException {
        Session session = null;
        try {
            session = openSession();
            ScpToMessage message = new ScpToMessage(session, file, remoteDirectory, getVerbose());
            if (listener.isPresent()) {
                message.setLogListener(listener.get());
            }
            message.execute();
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
    }

}
