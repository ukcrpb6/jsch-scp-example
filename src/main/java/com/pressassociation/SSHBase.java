package com.pressassociation;

import com.google.common.base.Optional;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.logging.Level;

import static com.google.common.base.Preconditions.checkNotNull;

public class SSHBase {

    public static final int DEFAULT_SSH_PORT = 22;

    private boolean verbose;
    private SSHUserInfo userInfo;
    private String knownHosts;
    private String host;
    private int port;

    protected Optional<LogListener> listener = Optional.absent();

    public SSHBase(String host, int port) {
        this.host = checkNotNull(host);
        this.port = port;
        this.userInfo = new SSHUserInfo();
    }

    /**
     * Get user authentication.
     *
     * @return User authentication details
     */
    public SSHUserInfo getUserInfo() {
        return userInfo;
    }

    /**
     * Remote host, either DNS name or IP.
     *
     * @param host The new host value
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Get the host.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Set the verbose flag.
     *
     * @param verbose if true output more verbose logging
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Get the verbose flag.
     *
     * @return the verbose flag
     */
    public boolean getVerbose() {
        return verbose;
    }

    /**
     * Username known to remote host.
     *
     * @param username The new username value
     */
    public void setUsername(String username) {
        userInfo.setName(username);
    }


    /**
     * Sets the password for the user.
     *
     * @param password The new password value
     */
    public void setPassword(String password) {
        userInfo.setPassword(password);
    }

    /**
     * Sets the keyfile for the user.
     *
     * @param keyfile The new keyfile value
     */
    public void setKeyfile(String keyfile) {
        userInfo.setKeyfile(keyfile);
    }

    /**
     * Sets the passphrase for the users key.
     *
     * @param passphrase The new passphrase value
     */
    public void setPassphrase(String passphrase) {
        userInfo.setPassphrase(passphrase);
    }

    /**
     * Sets the path to the file that has the identities of
     * all known hosts.  This is used by SSH protocol to validate
     * the identity of the host.  The default is
     * <i>${user.home}/.ssh/known_hosts</i>.
     *
     * @param knownHosts a path to the known hosts file.
     */
    public void setKnownhosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }

    /**
     * Setting this to true trusts hosts whose identity is unknown.
     *
     * @param yesOrNo if true trust the identity of unknown hosts.
     */
    public void setTrust(boolean yesOrNo) {
        userInfo.setTrust(yesOrNo);
    }

    /**
     * Open an ssh seession.
     *
     * @return the opened session
     * @throws JSchException on error
     */
    public Session openSession() throws JSchException {
        JSch jsch = new JSch();
        if (verbose && listener.isPresent()) {
            JSch.setLogger(new com.jcraft.jsch.Logger() {
                public boolean isEnabled(int level) {
                    return true;
                }

                public void log(int level, String message) {
                    listener.get().log(Level.FINE, message);
                }
            });
        }
        if (null != userInfo.getKeyfile()) {
            jsch.addIdentity(userInfo.getKeyfile());
        }

        if (!userInfo.getTrust() && knownHosts != null) {
            if (listener.isPresent()) {
                listener.get().log(Level.FINE, "Using known hosts: " + knownHosts);
            }
            jsch.setKnownHosts(knownHosts);
        }

        Session session = jsch.getSession(userInfo.getName(), host, port);
        session.setUserInfo(userInfo);
        if (listener.isPresent()) {
            listener.get().log(Level.INFO, "Connecting to " + host + ":" + port);
        }
        session.connect();
        return session;
    }

    /**
     * Configure logging listener.
     *
     * @param listener
     */
    public void setListener(LogListener listener) {
        this.listener = Optional.of(listener);
    }
}
