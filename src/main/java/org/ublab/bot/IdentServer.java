/**
 *  This file is part of the ublab-bot
 *  Copyright (C) 2015  Black Hackers(Elite Programmers Club, University of Buea)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
package org.ublab.bot;

import java.net.*;
import java.io.*;

/**
 * A simple IdentServer (also know as "The Identification Protocol").
 * An ident server provides a means to determine the identity of a
 * user of a particular TCP connection.
 *  <p>
 * Most IRC servers attempt to contact the ident server on connecting
 * hosts in order to determine the user's identity.  A few IRC servers
 * will not allow you to connect unless this information is provided.
 *  <p>
 * So when a UblabBot is run on a machine that does not run an ident server,
 * it may be necessary to provide a "faked" response by starting up its
 * own ident server and sending out apparently correct responses.
 *  <p>
 * An instance of this class can be used to start up an ident server
 * only if it is possible to do so.  Reasons for not being able to do
 * so are if there is already an ident server running on port 113, or
 * if you are running as an unprivileged user who is unable to create
 * a server socket on that port number.
 *
 */
public class IdentServer extends Thread {
    
    /**
     * Constructs and starts an instance of an IdentServer that will
     * respond to a client with the provided login.  Rather than calling
     * this constructor explicitly from your code, it is recommended that
     * you use the startIdentServer method in the UblabBot class.
     *  <p>
     * The ident server will wait for up to 60 seconds before shutting
     * down.  Otherwise, it will shut down as soon as it has responded
     * to an ident request.
     *
     * @param bot The UblabBot instance that will be used to log to.
     * @param login The login that the ident server will respond with.
     */
    IdentServer(UblabBot bot, String login) {
        _bot = bot;
        _login = login;

        try {
            _ss = new ServerSocket(113);
            _ss.setSoTimeout(60000);
        }
        catch (Exception e) {
            _bot.log("*** Could not start the ident server on port 113.");
            return;
        }
        
        _bot.log("*** Ident server running on port 113 for the next 60 seconds...");
        this.setName(this.getClass() + "-Thread");
        this.start();
    }
    
    
    /**
     * Waits for a client to connect to the ident server before making an
     * appropriate response.  Note that this method is started by the class
     * constructor.
     */
    public void run() {
        try {
            Socket socket = _ss.accept();
            socket.setSoTimeout(60000);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            String line = reader.readLine();
            if (line != null) {
                _bot.log("*** Ident request received: " + line);
                line = line + " : USERID : UNIX : " + _login;
                writer.write(line + "\r\n");
                writer.flush();
                _bot.log("*** Ident reply sent: " + line);
                writer.close();
            }
        }
        catch (Exception e) {
            // We're not really concerned with what went wrong, are we?
        }
        
        try {
            _ss.close();
        }
        catch (Exception e) {
            // Doesn't really matter...
        }
        
        _bot.log("*** The Ident server has been shut down.");
    }
    
    private UblabBot _bot;
    private String _login;
    private ServerSocket _ss = null;
    
}
