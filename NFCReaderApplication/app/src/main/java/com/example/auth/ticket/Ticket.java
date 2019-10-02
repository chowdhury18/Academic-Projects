package com.example.auth.ticket;

import android.app.Activity;
import android.content.ContextWrapper;
import android.util.Log;
import android.widget.Toast;

import com.example.auth.app.ulctools.Commands;
import com.example.auth.app.ulctools.Utilities;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.sql.Time;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * TODO: Complete the implementation of this class. Most of the code are already implemented. You
 * will need to change the keys, design and implement functions to issue and validate tickets.
 */
public class Ticket {

   private static byte[] defaultAuthenticationKey = "BREAKMEIFYOUCAN!".getBytes();// 16-byte key

    /** TODO: Change these according to your design. Diversify the keys. */
    private static byte[] authenticationKey = defaultAuthenticationKey;// 16-byte key
    private static byte[] hmacKey = "0123456789ABCDEF".getBytes(); // min 16-byte key

    public static byte[] data = new byte[192];

    private static TicketMac macAlgorithm; // For computing HMAC over ticket data, as needed
    private static Utilities utils;
    private static Commands ul;

    private Boolean isValid = false;
    private int remainingUses = 0;
    private int remainRides = 0;
    private int expiryTime = 0;
    private byte[] resetKey;

    private static String infoToShow; // Use this to show messages in Normal Mode

    /** Create a new ticket */
    public Ticket() throws GeneralSecurityException {
        // Set HMAC key for the ticket
        macAlgorithm = new TicketMac();
        macAlgorithm.setKey(hmacKey);

        ul = new Commands();
        utils = new Utilities(ul);
        resetKey = genResetKey();
    }

    /** After validation, get ticket status: was it valid or not? */
    public boolean isValid() {
        return isValid;
    }

    /** After validation, get the number of remaining uses */
    public int getRemainingUses() {
        return remainingUses;
    }

    /** After validation, get the expiry time */
    public int getExpiryTime() {
        return expiryTime;
    }

    /** After validation/issuing, get information */
    public static String getInfoToShow() {
        String tmp = infoToShow;
        infoToShow = "";
        return tmp;
    }
    /**
     * Issue new tickets
     *
     * TODO: IMPLEMENT
     */
    public boolean issue(int daysValid, int uses) throws GeneralSecurityException {
        boolean defaultKey;
        // Authenticate
        //defaultKey = utils.authenticate(resetKey);

        defaultKey = utils.authenticate(authenticationKey);
        if (defaultKey) {
            Log.d("TAG:", " ok");
            firstTimeFormat();
            //    issueKey();
            //authenNewKey();
            infoToShow = "First time format this card";
            return true;
        }


        if (!authenNewKey()){
           infoToShow = "Authentication Failed";
           return false;
        }

        byte[] mac = calculateMac();
        if (!checkMAC(mac)) {
            format();
            infoToShow = "Data is modified, this card is being resetting now";
            return true;
        }



        byte[] expiryDateFromCard = getDate();
        //Activate section
//        if (compareArray(expiryDateFromCard, AUTH1)){
//
        int remainUses = readRemainUses();
//        }

        if(checkExpiradateEqualZero(expiryDateFromCard)){
           extendNoR();
           infoToShow = "You haven't use the card for the first time. Add 5 more time to play ";
           return true;
        }
        if (!checkSafeLimits(expiryDateFromCard, remainingUses)){
            infoToShow="We detect the suspicious activities in your card. We will detain your card.";

            return false;
        }
        if (checkExpiryDate(expiryDateFromCard)) { //if expired
            //resetDateNoR();
            format();
            infoToShow = "The expiry date is over, this card is being resetting now";
            return true;
        }
        else {
           extendNoR();
            int counter = readCounter();
            int remainRides = readRemainUses();
            int total = remainRides - counter ;
            infoToShow = "5 rides added\nYou have " + total + " rides in total";

            //return true;
        }
        return true;
    }
    private boolean checkSafeLimits(byte[] expiryDate, int remainingUses){
      Date date = new Date();
      Calendar cal = Calendar.getInstance();

      cal.setTime(date);
      int year = (int)expiryDate[0] + 2000;
      int month = (int)expiryDate[1] - 1;
      int day = (int)expiryDate[2];
      Calendar nowCal = Calendar.getInstance();
      nowCal.clear();
      nowCal.set(Calendar.YEAR, year);
      nowCal.set(Calendar.MONTH, month);
      nowCal.set(Calendar.DAY_OF_MONTH, day);
      Date expiry = nowCal.getTime();
      long diff = expiry.getTime()  - date.getTime();
      long d = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
      if (d > 2)
          return false;

      int counter = readCounter();
      if (remainingUses - counter > 50){
          return false;
      }
      return true;
      //true is safe




    }

