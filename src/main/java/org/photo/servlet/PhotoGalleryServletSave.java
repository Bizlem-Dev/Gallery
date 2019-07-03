package org.photo.servlet;

import java.awt.print.Printable;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.jcr.Node;
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
		@Property(name = "sling.servlet.paths", value = { "/servlet/photo/photoshowsave" }),
		@Property(name = "sling.servlet.resourceTypes", value = "sling/servlet/default"),
		@Property(name = "sling.servlet.extensions", value = { "view", "add",
				"viewstory"

		})

})
@SuppressWarnings("serial")
public class PhotoGalleryServletSave extends SlingAllMethodsServlet {

	/** The repo. */
	@Reference
	private SlingRepository repo;

	/** The service. */
	@Reference
	private PhotoGalleryService service;
	final String VIDEOEXTENSION[] = { ".3g2", ".3gp", ".asf", ".asx",
			".avi", ".flv", ".mov", ".mp4", ".mpg", ".rm", ".swf", ".vob",
			".wmv" };

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.sling.api.servlets.SlingAllMethodsServlet#doPost(org.apache
	 * .sling.api.SlingHttpServletRequest,
	 * org.apache.sling.api.SlingHttpServletResponse)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws ServletException,
			IOException {
		if (request.getRequestPathInfo().getExtension().equals("view")) {
			Session session;
			Node storyNode, storyTitleNode, jcrNode = null, fileNode, propNode;
			PrintWriter out = response.getWriter();
			try {
				String user = request.getParameter("userId");
				session = repo.login(new SimpleCredentials("admin", "admin"
						.toCharArray()));
				String user1 = user.replace("@", "_");

				Node userNode = session.getRootNode().getNode("content");

				if (userNode.hasNode("photo")) {
					storyNode = userNode.getNode("photo");
				} else {
					storyNode = userNode.addNode("photo");
				}
				Date c = new Date();

				String s = c.getTime() + "" + c.getYear() + "" + c.getMonth()
						+ "" + +c.getDate();
				//
				storyTitleNode = storyNode.addNode(s);
				//out.print("----1");
				storyTitleNode.setProperty("title",
						request.getParameter("storytitle"));
				//out.print("----1");
				storyTitleNode.setProperty("mtag",
						request.getParameter("metatag"));
				out.print("----1");
				storyTitleNode.setProperty("metades",
						request.getParameter("metadescription"));
				//out.print("----4");
				String[] descri = request.getParameterValues("picdesc");

				String picDesc = null;
				storyTitleNode.setProperty("ceraterEmail", request.getRemoteUser());
				storyTitleNode.setProperty("privacy", request.getParameter("privacyType"));
				if (request.getParameter("file") != null
						&& !request.getParameter("file").equals("")) {
					RequestParameter[] ap = request.getRequestParameterMap()
							.get("file");
						String fileType="";
					for (int i = 0; i < ap.length; i++) {

						picDesc = c.getDate() + "" + c.getMonth() + ""
								+ c.getYear() + "" + c.getTime();
						String filenam = ap[i].getFileName();
						fileType="";
						try {
							if (ap[i] != null) {
								for (int j = 0; j < VIDEOEXTENSION.length; j++) {
									if (filenam.indexOf(VIDEOEXTENSION[j]) != -1) {
										fileType="video";
									}
								}
								propNode = storyTitleNode.addNode(picDesc + i);
								propNode.setProperty("picdata", descri[i]);
								fileNode = propNode.addNode(picDesc, "nt:file");

								jcrNode = fileNode.addNode("jcr:content",
										"nt:resource");

								jcrNode.setProperty("jcr:data",
										ap[i].getInputStream());

								jcrNode.setProperty("jcr:mimeType",
										"image/jpeg");
								
								propNode.setProperty("fileType", fileType);
								propNode.setProperty("imgPath",
										request.getContextPath()
												+ "/content/photo/" + s + "/"
												+ picDesc + i + "/" + picDesc);
							}
						} catch (Exception e) {
							out.print("</br>");
							out.print(e.getLocalizedMessage()
									+ "Catch Exception");
						}
					}
					if (session.isLive() && session != null) {
						out.print("<br>");
						out.print("inside session-");

						session.save();
					}

					// response.sendRedirect(request.getContextPath()
					// + "/servlet/company/show.view?compN="
					// + request.getParameter("companyName")
					// .replaceAll("\\s+", ""));
					response.sendRedirect(request.getContextPath()
							+ "/servlet/photo/photoshow.viewstory?recentAdd="
							+ s);
				}

			} catch (Exception x) {
				out.print(x.getMessage());
				out.print(x.getCause() + "from WExeptio");

			}
		}

	}

}
