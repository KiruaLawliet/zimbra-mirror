/**
 * $RCSfile: ConnectionManagerImpl.java,v $
 * $Revision: 3159 $
 * $Date: 2005-12-04 22:56:40 -0300 (Sun, 04 Dec 2005) $
 *
 * Copyright (C) 2004 Jive Software. All rights reserved.
 *
 * This software is published under the terms of the GNU Public License (GPL),
 * a copy of which is included in this distribution.
 */

package org.jivesoftware.wildfire.spi;

import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.Log;
import org.jivesoftware.wildfire.*;
import org.jivesoftware.wildfire.container.BasicModule;
import org.jivesoftware.wildfire.multiplex.MultiplexerPacketDeliverer;
import org.jivesoftware.wildfire.net.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConnectionManagerImpl extends BasicModule implements ConnectionManager {

    private SocketAcceptThread socketThread;
    private SSLSocketAcceptThread sslSocketThread;
    private SocketAcceptThread componentSocketThread;
    private SocketAcceptThread serverSocketThread;
    private SocketAcceptThread multiplexerSocketThread;
    private ArrayList<ServerPort> ports;

    private SessionManager sessionManager;
    private PacketDeliverer deliverer;
    private PacketRouter router;
    private RoutingTable routingTable;
    private String localIPAddress = null;

    // Used to know if the sockets can be started (the connection manager has been started)
    private boolean isStarted = false;
    // Used to know if the sockets have been started
    private boolean isSocketStarted = false;

    public ConnectionManagerImpl() {
        super("Connection Manager");
        ports = new ArrayList<ServerPort>(4);
    }

    private void createSocket() {
        if (!isStarted || isSocketStarted || sessionManager == null || deliverer == null ||
                router == null)
        {
            return;
        }
        isSocketStarted = true;

        // Setup port info
        try {
            localIPAddress = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e) {
            if (localIPAddress == null) {
                localIPAddress = "Unknown";
            }
        }
        // Start the port listener for s2s communication
        startServerListener(localIPAddress);
        // Start the port listener for Connections Multiplexers
        startConnectionManagerListener(localIPAddress);
        // Start the port listener for external components
        startComponentListener(localIPAddress);
        // Start the port listener for clients
        startClientListeners(localIPAddress);
        // Start the port listener for secured clients
        startClientSSLListeners(localIPAddress);
    }

    private void startServerListener(String localIPAddress) {
        // Start servers socket unless it's been disabled.
        if (isServerListenerEnabled()) {
            int port = getServerListenerPort();
            try {
                serverSocketThread = new SocketAcceptThread(this, 
                            new ServerPort(port, XMPPServer.getInstance().getServerNames(),
                                        localIPAddress, false, null, ServerPort.Type.server));
                ports.add(serverSocketThread.getServerPort());
                serverSocketThread.setDaemon(true);
                serverSocketThread.setPriority(Thread.MAX_PRIORITY);
                serverSocketThread.start();

                List<String> params = new ArrayList<String>();
                params.add(Integer.toString(serverSocketThread.getPort()));
                Log.info(LocaleUtils.getLocalizedString("startup.server", params));
            }
            catch (Exception e) {
                System.err.println("Error starting server listener on port " + port + ": " +
                        e.getMessage());
                Log.error(LocaleUtils.getLocalizedString("admin.error.socket-setup"), e);
            }
        }
    }

    private void stopServerListener() {
        if (serverSocketThread != null) {
            serverSocketThread.shutdown();
            ports.remove(serverSocketThread.getServerPort());
            serverSocketThread = null;
        }
    }

    private void startConnectionManagerListener(String localIPAddress) {
        // Start multiplexers socket unless it's been disabled.
        if (isConnectionManagerListenerEnabled()) {
            int port = getConnectionManagerListenerPort();
            try {
                multiplexerSocketThread = new SocketAcceptThread(this, new ServerPort(port,
                        XMPPServer.getInstance().getServerNames(), localIPAddress, false, null,
                        ServerPort.Type.connectionManager));
                ports.add(multiplexerSocketThread.getServerPort());
                multiplexerSocketThread.setDaemon(true);
                multiplexerSocketThread.setPriority(Thread.MAX_PRIORITY);
                multiplexerSocketThread.start();

                List<String> params = new ArrayList<String>();
                params.add(Integer.toString(multiplexerSocketThread.getPort()));
                Log.info(LocaleUtils.getLocalizedString("startup.multiplexer", params));
            }
            catch (Exception e) {
                System.err.println("Error starting multiplexer listener on port " + port + ": " +
                        e.getMessage());
                Log.error(LocaleUtils.getLocalizedString("admin.error.socket-setup"), e);
            }
        }
    }

    private void stopConnectionManagerListener() {
        if (multiplexerSocketThread != null) {
            multiplexerSocketThread.shutdown();
            ports.remove(multiplexerSocketThread.getServerPort());
            multiplexerSocketThread = null;
        }
    }

    private void startComponentListener(String localIPAddress) {
        // Start components socket unless it's been disabled.
        if (isComponentListenerEnabled()) {
            int port = getComponentListenerPort();
            try {
                componentSocketThread = new SocketAcceptThread(this, new ServerPort(port,
                            XMPPServer.getInstance().getServerNames(), localIPAddress, false, null, ServerPort.Type.component));
                ports.add(componentSocketThread.getServerPort());
                componentSocketThread.setDaemon(true);
                componentSocketThread.setPriority(Thread.MAX_PRIORITY);
                componentSocketThread.start();

                List<String> params = new ArrayList<String>();
                params.add(Integer.toString(componentSocketThread.getPort()));
                Log.info(LocaleUtils.getLocalizedString("startup.component", params));
            }
            catch (Exception e) {
                System.err.println("Error starting component listener on port " + port + ": " +
                        e.getMessage());
                Log.error(LocaleUtils.getLocalizedString("admin.error.socket-setup"), e);
            }
        }
    }

    private void stopComponentListener() {
        if (componentSocketThread != null) {
            componentSocketThread.shutdown();
            ports.remove(componentSocketThread.getServerPort());
            componentSocketThread = null;
        }
    }

    private void startClientListeners(String localIPAddress) {
        // Start clients plain socket unless it's been disabled.
        if (isClientListenerEnabled()) {
            int port = getClientListenerPort();

            try {
                socketThread = new SocketAcceptThread(this, new ServerPort(port, XMPPServer.getInstance().getServerNames(),
                        localIPAddress, false, null, ServerPort.Type.client));
                ports.add(socketThread.getServerPort());
                socketThread.setDaemon(true);
                socketThread.setPriority(Thread.MAX_PRIORITY);
                socketThread.start();

                List<String> params = new ArrayList<String>();
                params.add(Integer.toString(socketThread.getPort()));
                Log.info(LocaleUtils.getLocalizedString("startup.plain", params));
            }
            catch (Exception e) {
                System.err.println("Error starting XMPP listener on port " + port + ": " +
                        e.getMessage());
                Log.error(LocaleUtils.getLocalizedString("admin.error.socket-setup"), e);
            }
        }
    }

    private void stopClientListeners() {
        if (socketThread != null) {
            socketThread.shutdown();
            ports.remove(socketThread.getServerPort());
            socketThread = null;
        }
    }

    private void startClientSSLListeners(String localIPAddress) {
        // Start clients SSL unless it's been disabled.
        if (isClientSSLListenerEnabled()) {
            int port = getClientSSLListenerPort();
            String algorithm = JiveGlobals.getProperty("xmpp.socket.ssl.algorithm");
            if ("".equals(algorithm) || algorithm == null) {
                algorithm = "TLS";
            }
            try {
                sslSocketThread = new SSLSocketAcceptThread(this, new ServerPort(port, XMPPServer.getInstance().getServerNames(),
                        localIPAddress, true, algorithm, ServerPort.Type.client));
                ports.add(sslSocketThread.getServerPort());
                sslSocketThread.setDaemon(true);
                sslSocketThread.setPriority(Thread.MAX_PRIORITY);
                sslSocketThread.start();

                List<String> params = new ArrayList<String>();
                params.add(Integer.toString(sslSocketThread.getPort()));
                Log.info(LocaleUtils.getLocalizedString("startup.ssl", params));
            }
            catch (Exception e) {
                System.err.println("Error starting SSL XMPP listener on port " + port + ": " +
                        e.getMessage());
                Log.error(LocaleUtils.getLocalizedString("admin.error.ssl"), e);
            }
        }
    }

    private void stopClientSSLListeners() {
        if (sslSocketThread != null) {
            sslSocketThread.shutdown();
            ports.remove(sslSocketThread.getServerPort());
            sslSocketThread = null;
        }
    }

    public Iterator<ServerPort> getPorts() {
        return ports.iterator();
    }

    public SocketReader createSocketReader(Socket realSock, boolean isSecure, ServerPort serverPort,
            boolean useBlockingMode) throws IOException {
        
        FakeSocket.RealFakeSocket sock = FakeSocket.create(realSock);
        
        if (serverPort.isClientPort()) {
            SocketConnection conn = new StdSocketConnection(deliverer, sock, isSecure);
            return new ClientSocketReader(router, routingTable, sock, conn,
                    useBlockingMode);
        }
        else if (serverPort.isComponentPort()) {
            SocketConnection conn = new StdSocketConnection(deliverer, sock, isSecure);
            return new ComponentSocketReader(router, routingTable, sock, conn,
                    useBlockingMode);
        }
        else if (serverPort.isServerPort()) {
            SocketConnection conn = new StdSocketConnection(deliverer, sock, isSecure);
            return new ServerSocketReader(router, routingTable, sock, conn,
                    useBlockingMode);
        }
        else {
            // Use the appropriate packeet deliverer for connection managers. The packet
            // deliverer will be configured with the domain of the connection manager once
            // the connection manager has finished the handshake.
            SocketConnection conn = new StdSocketConnection(new MultiplexerPacketDeliverer(), sock, isSecure);
            return new ConnectionMultiplexerSocketReader(router, routingTable, sock,
                    conn, useBlockingMode);
        }
    }
    
    public SocketReader createSocketReader(FakeSocket.MinaFakeSocket sock, boolean isSecure, ServerPort serverPort,
                boolean useBlockingMode) throws IOException {
            if (serverPort.isClientPort()) {
                SocketConnection conn = new NioSocketConnection(deliverer, sock, isSecure);
                return new ClientSocketReader(router, routingTable, sock, conn,
                        useBlockingMode);
            }
            else if (serverPort.isComponentPort()) {
                SocketConnection conn = new NioSocketConnection(deliverer, sock, isSecure);
                return new ComponentSocketReader(router, routingTable, sock, conn,
                        useBlockingMode);
            }
            else if (serverPort.isServerPort()) {
                SocketConnection conn = new NioSocketConnection(deliverer, sock, isSecure);
                return new ServerSocketReader(router, routingTable, sock, conn,
                        useBlockingMode);
            }
            else {
                // Use the appropriate packeet deliverer for connection managers. The packet
                // deliverer will be configured with the domain of the connection manager once
                // the connection manager has finished the handshake.
                SocketConnection conn =
                        new NioSocketConnection(new MultiplexerPacketDeliverer(), sock, isSecure);
                return new ConnectionMultiplexerSocketReader(router, routingTable, sock,
                        conn, useBlockingMode);
            }
        }
    

    public void initialize(XMPPServer server) {
        super.initialize(server);
        router = server.getPacketRouter();
        routingTable = server.getRoutingTable();
        deliverer = server.getPacketDeliverer();
        sessionManager = server.getSessionManager();
    }

    public void enableClientListener(boolean enabled) {
        if (enabled == isClientListenerEnabled()) {
            // Ignore new setting
            return;
        }
        if (enabled) {
            JiveGlobals.setProperty("xmpp.socket.plain.active", "true");
            // Start the port listener for clients
            startClientListeners(localIPAddress);
        }
        else {
            JiveGlobals.setProperty("xmpp.socket.plain.active", "false");
            // Stop the port listener for clients
            stopClientListeners();
        }
    }

    public boolean isClientListenerEnabled() {
        return JiveGlobals.getBooleanProperty("xmpp.socket.plain.active", true);
    }

    public void enableClientSSLListener(boolean enabled) {
        if (enabled == isClientSSLListenerEnabled()) {
            // Ignore new setting
            return;
        }
        if (enabled) {
            JiveGlobals.setProperty("xmpp.socket.ssl.active", "true");
            // Start the port listener for secured clients
            startClientSSLListeners(localIPAddress);
        }
        else {
            JiveGlobals.setProperty("xmpp.socket.ssl.active", "false");
            // Stop the port listener for secured clients
            stopClientSSLListeners();
        }
    }

    public boolean isClientSSLListenerEnabled() {
        return JiveGlobals.getBooleanProperty("xmpp.socket.ssl.active", true);
    }

    public void enableComponentListener(boolean enabled) {
        if (enabled == isComponentListenerEnabled()) {
            // Ignore new setting
            return;
        }
        if (enabled) {
            JiveGlobals.setProperty("xmpp.component.socket.active", "true");
            // Start the port listener for external components
            startComponentListener(localIPAddress);
        }
        else {
            JiveGlobals.setProperty("xmpp.component.socket.active", "false");
            // Stop the port listener for external components
            stopComponentListener();
        }
    }

    public boolean isComponentListenerEnabled() {
        return JiveGlobals.getBooleanProperty("xmpp.component.socket.active", false);
    }

    public void enableServerListener(boolean enabled) {
        if (enabled == isServerListenerEnabled()) {
            // Ignore new setting
            return;
        }
        if (enabled) {
            JiveGlobals.setProperty("xmpp.server.socket.active", "true");
            // Start the port listener for s2s communication
            startServerListener(localIPAddress);
        }
        else {
            JiveGlobals.setProperty("xmpp.server.socket.active", "false");
            // Stop the port listener for s2s communication
            stopServerListener();
        }
    }

    public boolean isServerListenerEnabled() {
        return JiveGlobals.getBooleanProperty("xmpp.server.socket.active", true);
    }

    public void enableConnectionManagerListener(boolean enabled) {
        if (enabled == isConnectionManagerListenerEnabled()) {
            // Ignore new setting
            return;
        }
        if (enabled) {
            JiveGlobals.setProperty("xmpp.multiplex.socket.active", "true");
            // Start the port listener for s2s communication
            startConnectionManagerListener(localIPAddress);
        }
        else {
            JiveGlobals.setProperty("xmpp.multiplex.socket.active", "false");
            // Stop the port listener for s2s communication
            stopConnectionManagerListener();
        }
    }

    public boolean isConnectionManagerListenerEnabled() {
        return JiveGlobals.getBooleanProperty("xmpp.multiplex.socket.active", false);
    }

    public void setClientListenerPort(int port) {
        if (port == getClientListenerPort()) {
            // Ignore new setting
            return;
        }
        JiveGlobals.setProperty("xmpp.socket.plain.port", String.valueOf(port));
        // Stop the port listener for clients
        stopClientListeners();
        if (isClientListenerEnabled()) {
            // Start the port listener for clients
            startClientListeners(localIPAddress);
        }
    }

    public int getClientListenerPort() {
        return JiveGlobals.getIntProperty("xmpp.socket.plain.port",
                SocketAcceptThread.DEFAULT_PORT);
    }

    public void setClientSSLListenerPort(int port) {
        if (port == getClientSSLListenerPort()) {
            // Ignore new setting
            return;
        }
        JiveGlobals.setProperty("xmpp.socket.ssl.port", String.valueOf(port));
        // Stop the port listener for secured clients
        stopClientSSLListeners();
        if (isClientSSLListenerEnabled()) {
            // Start the port listener for secured clients
            startClientSSLListeners(localIPAddress);
        }
    }

    public int getClientSSLListenerPort() {
        return JiveGlobals.getIntProperty("xmpp.socket.ssl.port",
                SSLSocketAcceptThread.DEFAULT_PORT);
    }

    public void setComponentListenerPort(int port) {
        if (port == getComponentListenerPort()) {
            // Ignore new setting
            return;
        }
        JiveGlobals.setProperty("xmpp.component.socket.port", String.valueOf(port));
        // Stop the port listener for external components
        stopComponentListener();
        if (isComponentListenerEnabled()) {
            // Start the port listener for external components
            startComponentListener(localIPAddress);
        }
    }

    public int getComponentListenerPort() {
        return JiveGlobals.getIntProperty("xmpp.component.socket.port",
                SocketAcceptThread.DEFAULT_COMPONENT_PORT);
    }

    public void setServerListenerPort(int port) {
        if (port == getServerListenerPort()) {
            // Ignore new setting
            return;
        }
        JiveGlobals.setProperty("xmpp.server.socket.port", String.valueOf(port));
        // Stop the port listener for s2s communication
        stopServerListener();
        if (isServerListenerEnabled()) {
            // Start the port listener for s2s communication
            startServerListener(localIPAddress);
        }
    }

    public int getServerListenerPort() {
        return JiveGlobals.getIntProperty("xmpp.server.socket.port",
                SocketAcceptThread.DEFAULT_SERVER_PORT);
    }

    public void setConnectionManagerListenerPort(int port) {
        if (port == getConnectionManagerListenerPort()) {
            // Ignore new setting
            return;
        }
        JiveGlobals.setProperty("xmpp.multiplex.socket.port", String.valueOf(port));
        // Stop the port listener for connection managers
        stopConnectionManagerListener();
        if (isConnectionManagerListenerEnabled()) {
            // Start the port listener for connection managers
            startConnectionManagerListener(localIPAddress);
        }
    }

    public int getConnectionManagerListenerPort() {
        return JiveGlobals.getIntProperty("xmpp.multiplex.socket.port",
                SocketAcceptThread.DEFAULT_MULTIPLEX_PORT);
    }

    // #####################################################################
    // Module management
    // #####################################################################

    public void start() {
        super.start();
        isStarted = true;
        createSocket();
        SocketSendingTracker.getInstance().start();
    }

    public void stop() {
        super.stop();
        stopClientListeners();
        stopClientSSLListeners();
        stopComponentListener();
        stopConnectionManagerListener();
        stopServerListener();
        SocketSendingTracker.getInstance().shutdown();
    }
}
