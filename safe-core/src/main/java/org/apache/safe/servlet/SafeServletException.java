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

import javax.servlet.ServletException;

import org.apache.safe.ErrorCode;
import org.apache.safe.util.LogUtils;

/**
 * Specialized Safe servlet exception that uses Safe error codes. <p/> It extends ServletException so it can be
 * handled in the <code>Servlet.service</code> method of the {@link BaseServlet}.
 */
public class SafeServletException extends ServletException {
    private static final long serialVersionUID = 1L;
    private ErrorCode errorCode;
    private int httpStatusCode;



    /**
     * Create a XServletException that triggers a specified HTTP error code.
     *
     * @param httpStatusCode HTTP error code to return.
     * @param errorCode Safe error code.
     * @param params paramaters to use in the error code template. If the last parameter is an Exception,
     */
    public SafeServletException(int httpStatusCode, ErrorCode errorCode, Object... params) {
        super (LogUtils.format(errorCode.getTemplate(), params));
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }
    
    /**
     * Utility method that extracts the <code>Throwable</code>, if present, from the parameters.
     *
     * @param params parameters.
     * @return a <code>Throwable</code> instance if it is the last parameter, <code>null</code> otherwise.
     */
    public static Throwable getCause(Object... params) {
        Throwable throwable = null;
        if (params != null && params.length > 0 && params[params.length - 1] instanceof Throwable) {
            throwable = (Throwable) params[params.length - 1];
        }
        return throwable;
    }

    /**
     * Return the Safe error code for the exception.
     *
     * @return error code for the exception.
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Return the HTTP error code to return to the client.
     *
     * @return HTTP error code.
     */
    public int getHttpStatusCode() {
        return httpStatusCode;
    }
    
}
