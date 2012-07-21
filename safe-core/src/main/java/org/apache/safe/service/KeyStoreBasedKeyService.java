/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. See accompanying LICENSE file.
 */
package org.apache.safe.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KeyStoreBasedKeyService implements KeyService {

  private static final Log LOG =
    LogFactory.getLog(KeyStoreBasedKeyService.class);

  private PasswordProtection keyStorePP ;

  private KeyStore keystore;

  void initialize (InputStream ksfs, InputStream pwdfs) throws IOException{
    Properties passwords = null;
    try {
      if (pwdfs != null){
        passwords = new Properties ();
        passwords.load(pwdfs);
      }
      char[] keyStorePassword = null;
      if (passwords != null){
        String strPassword = passwords.getProperty("keystore.password");
        if (strPassword != null){
          keyStorePassword = strPassword.toCharArray();
        }
        else {
          LOG.error("Canot obtain password for keystore");
          throw new IOException("Could not read secrets from keystore");
        }
      }
      else {
        System.out.println ("Enter KeyStore Password: ");
        keyStorePassword = System.console().readPassword();
      }

      keystore = KeyStore.getInstance("JCEKS");
      keystore.load(ksfs, // InputStream to keystore
          keyStorePassword);
      keyStorePP = new PasswordProtection(keyStorePassword);
    } catch (FileNotFoundException e) {
      throw new IOException (e);
    } catch (IOException e) {
      throw new IOException (e);
    } catch (KeyStoreException e) {
      throw new IOException (e);
    } catch (NoSuchAlgorithmException e) {
      throw new IOException (e);
    } catch (CertificateException e) {
      throw new IOException (e);
    }
  }

  @Override
  public byte[] getKey(String keyId) throws IOException {

    KeyStore.Entry entry;
    String realKeyId = keyId;
    if (keyId.endsWith(".publickey")){
      realKeyId = keyId.substring(0, keyId.length() - 10);
    }
    try {
      entry = keystore.getEntry (realKeyId, keyStorePP);

      if (entry != null){
        // is it a secret key
        byte[] value;
        if (entry instanceof SecretKeyEntry){
          SecretKeyEntry ske =
            (SecretKeyEntry)entry;
          value =  ske.getSecretKey().getEncoded();
          // is it a key pair
        }else if (entry instanceof PrivateKeyEntry){
          PrivateKeyEntry pke =
            (PrivateKeyEntry)entry;
          if (keyId.endsWith(".publickey")){
            value = pke.getCertificate().getPublicKey().getEncoded();
          }
          else {      
            value = pke.getPrivateKey().getEncoded();
          }
          // is it a certificate
        }else if (entry instanceof TrustedCertificateEntry){
          TrustedCertificateEntry tce = 
            (TrustedCertificateEntry) entry;
          value = tce.getTrustedCertificate().getEncoded();
        }
        else {
          throw new IOException ("Unknown Key type");   
        }
        return  value;
      } else {
        throw new IOException ("Key not found");
      }
    } catch (NoSuchAlgorithmException e) {
      throw new IOException (e);
    } catch (UnrecoverableEntryException e) {
      throw new IOException (e);
    } catch (KeyStoreException e) {
      throw new IOException (e);
    } catch (CertificateEncodingException e) {
      throw new IOException (e);
    }
  }

  @Override
  public void init(Configuration conf) throws IOException {
    try {
      String ksFileName =  conf.get("keystore.file");
      if (ksFileName == null){
        LOG.error("keystore.file is not set" );
        throw new IOException("keystore.file is not set");
      }
      File ksFile = new File (ksFileName);
      if (!ksFile.exists()){
        LOG.error("The file - " + ksFileName +" does not exist." );
        throw new IOException("The file - " + ksFileName +" does not exist.");
      }
      FileInputStream ksfs = new FileInputStream (ksFile);

      FileInputStream pwdfs = null;
      String pwdFileName =  conf.get("keystore.password.file");
      if (pwdFileName !=  null){
        File pwdFile = new File (pwdFileName);
        if (!pwdFile.exists()){
          LOG.error("The file - " + pwdFileName +" does not exist." );
          throw new IOException("The file - " + pwdFileName +" does not exist.");
        }
        pwdfs = new FileInputStream (pwdFile);
      }
      initialize (ksfs, pwdfs);
    } catch (FileNotFoundException e) {
      //ignore
    }

  }
}
