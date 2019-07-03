package org.photo.service;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;

/**
 * <code>GalleryService</code> is an interface which contains all method
 * declaration related to Image and Video Gallery section.
 * 
 * @author pranav.arya
 * 
 */
public interface PhotoGalleryService {

    /**
     * This method is used to upload all photos. This method can upload multiple
     * files in one request. Cropping of image is also done in this method. This
     * method maintains three size i.e. 150px, 320p and original size. While
     * cropping, image size is also decreased by calling another method.
     * 
     * @param userId
     *            Contains the userId of loggedIn user which is unique
     * @param albumName
     *            Contains the name of album(albumId) for respective user
     * @param request
     *            Contains the HTTP request object which is used to get all the
     *            images from request object
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    void allFileUpload(String userId, String albumName,
            SlingHttpServletRequest request) throws ServletException,
            IOException;

    /**
     * This method is used to upload and convert video. All videos are
     * converted in .webm format. Firstly the video is stored physically
     * with randomly generated number at server
     * side("Tomcat's /temp directory") then it is converted into ".webm"
     * format at server side and then uploaded to sling Repository.
     * Conversion of video is done in another method, this method
     * just only calls the conversion method.
     * 
     * @param userId
     *            Contains the userId of loggedIn user which is unique
     * @param request
     *            Contains the HTTP request object which is used to get video
     *            from request object
     * @param category
     *            Contains the category name that is like album name. Category
     *            is used to categorized the video in specific category for
     *            respective user.
     * @return the string not used till now
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    String videoUpload(String userId, SlingHttpServletRequest request,
            String category) throws ServletException, IOException;

    /**
     * This method is used to delete the photo and videos on the basis of full
     * path of respective node.
     * 
     * @param deletPath
     *            the delete path
     */
    void deletePhoto(String deletePath);

    /**
     * This method is used to declare the cover photo of the album.
     * 
     * @param albumNode
     *            Contains the full path of album Node
     * @param photoId
     *            Contains the photoId of respective album which is declared as
     *            a cover photo of the album
     */
    void makeAlbumCover(String albumNode, String photoId);

    /**
     * Edits the album.
     * 
     * @param userId
     *            Contains the userId of loggedIn user which is unique
     * @param albumName
     *            Contains the albumId for respective user
     * @param name
     *            Contains the album name
     * @param albumDescription
     *            Contain the album description
     * @param albumPrivacy
     *            Contain the album privacy flag i.e. private or public
     */
    void editAlbum(String userId, String albumName, String name,
            String albumDescription, String albumPrivacy);

    /**
     * Edits the photo.
     * 
     * @param userId
     *            Contains the userId of loggedIn user which is unique
     * @param albumName
     *            Contains the albumId for respective user
     * @param photoId
     *            Contains photoId for respective album
     * @param photoDescription
     *            Contains photo description
     * @param photoPrivacy
     *            Contains photo privacy flag i.e. public or private
     */
    void editPhoto(String userId, String albumName, String photoId,
            String photoDescription, String photoPrivacy);

    /**
     * This method contains the functionality for like and unlike on photo.
     * 
     * @param userId
     *            Contains the userId of loggedIn user which is unique
     * @param albumName
     *            Contains the albumId for respective user
     * @param photoId
     *            Contains photoId for respective album
     * @param ownerId
     *            Contains userId for owner of the album or photo
     */
    void photolikeNUnlike(String userId, String albumName, String photoId,
            String ownerId);

    /**
     * This method contains the functionality for commenting on photo. User can
     * also edit his/her previous comment
     * 
     * @param ownerId
     *            Contains userId for owner of the album or photo
     * @param userId
     *            Contains the userId of loggedIn user which is unique
     * @param albumName
     *            Contains the albumId for respective user
     * @param comment
     *            Contains the comment
     * @param photoId
     *            Contains photoId for respective album
     * @param editNode
     *            Contains the CommentId i.e. 1,2,3,4...
     * @return the CommentId
     */
    int photoComment(String ownerId, String userId, String albumName,
            String comment, String photoId, String editNode);

    /**
     * Generate random number.
     * 
     * @return the randomly generated string
     */
    String generateRandomNumber();

    /**
     * Converts the video file. Here Video file is converted in ".webm" format.
     * Source video location is provided as a parameter. This converts the video
     * at the same same location with ".webm" as an extension. For conversion,
     * we have to provide the video codec and audio codec format.
     * 
     * @param fileName
     *            Contains the file name i.e. randomly generated number
     * @param path
     *            Contains the physical path of local server where video is
     *            stored for temporary basis.
     * @return the success or failure flag
     */
    String convert(String fileName, String path);

    /**
     * Generates thumbnail of images .
     * 
     * @param filePath
     *            Contains the physical path of local server where images are
     *            stored for temporary basis.
     * @param filname
     *            Contains the file name i.e. randomly generated number.
     * @param fileData
     *            Contains the image as a stream.
     * @param width
     *            Contains the width of image.
     */
    void generateThumbnail(String filePath, String filname,
            InputStream fileData, int width);
}