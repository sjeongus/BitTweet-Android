package org.bittweet.android;

import java.util.List;
import java.util.Random;

import static java.util.Arrays.asList;

/**
 * Created by jrgmadrid on 2014-04-01.
 */
public class GoHome {

    static List<String> tweets = asList(
            "member of I Tweet Things By Pressing Links",
            "CEO of Bitmop. We are Japanese company.",
            "but you may know me better as twitter user at-sign cooltouhou",
            "and I'd like to talk to you about Notedrop. What IS Notedrop? Nobody really knows.",
            "twitter celebrity who changes usernames once a week",
            "the mastermind behind bitmapcoin.",
            "yuyushiki irl",
            "founder of HooYa! The most popular search engine in Japan!",
            "and I disagree. your favorite same is SHIT",
            "and my username means 'Bread Person'",
            "and this is the part where I run away screaming NOT MY TEAM",
            "and I did not write this tweet, but I will take credit for it.",
            "let me tell you about the beauty of Kill Me Baby",
            "and I'D IRONICALLY WEAR AN I <3 JPEG SHIRT",
            "and I ask corporate twitter accounts to send me free things",
            "and I suck at thinking of things to tweet.",
            "and this is a note for myself - never play mahjong for money",
            "and this sax bgm too good",
            "and RIP Bitmap, He Never Scored. Died of a mahjong-induced heart attack.",
            "dammit moshi go to bed",
            "Parse error - syntax error, unexpected 'pan_ningen', expecting 'bitmap' in /home/html/warton/benson.php on line 71",
            "and I'm currently rate limited right now. Please leave a message after the beep.",
            "and Warning - This expression has type int but is here used with type int",
            "and I'm testing this code in production what is the worst that can happ Parse error - syntax error",
            "and why am I making the same tired joke every single time",
            "and I am the reason why we cannot have nice things",
            "and I think sonic the hedgehog is cool",
            "internet celebrity. you can relive all of the good bitmap memories here bittweet.org",
            "but you may know me better as twitter user at-sign bitmap",
            "and I am the author of the light novel All of My Internet Friends are More Famous than I Am",
            "iOS/Android developer. please download my bitmapp.",
            "and I thought I should put that out there",
            "I HAVE OPINIONS ON FROYO",
            "twitter user at-sign BIG_THUMPIN_BOOTYS",
            "mediocre digital artist. would you like to learn more about jesus?",
            "you may remember me from tv shows such as 'everybody loves bitmap.' today i'm here to talk to you about kirby.",
            "and follow me for more great tweets like these.",
            "professional bee gourmand. the tastiest part of the bee are the fuzzy stripes",
            "but you may know me better for my loud opinions as the Angry Otaku. Baby, please kill me.",
            "and spam is a part of my cultural culinary history -shoves entire tin of tabasco spam into mouth-",
            "and I tweet about tweeting while listening to a song about tweeting",
            "and so long as the tsun rises, I will always be dere for you. GOOD MORNING DESS");
    static Random rng = new Random();
    //AssetManager am = getAssets();

    public List<String> getTweets() {
        return this.tweets;
    }

    public static String getStatus(String username) {
        int n = tweets.size();
        int randIndex =  rng.nextInt(n);
        return "Hi, it's me, @" + username + ", " + tweets.get(randIndex);
    }


}