    private boolean checkExpiradateEqualZero(byte[] expiryData){
        byte[] zero = {(byte)0,(byte)0,(byte)0,(byte)0 };
        return compareArray(expiryData,zero);
    }
    private void firstTimeFormat(){
        issueKey();
        authenNewKey();

        // Example of writing:
        issueTagVersion();
        resetDateNoR();
        remainingUses = issueNoR();
        issueMAC();
        issueAUTH();
    }
    private void format(){
        //issueKey();
        //authenNewKey();

        // Example of writing:
        issueTagVersion();
        resetDateNoR();
        remainingUses = issueNoR();
        issueMAC();
        issueAUTH();
    }

    private void issueTagVersion(){
        utils.writePages(TAG,0,  4, 1);
        utils.writePages(VERSION, 0, 5, 1);
    }
    private int issueNoR(){
        int counter = readCounter();
        Log.d("TAG:", "Number of Ride: " + new Integer(counter).toString());
        counter = counter + 5;

        byte[] noR = intToByte(counter);
        utils.writePages(noR, 0, 7, 1);
        return counter;
    }
    private int extendNoR(){
        remainingUses = readRemainUses();
        remainingUses = remainingUses + 5;

        byte[] noR = intToByte(remainingUses);
        utils.writePages(noR, 0, 7, 1);
        issueMAC();
        return remainingUses;

    }
    private void issueMAC(){
        byte[] mac = calculateMac();
        boolean resWrite = utils.writePages(mac, 0, 8,1);
        if(resWrite){
            Log.d("TAG:", "WRITE MAC OK");
        }
    }
    private void issueAUTH(){
        utils.writePages(AUTH0, 0, 42, 1);
        utils.writePages(AUTH1, 0, 43, 1);
    }


    private void issueKey(){
        byte[] key = createKey(DEFAULT_KEY);

       boolean res = utils.writePages(key, 0, 44, 4);


        if (res){
            infoToShow = "Wrote: " + new String("KEY OK");
        } else {
            Log.d("TAG:", "Cannot write new key");
            infoToShow = "Failed to write key";
        }
    }
    /**
     *  TODO: create authenticate key
     */
    private byte[] createKey(boolean defaultKey){
       if (defaultKey)
           return defaultAuthenticationKey;
           //return resetKey;
       byte[] uid = getUID();
       byte[] uidSecretMessage = addAll(uid, SECRET_MESSAGE.getBytes());
       //TODO: test addAll function
       byte[]  fullKey = getHash(uidSecretMessage);
       return  truncated(fullKey);

    }

    private byte[] getUID(){
        byte[] uid = new byte[12];
        utils.readPages(0, 3, uid, 0);
        uid = Arrays.copyOf(uid, 9);
        return uid;
    }

