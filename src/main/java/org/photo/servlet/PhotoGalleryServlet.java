package org.photo.servlet;

import java.awt.print.Printable;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.photo.service.PhotoGalleryService;

import biz.com.service.EventService;


/**
 * <code>GalleryServlet</code> is a servlet which contains all request and
 * response related to this gallery module.
 * 
 * @author pranav.arya
 */
@Component(immediate = true, metatype = false)
@Service(value = javax.servlet.Servlet.class)
@Properties({
		@Property(name = "service.description", value = "PhotoGallery Show Servlet"),
		@Property(name = "service.vendor", value = "The Apache Software Foundation"),
		@Property(name = "sling.servlet.paths", value = { "/servlet/photo/photoshow" }),
		@Property(name = "sling.servlet.resourceTypes", value = "sling/servlet/default"),
		@Property(name = "sling.servlet.extensions", value = { "liststory",
				"addstory", "viewstory"

		})

})
@SuppressWarnings("serial")
public class PhotoGalleryServlet extends SlingAllMethodsServlet {

	/** The repo. */
	@Reference
	private SlingRepository repo;

	/** The service. */
	@Reference
	private PhotoGalleryService service;

	@Reference
	private EventService eventService;

	private int NUMBEROFRESULTSPERPAGE=10;
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.sling.api.servlets.SlingAllMethodsServlet#doPost(org.apache
	 * .sling.api.SlingHttpServletRequest,
	 * org.apache.sling.api.SlingHttpServletResponse)
	 */

	@Override
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {
		Session session;
		
		try {
			if (request.getRequestPathInfo().getExtension().equals("addstory")) {
				try {
					request.getRequestDispatcher("/content/photo/.addStory")
							.forward(request, response);

				} catch (Exception e) {
					request.getRequestDispatcher("/content/photo/.addStory")
							.forward(request, response);

				}

			} else if (request.getRequestPathInfo().getExtension()
					.equals("viewstory")) {

				Node rootNode;
				try {

					session = repo.login(new SimpleCredentials("admin", "admin"
							.toCharArray()));
					rootNode = session.getNode("/content/photo/"
							+ request.getParameter("recentAdd"));

					request.getRequestDispatcher(
							"/content/photo/"
									+ request.getParameter("recentAdd")
									+ ".photostory").forward(request, response);
				} catch (Exception e) {
					request.getRequestDispatcher("/content/photo/*.photostory")
							.forward(request, response);
				}
			} else if (request.getRequestPathInfo().getExtension()
					.equals("liststory")) {
				PrintWriter p = response.getWriter();
				Node rootNode;

				session = repo.login(new SimpleCredentials("admin", "admin"
						.toCharArray()));

				rootNode = session.getRootNode().getNode("content")
						.getNode("photo");

				//p.print(eventService.getPhotoStoryListList(request));
				request.setAttribute("photostory",
						eventService.getPhotoStoryListList(request));
				
				String style = request.getParameter("style");
				if (style != null) {
					request.getRequestDispatcher("/content/photo/.stylestory")
							.forward(request, response);
				} else {
					request.getRequestDispatcher("/content/photo/.listStory")
							.forward(request, response);
				}
				

			} else if (request.getRequestPathInfo().getExtension()
					.equals("delete")) {
				if (request.getRemoteUser() != null
						&& !request.getRemoteUser().equals("anonymous")) {
					session = repo.login(new SimpleCredentials("admin", "admin"
							.toCharArray()));

					if (request.getParameter("photodelete") != null
							&& request.getParameter("photodelete") != "") {
						Node exhi = session.getRootNode().getNode("content")
								.getNode("photo");
						if (exhi.hasNode(request.getParameter("photodelete"))) {
							Node d = exhi.getNode(request
									.getParameter("photodelete"));
							d.remove();
							session.save();
							response.sendRedirect(request.getContextPath()
									+ "/servlet/photo/photoshow.photolist?delete=true");
						} else {
							response.sendRedirect(request.getContextPath()
									+ "/servlet/photo/photoshow.photolist");
						}

					} else {
						response.sendRedirect(request.getContextPath()
								+ "/servlet/photo/photoshow.photolist");
					}

				} else {
					response.sendRedirect(request.getContextPath()
							+ "/servlet/photo/photoshow.photolist");
				}
			} else if (request.getRequestPathInfo().getExtension()
					.equals("photolist")) {
				Node groupNode = null;
				

				String from = null;
				String to = null;
				int t, f;
				from = request.getParameter("from");
				to = from;

				if (to != null && from != null) {
					try {
						t = Integer.parseInt(to);
						f = Integer.parseInt(from);
						f = (f - 1) * NUMBEROFRESULTSPERPAGE;
						t = t * NUMBEROFRESULTSPERPAGE;
						Node tempCmpNode = null;
						ArrayList<Node> m = new ArrayList<Node>();
						session = repo.login(new SimpleCredentials("admin",
								"admin".toCharArray()));
						groupNode = session.getRootNode().getNode("content")
								.getNode("photo");
						if (groupNode.hasNodes()) {
							NodeIterator iterator = groupNode.getNodes();
							while (iterator.hasNext()) {
								tempCmpNode = iterator.nextNode();
								
								m.add(tempCmpNode);

							}
						}
						request.setAttribute("total", m.size());
						ArrayList<Node> alist = new ArrayList<Node>();
						if (m.size() > t) {
							for (int i = f; i < t; i++) {
								alist.add(m.get(i));
							}
						} else {
							for (int i = f; i < m.size(); i++) {
								alist.add(m.get(i));
							}
						}
						request.setAttribute("storylist", alist);
						request.getRequestDispatcher(
								"/content/photo/.storylistrender")
								.forward(request, response);
					} catch (NumberFormatException e) {
						Node tempCmpNode = null;
						ArrayList<Node> m = new ArrayList<Node>();
						session = repo.login(new SimpleCredentials("admin",
								"admin".toCharArray()));
						groupNode = session.getRootNode().getNode("content")
								.getNode("photo");
						if (groupNode.hasNodes()) {
							NodeIterator iterator = groupNode.getNodes();
							while (iterator.hasNext()) {
								tempCmpNode = iterator.nextNode();
								m.add(tempCmpNode);

							}
						}

						request.setAttribute("total", m.size());
						ArrayList<Node> alist = new ArrayList<Node>();
						if (m.size() > NUMBEROFRESULTSPERPAGE) {
							for (int i = 0; i < NUMBEROFRESULTSPERPAGE; i++) {
								alist.add(m.get(i));

							}
						} else {
							for (int i = 0; i < m.size(); i++) {
								alist.add(m.get(i));

							}
						}

						request.setAttribute("storylist", alist);
						request.getRequestDispatcher(
								"/content/photo/.storylistrender")
								.forward(request, response);
					}

				} else {
					Node tempCmpNode = null;
					ArrayList<Node> m = new ArrayList<Node>();
					session = repo.login(new SimpleCredentials("admin", "admin"
							.toCharArray()));
					groupNode = session.getRootNode().getNode("content")
							.getNode("photo");
					if (groupNode.hasNodes()) {
						NodeIterator iterator = groupNode.getNodes();
						while (iterator.hasNext()) {
							tempCmpNode = iterator.nextNode();
							m.add(tempCmpNode);

						}
					}

					request.setAttribute("total", m.size());
					ArrayList<Node> alist = new ArrayList<Node>();
					if (m.size() > NUMBEROFRESULTSPERPAGE) {
						for (int i = 0; i < NUMBEROFRESULTSPERPAGE; i++) {
							alist.add(m.get(i));

						}
					} else {
						for (int i = 0; i < m.size(); i++) {
							alist.add(m.get(i));

						}
					}
					request.setAttribute("storylist", alist);
					request.getRequestDispatcher(
							"/content/photo/.storylist").forward(request,
							response);
				}
				
			}
		} catch (Exception e) {
			PrintWriter p = response.getWriter();
			p.print(e.getLocalizedMessage() + "from liststory or");

			// request.getRequestDispatcher("/content/photo/*.photostory")
			// .forward(request, response);
		}
	}
}