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
import com.berinchik.sip.service.registrar.database.util.Binding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static javax.servlet.sip.SipServletResponse.*;

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
	static final String SC_EXPIRES_HEADER = "Expire";

	private static Log logger = LogFactory.getLog(FlexibleCommunicationService.class);

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
		
		SipServletResponse sipServletResponse = request.createResponse(SC_OK);
		sipServletResponse.send();		
	}

	@Override
	protected void doRegister(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got REGISTER:\n"
				+ request.toString());

		Registrar registrar = getRegistrarHelper(request);

		URI toURI = sipFactory.createURI(request.getHeader(SC_TO_HEADER));
		String cleanToURI = cleanUri(toURI);

		ListIterator<String> contacts = request.getHeaders(SC_CONTACT_HEADER);

		String firstContact = null;
		if (contacts.hasNext())
			firstContact = contacts.next();

		SipServletResponse resp = null;

		try {
			if (!registrar.isPrimary(cleanToURI)) {
				logger.info("Primary user " + cleanToURI + " not found during registration");
				resp = request.createResponse(SC_NOT_FOUND, "No such user");
			}
			else if(firstContact == null) {
				logger.info("No contact headers during registration to "
						+ cleanToURI
						+ "\nReturning all bindings");

				resp = create200OkWithAllBindings(request, registrar);
			}
			else if ("*".equals(firstContact)) {
				if (request.getExpires() == 0) {
					logger.info("Contact = *, Expire = 0: complete de registration");
					registrar.deregisterUser(cleanToURI);
					resp = request.createResponse(SC_OK, "Ok");
					resp.removeHeader(SC_EXPIRES_HEADER);
				}
				else {
					logger.info("Contact = *, Expire != 0: request could not be understood");
					resp = request.createResponse(SC_BAD_REQUEST, "Bad request");
				}
			}
			else {
				logger.info("Request is recognized as registration request");
				String nextContact = firstContact;

				while(contacts.hasNext()) {
					logger.info("Trying to register contact "
							+ nextContact
							+ " to user "
							+ cleanToURI);
					Address contactAddress = sipFactory.createAddress(nextContact);
					int expires = getExpires(contactAddress, request);

					registrar.registerUser(cleanToURI, nextContact, expires);
					nextContact = contacts.next();
				}

				resp = create200OkWithAllBindings(request, registrar);
			}
		} catch (SQLException e) {
			logger.error("SQL Exception durin registration", e);
			resp = request.createResponse(SC_SERVER_INTERNAL_ERROR, "Server internal error");
		} catch (RuntimeException e) {
			logger.error("Runtime exception", e);
			resp = request.createResponse(SC_SERVER_INTERNAL_ERROR, "Server internal error");
		}

		resp.send();
	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		SipServletResponse sipServletResponse = request.createResponse(SC_OK);
		sipServletResponse.send();	
	}

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

	String cleanUri(Address address) {
		return cleanUri(address.getURI());
	}

	/**
	 * Adds all contacts from List bindings to message, with expires to every contact
	 *
	 * @param message SipServletMessage
	 * @param bindings List of Binding
	 * @throws ServletParseException
	 */
	private void addContactHeaders(SipServletMessage message, List<Binding> bindings) throws ServletParseException {
		for (Binding binding:
			 bindings) {
			long expiresTime = binding.getDuration();
			String contact = binding.getBindingURI();

			Address contactAddress = sipFactory.createAddress(contact);
			contactAddress.setExpires((int) expiresTime);
			message.setAddressHeader(SC_CONTACT_HEADER, contactAddress);
		}
	}

	private SipServletResponse create200OkWithAllBindings(SipServletRequest request, Registrar registrar)
			throws SQLException, ServletParseException {

		String cleanToUri = cleanUri(request.getTo());

		SipServletResponse resp = request.createResponse(SC_OK, "Ok");
		addContactHeaders(resp, registrar.getBindings(cleanToUri));
		resp.removeHeader(SC_EXPIRES_HEADER);

		return resp;
	}

	int getExpires(Address contact, SipServletMessage message) {
		int expires = contact.getExpires();
		if (expires < 0) {
			expires = message.getExpires();
		}

		return expires;
	}

	/*RegistrationStatus doRegister(URI to, Address binding, long expires, Registrar registrar) throws SQLException {

		String toUri = cleanUri(to);

		if (registrar.) {

		}

		String expiresParameter = binding.getParameter(SC_EXPIRES_HEADER);

		if (expiresParameter != null) {
			expires = Long.valueOf(expiresParameter);
		}

		if (expires != 0) {

		}


	}*/
}
