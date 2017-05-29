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

import java.util.ListIterator;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.sip.*;
import javax.servlet.sip.annotation.SipListener;

import com.berinchik.sip.service.fsm.FcsServiceContext;
import com.berinchik.sip.service.registrar.Registrar;
import com.berinchik.sip.service.registrar.SimpleRegisterHelper;

import com.berinchik.sip.util.CommonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.media.server.io.sdp.SdpException;

import static javax.servlet.sip.SipServletResponse.*;

/**
 * This example shows a typical UAS and reply 200 OK to any INVITE or BYE it receives
 * 
 * @author Jean Deruelle
 *
 */
@SipListener
public class FlexibleCommunicationServlet
		extends SipServlet implements SipErrorListener, TimerListener {

	private static final String SC_DATABASE_ACCESS = "DatabaseAccess";
	private static final String SC_LOCAL_SERVER_PORT = "5080";

	private static Log logger = LogFactory.getLog(FlexibleCommunicationServlet.class);

	private void normaliseInviteRequest(SipServletRequest request) throws ServletParseException {
		String stringRequestURI = removeLocalServerPortFromURI(request.getRequestURI());
		String stringToUri = removeLocalServerPortFromURI(request.getTo().getURI());

		request.setRequestURI(sipFactory.createURI(stringRequestURI));
		request.getTo().setURI(sipFactory.createURI(stringToUri));

	}

	private void normaliseRegisterRequest(SipServletRequest request) throws ServletParseException {
		String stringToUri = removeLocalServerPortFromURI(request.getTo().getURI());
		request.getTo().setURI(sipFactory.createURI(stringToUri));
	}

	private String removeLocalServerPortFromURI(URI uri) {

		String stringURI = uri.toString();

		if(stringURI.contains(":" + SC_LOCAL_SERVER_PORT)) {
			stringURI
					= stringURI.replaceFirst(":" + SC_LOCAL_SERVER_PORT, "");
		}

		return stringURI;
	}

	@Resource
	private SipFactory sipFactory;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the FlexibleCommunicationServlet servlet has been started");
		super.init(servletConfig);
	}

	@Override
	public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException{

		try {
			addUtilAttributesToAppSession(CommonUtils.getAppSession((SipServletMessage)req, (SipServletMessage)resp));
		}
		catch (SQLException ex) {
			logger.error("Unable to connect data source", ex);
			throw new ServletException("Server is down");
		}


		super.service(req, resp);
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {


		logger.trace("INVITE before normalisation:\n"
				+ request);
		normaliseInviteRequest(request);
		logger.trace("INVITE after normalisation:\n"
				+ request);

		try {
			request.createResponse(SC_TRYING, "Trying");
			CommonUtils.getSipServiceContext(request).doInvite(request);
		} catch (SQLException e) {
			logger.error("Service context error", e);
			request.createResponse(SC_SERVER_INTERNAL_ERROR, "Server internal error");
		}


	}

	@Override
	protected void doBye(SipServletRequest request) throws ServletException,
			IOException {
		CommonUtils.getSipServiceContext(request).doBye(request);
	}

	@Override
	protected void doCancel(SipServletRequest request) throws ServletException,
			IOException {
		CommonUtils.getSipServiceContext(request).doCancel(request);
	}

	@Override
	protected void doAck(SipServletRequest request) throws ServletException,
			IOException {
		CommonUtils.getSipServiceContext(request).doAck(request);
	}

	@Override
	protected void doSuccessResponse(SipServletResponse response) throws IOException {
		try {
			CommonUtils.getSipServiceContext(response).doSuccessResponse(response);
		} catch (SdpException e) {
			//TODO: process this on lower level to sent error response
			logger.error("Sdp negotiation failed", e);
		}
	}

	@Override
	protected void doProvisionalResponse(SipServletResponse response) throws IOException {
		CommonUtils.getSipServiceContext(response).doProvisionalResponse(response);
	}

	@Override
	protected void doErrorResponse(SipServletResponse response) throws IOException {
		try {
			CommonUtils.getSipServiceContext(response).doErrorResponse(response);
		} catch (SQLException e) {
			logger.error("Database access error: ", e);
		} catch (ServletParseException e) {
			logger.error("Servlet parse error: ", e);
		}
	}

	@Override
	protected void doRegister(SipServletRequest request) throws ServletException,
			IOException {

		logger.trace("Got REGISTER:\n"
				+ request);
		normaliseRegisterRequest(request);
		logger.trace("normalisation performed");
		logMessageInfo(request);

		Registrar registrar = CommonUtils.getRegistrarHelper(request);

		URI toURI = request.getTo().getURI();

		ListIterator<Address> contacts = request.getAddressHeaders(CommonUtils.SC_CONTACT_HEADER);

		Address firstContact = null;
		if (contacts.hasNext()){
			firstContact = contacts.next();
			contacts.previous();
			logger.trace("contact: " + firstContact);
		}

		SipServletResponse resp = null;

		try {
			logger.info("trying to register");
			if (registrar == null) {
				logger.error("registrar not initialised!!!");
				throw new RuntimeException("Registrar is not initialised");
			}

			if (!registrar.isPrimary(toURI.toString())) {
				logger.debug("Primary user " + toURI + " not found during registration");
				resp = request.createResponse(SC_NOT_FOUND, "No such user");
			}
			else if(firstContact == null) {
				logger.debug("No contact headers during registration to "
						+ toURI
						+ "\nReturning all bindings");

				resp = registrar.createRegisterSuccessResponse(request);
			}
			else if (firstContact.isWildcard()) {
				logger.debug("First contact is Wildcard");
				if (request.getExpires() == 0) {
					logger.debug("Contact = *, Expire = 0: complete de registration");
					registrar.deregisterUser(toURI.toString());
					resp = request.createResponse(SC_OK, "Ok");
					resp.removeHeader(CommonUtils.SC_EXPIRES_HEADER);
				}
				else {
					logger.debug("Contact = *, Expire != 0: request could not be understood");
					resp = request.createResponse(SC_BAD_REQUEST, "Bad request");
				}
			}
			else if (request.getExpires() == 0) {
				logger.info("Request is recognized as de registration"
						+ "\ncontact - " + firstContact
						+ "\nuser - " + toURI);
				registrar.deregisterUser(toURI.toString(), firstContact.getURI().toString());
				resp = request.createResponse(SC_OK, "Ok");
				resp.removeHeader(CommonUtils.SC_EXPIRES_HEADER);
			}
			else {
				logger.debug("Request is recognized as registration request");
				while(contacts.hasNext()) {

					Address nextContact = contacts.next();

					logger.trace("Trying to register contact "
							+ nextContact.getURI()
							+ " to user "
							+ toURI);

					int expires = CommonUtils.getExpires(nextContact, request);

					registrar.registerUser(toURI.toString(), nextContact.getURI().toString(), expires);
				}

				resp = registrar.createRegisterSuccessResponse(request);
			}
		} catch (SQLException e) {
			logger.error("SQL Exception durin registration", e);
			resp = request.createResponse(SC_SERVER_INTERNAL_ERROR, "Server internal error");
		} catch (RuntimeException e) {
			logger.error("Runtime exception", e);
			resp = request.createResponse(SC_SERVER_INTERNAL_ERROR, "Server internal error");
		}

		logger.trace("Sending response: \n" + resp);

		resp.send();
	}

	@Override
	public void noAckReceived(SipErrorEvent sipErrorEvent) {
		CommonUtils.getSipServiceContext(sipErrorEvent.getRequest()).noAckReceived(sipErrorEvent);
	}

	@Override
	public void noPrackReceived(SipErrorEvent sipErrorEvent) {
		throw new UnsupportedOperationException("No prack supported");
	}

	public void timeout(ServletTimer timer) {
		logger.info("Timeout received" + timer);
		try {
			CommonUtils.getSipServiceContext(timer.getApplicationSession()).doTimeout(timer);
		} catch (Exception e) {
			logger.error("Error while processing timeout", e);
		}
	}

	private void addUtilAttributesToAppSession(SipApplicationSession appSession) throws SQLException{
		if (appSession.getAttribute(CommonUtils.SC_REGISTER_HELPER) == null) {
			appSession.setAttribute(CommonUtils.SC_REGISTER_HELPER, new SimpleRegisterHelper());
		}
		if (appSession.getAttribute(CommonUtils.SC_FLEX_COMM_SERVICE_CONTEXT) == null) {
			appSession.setAttribute(CommonUtils.SC_FLEX_COMM_SERVICE_CONTEXT,
					new FcsServiceContext(this.sipFactory));
		}
		if (appSession.getAttribute(CommonUtils.SC_SIP_FACTORY) == null) {
			appSession.setAttribute(CommonUtils.SC_SIP_FACTORY, this.sipFactory);
		}
		if (CommonUtils.getTimerService() == null) {
			CommonUtils.setTimerService((TimerService) getServletContext().getAttribute(TIMER_SERVICE));
		}
	}

	void logMessageInfo(SipServletMessage message) throws ServletParseException {

		Address address = message.getTo();
		logger.trace("\n\nTo header Address info:\n");
		traceLogAddressInfo(address);

		address = message.getFrom();
		logger.trace("\n\nFrom header Address info:\n");
		traceLogAddressInfo(address);

		address = message.getAddressHeader(CommonUtils.SC_CONTACT_HEADER);
		logger.trace("\n\nContact header Address info:\n");
		traceLogAddressInfo(address);

	}

	void traceLogAddressInfo(Address toAddress) {
		logger.trace("\nexpires: " + toAddress.getExpires()
				+ "\ntoAddress: " + toAddress
				+ "\ntoAddress.getDisplayName(): " + toAddress.getDisplayName()
				+ "\ntoAddress.getURI(): " + toAddress.getURI()
				+ "\ntoAddress.getValue(): " + toAddress.getValue()
				+ "\ntoAddress.getQ: " + toAddress.getQ()
		);
	}


}
