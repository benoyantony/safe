package org.apache.safe.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.safe.ConfigKeys;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestSafeService {

  private static File ksFile;
  private static KeyStore ks;
  private static File pwdFile;
  private static File aclFile;
  private static SafeService safeService;

  @BeforeClass
  public static void setup(){
    try {
      aclFile = new File("acls.xml");
      FileOutputStream os = new FileOutputStream(aclFile);
      String acl = "<?xml version=\"1.0\"?><acls><acl><key>alias</key>" +
      "<users>user1</users><groups>hdmi-hadoopeng</groups></acl></acls>";
      os.write(acl.getBytes());
      os.close();

      Configuration conf = new Configuration();
      conf.set(ConfigKeys.ACLS_FILE, aclFile.getAbsolutePath());


      createKeyStore();
      createPasswordFile();

      conf.set(ConfigKeys.KEYSTORE_FILE, ksFile.getAbsolutePath());
      conf.set(ConfigKeys.KEYSTORE_PASSWORD_FILE, pwdFile.getAbsolutePath());

      safeService = new SafeService();
      safeService.init(conf);
      safeService.init();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @AfterClass 
  public static void tearDown(){
    new File ("test.jceks").delete();
    new File ("password.properties").delete();
    aclFile.delete(); 
  }

  private static void createKeyStore () throws IOException{
    ksFile = new File ("test.jceks");
    FileInputStream fs = null;
    try {
      char[] keyStorePassword = "kspwd".toCharArray();

      ks = KeyStore.getInstance("JCEKS");

      ks.load(fs, keyStorePassword);
      char[] password = ("password").toCharArray();

      String key = "alias";
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
      SecretKey generatedSecret =
        factory.generateSecret(new PBEKeySpec(    
            password));

      PasswordProtection keyStorePP = new PasswordProtection(keyStorePassword);


      ks.setEntry(key, new SecretKeyEntry(
          generatedSecret), keyStorePP);

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
 public void testReadKey(){
    try {
      assertEquals (new String(safeService.getKey("user1", "alias")), "password");
    } catch (SafeException e) {
      e.printStackTrace();
      fail();
    }
  }
  
  @Test
  public void testReadNonExistentKey(){
     try {
       assertNull (safeService.getKey("user1", "nokey"));
     } catch (SafeException e) {
       e.printStackTrace();
     }
   }
}
