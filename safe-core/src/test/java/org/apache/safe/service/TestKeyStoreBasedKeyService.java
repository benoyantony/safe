package org.apache.safe.service;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.SecretKeyEntry;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.safe.ConfigKeys;
import org.apache.safe.util.ReflectionUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestKeyStoreBasedKeyService {

  private static final int NUM_OF_KEYS = 10;
  private static File ksFile;
  private static KeyStore ks;
  private static File pwdFile;
  private static KeyService keyService;

  @BeforeClass
  public static void setup(){
    try {
      createKeyStore();
      createPasswordFile();
      Configuration conf = new Configuration();
      conf.set(ConfigKeys.KEYSTORE_FILE, ksFile.getAbsolutePath());
      conf.set(ConfigKeys.KEYSTORE_PASSWORD_FILE, pwdFile.getAbsolutePath());
      keyService = ReflectionUtils.newInstance(KeyStoreBasedKeyService.class);
      keyService.init(conf);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @AfterClass 
  public static void tearDown(){
    new File ("test.jceks").delete();
    new File ("password.properties").delete();
  }

  private static void createKeyStore () throws IOException{
    ksFile = new File ("test.jceks");
    FileInputStream fs = null;
    try {
      char[] keyStorePassword = "kspwd".toCharArray();

      ks = KeyStore.getInstance("JCEKS");

      ks.load(fs, keyStorePassword);
      for(int i=0; i<NUM_OF_KEYS; i++) {
        char[] password = ("password"+i).toCharArray();

        String key = "alias"+i;
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
        SecretKey generatedSecret =
          factory.generateSecret(new PBEKeySpec(    
              password));

        PasswordProtection keyStorePP = new PasswordProtection(keyStorePassword);


        ks.setEntry(key, new SecretKeyEntry(
            generatedSecret), keyStorePP);
      }

      FileOutputStream fos = new FileOutputStream(ksFile);

      ks.store(fos, 
          keyStorePassword);
    }
    catch (KeyStoreException e) {
      e.printStackTrace();
      throw new IOException(e);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      throw new IOException(e);
    } catch (CertificateException e) {
      e.printStackTrace();
      throw new IOException(e);
    } catch (IOException e) {
      e.printStackTrace();
      throw new IOException(e);
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
      throw new IOException(e);
    }
  }

  public static void createPasswordFile () throws FileNotFoundException, IOException{
    pwdFile = new File("password.properties");
    Properties props = new Properties();
    props.put("keystore.password", "kspwd");
    props.store(new FileOutputStream(pwdFile), "blah"); 
  }
  
  @Test
  public void testSecretKey(){
    for(int i=0; i<NUM_OF_KEYS; i++) {
      String key = "alias"+i;
      try {
        byte[] secret = keyService.getKey(key);
        assertEquals (new String(secret), "password"+i);
      } catch (IOException e) {
        e.printStackTrace();
        fail();
      }
    }
    
  }
}
