package org.gallery.servlet;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.jcr.api.SlingRepository;
import org.gallery.service.GalleryService;

/**
 * <code>GalleryServlet</code> is a servlet which contains all request and
 * response related to this gallery module.
 * 
 * @author pranav.arya
 */
@Component(immediate = true, metatype = false)
@Service(value = javax.servlet.Servlet.class)
@Properties({
        @Property(name = "service.description", value = "Gallery Show Servlet"),
        @Property(name = "service.vendor", value = "The Apache Software Foundation"),
        @Property(name = "sling.servlet.paths", value = { "/servlet/gallery/show" }),
        @Property(name = "sling.servlet.extensions", value = { "view",
                "upload", "albumCover", "uploadVideo", "deletePhoto",
                "photoView", "albumEdit", "likePhoto", "editComment",
                "deleteComment", "videoGallery", "addPhoto", "saveAlbum",
                "addVideo" })

})
@SuppressWarnings("serial")
public class GalleryServlet extends SlingAllMethodsServlet {

    /** The repo. */
    @Reference
    private SlingRepository repo;

    /** The service. */
    @Reference
    private GalleryService service;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.sling.api.servlets.SlingAllMethodsServlet#doPost(org.apache
     * .sling.api.SlingHttpServletRequest,
     * org.apache.sling.api.SlingHttpServletResponse)
     */
    @Override
    protected void doPost(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException,
            IOException {
        if (request.getRequestPathInfo().getExtension().equals("upload")) {
            service.allFileUpload(request.getRemoteUser().replaceAll("@", "_"),
                    request.getParameter("albumName").replaceAll("\\s+", ""),
                    request);
            response.getOutputStream()
                    .print("{\"jsonrpc\" : \"2.0\", \"result\" : null, \"id\" : \"id\"}");
        } else if (request.getRequestPathInfo().getExtension()
                .equals("uploadVideo")) {
            String category = "";

            if (request.getParameter("videoAddCategory") != null) {
                category = request.getParameter("videoAddCategory");
            } else {
                category = request.getParameter("videoCategory");
            }
            service.videoUpload(request.getRemoteUser().replaceAll("@", "_"),
                    request, category);
            response.getOutputStream()
                    .print("{\"jsonrpc\" : \"2.0\", \"result\" : null, \"id\" : \"id\"}");
            response.sendRedirect(request.getContextPath()
                    + "/servlet/gallery/show.view?userId="
                    + request.getRemoteUser().replaceAll("@", "_"));
        } else if (request.getRequestPathInfo().getExtension()
                .equals("albumCover")) {
            service.makeAlbumCover(request.getParameter("nodeName"),
                    request.getParameter("photoId"));

        } else if (request.getRequestPathInfo().getExtension()
                .equals("likePhoto")) {
            service.photolikeNUnlike(
                    request.getRemoteUser().replaceAll("@", "_"),
                    request.getParameter("albumId"),
                    request.getParameter("photoId"),
                    request.getParameter("userId"));

        } else if (request.getRequestPathInfo().getExtension()
                .equals("commentPhoto")) {
            response.getOutputStream().print(
                    service.photoComment(request.getParameter("userId"),
                            request.getRemoteUser().replaceAll("@", "_"),
                            request.getParameter("albumId"),
                            request.getParameter("comment"),
                            request.getParameter("photoId"),
                            request.getParameter("commentId")));

        } else if (request.getRequestPathInfo().getExtension()
                .equals("editComment")) {
            response.getOutputStream().print(
                    service.photoComment(request.getParameter("userId"),
                            request.getRemoteUser().replaceAll("@", "_"),
                            request.getParameter("albumId"),
                            request.getParameter("comment"),
                            request.getParameter("photoId"),
                            request.getParameter("commentId")));

        } else if (request.getRequestPathInfo().getExtension()
                .equals("deleteComment")) {
            service.deletePhoto("/content/user/"
                    + request.getParameter("userId") + "/media/photo/"
                    + request.getParameter("albumId") + "/"
                    + request.getParameter("photoId") + "/Feature/Comment/"
                    + request.getParameter("commentId"));

        } else if (request.getRequestPathInfo().getExtension()
                .equals("deletePhoto")) {
            service.deletePhoto(request.getParameter("nodeName"));
        } else if (request.getRequestPathInfo().getExtension()
                .equals("saveAlbum")) {

            Object[] parameters = request.getParameterMap().keySet().toArray();
            for (Object parameter : parameters) {
                if (!parameter.toString().equals("userId")
                        && !parameter.toString().equals("albumName")) {
                    String values = request.getParameter(parameter.toString());
                    service.editPhoto(request.getParameter("userId"),
                            request.getParameter("albumName"),
                            parameter.toString(), values, "public");
                }
            }
            response.sendRedirect(request.getContextPath()
                    + "/servlet/gallery/show.photoView?" + "userId="
                    + request.getParameter("userId") + "&album="
                    + request.getParameter("albumName"));
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.sling.api.servlets.SlingSafeMethodsServlet#doGet(org.apache
     * .sling.api.SlingHttpServletRequest,
     * org.apache.sling.api.SlingHttpServletResponse)
     */
    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException,
            IOException {
        if (request.getRequestPathInfo().getExtension().equals("view")) {
            Session session;
            Node userNode;
            try {

                session = repo.login(new SimpleCredentials("admin", "admin"
                        .toCharArray()));
                userNode = session.getNode("/content/user/"
                        + request.getParameter("userId"));
                if (userNode.hasNode("media")) {
                    request.getRequestDispatcher(
                            "/content/user/" + request.getParameter("userId")
                                    + "/media.gallery").forward(request,
                            response);
                } else {
                    request.getRequestDispatcher("/content/gallery/*.gallery")
                            .forward(request, response);
                }
            } catch (Exception e) {
                request.getRequestDispatcher("/content/gallery/*.gallery")
                        .forward(request, response);
            }
        } else if (request.getRequestPathInfo().getExtension()
                .equals("photoView")) {
            request.getRequestDispatcher(
                    "/content/user/" + request.getParameter("userId")
                            + "/media/photo/" + request.getParameter("album")
                            + ".imageGallery").forward(request, response);

        } else if (request.getRequestPathInfo().getExtension()
                .equals("albumEdit")) {
            request.getRequestDispatcher(
                    "/content/user/" + request.getParameter("userId")
                            + "/media/photo/" + request.getParameter("album")
                            + ".editAlbum").forward(request, response);
        } else if (request.getRequestPathInfo().getExtension()
                .equals("viewPhoto")) {
            DateFormat dateFormat = new SimpleDateFormat("MMM d,yyyy HH:mm");
            Date date = new Date();
            request.setAttribute("todayDate", dateFormat.format(date));
            request.getRequestDispatcher(
                    "/content/user/" + request.getParameter("userId")
                            + "/media/photo/" + request.getParameter("album")
                            + ".photoView").forward(request, response);
        } else if (request.getRequestPathInfo().getExtension()
                .equals("videoGallery")) {
            request.getRequestDispatcher(
                    "/content/user/" + request.getParameter("userId")
                            + "/media" + ".videoGallery").forward(request,
                    response);
        } else if (request.getRequestPathInfo().getExtension()
                .equals("addPhoto")) {
            if (request.getParameter("photo").equals("add")) {
                request.setAttribute("albumName", request.getParameter("album"));
                request.getRequestDispatcher("/content/gallery/*.uploadImage")
                        .forward(request, response);
            } else {
                request.getRequestDispatcher("/content/gallery/*.uploadImage")
                        .forward(request, response);
            }

        } else if (request.getRequestPathInfo().getExtension()
                .equals("addVideo")) {

            Session session;
            Node userNode;
            try {

                session = repo.login(new SimpleCredentials("admin", "admin"
                        .toCharArray()));
                userNode = session.getNode("/content/user/"
                        + request.getParameter("userId"));
                if (userNode.hasNode("media")) {
                    request.getRequestDispatcher(
                            "/content/user/" + request.getParameter("userId")
                                    + "/media.videoUpload").forward(request,
                            response);
                } else {
                    request.getRequestDispatcher(
                            "/content/gallery/*.videoUpload").forward(request,
                            response);
                }
            } catch (Exception e) {
                request.getRequestDispatcher("/content/gallery/*.videoUpload")
                        .forward(request, response);
            }
        }else if (request.getRequestPathInfo().getExtension()
                .equals("photogallery")) {
        	request.getRequestDispatcher("/content/gallery/.photogallery")
            .forward(request, response);
        	
        }else if (request.getRequestPathInfo().getExtension()
                .equals("createalbum")) {
        	request.getRequestDispatcher("/content/gallery/.albumcreate")
            .forward(request, response);
        	
        }
    }
}
