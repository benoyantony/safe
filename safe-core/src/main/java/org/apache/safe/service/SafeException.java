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

import org.apache.safe.ErrorCode;

public class SafeException extends Exception {
  
  ErrorCode errorCode ;

  public SafeException(ErrorCode errorCode) {
    super(errorCode.getTemplate());
    this.errorCode = errorCode;
  }
  
  public SafeException(ErrorCode errorCode, Throwable arg1) {
    super(errorCode.getTemplate(), arg1);
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
 
}
