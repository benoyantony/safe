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
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.safe.util.Shell;
import org.apache.safe.util.Shell.ExitCodeException;

public class ShellBasedGroupService implements GroupService  {
  
  private static final Log LOG =
    LogFactory.getLog(ShellBasedGroupService.class);
  
  private static class CachedGroups {
    final long timestamp;
    final Set<String> groups;
    
    CachedGroups(Set<String> groups) {
      this.groups = groups;
      this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
      return timestamp;
    }

    public Set<String> getGroups() {
      return groups;
    }
  }
  ConcurrentHashMap<String, CachedGroups> userToGroupsMap = new ConcurrentHashMap<String, CachedGroups>();
  
  //TODO Expose as a configuration
  private final long cacheTimeout= 30*60*1000; // 30 minutes
  
  
  
  /* (non-Javadoc)
   * @see org.apache.safe.GroupService#getGroups(java.lang.String)
   */
  @Override
  public  Set<String> getGroups(final String user) throws IOException {
    CachedGroups groups = userToGroupsMap.get(user);
    long now = System.currentTimeMillis();
    if (groups != null && (groups.getTimestamp() + cacheTimeout > now)) {
      return groups.getGroups();
    }
      String result = "";
      try {
        result = Shell.execCommand(Shell.getGroupsForUserCommand(user));
      } catch (ExitCodeException e) {
        // if we didn't get the group - just return empty list;
        LOG.warn("got exception trying to get groups for user " + user, e);
      }

      StringTokenizer tokenizer = new StringTokenizer(result);
      Set<String>groupSet = new HashSet<String>();
      while (tokenizer.hasMoreTokens()) {
        groupSet.add(tokenizer.nextToken());
      }
      groups = new CachedGroups(groupSet);
      userToGroupsMap.put(user, groups);
      return groups.getGroups();
  }
  
  /* (non-Javadoc)
   * @see org.apache.safe.GroupService#refresh()
   */
  @Override
  public void refresh() {
    LOG.info("clearing userToGroupsMap cache");
    userToGroupsMap.clear();
  }
}
