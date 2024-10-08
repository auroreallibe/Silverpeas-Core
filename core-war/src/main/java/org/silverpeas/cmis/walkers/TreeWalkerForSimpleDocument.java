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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.cmis.walkers;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PartialContentStreamImpl;
import org.silverpeas.cmis.Filtering;
import org.silverpeas.cmis.Paging;
import org.silverpeas.cmis.util.CmisProperties;
import org.silverpeas.kernel.exception.NotFoundException;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cmis.model.CmisFolder;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.cmis.model.DocumentFile;
import org.silverpeas.core.cmis.model.TypeId;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.Document;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.util.AttachmentSettings;
import org.silverpeas.core.contribution.attachment.webdav.WebdavService;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.i18n.LocalizedResource;
import org.silverpeas.core.io.media.MetaData;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link CmisObjectsTreeWalker} object that knows how to walk down the subtree rooted to an
 * attachment of a contribution in a given Silverpeas application. The attachment is here
 * implemented by the {@link org.silverpeas.core.contribution.attachment.model.SimpleDocument} class
 * that is a localized contribution referring a document file in the filesystem of Silverpeas.
 * The document is expected to be attached either to a folder or to a publication in the CMIS
 * objects tree.
 * @author mmoquillon
 */
@Service
@Singleton
public class TreeWalkerForSimpleDocument extends AbstractCmisObjectsTreeWalker {

  @Inject
  private AttachmentService attachmentService;

  @Inject
  private WebdavService webdavService;

  @Inject
  private OrganizationController organizationController;

  @Inject
  private UserSubscriptionNotificationSendingHandler userNotificationHandler;

  @Override
  protected CmisObject createObjectData(final CmisProperties properties,
      final ContentStream contentStream, final String language) {
    ContentStreamLoader loader = new ContentStreamLoader(properties);
    try {
      File contentFile = loader.loadFile(contentStream);
      SimpleDocument document = createSimpleDocumentFrom(properties, language);
      userNotificationHandler.skipNotificationSend();
      SimpleDocument created =
          attachmentService.createAttachment(document, contentFile, properties.isIndexed(), true);
      if (isVersioningEnabled(created.getInstanceId())) {
        // in the case the application has versioning enabled, then by default checkout the newly
        // created document to pursue its edition.
        attachmentService.lock(created.getId(), created.getCreatedBy(), language);
      }
      return encodeToCmisObject(new Document(created), language);
    } finally {
      loader.deleteFile();
    }
  }

  @Override
  protected ContentStream getContentStream(final LocalizedResource object, final String language,
      final long offset, final long length) {
    User currentUser = User.getCurrentRequester();
    try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
      Document document = (Document) object;
      SimpleDocument translation = document.getTranslation(language);

      if (translation.isEditedBy(currentUser)) {
        webdavService.loadContentInto(translation, buffer);
      } else {
        attachmentService.getBinaryContent(buffer, translation.getPk(), translation.getLanguage(),
            offset, length <= 0 ? -1 : length);
      }
      ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer.toByteArray());

      ContentStreamImpl contentStream;
      if ((offset > 0) || length > 0) {
        contentStream = new PartialContentStreamImpl();
      } else {
        contentStream = new ContentStreamImpl();
      }

      contentStream.setFileName(translation.getFilename());
      contentStream.setLength(BigInteger.valueOf(translation.getSize()));
      contentStream.setMimeType(translation.getContentType());
      contentStream.setStream(inputStream);

