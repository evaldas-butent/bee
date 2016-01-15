package com.butent.bee.server.rest;

import com.google.common.net.UrlEscapers;

import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.modules.administration.FileStorageBean;
import com.butent.bee.server.rest.annotations.Authorized;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ejb.EJB;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@ApplicationPath(AdministrationConstants.FILE_URL)
@Path("/")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
@Authorized
public class FileServiceApplication extends Application {

  private static BeeLogger logger = LogUtils.getLogger(FileServiceApplication.class);

  @EJB
  FileStorageBean fs;

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<>();

    classes.add(AuthenticationFilter.class);
    classes.add(this.getClass());

    return classes;
  }

  @GET
  @Path("{id:\\d+}")
  public Response getFile(@PathParam("id") Long fileId) {
    return getFile(fileId, null);
  }

  @GET
  @Path("{id:\\d+}/{name}")
  public Response getFile(@PathParam("id") Long fileId, @PathParam("name") String fileName) {
    FileInfo fileInfo;

    try {
      fileInfo = fs.getFile(fileId);
      fileInfo.setCaption(fileName);
    } catch (IOException e) {
      logger.error(e);
      throw new InternalServerErrorException(e);
    }
    return response(fileInfo);
  }

  @GET
  @Path("{name}/{path}")
  public Response getFile(@PathParam("name") String fileName, @PathParam("path") String filePath) {
    String path = Codec.decodeBase64(filePath);

    if (!FileUtils.isInputFile(path)) {
      throw new NotFoundException(path);
    }
    FileInfo fileInfo = new FileInfo(null, fileName, new File(path).length(),
        URLConnection.guessContentTypeFromName(fileName));
    fileInfo.setPath(path);
    fileInfo.setTemporary(true);

    return response(fileInfo);
  }

  @GET
  @Path("{name}")
  public Response getFiles(@PathParam("name") String fileName,
      @QueryParam(Service.VAR_FILES) String files) {

    if (BeeUtils.isEmpty(files)) {
      throw new BadRequestException();
    }
    Map<String, String> fileMap = Codec.deserializeMap(Codec.decodeBase64(files));
    File tmp;

    try {
      tmp = File.createTempFile("bee_", ".zip");
      tmp.deleteOnExit();

      try (
          ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmp))
      ) {
        Set<String> names = new HashSet<>();

        for (Map.Entry<String, String> entry : fileMap.entrySet()) {
          FileInfo fileInfo = fs.getFile(BeeUtils.toLong(entry.getKey()));
          String name = BeeUtils.notEmpty(entry.getValue(), fileInfo.getName());

          if (!names.add(name)) {
            int idx = name.lastIndexOf(BeeConst.CHAR_POINT);
            String stem;
            String ext;
            int i = 0;

            if (BeeConst.isUndef(idx)) {
              stem = name;
              ext = BeeConst.STRING_EMPTY;
            } else {
              stem = name.substring(0, idx);
              ext = name.substring(idx);
            }
            do {
              name = stem + BeeUtils.parenthesize(++i) + ext;
            } while (!names.add(name));
          }
          ZipEntry ze = new ZipEntry(name);
          zos.putNextEntry(ze);

          Files.copy(fileInfo.getFile().toPath(), zos);

          zos.closeEntry();
          fileInfo.close();
        }
      }
    } catch (IOException e) {
      logger.error(e);
      throw new InternalServerErrorException(e);
    }
    FileInfo fileInfo = new FileInfo(null, fileName, tmp.length(),
        URLConnection.guessContentTypeFromName(fileName));
    fileInfo.setPath(tmp.getAbsolutePath());
    fileInfo.setTemporary(true);

    return response(fileInfo);
  }

  @POST
  @Path("{name}")
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  @Produces(RestResponse.JSON_TYPE)
  public RestResponse upload(@PathParam("name") String fileName, InputStream is) {
    try {
      return RestResponse.ok(fs.storeFile(is, fileName, null));
    } catch (IOException e) {
      return RestResponse.error(e);
    }
  }

  private static Response response(FileInfo fileInfo) {
    StreamingOutput so = new StreamingOutput() {
      @Override
      public void write(OutputStream outputStream) throws WebApplicationException {
        try (
            FileInfo file = fileInfo;
            BufferedOutputStream bus = new BufferedOutputStream(outputStream)
        ) {
          Files.copy(file.getFile().toPath(), bus);
          bus.flush();
        } catch (IOException e) {
          logger.error(e);
        }
      }
    };
    String name = BeeUtils.notEmpty(fileInfo.getCaption(), fileInfo.getName());

    if (!BeeUtils.isEmpty(name)) {
      name = UrlEscapers.urlFragmentEscaper()
          .escape(name.replace(BeeConst.CHAR_COMMA, BeeConst.CHAR_SPACE));
    }
    return Response.ok(so,
        BeeUtils.notEmpty(fileInfo.getType(), MediaType.APPLICATION_OCTET_STREAM))
        .header("Content-Length", fileInfo.getSize())
        .header("Content-Disposition", "inline; filename*=" + name)
        .build();
  }
}
