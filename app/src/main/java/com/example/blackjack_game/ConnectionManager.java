package com.example.blackjack_game;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionManager {
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;

    public static void setSocket(Socket s) {
        socket = s;
    }

    public static Socket getSocket() {
        return socket;
    }

    public static void setOut(PrintWriter o) {
        out = o;
    }

    public static PrintWriter getOut() {
        return out;
    }

    public static void setIn(BufferedReader i) {
        in = i;
    }

    public static BufferedReader getIn() {
        return in;
}

}
