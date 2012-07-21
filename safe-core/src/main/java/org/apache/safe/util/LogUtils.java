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
package org.apache.safe.util;

import java.text.MessageFormat;

import org.apache.safe.servlet.ParamChecker;

public class LogUtils {
  /**
   * Utility method that does uses the <code>StringFormat</code> to format the message template using the provided
   * parameters. <p/> In addition to the <code>StringFormat</code> syntax for message templates, it supports
   * <code>{E}</code> for ENTER. <p/> The last parameter is ignored for the formatting if it is an Exception.
   *
   * @param msgTemplate message template.
   * @param params paramaters to use in the template. If the last parameter is an Exception, it is ignored.
   * @return formatted message.
   */
  public static String format(String msgTemplate, Object... params) {
      ParamChecker.notEmpty(msgTemplate, "msgTemplate");
      msgTemplate = msgTemplate.replace("{E}", System.getProperty("line.separator"));
      if (params != null && params.length > 0) {
          msgTemplate = MessageFormat.format(msgTemplate, params);
      }
      return msgTemplate;
  }

}
