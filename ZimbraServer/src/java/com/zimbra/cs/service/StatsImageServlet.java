/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 ("License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite Server.
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc.
 * Portions created by Zimbra are Copyright (C) 2005, 2006 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): 
 * 
 * ***** END LICENSE BLOCK *****
 */

/*
 * Created on Dec 20, 2004
 * @author Greg Solovyev
 * */
package com.zimbra.cs.service;

import java.io.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.servlet.ZimbraServlet;
import com.zimbra.cs.util.ByteUtil;
import com.zimbra.cs.localconfig.LC;

public class StatsImageServlet extends ZimbraServlet {

    private static Log mLog = LogFactory.getLog(StatsImageServlet.class);

    private static final String IMG_NOT_AVAIL = "data_not_available.gif";

    public void init() throws ServletException {
        String name = getServletName();
        mLog.info("Servlet " + name + " starting up");
        super.init();
    }

    public void destroy() {
        String name = getServletName();
        mLog.info("Servlet " + name + " shutting down");
        super.destroy();
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
    {
        AuthToken authToken = getAdminAuthTokenFromCookie(req, resp);
        if (authToken == null) 
            return;
        
        String imgName = null;
        InputStream is = null;
        boolean imgAvailable = true;
        boolean localServer = false;
        boolean systemWide = false;
        
        String serverAddr = "";
    	
        String noDefaultImg = req.getParameter("nodef");
        boolean noDefault = false;
        if (noDefaultImg != null && !noDefaultImg.equals("") && noDefaultImg.equals("1")){
            noDefault = true;
        }
        String reqPath = req.getRequestURI(); 
        try { 
        	
        	
	        //check if this is the logger host, otherwise proxy the request to the logger host 
			String serviceHostname = Provisioning.getInstance().getLocalServer().getAttr(Provisioning.A_zimbraServiceHostname);
			String logHost  = Provisioning.getInstance().getConfig().getAttr(Provisioning.A_zimbraLogHostname);
			if(!serviceHostname.equalsIgnoreCase(logHost)) {
				StringBuffer url = new StringBuffer("https");
				url.append("://").append(logHost).append(':').append(LC.zimbra_admin_service_port.value());
				url.append(reqPath);
				String queryStr = req.getQueryString();
				if(queryStr != null)
					url.append('?').append(queryStr);
				
				// create an HTTP client with the same cookies
		        HttpState state = new HttpState();
		        try {
		            state.addCookie(new org.apache.commons.httpclient.Cookie(logHost, COOKIE_ZM_ADMIN_AUTH_TOKEN, authToken.getEncoded(), "/", null, false));
		        } catch (AuthTokenException ate) {
		            throw ServiceException.PROXY_ERROR(ate, url.toString());
		        }
		        HttpClient client = new HttpClient();
		        client.setState(state);
		        GetMethod get = new GetMethod(url.toString());
		        try {
		            int statusCode = client.executeMethod(get);
		            if (statusCode != HttpStatus.SC_OK)
		                throw ServiceException.RESOURCE_UNREACHABLE(get.getStatusText(), null);
		            
		            resp.setContentType("image/gif");
		            ByteUtil.copy(get.getResponseBodyAsStream(), true, resp.getOutputStream(), false);
		            return;
		        } catch (HttpException e) {
		            throw ServiceException.RESOURCE_UNREACHABLE(get.getStatusText(), e);
		        } catch (IOException e) {
		            throw ServiceException.RESOURCE_UNREACHABLE(get.getStatusText(), e);
		        }	
			}
        } catch (Exception ex) {
        	resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
        	return;
        }       
        try { 
	        
	        if(reqPath == null && reqPath.length()==0) {
	        	resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
	    		return;
	        } 
	        
	        if (mLog.isDebugEnabled())
	            mLog.debug("received request to:("+reqPath+")");

	        String reqParts[] = reqPath.split("/");
	
	        String reqFilename = reqParts[3];
	        imgName = LC.stats_img_folder.value() + File.separator + reqFilename;        	
	        try { 
	        	is = new FileInputStream(imgName);
	        } catch (FileNotFoundException ex) {//unlikely case - only if the server's files are broken
	        	if(is != null)
	        		is.close();
	        	if (!noDefault){
                    imgName = LC.stats_img_folder.value() + File.separator + IMG_NOT_AVAIL;
                    is = new FileInputStream(imgName);
                
	        	} else {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Image not found");
                    return;
                }
	        }        
        } catch (Exception ex) {
        	if(is != null)
        		is.close();

        	resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "FNF image File not found");
        	return;
        }
    	resp.setContentType("image/gif");
    	ByteUtil.copy(is, true, resp.getOutputStream(), false);
    }
}
