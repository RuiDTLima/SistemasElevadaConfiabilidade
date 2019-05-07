
package pt.ist.sec.g27.hds_client.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.lang.Math;

// Proof of work
public class Pow {

    private final String SHA256 = "SHA-256";
    private final int BYTE_SIZE = 8; // 8 BIT
    private byte[] message;
    private int number_bit;

    Pow(String message, int number_bit) throws Exception {
        if( number_bit > 256 ) { throw new Exception("Number of bits must be at max 256 bit"); }
        this.message = message.getBytes(StandardCharsets.UTF_8);
        this.number_bit = number_bit;
    }

    Pow(byte[] message, int number_bit) throws Exception {
        if( number_bit > 256 ) { throw new Exception("Number of bits must be at max 256 bit"); }
        this.message = message;
        this.number_bit = number_bit;
    }

    public BigInteger compute(){

        BigInteger i= BigInteger.ZERO;

        while(true)
        {
            try
            {
                boolean satisfies = verify(i);
                if(satisfies){return i;}
                i = i.add(BigInteger.ONE);
            }catch(NoSuchAlgorithmException e)
            {
                System.out.println("Erro: NoSuchAlgorithmException " + e.getMessage());
            }
        }

    }

    private boolean verify(BigInteger i) throws NoSuchAlgorithmException
    {

        MessageDigest digest = MessageDigest.getInstance(SHA256);
        byte[] concat_message_i = concat(message,i.toByteArray());
        byte[] encodedhash = digest.digest(concat_message_i);
        int number_of_byte = (int) Math.ceil(number_bit/8.0);


        for(int j=0 ; j<number_of_byte; j++)
        {
            for(int t=0 ; t<BYTE_SIZE ; t ++)
            {
                int bit = (encodedhash[j] >> t) & 1;
                if( j*BYTE_SIZE + t >= number_bit ){
                    break;
                }
                if(bit == 1){
                    return false;
                }
            }
        }

        return true;

    }

    private byte[] concat(byte[] a, byte[] b)
    {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try
        {
            outputStream.write(a);
            outputStream.write(b);
            byte c[] = outputStream.toByteArray();
            return c;
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }

        return null;

    }

}
