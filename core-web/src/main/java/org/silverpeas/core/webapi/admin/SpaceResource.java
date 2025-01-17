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
package org.silverpeas.core.webapi.admin;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.admin.component.model.PersonalComponent;
import org.silverpeas.core.admin.component.model.PersonalComponentInstance;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.SpaceProfile;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.SpaceProfileInst;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;
import org.silverpeas.core.webapi.profile.ProfileResourceBaseURIs;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.*;
import java.util.stream.Stream;

import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.silverpeas.core.webapi.admin.AdminResourceURIs.*;

/**
 * A REST Web resource giving space data.
 * @author Yohann Chastagnier
 */
@WebService
@Path(SPACES_BASE_URI)
@Authenticated
public class SpaceResource extends AbstractAdminResource {

  @Override
  protected String getResourceBasePath() {
    return SPACES_BASE_URI;
  }

  /**
   * Gets the JSON representation of root spaces.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authenticated, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param forceGettingFavorite forcing the user favorite space search even if the favorite
   * feature is disabled.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         space.
   */
  @GET
  @Produces(APPLICATION_JSON)
  public Collection<SpaceEntity> getAll(
      @QueryParam(FORCE_GETTING_FAVORITE_PARAM) final boolean forceGettingFavorite) {
    try {
      return asWebEntities(loadSpaces(OrganizationController.get().getAllRootSpaceIds()),
          forceGettingFavorite);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of the given existing space.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authenticated, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @param forceGettingFavorite forcing the user favorite space search even if the favorite
   * feature is disabled.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         space.
   */
  @GET
  @Path("{spaceId}")
  @Produces(APPLICATION_JSON)
  public SpaceEntity get(@PathParam("spaceId") final String spaceId,
      @QueryParam(FORCE_GETTING_FAVORITE_PARAM) final boolean forceGettingFavorite) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);
      return asWebEntity(loadSpace(spaceId), forceGettingFavorite);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets users and groups roles indexed by role names.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authenticated, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @param roles aimed roles (each one separated by comma). If empty, all roles are returned.
   * @return the JSON response to the HTTP GET request.
   */
  @GET
  @Path("{spaceId}/" + USERS_AND_GROUPS_ROLES_URI_PART)
  @Produces(APPLICATION_JSON)
  public Map<SilverpeasRole, UsersAndGroupsRoleEntity> getUsersAndGroupsRoles(
      @PathParam("spaceId") final String spaceId, @QueryParam(ROLES_PARAM) final String roles) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);
      return StringUtil.isDefined(roles) ?
          getUsersInSpacePerPlayedRoles(spaceId,
              Stream.of(StringUtils.split(roles, ","))
                  .map(SilverpeasRole::fromString)
                  .filter(not(isEqual(SilverpeasRole.NONE)))) :
          getAllUsersInSpacePerRoles(spaceId);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  private Map<SilverpeasRole, UsersAndGroupsRoleEntity> getAllUsersInSpacePerRoles(String spaceId) {
    var spaceInst = getOrganisationController().getSpaceInstById(spaceId);
    if (spaceInst == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    Stream<SilverpeasRole> roles = spaceInst.getAllSpaceProfilesInst().stream()
        .map(SpaceProfileInst::getName)
        .map(SilverpeasRole::fromString)
        .filter(not(isEqual(SilverpeasRole.NONE)));

    return getUsersInSpacePerPlayedRoles(spaceId, roles);
  }

  private Map<SilverpeasRole, UsersAndGroupsRoleEntity> getUsersInSpacePerPlayedRoles(String spaceId,
      Stream<SilverpeasRole> roles) {
    LocalizationBundle resource = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle",
        getUserPreferences().getLanguage());
    Map<SilverpeasRole, UsersAndGroupsRoleEntity> result = new LinkedHashMap<>();
    roles.forEach(role -> {
      UsersAndGroupsRoleEntity roleEntity = result.computeIfAbsent(role, r ->
        UsersAndGroupsRoleEntity
            .createFrom(role, resource.getString("JSPP." + role.getName()))
            .withURI(getUri().getWebResourcePathBuilder()
              .path(spaceId)
              .path(USERS_AND_GROUPS_ROLES_URI_PART)
              .queryParam("roles", role.getName())
              .build())
            .withParentURI(getUri().getWebResourcePathBuilder().path(spaceId).build()));
      SpaceProfile profile = getOrganisationController().getSpaceProfile(spaceId, role);
      if (profile == null) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
      profile.getAllUserIds().forEach(uid ->
        roleEntity.addUser(getUri().getBaseUriBuilder()
            .path(ProfileResourceBaseURIs.USERS_BASE_URI)
            .path(uid)
            .build())
      );
      profile.getAllGroupIds().forEach(gid ->
          roleEntity.addGroup(getUri().getBaseUriBuilder()
              .path(ProfileResourceBaseURIs.GROUPS_BASE_URI)
              .path(gid)
              .build()));
    });
    return result;
  }

