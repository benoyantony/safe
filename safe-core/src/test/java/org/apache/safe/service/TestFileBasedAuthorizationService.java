package org.apache.safe.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.safe.ConfigKeys;
import org.apache.safe.util.ReflectionUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestFileBasedAuthorizationService {

  private static File aclFile;
  private static AuthorizationService authorizationService;

  @BeforeClass
  public static void setup(){
    try {
      aclFile = new File("acls.xml");
      FileOutputStream os = new FileOutputStream(aclFile);
      String acl = "<?xml version=\"1.0\"?><acls><acl><key>key</key>" +
      		"<users>user1</users><groups>hdmi-hadoopeng</groups></acl></acls>";
      os.write(acl.getBytes());
      os.close();


      Configuration conf = new Configuration();
      conf.set(ConfigKeys.ACLS_FILE, aclFile.getAbsolutePath());
      authorizationService = ReflectionUtils.newInstance(FileBasedAuthorizationService.class);
      authorizationService.init(conf);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      fail();
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    } 
  }

  @AfterClass
  public static void tearDown(){
    aclFile.delete(); 
  }

  @Test
  public void basic (){

    // test with a user in the acl
    try {
      assertTrue (authorizationService.checkAccess("key", "user1"));
      //test with a user not in acl
      assertFalse (authorizationService.checkAccess("key", "user2"));
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }  
  }

  @Test
  public void keyNotPresent (){

    // test with a non-existent key
    try {
      assertFalse (authorizationService.checkAccess("key1", "user1"));
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Test
  public void testPublicKey (){


    // test with a non-existent key
    try {
      assertTrue (authorizationService.checkAccess("key.publickey", "user1"));
    } catch (IOException e) {
      e.printStackTrace();
      fail();
    }
  }

}
