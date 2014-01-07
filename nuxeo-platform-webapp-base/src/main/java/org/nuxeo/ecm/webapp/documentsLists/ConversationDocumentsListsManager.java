/*
 * (C) Copyright 2006-2012 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo
 *     Antoine Taillefer
 */

package org.nuxeo.ecm.webapp.documentsLists;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.webapp.helpers.EventNames;

@Name("conversationDocumentsListsManager")
@Scope(org.jboss.seam.ScopeType.EVENT)
public class ConversationDocumentsListsManager extends
        BaseDocumentsListsManager implements Serializable {

    private static final long serialVersionUID = 9876098763432L;

    private Boolean initialized = false;

    private DocumentRef lastDocumentRef;

    @Override
    protected void notifyListUpdated(String listName) {
        Events.instance().raiseEvent(listName + "Updated");
    }

    @Create
    public void initListManager() {
        if (!initialized) {
            List<String> listContribNames = getService().getDocumentsListDescriptorsName();
            for (String listName : listContribNames) {
                DocumentsListDescriptor desc = getService().getDocumentsListDescriptor(
                        listName);
                if (!desc.getIsSession()) {
                    createWorkingList(listName, desc);
                }
            }
            initialized = true;
        }
    }

    // Event listeners
    @Observer(value = { EventNames.FOLDERISHDOCUMENT_SELECTION_CHANGED }, create = false)
    public void refreshLists(DocumentModel selectedDocument) {

        if (selectedDocument != null) {
            refreshLists(EventNames.FOLDERISHDOCUMENT_SELECTION_CHANGED,
                    selectedDocument);
        }
    }

    /**
     * @since 5.6
     */
    @Observer(value = { EventNames.DOCUMENT_SELECTION_CHANGED }, create = false)
    public void refreshListsOnDocumentSelectionChanged(
            DocumentModel selectedDocument) {

        if (selectedDocument != null) {
            refreshLists(EventNames.DOCUMENT_SELECTION_CHANGED,
                    selectedDocument);
        }
    }

    /**
     * @since 5.6
     */
    public void refreshLists(String eventName, DocumentModel selectedDocument) {

        if (lastDocumentRef != null
                && lastDocumentRef.equals(selectedDocument.getRef())) {
            return;
        }

        if (!documentsLists_events.containsKey(eventName)) {
            return;
        }
        for (String listName : documentsLists_events.get(eventName)) {

            List<DocumentModel> docList = documentsLists.get(listName);
            if (!docList.isEmpty()) {
                docList.clear();
                notifyListUpdated(listName);
            }
        }

        lastDocumentRef = selectedDocument.getRef();
    }

    /**
     * Refresh lists when a search is performed
     */
    @Observer(value = { EventNames.SEARCH_PERFORMED }, create = false)
    public void refreshListsOnSearch() {
        if (!documentsLists_events.containsKey(EventNames.SEARCH_PERFORMED)) {
            return;
        }
        for (String listName : documentsLists_events.get(EventNames.SEARCH_PERFORMED)) {
            List<DocumentModel> docList = documentsLists.get(listName);
            if (!docList.isEmpty()) {
                docList.clear();
                notifyListUpdated(listName);
            }
        }
    }

}
