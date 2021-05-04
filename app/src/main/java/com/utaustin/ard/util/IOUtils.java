package com.utaustin.ard.util;

import android.util.Log;

import com.utaustin.ard.constants.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
    private static final String DEBUG = Constants.DEBUG_PERMISSIONS;
    private static final int BUFFER_SIZE = 1024 * 2;

    public static int copy(InputStream input, OutputStream output) throws IOException {
        Log.d(DEBUG, "Copying to output stream (" + BUFFER_SIZE + "B buffer)");
        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int bytesCopied = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                bytesCopied += n;
            }
            out.flush();
        } finally {
            try {
                out.close();
                Log.d(DEBUG, "Successfully closed output stream");
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
                Log.d(DEBUG, "Successfully closed input stream");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(DEBUG, "Successfully copied " + bytesCopied + "B to output stream");
        return bytesCopied;
    }
}
