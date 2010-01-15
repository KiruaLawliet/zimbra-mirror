/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2005, 2006, 2007, 2009 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.2 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.common.util;

import java.security.SecureRandom;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.GnuParser;

public class RandomPassword {
    
    /**
     * 64 entry alphabet gives a 6 bits of entropy per character in the
     * password.
     *
     * http://world.std.com/~reinhold/dicewarefaq.html#calculatingentropy
     *
     * If the passphrase is made out of M symbols, each chosen at
     * random from a universe of N possibilities, each equally likely,
     * the entropy is M*log2(N).
     *
     */
    private static final String ALPHABET = 
        "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_.";
    
    /*
     * RandomPassword is also used to generate local part of email.  
     * Postfix has a limitation that it does not allow two dots in local part.
     * In the installer often local part generated by RandomPassword is concatenated 
     * with a prefix, e.g. "spam.", and if the generated string has a leading dot then 
     * the resulting local part will have two dots.  To get around it, we use a 
     * alphabet set that does not contain . when generating local part.
     */
    private static final String ALPHABET_NO_DOT = 
        "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
    
    private static final int DEFAULT_MIN_LENGTH = 24;
    
    private static final int DEFAULT_MAX_LENGTH = 32;
    
    /** 
     * http://world.std.com/~reinhold/passgen.html
     *
     * When using an 8-bit value to select a character from an
     * alphabet of length k, there is a risk of bias if k does not
     * evenly divide 256. To eliminate this, candidate cipher output
     * bytes are discarded if they are greater than or equal to the
     * largest multiple of k less than 256.
     */
    private static int byteLimit(int alphabetLength) {
        if (alphabetLength > 256) {
            // ie, some of the alphabet will never show up!
            throw new IllegalStateException
                ("alphabet length " + alphabetLength + " has risk of bias");
        }
        return 256 - (256 % alphabetLength);
    }
    
    /**
     * Generate a random password of random length.
     */
    private static String generate(int minLength, int maxLength, boolean localpart) {
        SecureRandom random = new SecureRandom();

        // Calculate the desired length of the password
        int length;
        if (minLength > maxLength) {
            throw new IllegalArgumentException("minLength=" + minLength + 
                                               " > maxLength=" + maxLength);
        } else if (minLength < maxLength) {
            length = minLength + random.nextInt(1 + maxLength - minLength);
        } else {
            length = maxLength;
        }
        
        String alphabet = localpart?ALPHABET_NO_DOT:ALPHABET;
        
        int alphabetLength = alphabet.length();
        int limit = byteLimit(alphabetLength);
        
        StringBuffer password = new StringBuffer(length);
        byte[] randomByte = new byte[1];
        
        while (password.length() < length) {
            random.nextBytes(randomByte);
            int i = randomByte[0] + 128;
            if (i < limit) {
                password.append(alphabet.charAt(i % alphabetLength));
            }
        }
        
        return password.toString();            
    }
    
    public static String generate() {
        return generate(DEFAULT_MIN_LENGTH, DEFAULT_MAX_LENGTH, false);
    }

    private static void usage() {
        System.out.println("");
        System.out.println("RandomPassword [-l] <minLength> <maxLength>");
        System.exit(1);
    }
    
    public static void main(String args[]) {
        CommandLineParser parser = new GnuParser();
        Options options = new Options();
        options.addOption("l", "localpart", false, "genarated string does not contain dot(.)");
        
        CommandLine cl = null;
        boolean err = false;
        
        try {
            cl = parser.parse(options, args, true);
        } catch (ParseException pe) {
            System.err.println("error: " + pe.getMessage());
            err = true;
        }
            
        if (err || cl.hasOption('h')) {
            usage();
        }
        
        boolean localpart = false;
        int minLength = DEFAULT_MIN_LENGTH;
        int maxLength = DEFAULT_MAX_LENGTH;
        
        if (cl.hasOption('l'))
            localpart = true;
        
        args = cl.getArgs();

        if (args.length != 0) {
            if (args.length != 2) {
                usage();
            }
            try {
                minLength = Integer.valueOf(args[0]).intValue();
                maxLength = Integer.valueOf(args[1]).intValue();
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace();
            }
        }
        
        System.out.println(generate(minLength, maxLength, localpart));
    }
}
