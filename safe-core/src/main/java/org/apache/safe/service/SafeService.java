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

import java.io.IOException;

import org.apache.safe.ErrorCode;
import org.apache.safe.util.ReflectionUtils;

public class SafeService {

  private KeyService keyService;
  private AuthorizationService authorizationService;
  private Configuration configuration;

  public void init ( Configuration conf)  throws IOException{
    configuration = conf;
    keyService = ReflectionUtils.newInstance(configuration.getClass("keyservice.impl", 
        KeyStoreBasedKeyService.class, KeyService.class));

    keyService.init(configuration);

    authorizationService = ReflectionUtils.newInstance(configuration.getClass("authorizationservice.impl", 
        FileBasedAuthorizationService.class, AuthorizationService.class));

    authorizationService.init(configuration);   
  }

  public void init () throws IOException{
    if (configuration == null){
      configuration = new Configuration();
    }
    init(configuration);
  }

  public byte[] getKey (String userId, String keyId) throws SafeException{
    boolean authorized = false;
    try{
      authorized = authorizationService.checkAccess(keyId, userId);   
    }catch (IOException e){
      throw new SafeException(ErrorCode.E0001);
    }
    if (!authorized){
      throw new SafeException(ErrorCode.E0000);
    }
    try {
      byte [] result =  keyService.getKey(keyId);
      if (result == null){
        throw new SafeException(ErrorCode.E0101);
      }
      return result;
    }catch (IOException e){
      throw new SafeException(ErrorCode.E0100);
    }
  }
}