  /**
   * Updates the space data from its JSON representation and returns it once updated.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authenticated, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @param spaceEntity space entity to update.
   * @return the response to the HTTP PUT request with the JSON representation of the updated
   *         space.
   */
  @PUT
  @Path("{spaceId}")
  @Produces(APPLICATION_JSON)
  @Consumes(APPLICATION_JSON)
  public SpaceEntity update(@PathParam("spaceId") final String spaceId,
      final SpaceEntity spaceEntity) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);

      // Old space entity data
      final SpaceEntity oldSpaceEntity = get(spaceId, true);

      // Favorite data change
      if (!oldSpaceEntity.getFavorite().equals(spaceEntity.getFavorite())) {
        // Updating space favorite
        if (spaceEntity.getFavorite().equals(String.valueOf(Boolean.TRUE))) {
          getLookDelegate().addToUserFavorites(loadSpace(spaceId));
        } else if (spaceEntity.getFavorite().equals(String.valueOf(Boolean.FALSE))) {
          getLookDelegate().removeFromUserFavorites(loadSpace(spaceId));
        }
      }

      // Space entity reloading
      return asWebEntity(loadSpace(spaceId), true);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of spaces of the given existing space.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authenticated, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @param forceGettingFavorite forcing the user favorite space search even if the favorite
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         space.
   */
  @GET
  @Path("{spaceId}/" + SPACES_SPACES_URI_PART)
  @Produces(APPLICATION_JSON)
  public Collection<SpaceEntity> getSpaces(@PathParam("spaceId") final String spaceId,
      @QueryParam(FORCE_GETTING_FAVORITE_PARAM) final boolean forceGettingFavorite) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);
      return asWebEntities(loadSpaces(orgaController.getAllSubSpaceIds(spaceId)),
          forceGettingFavorite);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of components of the given existing space.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authenticated, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         space.
   */
  @GET
  @Path("{spaceId}/" + SPACES_COMPONENTS_URI_PART)
  @Produces(APPLICATION_JSON)
  public Collection<ComponentEntity> getComponents(@PathParam("spaceId") final String spaceId) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);
      return asWebEntities(loadComponents(orgaController.getAllComponentIds(spaceId)));
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of content of the given existing space.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authenticated, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         space.
   */
  @GET
  @Path("{spaceId}/" + SPACES_CONTENT_URI_PART)
  @Produces(APPLICATION_JSON)
  public Response getContent(@PathParam("spaceId") final String spaceId,
      @QueryParam(FORCE_GETTING_FAVORITE_PARAM) final boolean forceGettingFavorite) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);
      final List<AbstractTypeEntity> content = new ArrayList<>();
      content.addAll(getSpaces(spaceId, forceGettingFavorite));
      content.addAll(getComponents(spaceId));
      return Response.ok(content).build();
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of the given existing space.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authenticated, a 401 HTTP code is returned.
   * If the user isn't authorized to access the space, a 403 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param spaceId the id of space to process.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         space.
   */
  @GET
  @Path("{spaceId}/" + SPACES_APPEARANCE_URI_PART)
  @Produces(APPLICATION_JSON)
  public SpaceAppearanceEntity getAppearance(@PathParam("spaceId") final String spaceId) {
    try {
      verifyUserAuthorizedToAccessSpace(spaceId);
      final SpaceInstLight space = loadSpace(spaceId);
      return asWebEntity(space, getLookDelegate().getLook(space),
          getLookDelegate().getWallpaper(space), getLookDelegate().getCSS(space));
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of the content of user's personal space.
   * When all query parameters are set at false then the service understands that it has to return
   * all personal entities.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authenticated, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param getNotUsedComponents boolean indicating if the not used components are concerned
   * @param getUsedComponents boolean indicating if the used components are concerned
   * @param getUsedTools a boolean indicating if the used tools are concerned
   * @return the response to the HTTP GET request with the JSON representation of the asked space
   */
  @GET
  @Path(SPACES_PERSONAL_URI_PART)
  @Produces(APPLICATION_JSON)
  public Response getPersonals(
      @QueryParam(GET_NOT_USED_COMPONENTS_PARAM) final boolean getNotUsedComponents,
      @QueryParam(GET_USED_COMPONENTS_PARAM) final boolean getUsedComponents,
      @QueryParam(GET_USED_TOOLS_PARAM) final boolean getUsedTools) {
    try {

      // When all query parameters are set at false then the service understands that it has to
      // return all personal entities
      final boolean getAll = !getNotUsedComponents && !getUsedComponents && !getUsedTools;

      final Collection<AbstractPersonnalEntity> personals = new ArrayList<>();

      if (getAll) {
        PersonalComponent.getAll().stream()
            .map(p -> PersonalComponentInstance.from(getUser(), p))
            .map(this::asWebPersonalEntity)
            .forEach(personals::add);
      }
      if (getAll || getNotUsedComponents) {
        personals.addAll(asWebPersonalEntities(getAdminPersonalDelegate().getNotUsedComponents()));
      }
      if (getAll || getUsedComponents) {
        personals.addAll(asWebPersonalEntities(getAdminPersonalDelegate().getUsedComponents()));
      }
      if (getAll || getUsedTools) {
        personals.addAll(asWebPersonalEntities(getAdminPersonalDelegate().getUsedTools()));
      }
      return Response.ok(personals).build();
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Instantiates the requested component in the user's personal space. It returns the JSON
   * representation of the instantiated component.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authenticated, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param componentName the name of component to add in the user's personal space
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         space.
   */
  @PUT
  @Path(SPACES_PERSONAL_URI_PART + "/{componentName}")
  @Produces(APPLICATION_JSON)
  public PersonalComponentEntity useComponent(
      @PathParam("componentName") final String componentName) {
    try {
      return asWebPersonalEntity(getAdminPersonalDelegate().useComponent(componentName));
    } catch (final AdminException ex) {
      SilverLogger.getLogger(this)
          .error("{0} is already instantiated into personal space", componentName);
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Removes from the user's personal space the instantiation of the requested component. It
   * returns
   * the JSON representation of WAComponent.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authenticated, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param componentName the name of component to add in the user's personal space
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         space.
   */
  @DELETE
  @Path(SPACES_PERSONAL_URI_PART + "/{componentName}")
  @Produces(APPLICATION_JSON)
  public PersonalComponentEntity discardComponent(
      @PathParam("componentName") final String componentName) {
    try {
      return asWebPersonalEntity(getAdminPersonalDelegate().discardComponent(componentName));
    } catch (final AdminException ex) {
      SilverLogger.getLogger(this)
          .error("{0} is not instantiated into personal space", componentName);
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.RESTWebService#getComponentId()
   */
  @Override
  public String getComponentId() {
    // No linked component identifier for spaces...
    return null;
  }
}
