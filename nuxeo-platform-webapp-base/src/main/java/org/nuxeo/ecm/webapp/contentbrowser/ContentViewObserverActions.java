/*
 * (C) Copyright 2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Anahide Tchertchian
 */
package org.nuxeo.ecm.webapp.contentbrowser;

import static org.jboss.seam.ScopeType.CONVERSATION;

import java.io.Serializable;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.nuxeo.ecm.platform.contentview.seam.ContentViewActions;
import org.nuxeo.ecm.webapp.helpers.EventNames;

/**
 * Handles observers for refresh and reset of content views.
 *
 * @author Anahide Tchertchian
 * @since 5.4
 */
@Name("contentViewObserverActions")
@Scope(org.jboss.seam.ScopeType.EVENT)
public class ContentViewObserverActions implements Serializable {

    private static final long serialVersionUID = 1L;

    @In(create = true)
    protected ContentViewActions contentViewActions;

    /**
     * Refreshes and resets content views that have declared event
     * {@link EventNames#DOCUMENT_CHANGED} as a refresh/reset event.
     */
    @Observer(value = { EventNames.DOCUMENT_CHANGED })
    public void onDocumentChanged() {
        contentViewActions.refreshOnSeamEvent(EventNames.DOCUMENT_CHANGED);
        contentViewActions.resetPageProviderOnSeamEvent(EventNames.DOCUMENT_CHANGED);
    }

    /**
     * Refreshes and resets content views that have declared event
     * {@link EventNames#DOCUMENT_CHILDREN_CHANGED} as a refresh/reset event.
     */
    @Observer(value = { EventNames.DOCUMENT_CHILDREN_CHANGED })
    public void onDocumentChildrenChanged() {
        contentViewActions.refreshOnSeamEvent(EventNames.DOCUMENT_CHILDREN_CHANGED);
        contentViewActions.resetPageProviderOnSeamEvent(EventNames.DOCUMENT_CHILDREN_CHANGED);
    }

    /**
     * Refreshes and resets content views that have declared event
     * {@link EventNames#DOCUMENT_PUBLICATION_REJECTED} as a refresh/reset
     * event.
     *
     * @since 5.6
     */
    @Observer(value = { EventNames.DOCUMENT_PUBLICATION_REJECTED })
    public void onDocumentPublicationRejected() {
        contentViewActions.refreshOnSeamEvent(EventNames.DOCUMENT_PUBLICATION_REJECTED);
        contentViewActions.resetPageProviderOnSeamEvent(EventNames.DOCUMENT_PUBLICATION_REJECTED);
    }

    /**
     * Resets all caches on {@link EventNames#FLUSH_EVENT}, triggered by hot
     * reload when dev mode is set.
     *
     * @since 5.6
     */
    @Observer(value = { EventNames.FLUSH_EVENT }, create = true)
    public void onHotReloadFlush() {
        contentViewActions.resetAll();
    }

}