      return contentStream;
    } catch (IOException e) {
      throw new CmisStorageException(e.getMessage());
    } catch (NotFoundException e) {
      throw new CmisObjectNotFoundException(e.getMessage());
    }
  }

  @Override
  protected CmisObject updateObjectData(final LocalizedResource object,
      final CmisProperties properties, final ContentStream contentStream, final String language) {
    ContentStreamLoader loader = new ContentStreamLoader(properties);
    try {
      User user = User.getCurrentRequester();
      Document document = (Document) object;
      SimpleDocument translation = document.getTranslation(language);
      if (translation.isReadOnly() ||
          (translation.isVersioned() && !translation.isEditedBy(user))) {
        throw new CmisPermissionDeniedException("The document is read only!");
      }
      File content = loader.loadFile(contentStream);
      SimpleAttachment attachment = translation.getAttachment();
      if (!properties.getContentMimeType().equals(attachment.getContentType())) {
        throw new CmisStreamNotSupportedException(
            "Expected content type " + attachment.getContentType());
      }

      if (content.exists()) {
        setWithFileMetadata(properties, content);

        // we override the title only if set
        if (StringUtil.isDefined(properties.getName())) {
          attachment.setTitle(properties.getName());
        }

        // we override the description only if set
        if (StringUtil.isDefined(properties.getDescription())) {
          attachment.setDescription(properties.getDescription());
        }

        // we override the filename only if set
        if (StringUtil.isDefined(properties.getContentFileName())) {
          attachment.setFilename(properties.getContentFileName());
        }

        attachment.setSize(content.length());
        attachment.setUpdatedBy(User.getCurrentRequester().getId());

        updateDocumentContent(document, translation, content);
      }

      return encodeToCmisObject(getSilverpeasObjectById(document.getIdentifier()
          .asString()), language);
    } finally {
      loader.deleteFile();
    }
  }

  private void updateDocumentContent(final Document document, final SimpleDocument translation,
      final File content) {
    if (document.isVersioned() || document.isEdited()) {
      try (InputStream in = new BufferedInputStream(new FileInputStream(content))) {
        webdavService.updateContentFrom(translation, in);
      } catch (IOException e) {
        throw new CmisStorageException(e.getMessage());
      }
    } else {
      userNotificationHandler.skipNotificationSend();
      attachmentService.updateAttachment(translation, content, true, true);
    }
  }

  private void setWithFileMetadata(final CmisProperties properties, final File content) {
    if (AttachmentSettings.isUseFileMetadataForAttachmentDataEnabled()) {
      MetadataExtractor extractor = MetadataExtractor.get();
      MetaData metadata = extractor.extractMetadata(content);
      if (StringUtil.isDefined(metadata.getTitle())) {
        properties.setName(metadata.getTitle());
      }
      if (StringUtil.isDefined(metadata.getSubject())) {
        properties.setDescription(metadata.getSubject());
      }
    }
  }

  private SimpleDocument createSimpleDocumentFrom(final CmisProperties properties,
      final String language) {
    String lang = I18NHelper.checkLanguage(language);
    File content = new File(properties.getContentPath());
    if (content.exists()) {
      setWithFileMetadata(properties, content);
    }

    ContributionIdentifier parentId = ContributionIdentifier.decode(properties.getParentObjectId());
    SimpleAttachment attachment = SimpleAttachment.builder(lang)
        .setCreationData(User.getCurrentRequester()
            .getId(), new Date())
        .setContentType(properties.getContentMimeType())
        .setFilename(properties.getContentFileName())
        .setTitle(properties.getName())
        .setDescription(properties.getDescription())
        .setSize(content.length())
        .build();
    SimpleDocument document;
    if (isVersioningEnabled(parentId.getComponentInstanceId())) {
      document = new HistorisedDocument();
      document.setPublicDocument(false);
    } else {
      document = new SimpleDocument();
    }

    document.setPK(
        new SimpleDocumentPK(ResourceReference.UNKNOWN_ID, parentId.getComponentInstanceId()));
    document.setForeignId(parentId.getLocalId());
    document.setDocumentType(DocumentType.attachment);
    document.setOrder(0);

    document.setAttachment(attachment);
    return document;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Document getSilverpeasObjectById(final String objectId) {
    try {
      ContributionIdentifier id = ContributionIdentifier.decode(objectId);
      return new Document(id);
    } catch (NotFoundException e) {
      throw new CmisObjectNotFoundException(e.getMessage());
    }
  }

  @Override
  protected Stream<LocalizedResource> getAllowedChildrenOfSilverpeasObject(
      final ResourceIdentifier parentId, final User user) {
    return Stream.empty();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected DocumentFile encodeToCmisObject(final LocalizedResource resource,
      final String language) {
    Document document = (Document) resource;
    SimpleDocument translation = document.getTranslation(language);
    LocalizedResource parent =
        findParentContribution(translation.getForeignId(), translation.getInstanceId());
    return getObjectFactory().createDocument(translation, parent.getIdentifier());
  }

  @Override
  protected boolean isObjectSupported(final String objectId) {
    try {
      return SimpleDocument.isASimpleDocument(ContributionIdentifier.decode(objectId));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  protected boolean isTypeSupported(final TypeId typeId) {
    return typeId == TypeId.SILVERPEAS_DOCUMENT;
  }

  @Override
  protected List<ObjectInFolderContainer> browseObjectsInFolderTree(final LocalizedResource object,
      final Filtering filtering, final long depth) {
    return Collections.emptyList();
  }

  @Override
  protected ObjectInFolderList browseObjectsInFolder(final LocalizedResource object,
      final Filtering filtering, final Paging paging) {
    ObjectInFolderListImpl folderList = new ObjectInFolderListImpl();
    folderList.setObjects(Collections.emptyList());
    folderList.setNumItems(BigInteger.ZERO);
    folderList.setHasMoreItems(false);
    return folderList;
  }

  @Override
  protected List<ObjectParentData> browseParentsOfObject(final LocalizedResource object,
      final Filtering filtering) {
    Document document =
        (Document) object;
    String language = filtering.getLanguage();
    ContributionIdentifier parentId = document.getSourceContribution();
    LocalizedResource parent =
        findParentContribution(parentId.getLocalId(), parentId.getComponentInstanceId());
    CmisFolder cmisParent = getCmisObject(parent, language);
    CmisObject cmisObject = encodeToCmisObject(document, language);
    ObjectParentData parentData = buildObjectParentData(cmisParent, cmisObject, filtering);
    return Collections.singletonList(parentData);
  }

  private LocalizedResource findParentContribution(final String id, final String instanceId) {
    String parentId = ContributionIdentifier.from(instanceId, id, PublicationDetail.TYPE)
        .asString();
    return getTreeWalkerSelector().selectByObjectIdOrFail(parentId)
        .getSilverpeasObjectById(parentId);
  }

  private boolean isVersioningEnabled(final String instanceId) {
    return StringUtil.getBooleanValue(organizationController.getComponentParameterValue(instanceId,
        AttachmentService.VERSION_MODE)) && !StringUtil.getBooleanValue(
        organizationController.getComponentParameterValue(instanceId, "publicationAlwaysVisible"));
  }
}
  