package org.apache.safe;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.cloudera.alfredo.client.AuthenticatedURL;
import com.cloudera.alfredo.client.AuthenticationException;

public class SafeClient {
    private static final Log LOG = LogFactory.getLog(SafeClient.class);
    String keystore;
    
    public  SafeClient(String keystore){
      this.keystore = keystore;
    }

    public byte[][] getValues( String[] keyNames) throws IOException {

      StringBuffer safeUrl = new StringBuffer();
      StringBuffer sbKeyNames = new StringBuffer();
      for (String keyName: keyNames){
        sbKeyNames.append(keyName);sbKeyNames.append(",");
      }
      safeUrl.append(keystore).append("?action=getkeys").
        append("&key=").append(sbKeyNames.toString());
      URL url = new URL (safeUrl.toString());

      AuthenticatedURL.Token token = new AuthenticatedURL.Token();
      try {
        HttpURLConnection conn = new AuthenticatedURL().openConnection(url, token);
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
          byte[][]  result = loadResource(keyNames, conn.getInputStream());
          return result;
        }
        else {
          LOG.error("Failed to get keys from Safe. The http error code is "+ conn.getResponseCode() + 
              " and message is "+ conn.getResponseMessage());
          throw new IOException ("Failed to get keys from Safe. The http error code is "+ conn.getResponseCode() + 
              " and message is "+ conn.getResponseMessage());
        }
      } catch (AuthenticationException e) {
        LOG.error("Failed to authenticate user to Safe. Unable to get Keys.", e);
        throw new IOException ("Failed to authenticate user to Safe. Unable to get keys." ,e);
      } 
    }

    private byte[][] loadResource( String[] keyNames,InputStream input) throws IOException {
      byte [][] result = new byte[keyNames.length][];
      try {
        DocumentBuilderFactory docBuilderFactory 
        = DocumentBuilderFactory.newInstance();
        //ignore all comments inside the xml file
        docBuilderFactory.setIgnoringComments(true);

        //allow includes in the xml file
        docBuilderFactory.setNamespaceAware(true);
        try {
          docBuilderFactory.setXIncludeAware(true);
        } catch (UnsupportedOperationException e) {
          LOG.error("Failed to set setXIncludeAware(true) for parser "
              + docBuilderFactory
              + ":" + e,
              e);
        }
        DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
        Document doc = null;
        Element root = null;

        try {
          doc = builder.parse((InputStream)input);
        } finally {
          ((InputStream)input).close();
        }

        if (doc == null && root == null) {
          throw new RuntimeException(input + " not found");
        }

        if (root == null) {
          root = doc.getDocumentElement();
        }
        if (!"keys".equals(root.getTagName())){
          LOG.error("bad safe response: top-level element not <keys>");
          throw new IOException("bad safe response: top-level element not <keys>");
        }
        NodeList props = root.getChildNodes();
        int keyCount =0;
        for (int i = 0; i < props.getLength(); i++) {
          Node keyNode = props.item(i);
          if (!(keyNode instanceof Element))
            continue;
          Element keyElement = (Element)keyNode;
          if (!"key".equals(keyElement.getTagName())){
            LOG.error("bad safe response: element not <key>");
            throw new IOException("bad safe response: element not <key>");
          }
          if (!keyElement.getAttribute("id").toString().equals(keyNames[keyCount])){
            LOG.error("bad safe response: The keys are not in the requested order.");
            throw new IOException("The keys are not in the requested order");
          }
          String keyValue = ((Text)keyElement.getFirstChild()).getData().trim();
          result[keyCount] = Base64.decodeBase64(keyValue);
          keyCount++;
        }
        return result;
      } catch (DOMException e) {
        LOG.error("error parsing Safe Output: " + e);
        throw new IOException(e);
      } catch (SAXException e) {
        LOG.error("error parsing Safe Output: " + e);
        throw new RuntimeException(e);
      } catch (ParserConfigurationException e) {
        LOG.fatal("error parsing Safe Output: " + e);
        throw new IOException(e);
      }
    }
}
