package com.golden.owaranai;

import java.util.ArrayList;
import java.util.Random;
import java.io.*;

/**
 * Created by jrgmadrid on 2014-04-01.
 */
public class GoHome {

    ArrayList<String> tweets;
    FileReader file = null;
    Random rng;

    public GoHome() throws RuntimeException {
        rng = new Random();
        tweets = new ArrayList<String>();
        try {
            file = new FileReader("HI ITS ME");
            BufferedReader reader = new BufferedReader(file);
            String line = "";
            while ((line = reader.readLine()) != null) {
                tweets.add(line);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Thanks, Benson.");
        }
        finally {
            if (file != null) {
                try {
                    file.close();
                }
                catch (IOException e) {

                }
            }
        }
    }

    public String getStatus(String username) {
        int n = tweets.size();
        int randIndex =  rng.nextInt(n);
        return "Hi, it's me, @" + username + ", " + tweets.get(randIndex);
    }
}