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
package org.apache.safe;

public enum ErrorCode {
  
    E0000( "Authorization Failure"),
    E0001("Error in Authorizing"),
    E0100("Error in getting key"),
    E0101("Key doesn't exist"),
    E0200("Safe Service is not inititalized"),
    E0300( "Invalid content-type [{0}]"),
    E0301( "Invalid resource [{0}]"),
    E0302( "Invalid parameter [{0}]"),
    E0303( "Invalid parameter value, [{0}] = [{1}]"),
    E0304( "Invalid parameter type, parameter [{0}] expected type [{1}]"),
    E0305( "Missing parameter [{0}]"),
    E0306( "Invalid parameter"),
    E0307( "Runtime error [{0}]"),;

    private String template;

    /**
     * Create an error code.
     *
     * @param template template for the exception message.
     */
    private ErrorCode( String template) {
        this.template = template;
    }

    /**
     * Return the message (StringFormat) template for the error code.
     *
     * @return message template.
     */
    public String getTemplate() {
        return template;
    }





}
