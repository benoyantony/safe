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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.safe.util.LogUtils;

/**
 * Base class for Safe web service API Servlets. <p/> This class provides validation, error logging and
 * other common functionality.
 */
public abstract class BaseServlet extends HttpServlet {

     private static final Log LOG =
      LogFactory.getLog(BaseServlet.class);

    /**
     * Dispatches to super after loginfo and intrumentation handling. In case of errors dispatches error response codes
     * and does error logging.
     */
    @SuppressWarnings("unchecked")
    protected final void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        try {
            super.service(request, response);
        }
        catch (SafeServletException ex) {
           LOG.warn(LogUtils.format("URL[{0} {1}] error[{2}], {3}", request.getMethod(), getRequestUrl(request), ex.getErrorCode(), ex
               .getMessage(), ex));
            sendErrorResponse(response, ex.getHttpStatusCode(), ex.getErrorCode().toString(), ex.getMessage());
        }
        catch (RuntimeException ex) {        
            LOG.error(LogUtils.format("URL[{0} {1}] error, {2}", request.getMethod(), getRequestUrl(request), ex.getMessage(), ex));
            throw ex;
        }
    }

    private String getRequestUrl(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        if (request.getQueryString() != null) {
            url.append("?").append(request.getQueryString());
        }
        return url.toString();
    }

    /**
     * Return the user name of the request if any.
     *
     * @param request request.
     * @return the user name, <code>null</code> if there is none.
     */
    protected String getUser(HttpServletRequest request) {
        return request.getRemoteUser();
    }
    
    /**
     * Sends a error response.
     *
     * @param response servlet response.
     * @param statusCode HTTP status code.
     * @param error error code.
     * @param message error message.
     * @throws java.io.IOException thrown if the error response could not be set.
     */
    protected void sendErrorResponse(HttpServletResponse response, int statusCode, String error, String message)
            throws IOException {
        response.setHeader(Constants.SAFE_ERROR_CODE, error);
        response.setHeader(Constants.SAFE_ERROR_MESSAGE, message);
        response.sendError(statusCode, error+ ":" + message);
    }
}
