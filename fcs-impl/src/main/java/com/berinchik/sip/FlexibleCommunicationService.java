/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.berinchik.sip;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.sip.*;

import com.berinchik.sip.service.registrar.Registrar;
import com.berinchik.sip.service.registrar.RegistrationStatus;
import com.berinchik.sip.service.registrar.SimpleRegisterHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This example shows a typical UAS and reply 200 OK to any INVITE or BYE it receives
 * 
 * @author Jean Deruelle
 *
 */
public class FlexibleCommunicationService extends SipServlet {

	static final String SC_REGISTER_HELPER = "registerHelper";
	static final String SC_DATABASE_ACCESS = "databaseAccess";

	static final String SC_CONTACT_HEADER = "Contact";
	static final String SC_TO_HEADER = "To";
	static final String SC_EXPIRES_HEADER = "Expires";

	private static Log logger = LogFactory.getLog(FlexibleCommunicationService.class);


	SipApplicationSession getAppSession(SipServletMessage message1, SipServletMessage message2) {

		SipApplicationSession appSession = null;

		if (message1 != null)
			appSession = message1.getApplicationSession();
		else if (message2 != null)
			appSession = message2.getApplicationSession();

		return appSession;
	}

	SipApplicationSession getAppSession(SipServletMessage message) {

		SipApplicationSession appSession = null;

		if (message != null)
			appSession = message.getApplicationSession();

		return appSession;
	}

	void addUtilAttributesToAppSession(SipApplicationSession appSession) {
		if (appSession.getAttribute(SC_REGISTER_HELPER) == null) {
			appSession.setAttribute(SC_REGISTER_HELPER, new SimpleRegisterHelper());
		}
	}

	Registrar getRegistrarHelper(SipServletMessage message) {
		return (Registrar) message.getAttribute(SC_REGISTER_HELPER);
	}

	String cleanUri(URI uri) {
		Iterator<String> parameterNames = uri.getParameterNames();
		String parameter = null;
		while ((parameter = parameterNames.next())!= null) {
			uri.removeParameter(parameter);
		}
		return uri.toString();
	}

	RegistrationStatus doRegister(URI to, Address binding, long expires, Registrar registrar) throws SQLException {

		String toUri = cleanUri(to);

		if (registrar.) {

		}

		String expiresParameter = binding.getParameter(SC_EXPIRES_HEADER);

		if (expiresParameter != null) {
			expires = Long.valueOf(expiresParameter);
		}

		if (expires != 0) {

		}


	}

	@Resource
	private SipFactory sipFactory;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the HelloSipWorld servlet has been started");
		super.init(servletConfig);
	}

	@Override
	public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {

		addUtilAttributesToAppSession(getAppSession((SipServletMessage)req, (SipServletMessage)resp));

		super.service(req, resp);
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n"
				+ request.toString());
		String fromUri = request.getFrom().getURI().toString();
		logger.info(fromUri);
		
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();		
	}

	@Override
	protected void doRegister(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got REGISTER:\n"
				+ request.toString());

		Registrar registrar = getRegistrarHelper(request);

		ListIterator<String> contacts = request.getHeaders(SC_CONTACT_HEADER);
		String contact = contacts.next();
		String to = request.getHeader(SC_TO_HEADER);
		long expires = Long.valueOf(request.getHeader(SC_EXPIRES_HEADER));
		RegistrationStatus regStatus;


		if(contact == null) {
			//TODO: return info about all bindings
		}
		if (contact.toString() == "*") {
			//TODO: inplelent deregistration
		}
		else {
			do {
				regStatus = registrar.registerUser(to, contact, expires);
				SipServletResponse sipServletResponse;

				switch (regStatus) {
					case ERROR:

						break;
					case USER_NOT_FOUND:
						break;
					case OK:
						break;
				}

			}
		}




		 //= request.createResponse(SipServletResponse.SC_OK);

		Address address = request.getAddressHeader("Contact");
		String fromURI = request.getFrom().getURI().toString();

		int expires = address.getExpires();
		if(expires < 0) {
			expires = request.getExpires();
		}
		if(expires == 0) {
			logger.info("User " + fromURI + " unregistered");
		} else {
			sipServletResponse.setAddressHeader("Contact", address);
			logger.info("User " + fromURI +
					" registered with an Expire time of " + expires);
		}
		sipServletResponse.send();
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
		sipServletResponse.send();	
	}
}
