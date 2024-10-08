/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.viewer;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.viewer.model.DocumentView;
import org.silverpeas.core.viewer.service.ViewService;
import org.silverpeas.core.viewer.service.ViewerException;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Yohann Chastagnier
 */
@WebService
@Path(DocumentViewResource.PATH)
@Authenticated
public class DocumentViewResource extends AbstractViewResource {

  static final String PATH = "view";

  @Inject
  private ViewService viewService;

  /**
   * Gets the JSON representation of document view information. If it doesn't exist, a 404 HTTP code
   * is returned. If the user isn't authenticated, a 401 HTTP code is returned. If a problem occurs
   * when processing the request, a 503 HTTP code is returned.
   * @param id the identifier of a resource handled into Silverpeas's context.
   * @param type the type of the resource.
   * @param language the language used to select the content to view.
   * @return the response to the HTTP GET request with the JSON representation of document view
   * information.
   */
  @GET
  @Path("{type}/{id}")
  @Produces(APPLICATION_JSON)
  public DocumentViewEntity getDocumentView(@PathParam("id") final String id,
      @PathParam("type") final String type, @QueryParam("lang") final String language) {
    try {
      final ResourceView resource = getAuthorizedResourceView(id, type, language);
      return asWebEntity(viewService.getDocumentView(resource.getViewerContext()));
    } catch (final ViewerException pe) {
      throw new WebApplicationException(pe, Status.NOT_FOUND);
    }
  }

  /**
   * Converts the document view into its corresponding web entity.
   * @param documentView the view to convert.
   * @return the corresponding view entity.
   */
  protected DocumentViewEntity asWebEntity(final DocumentView documentView) {
    return DocumentViewEntity.createFrom(documentView).withURI(getUri().getRequestUri());
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }
}
