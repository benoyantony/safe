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
package org.apache.safe.servlet;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.safe.ErrorCode;
import org.apache.safe.service.SafeException;
import org.apache.safe.service.SafeService;
import org.apache.safe.util.XmlUtils;
import org.jdom.Attribute;
import org.jdom.Element;

public class SafeServlet extends BaseServlet {


  private static final SafeService SAFE_INSTANCE = new SafeService();

  private static final Log LOG =
    LogFactory.getLog(SafeServlet.class);

  private static boolean INITED = false;

  static {
    try {
      SAFE_INSTANCE.init();
      INITED = true;
    } catch (IOException e) {

    }
  }

  /**
   * Return information about SLA Events.
   */
  @SuppressWarnings("unchecked")
  public void doGet(HttpServletRequest request, HttpServletResponse response)
  throws ServletException, IOException {

    try {
      if (!INITED){
        throw new SafeServletException(HttpServletResponse.SC_BAD_REQUEST, ErrorCode.E0200);
      }
      String action = request
      .getParameter(Constants.ACTION_PARAM);
      if (action.equals(Constants.SAFE_GET_KEY)){
        String keyId = request
        .getParameter(Constants.SAFE_KEY_ID);
        
        ParamChecker.notEmpty(keyId, Constants.SAFE_KEY_ID);

        byte[] key = SAFE_INSTANCE.getKey(getUser(request), keyId);
        Element eKey = new Element("key");
        eKey.setAttribute(new Attribute("id", keyId));
        String strKey = Base64.encodeBase64String(key);
        eKey.setText(strKey);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(XmlUtils.prettyPrint(eKey) + "\n");

      }
      else  if (action.equals(Constants.SAFE_GET_KEYS)){
        String keyNames = request
        .getParameter(Constants.SAFE_KEY_ID);
        ParamChecker.notEmpty(keyNames, Constants.SAFE_KEY_ID);
        //get the list of key names
        String[] keyIds= keyNames.split("\\s*,\\s*");
        Element eKeys = new Element("keys");
        for (String keyId: keyIds){
          byte[] key = SAFE_INSTANCE.getKey(getUser(request), keyId);
          Element eKey = new Element("key");
          eKey.setAttribute(new Attribute("id", keyId));
          String strKey = Base64.encodeBase64String(key);
          eKey.setText(strKey);
          eKeys.addContent(eKey);
        }
        response.getWriter().write(XmlUtils.prettyPrint(eKeys) + "\n");
        response.setStatus(HttpServletResponse.SC_OK);
      }
    }catch ( SafeException  e){
      throw new SafeServletException(HttpServletResponse.SC_UNAUTHORIZED,
          e.getErrorCode(), e.getMessage());
    }catch (RuntimeException re) {
      re.printStackTrace();
      LOG.error("Runtime error ", re);
      throw new SafeServletException(HttpServletResponse.SC_BAD_REQUEST, ErrorCode.E0307, re.getMessage());
    }
  }
  

}
