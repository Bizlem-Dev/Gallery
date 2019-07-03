package org.photo.service.impl;

import org.convertor.impl.AudioAttributes;
import org.convertor.impl.Encoder;
import org.convertor.impl.EncoderException;
import org.convertor.impl.EncodingAttributes;
import org.convertor.impl.InputFormatException;
import org.convertor.impl.VideoAttributes;
import org.convertor.impl.VideoSize;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.jcr.api.SlingRepository;
import org.photo.service.PhotoGalleryService;

/**
 * <code>GalleryServiceImpl</code> contains all the implemented method related
 * to Image and Video Gallery section. Image uploading and resizing is done in
 * this module. Video uploading and conversion in particular format is also done
 * in this module. Image is uploaded album wise and video is uploaded category
 * wise for respective user. Media node in respective user node contains all
 * images and video.
 * 
 * @author atul
 */
@Component(configurationFactory = true)
@Service(PhotoGalleryService.class)
@Properties({ @Property(name = "PhotoGalleryService", value = "gallery") })
public class PhotoGalleryServiceImpl implements PhotoGalleryService {

    /** The repo variable is an object of SlingRepository interface. */
    @Reference
    private SlingRepository repo;

    // private int maxFileSize = 5000 * 1024;

    /*
     * (non-Javadoc)
     * 
     * @see org.gallery.service.GalleryService#videoUpload(java.lang.String,
     * org.apache.sling.api.SlingHttpServletRequest, java.lang.String)
     */
    @SuppressWarnings({ "deprecation" })
    public String videoUpload(String userId, SlingHttpServletRequest request,
            String category) throws ServletException, IOException {
        String path = request.getSession().getServletContext().getRealPath("/")
                + "/temp/";
        Session session;
        Node filePathNode, fileNode, jcrNode, userNode, mediaNode, videoNode,
        categoryNode = null;
        String randomNumber = generateRandomNumber();
        File files = new File(path);
        if (!files.exists()) {
            files.mkdirs();
        }
        try {
            byte[] data = request.getParameter("myFile").getBytes("ISO-8859-1");
            if (data.length > 0) {
                File file = new File(path, randomNumber);
                FileOutputStream fout = new FileOutputStream(file);
                fout.write(data);
                fout.close();

            }
            String response = convert(randomNumber, path);
            if (response.equals("success")) {
                session = repo.login(new SimpleCredentials("admin", "admin"
                        .toCharArray()));

                userNode = session.getNode("/content/user/" + userId);
                if (userNode.hasNode("media")) {
                    mediaNode = userNode.getNode("media");
                } else {
                    mediaNode = userNode.addNode("media");
                }
                if (mediaNode.hasNode("video")) {
                    videoNode = mediaNode.getNode("video");
                } else {
                    videoNode = mediaNode.addNode("video");
                    videoNode.setProperty("videoNumber", 0);
                }
                if (videoNode.hasNode(category)) {
                    categoryNode = videoNode.getNode(category.replaceAll(
                            "\\s+", ""));
                } else {
                    categoryNode = videoNode.addNode(category.replaceAll(
                            "\\s+", ""));
                    categoryNode.setProperty("categoryName", category);
                }

                filePathNode = categoryNode.addNode(randomNumber);
                fileNode = filePathNode.addNode(randomNumber + ".webm",
                        "nt:file");
                jcrNode = fileNode.addNode("jcr:content", "nt:resource");

                jcrNode.setProperty("jcr:data", new FileInputStream(path
                        + randomNumber + ".webm"));
                jcrNode.setProperty("jcr:lastModified", Calendar.getInstance());
                jcrNode.setProperty("jcr:mimeType", "video/webm");
                filePathNode.setProperty("videoName",
                        request.getParameter("videoTitle"));
                filePathNode.setProperty("videoDesc",
                        request.getParameter("videoDesc"));
                filePathNode.setProperty("videoCategory", category);
                videoNode.setProperty("videoNumber",
                        videoNode.getProperty("videoNumber").getLong() + 1);
                session.save();
            }
        } catch (LoginException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        } finally {
            File file = new File(path + randomNumber + ".webm");
            file.delete();
            File file2 = new File(path + randomNumber + "");
            file2.delete();
        }
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gallery.service.GalleryService#allFileUpload(java.lang.String,
     * java.lang.String, org.apache.sling.api.SlingHttpServletRequest)
     */
    @SuppressWarnings("deprecation")
    public void allFileUpload(String userId, String albumName,
            SlingHttpServletRequest request) throws ServletException,
            IOException {

        Session session;
        Node userNode, mediaNode, photoNode, albumNode, fileNode,
        thumbnalNode, thumbnalNodeJcrNode, picNode, thumbnalNode2,
        thumbnalNodeJcrNode2, jcrNode = null;
        String path = request.getSession().getServletContext().getRealPath("/")
                + "/temp/";
        String album = albumName.replaceAll("\\s+", "").trim();
        DateFormat dateFormat = new SimpleDateFormat("MMM d,yyyy HH:mm");
        Date date = new Date();
        try {

            session = repo.login(new SimpleCredentials("admin", "admin"
                    .toCharArray()));

            userNode = session.getNode("/content/user/" + userId);
            if (userNode.hasNode("media")) {
                mediaNode = userNode.getNode("media");
            } else {
                mediaNode = userNode.addNode("media");
                mediaNode.setProperty("sling:resourceType", "gallery");
            }
            if (mediaNode.hasNode("photo")) {
                photoNode = mediaNode.getNode("photo");
            } else {
                photoNode = mediaNode.addNode("photo");
            }
            if (photoNode.hasNode(album)) {
                albumNode = photoNode.getNode(album);
            } else {
                albumNode = photoNode.addNode(album);
                albumNode.setProperty("sling:resourceType", "gallery");
                albumNode.setProperty("albumName", albumName);
            }

            String mimeType = "";

            for (Entry<String, RequestParameter[]> e : request
                    .getRequestParameterMap().entrySet()) {
                for (RequestParameter p : e.getValue()) {
                    if (!p.isFormField()) {
                        mimeType = p.getContentType();
                        if (mimeType == null) {
                            mimeType = "application/octet-stream";
                        }
                        String randomNumber = generateRandomNumber();
                        fileNode = albumNode.addNode(randomNumber);
                        fileNode.setProperty("photoDate",
                                dateFormat.format(date));
                        picNode = fileNode.addNode("xOp", "nt:file");
                        jcrNode = picNode.addNode("jcr:content", "nt:resource");

                        jcrNode.setProperty("jcr:data", p.getInputStream());
                        jcrNode.setProperty("jcr:lastModified",
                                Calendar.getInstance());
                        jcrNode.setProperty("jcr:mimeType", mimeType);
                        generateThumbnail(path, randomNumber,
                                p.getInputStream(), 320);
                        File fileThumbnail = new File(path + randomNumber
                                + ".jpg");
                        InputStream thumbnailStream = new FileInputStream(
                                fileThumbnail);
                        thumbnalNode = fileNode.addNode("x320", "nt:file");
                        thumbnalNodeJcrNode = thumbnalNode.addNode(
                                "jcr:content", "nt:resource");

                        thumbnalNodeJcrNode.setProperty("jcr:data",
                                thumbnailStream);
                        thumbnalNodeJcrNode.setProperty("jcr:lastModified",
                                Calendar.getInstance());
                        thumbnalNodeJcrNode.setProperty("jcr:mimeType",
                                mimeType);
                        fileThumbnail.delete();
                        generateThumbnail(path, randomNumber,
                                p.getInputStream(), 150);
                        File fileThumbnail2 = new File(path + randomNumber
                                + ".jpg");
                        InputStream thumbnailStream2 = new FileInputStream(
                                fileThumbnail2);
                        thumbnalNode2 = fileNode.addNode("x150", "nt:file");
                        thumbnalNodeJcrNode2 = thumbnalNode2.addNode(
                                "jcr:content", "nt:resource");

                        thumbnalNodeJcrNode2.setProperty("jcr:data",
                                thumbnailStream2);
                        thumbnalNodeJcrNode2.setProperty("jcr:lastModified",
                                Calendar.getInstance());
                        thumbnalNodeJcrNode2.setProperty("jcr:mimeType",
                                mimeType);
                        fileThumbnail2.delete();
                    }
                }
            }

            session.save();
        } catch (Exception e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gallery.service.GalleryService#generateRandomNumber()
     */
    public String generateRandomNumber() {
        Random rand = new Random();
        long accumulator = 1 + rand.nextInt(9); // ensures that the 16th digit
        // isn't 0
        for (int i = 0; i < 15; i++) {
            accumulator *= 10L;
            accumulator += rand.nextInt(10);
        }
        System.out.println(accumulator);
        return accumulator + "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gallery.service.GalleryService#convert(java.lang.String,
     * java.lang.String)
     */
    public String convert(String fileName, String path) {
        File source = new File(path + fileName);
        File target = new File(path + fileName + ".webm");
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libvorbis");
        audio.setBitRate(new Integer(64000));
        audio.setChannels(new Integer(1));
        audio.setSamplingRate(new Integer(22050));
        VideoAttributes video = new VideoAttributes();
        video.setCodec("libvpx");
        video.setBitRate(new Integer(1080000));
        video.setFrameRate(new Integer(15));
        video.setSize(new VideoSize(800, 500));
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("webm");
        attrs.setAudioAttributes(audio);
        attrs.setVideoAttributes(video);
        Encoder encoder = new Encoder();
        try {
            encoder.encode(source, target, attrs);
            source.delete();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InputFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (EncoderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // target.deleteOnExit();
        return "success";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gallery.service.GalleryService#generateThumbnail(java.lang.String,
     * java.lang.String, java.io.InputStream, int)
     */
    public void generateThumbnail(String filePath, String filname,
            InputStream fileData, int width) {
        try {
            BufferedImage src = ImageIO.read(fileData);
            if (src == null) {
                final StringBuffer sb = new StringBuffer();
                for (String fmt : ImageIO.getReaderFormatNames()) {
                    sb.append(fmt);
                    sb.append(' ');
                }
                throw new IOException(
                        "Unable to read image, registered formats: " + sb);
            }

            final double scale = (double) width / src.getWidth();

            int destWidth = width;
            int destHeight = new Double(src.getHeight() * scale).intValue();
            BufferedImage dest = new BufferedImage(destWidth, destHeight,
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g = dest.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance(
                    (double) destWidth / src.getWidth(), (double) destHeight
                    / src.getHeight());
            g.drawRenderedImage(src, at);
            File files = new File(filePath);
            if (!files.exists()) {
                files.mkdirs();
            }
            File fileThumbnail = new File(filePath + filname + ".jpg");
            ImageIO.write(dest, "jpg", fileThumbnail);
        } catch (Exception e) {

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gallery.service.GalleryService#deletePhoto(java.lang.String)
     */
    public void deletePhoto(String deletPath) {
        Session session;
        Node node = null;
        try {

            session = repo.login(new SimpleCredentials("admin", "admin"
                    .toCharArray()));
            node = session.getNode(deletPath);
            node.remove();
            session.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gallery.service.GalleryService#makeAlbumCover(java.lang.String,
     * java.lang.String)
     */
    public void makeAlbumCover(String albumName, String photoId) {
        Session session;
        Node albumNode = null;
        try {

            session = repo.login(new SimpleCredentials("admin", "admin"
                    .toCharArray()));
            albumNode = session.getNode(albumName);

            albumNode.setProperty("albumCover", photoId);
            session.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gallery.service.GalleryService#editAlbum(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void editAlbum(String userId, String albumName, String name,
            String albumDescription, String albumPrivacy) {
        Session session;
        Node userNode, albumNode = null;
        try {

            session = repo.login(new SimpleCredentials("admin", "admin"
                    .toCharArray()));
            userNode = session.getNode("/content/user/" + userId
                    + "/media/photo");

            if (userNode.hasNode(albumName)) {
                albumNode = userNode.getNode(albumName);
            }

            albumNode.setProperty("albumName", name);
            albumNode.setProperty("albumDescription", albumDescription);
            albumNode.setProperty("albumPrivacy", albumPrivacy);
            session.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gallery.service.GalleryService#editPhoto(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void editPhoto(String userId, String albumName, String photoId,
            String photoDescription, String photoPrivacy) {
        Session session;
        Node userNode, albumNode = null, photoNode = null;
        try {

            session = repo.login(new SimpleCredentials("admin", "admin"
                    .toCharArray()));
            userNode = session.getNode("/content/user/" + userId
                    + "/media/photo");

            if (userNode.hasNode(albumName)) {
                albumNode = userNode.getNode(albumName);
            }
            if (albumNode.hasNode(photoId)) {
                photoNode = albumNode.getNode(photoId);
            }

            photoNode.setProperty("photoDescription", photoDescription);
            if (!photoPrivacy.equals(null)) {
                photoNode.setProperty("photoPrivacy", photoPrivacy);
            }
            session.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gallery.service.GalleryService#photolikeNUnlike(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    public void photolikeNUnlike(String userId, String albumName,
            String photoId, String ownerId) {
        Session session;
        Node userNode, albumNode = null, photoNode = null, featureNode,
                likeNode, node;
        try {

            session = repo.login(new SimpleCredentials("admin", "admin"
                    .toCharArray()));
            userNode = session.getNode("/content/user/" + ownerId
                    + "/media/photo");

            if (userNode.hasNode(albumName)) {
                albumNode = userNode.getNode(albumName);
            }
            if (albumNode.hasNode(photoId)) {
                photoNode = albumNode.getNode(photoId);
            }
            if (photoNode.hasNode("Feature")) {
                featureNode = photoNode.getNode("Feature");
            } else {
                featureNode = photoNode.addNode("Feature");
            }
            if (featureNode.hasNode("Like")) {
                likeNode = featureNode.getNode("Like");
            } else {
                likeNode = featureNode.addNode("Like");
                likeNode.setProperty("likenumber", "0");
            }
            if (likeNode.hasNode(userId)) {
                node = likeNode.getNode(userId);
                node.remove();
                likeNode.setProperty("likenumber",
                        likeNode.getProperty("likenumber").getLong() - 1);
            } else {
                node = likeNode.addNode(userId);
                likeNode.setProperty("likenumber",
                        likeNode.getProperty("likenumber").getLong() + 1);
            }

            session.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gallery.service.GalleryService#photoComment(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String)
     */
    public int photoComment(String ownerId, String userId, String albumName,
            String comment, String photoId, String editNode) {
        Session session;
        Node userNode, albumNode = null, photoNode = null, featureNode,
                commentNode, node;
        DateFormat dateFormat = new SimpleDateFormat("MMM d,yyyy HH:mm");
        Date date = new Date();
        int incrementedNumber = 0;
        try {

            session = repo.login(new SimpleCredentials("admin", "admin"
                    .toCharArray()));
            userNode = session.getNode("/content/user/" + ownerId
                    + "/media/photo");

            if (userNode.hasNode(albumName)) {
                albumNode = userNode.getNode(albumName);
            }
            if (albumNode.hasNode(photoId)) {
                photoNode = albumNode.getNode(photoId);
            }
            if (photoNode.hasNode("Feature")) {
                featureNode = photoNode.getNode("Feature");
            } else {
                featureNode = photoNode.addNode("Feature");
            }
            if (featureNode.hasNode("Comment")) {
                commentNode = featureNode.getNode("Comment");
            } else {
                commentNode = featureNode.addNode("Comment");
                commentNode.setProperty("commentNumber", "0");
            }
            if (commentNode.hasNode(editNode)) {
                node = commentNode.getNode(editNode);
            } else {
                incrementedNumber = (int) (commentNode.getProperty(
                        "commentNumber").getLong() + 1);
                node = commentNode.addNode(incrementedNumber + "");
                commentNode.setProperty("commentNumber", incrementedNumber);
                node.setProperty("commentDate", dateFormat.format(date));
            }
            node.setProperty("userId", userId);
            node.setProperty("comment", comment);

            session.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return incrementedNumber;
    }
}