    private byte[] getHash(byte[] origin){
        byte[] res = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            res = digest.digest(origin);

        }
        catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }
    private boolean authenNewKey(){
        byte[] key = createKey(DEFAULT_KEY);
        boolean res = utils.authenticate(key) ;
        if (res){
            infoToShow = "Wrote: " + new String("AUTHENTICATE OK");

            Log.d("TAG:", "AUTHEN OK");
            return true;
        } else {
            infoToShow = "Failed to authenticate";
            return false;
        }
    }

    /**
     * Use ticket once
     *
     * TODO: IMPLEMENT
     */
    public boolean use() throws GeneralSecurityException {
        boolean defaultKey;

        // Authenticate
        defaultKey = utils.authenticate(authenticationKey);
        if (defaultKey) {
            infoToShow = "Please issue this card before using it!";
            return false;
        }
        boolean authen = authenNewKey();
        if (!authen) {
                infoToShow = "Authentication failed!";
                return false;
        }
        // calculate the MAC
        byte[] mac = calculateMac();
        boolean macOK = checkMAC(mac);
        if (!macOK) {
            infoToShow = "The data is modified";
            return false;
        }

        remainingUses = readRemainUses();
        Log.d("TAG:", "remainUses:  " + new Integer(remainingUses).toString());

        //check expiry date
        //TODO: update the counter
         byte[] expiryDateFromCard = getDate();



        if (!checkExpiradateEqualZero(expiryDateFromCard) && !checkSafeLimits(expiryDateFromCard, remainingUses)){
            infoToShow="There is some issues with your card. Contact the ticket vendor. ";
            return false;
        }
         boolean firstTime = checkFirstTime(expiryDateFromCard);
        if (firstTime) {
            boolean validNor = updateCounter();
            if (!validNor){
                return false;
            }
                loadExpiryTimeToCard();
                //Update the MAC and then write it on card
           infoToShow = " you have " + remainRides + " rides left";
           Log.d("TAG:", "Card activated");
        }
        else{
            boolean expire = checkExpiryDate(expiryDateFromCard);
            if (expire) {

                //resetDateNoR();
                infoToShow = "Card expire. ";
                Log.d("TAG:", "Card Expire");
                return false;
            }
            else {
                //infoToShow = "Card expired. It will be reset";
                boolean validNor = updateCounter();
                if (!validNor){
                    return false;
                }
                infoToShow = "Enjoy the ride. \nYou have " + remainRides + " rides left";
                Log.d("TAG:", "Expiry date is not over");

            }
            //reset time to 00000
        }

//        byte[] bitCounter = {(byte)0, (byte)1, (byte)0, (byte)0};
//        utils.writePages(bitCounter,0, 41, 1 );


        // Example of reading:
//        byte[] message = new byte[4];
//        res = utils.readPages(6, 1, message, 0);
//
//        // Set information to show for the user
//        if (res) {
//            infoToShow = "Read: " + new String(message);
//        } else {
//            infoToShow = "Failed to read";

        return true;
    }

    private int readRemainUses(){
        byte[] res = new byte[4];
        utils.readPages(7, 1, res, 0);
        return bytesToInt(res);
    }

    private byte[] getDate(){
        byte[] date = new byte[4];
        utils.readPages(6,1,date,0);
        return date;
    }
    private boolean checkFirstTime(byte[] date){
        byte[] zero = {(byte)0,(byte)0,(byte)0,(byte)0};
        return compareArray(date, zero);
    }
    private void loadExpiryTimeToCard(){
        Date date = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH,1);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        byte[] dateByte = {(byte)(year - 2000), (byte)(month + 1), (byte)day, (byte)0};
        utils.writePages(dateByte, 0, 6, 1 );
        issueMAC();


    }
    private  boolean checkExpiryDate(byte[] dateInCard){
        Date date = new Date(); // your date
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        byte[] now = {(byte)(year - 2000), (byte)(month + 1), (byte)day, (byte)0};
        return !compareArraySmallerOrEqual(now, dateInCard); //true if expired

    }
    private void resetDateNoR(){
        byte[] zero = {(byte)0,(byte)0,(byte)0,(byte)0};
        utils.writePages(zero, 0, 6, 1);
        utils.writePages(zero, 0, 7, 1);
        Log.d("TAG:", "Reset date is called");
        issueMAC();

    }


    private boolean checkMAC(byte[] mac) {
        byte[] macInCard = new byte[4];
        utils.readPages(8,1,macInCard, 0);
        return compareArray(mac, macInCard);
    }

    private boolean compareArray(byte[] a1, byte[] a2) {
        return Arrays.equals(a1, a2);
    }
    private  boolean compareArraySmallerOrEqual(byte[] a1, byte[] a2){
        try {
            for (int i = 0; i < a1.length; i++){
                if (a1[i] > a2[i])
                    return false;
            }
        }
        catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }
            return true;
    }
    private byte[] addAll(byte[] a1, byte[] a2){
        byte[] res = new byte[a1.length + a2.length];
        for(int i = 0; i < res.length;i++){
            if (i < a1.length)
                res[i] = a1[i];
            else
                res[i] = a2[i-a1.length];
        }
        return res;
    }
    private  boolean updateCounter(){
        int  counter = readCounter();
        remainRides = remainingUses - counter;
        if (remainRides <= 0){
           infoToShow = "Card is out of Rides" ;
           Log.d("TAG:", "Card is out of Rides");
           return false;
        }
        add1ToCounter();
        remainRides = remainRides - 1;
        return true;

    }
    private int readCounter(){
        byte[] counterByte = new byte[4];
        utils.readPages(41, 1, counterByte, 0);
        Log.d(Tag, "16-bit counterbyte: " + new Byte(counterByte[0]).toString() + new Byte(counterByte[1]).toString());
        int counter = bytesToInt(counterByte);
        Log.d(Tag, "16 bit counter value: " + new Integer(counter));
        return counter;
    }

    private void add1ToCounter(){
        byte[] add = {(byte)1, (byte)0, (byte)0, (byte)0};
        utils.writePages(add, 0, 41, 1);
    }
    private void writeCounter(int counter){
        byte[] counterByte = intToByte(counter);
        Log.d("TAG:", "byte 16 BITS COUNTER: " + new Byte(counterByte[0]).toString() + new Byte(counterByte[1]));
        counterByte[1] = 0;
        counterByte[0] = 1;
        byte[] tmp = new byte[2];
        tmp[0] = 1; tmp[1] = 0;
        utils.writePages(counterByte, 0, 41, 1);
    }
    private byte[] intToByte(int counter){
        //byte[] counterByte = {(byte)((counter >> 8) & 0xff), (byte)(counter & 0xff)};
        byte[] counterByte = {(byte)(counter  & 0xff), (byte)((counter >> 8 ) & 0xff)};
        byte[] zero = {(byte)0, (byte)0};
        counterByte = addAll(counterByte,zero);
        return counterByte;

    }

    private int bytesToInt(byte[] a){
       //return ((int)a[0] << 8)  + (int)a[1];
       return ((int)a[1] << 8)  + (int)a[0];
    }

    private byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
    public long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();//need flip
        return buffer.getLong();
    }


    private byte[] genResetKey(){
        byte[] res = new byte[16];
        for (int i = 0; i < 16; i++){
           res[i] = (byte)0;
        }
        return res;
    }
    private byte[] truncated(byte[] data){
        byte[] res = Arrays.copyOf(data, data.length / 2);
        return res;
    }
    private  byte[] calculateMac(){
        byte[] data = new byte[16];
        boolean resRead = utils.readPages(4, 4, data, 0);
        if(resRead){
            Log.d("TAG:", "readMac " + new Integer(data.length).toString());
        }
        byte[] mac = macAlgorithm.generateMac(data);
        Log.d("TAG:", "data length: " + new Integer(mac.length));
        byte[] trunctedMac = truncated(truncated(truncated(mac)));
        Log.d("TAG:" ,new Integer(mac.length).toString());
        return trunctedMac;

    }
    //private final byte[] AUTHENTICATE_KEY = ""
    private  final byte[] TAG = "ATAG".getBytes();
    private final byte[] VERSION = {(byte)223, (byte)0, (byte)0, (byte)0};
    private  final byte[] AUTH0 = {(byte)4, (byte)0,(byte)0,(byte)0 };
    private  final byte[] AUTH1 = {(byte)1, (byte)0,(byte)0,(byte)0 };
    private final String SECRET_MESSAGE = "CRACKMEPLEASE";
    private final boolean DEFAULT_KEY = false;
    private final String Tag = "TAG:";

    public static void setInfoToShow(String infoToShow) {
        Ticket.infoToShow = infoToShow;
    }

}