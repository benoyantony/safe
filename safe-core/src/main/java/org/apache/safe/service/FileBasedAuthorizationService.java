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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.safe.util.ReflectionUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

public class FileBasedAuthorizationService implements AuthorizationService {

  private static final Log LOG =
    LogFactory.getLog(FileBasedAuthorizationService.class);

  static class ACL {

    private Set<String> users;
    private Set<String> groups; 

    public ACL(Set<String> users, Set<String> groups) {
      super();
      this.users = users;
      this.groups = groups;
    }

    public Set<String> getUsers() {
      return users;
    }

    public void setUsers(Set<String> users) {
      this.users = users;
    }

    public Set<String> getGroups() {
      return groups;
    }

    public void setGroups(Set<String> groups) {
      this.groups = groups;
    }
  }

  Map<String, ACL> acls = new HashMap<String, ACL>();
  GroupService groupService;

  void setGroupService(GroupService groups) {
    this.groupService = groups;
  }

  public void init (Configuration conf) throws IOException{
    String aclFileName =  conf.get("acls.file");
    if (aclFileName == null){
      LOG.error("acls.file is not set" );
      throw new IOException("acls.file is not set");
    }
    File aclFile = new File (aclFileName);
    if (!aclFile.exists()){
      LOG.error("The file - " + aclFileName +" does not exist." );
      throw new IOException("The file - " + aclFileName +" does not exist.");
    }
    FileInputStream aclfs = null;
    try {
      aclfs = new FileInputStream (aclFile);
      loadResource (aclfs);
    } catch (FileNotFoundException e) {
      //ignore
    }

    //set the GroupService
    GroupService as =  ReflectionUtils.newInstance(conf.getClass("groupservice.impl", 
        ShellBasedGroupService.class, GroupService.class));
    this.setGroupService(as);
  }

  void loadResource( InputStream input) {
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
      if (!"acls".equals(root.getTagName()))
        LOG.fatal("bad conf file: top-level element not <acls>");
      NodeList props = root.getChildNodes();
      for (int i = 0; i < props.getLength(); i++) {
        Node propNode = props.item(i);
        if (!(propNode instanceof Element))
          continue;
        Element prop = (Element)propNode;
        if (!"acl".equals(prop.getTagName()))
          LOG.warn("bad conf file: element not <acl>");
        NodeList fields = prop.getChildNodes();
        String key = null;
        String users = null;
        String groups = null;
        for (int j = 0; j < fields.getLength(); j++) {
          Node fieldNode = fields.item(j);
          if (!(fieldNode instanceof Element))
            continue;
          Element field = (Element)fieldNode;
          if ("key".equals(field.getTagName()) && field.hasChildNodes())
            key = ((Text)field.getFirstChild()).getData().trim();
          if ("users".equals(field.getTagName()) && field.hasChildNodes())
            users = ((Text)field.getFirstChild()).getData().trim();
          if ("groups".equals(field.getTagName()) && field.hasChildNodes())
            groups =((Text)field.getFirstChild()).getData();

          if (key != null ){
            Set<String> userSet = null;
            Set<String> groupSet = null;

            if ( users!= null && !users.isEmpty()){
              userSet = new HashSet<String>(Arrays.asList(users.split(",")));
            }
            if (groups != null && !groups.isEmpty()){
              groupSet = new HashSet<String>(Arrays.asList(groups.split(",")));
            }
            ACL acl = new ACL(userSet,groupSet);
            acls.put(key, acl);
          }
        }
      }
    } catch (IOException e) {
      LOG.fatal("error parsing conf file: " + e);
      throw new RuntimeException(e);
    } catch (DOMException e) {
      LOG.fatal("error parsing conf file: " + e);
      throw new RuntimeException(e);
    } catch (SAXException e) {
      LOG.fatal("error parsing conf file: " + e);
      throw new RuntimeException(e);
    } catch (ParserConfigurationException e) {
      LOG.fatal("error parsing conf file: " + e);
      throw new RuntimeException(e);
    }
  }

  /* (non-Javadoc)
   * @see org.apache.safe.AuthorizationService#checkAccess(java.lang.String, java.lang.String)
   */
  @Override
  public boolean checkAccess (String keyId , String userId) throws IOException{
    if ( keyId.endsWith(".publickey") || keyId.endsWith(".certificate")){
      return true;
    }
    ACL acl = acls.get(keyId);
    if (acl == null){
      return false;
    }
    if (acl.users != null && acl.users.contains(userId)){
      return true;
    }
    Set<String> keyGroups = acl.getGroups();
    if (keyGroups != null){
      Set<String>  userGroups = groupService.getGroups(userId);
      if (userGroups != null && !keyGroups.isEmpty()) {
        for (String group : userGroups) {
          if (keyGroups.contains(group)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
