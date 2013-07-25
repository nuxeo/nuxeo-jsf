/*
 * (C) Copyright 2010 Nuxeo SA (http://nuxeo.com/) and contributors.
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
package org.nuxeo.ecm.platform.contentview.jsf.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.platform.contentview.jsf.ContentView;
import org.nuxeo.ecm.platform.contentview.jsf.ContentViewHeader;
import org.nuxeo.ecm.platform.contentview.jsf.ContentViewLayout;
import org.nuxeo.ecm.platform.contentview.jsf.ContentViewService;
import org.nuxeo.ecm.platform.query.api.PageProvider;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryAndFetchPageProvider;
import org.nuxeo.ecm.platform.query.nxql.CoreQueryDocumentPageProvider;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.NXRuntimeTestCase;

/**
 * @author Anahide Tchertchian
 */
public class TestContentViewService extends NXRuntimeTestCase {

    protected ContentViewService service;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        deployContrib("org.nuxeo.ecm.platform.query.api",
                "OSGI-INF/pageprovider-framework.xml");
        deployContrib("org.nuxeo.ecm.platform.contentview.jsf",
                "OSGI-INF/contentview-framework.xml");
        deployContrib("org.nuxeo.ecm.platform.contentview.jsf.test",
                "test-contentview-contrib.xml");

        service = Framework.getService(ContentViewService.class);
        assertNotNull(service);
    }

    @Test
    public void testRegistration() throws Exception {
        assertNull(service.getContentView("foo"));

        ContentView contentView = service.getContentView("CURRENT_DOCUMENT_CHILDREN");
        assertNotNull(contentView);
        // check content view attributes
        assertEquals("CURRENT_DOCUMENT_CHILDREN", contentView.getName());
        assertEquals("current document children", contentView.getTitle());
        assertFalse(contentView.getTranslateTitle());
        assertEquals("/icons/document_listing_icon.png",
                contentView.getIconPath());
        assertEquals("CURRENT_SELECTION_LIST",
                contentView.getActionsCategories().get(0));
        assertEquals("simple", contentView.getPagination());

        List<ContentViewLayout> resultLayouts = contentView.getResultLayouts();
        assertNotNull(resultLayouts);
        assertEquals(1, resultLayouts.size());
        assertEquals("document_listing", resultLayouts.get(0).getName());
        assertEquals("label.document_listing.layout",
                resultLayouts.get(0).getTitle());
        assertTrue(resultLayouts.get(0).getTranslateTitle());
        assertEquals("/icons/myicon.png", resultLayouts.get(0).getIconPath());
        assertTrue(resultLayouts.get(0).getShowCSVExport());

        assertEquals("search_layout", contentView.getSearchLayout().getName());
        assertFalse(contentView.getSearchLayout().isFilterUnfolded());
        assertNull(contentView.getSearchLayout().getTitle());
        assertFalse(contentView.getSearchLayout().getTranslateTitle());
        assertNull(contentView.getEmptySentence());
        assertFalse(contentView.getTranslateEmptySentence());
        assertFalse(contentView.getShowTitle());
        assertNull(contentView.getSearchLayout().getIconPath());
        assertFalse(contentView.getSearchLayout().getShowCSVExport());
        assertEquals("quick",
                contentView.getSearchLayout().getFilterDisplayType());

        assertEquals("CURRENT_SELECTION", contentView.getSelectionListName());
        List<String> eventNames = contentView.getRefreshEventNames();
        assertNotNull(eventNames);
        assertEquals(1, eventNames.size());
        assertEquals("documentChildrenChanged", eventNames.get(0));
        eventNames = contentView.getResetEventNames();
        assertNotNull(eventNames);
        assertEquals(0, eventNames.size());
        assertFalse(contentView.getUseGlobalPageSize());

        List<String> flags = contentView.getFlags();
        assertNotNull(flags);
        assertEquals(2, flags.size());
        assertEquals("foo", flags.get(0));
        assertEquals("bar", flags.get(1));

        // headers
        ContentViewHeader header = service.getContentViewHeader("foo");
        assertNull(header);
        header = service.getContentViewHeader("CURRENT_DOCUMENT_CHILDREN");
        assertNotNull(header);
        assertEquals("CURRENT_DOCUMENT_CHILDREN", header.getName());
        assertEquals("current document children", header.getTitle());
        assertEquals("/icons/document_listing_icon.png", header.getIconPath());
        assertFalse(header.isTranslateTitle());
    }

    @Test
    public void testOverride() throws Exception {
        ContentView contentView = service.getContentView("CURRENT_DOCUMENT_CHILDREN_FETCH");
        assertNotNull(contentView);
        contentView = service.getContentView("CURRENT_DOCUMENT_CHILDREN");
        assertNotNull(contentView);
        assertTrue(contentView.getShowFilterForm());
        assertFalse(contentView.getShowRefreshCommand());

        deployContrib("org.nuxeo.ecm.platform.contentview.jsf.test",
                "test-contentview-override-contrib.xml");

        // check content view has been disabled correctly
        contentView = service.getContentView("CURRENT_DOCUMENT_CHILDREN_FETCH");
        assertNull(contentView);

        assertNull(service.getContentView("foo"));

        contentView = service.getContentView("CURRENT_DOCUMENT_CHILDREN");
        assertNotNull(contentView);
        // check content view attributes
        assertEquals("CURRENT_DOCUMENT_CHILDREN", contentView.getName());
        assertEquals("current document children overriden",
                contentView.getTitle());
        assertFalse(contentView.getTranslateTitle());
        assertEquals("label.my.empty.cv.sentence",
                contentView.getEmptySentence());
        assertTrue(contentView.getTranslateEmptySentence());
        assertTrue(contentView.getShowTitle());
        assertEquals("/icons/document_listing_icon.png",
                contentView.getIconPath());
        assertEquals("CURRENT_SELECTION_LIST_2",
                contentView.getActionsCategories().get(0));
        assertEquals("simple_2", contentView.getPagination());

        assertTrue(contentView.getShowFilterForm());
        assertFalse(contentView.getShowRefreshCommand());

        List<ContentViewLayout> resultLayouts = contentView.getResultLayouts();
        assertNotNull(resultLayouts);
        assertEquals(2, resultLayouts.size());
        assertEquals("document_listing", resultLayouts.get(0).getName());
        assertEquals("label.document_listing.layout",
                resultLayouts.get(0).getTitle());
        assertTrue(resultLayouts.get(0).getTranslateTitle());
        assertEquals("/icons/myicon.png", resultLayouts.get(0).getIconPath());
        assertTrue(resultLayouts.get(0).getShowCSVExport());
        assertEquals("document_listing_2", resultLayouts.get(1).getName());
        assertEquals("label.document_listing.layout_2",
                resultLayouts.get(1).getTitle());
        assertTrue(resultLayouts.get(1).getTranslateTitle());
        assertNull(resultLayouts.get(1).getIconPath());
        assertFalse(resultLayouts.get(1).getShowCSVExport());

        assertEquals("search_layout_2", contentView.getSearchLayout().getName());
        assertNull(contentView.getSearchLayout().getTitle());
        assertFalse(contentView.getSearchLayout().getTranslateTitle());
        assertNull(contentView.getSearchLayout().getIconPath());
        assertFalse(contentView.getSearchLayout().getShowCSVExport());
        assertTrue(contentView.getSearchLayout().isFilterUnfolded());

        assertEquals("CURRENT_SELECTION_2", contentView.getSelectionListName());
        List<String> eventNames = contentView.getRefreshEventNames();
        assertNotNull(eventNames);
        assertEquals(1, eventNames.size());
        assertEquals("documentChildrenChanged", eventNames.get(0));
        eventNames = contentView.getResetEventNames();
        assertNotNull(eventNames);
        assertEquals(0, eventNames.size());
        assertTrue(contentView.getUseGlobalPageSize());

        List<String> flags = contentView.getFlags();
        assertNotNull(flags);
        assertEquals(1, flags.size());
        assertEquals("foo2", flags.get(0));

        // headers
        ContentViewHeader header = service.getContentViewHeader("foo");
        assertNull(header);
        header = service.getContentViewHeader("CURRENT_DOCUMENT_CHILDREN");
        assertNotNull(header);
        assertEquals("CURRENT_DOCUMENT_CHILDREN", header.getName());
        assertEquals("current document children overriden", header.getTitle());
        assertEquals("/icons/document_listing_icon.png", header.getIconPath());
        assertFalse(header.isTranslateTitle());

        // check that result layouts are not emptied after merge (see NXP-9333)
        contentView = service.getContentView("CURRENT_DOCUMENT_CHILDREN_REF");
        assertNotNull(contentView);
        List<ContentViewLayout> rl = contentView.getResultLayouts();
        assertNotNull(rl);
        assertEquals(1, rl.size());
    }

    @Test
    public void testGetContentViewNames() throws Exception {
        Set<String> names = service.getContentViewNames();
        assertNotNull(names);
        assertEquals(10, names.size());
        List<String> orderedNames = new ArrayList<String>();
        orderedNames.addAll(names);
        Collections.sort(orderedNames);
        assertEquals("CURRENT_DOCUMENT_CHILDREN", orderedNames.get(0));
        assertEquals("CURRENT_DOCUMENT_CHILDREN_FETCH", orderedNames.get(1));
        assertEquals("CURRENT_DOCUMENT_CHILDREN_FETCH_REF", orderedNames.get(2));
        assertEquals("CURRENT_DOCUMENT_CHILDREN_REF", orderedNames.get(3));
        assertEquals("CURRENT_DOCUMENT_CHILDREN_WITH_SEARCH_DOCUMENT",
                orderedNames.get(4));
        assertEquals("CURRENT_DOCUMENT_CHILDREN_WITH_SEARCH_DOCUMENT_REF",
                orderedNames.get(5));

        // check after override too
        deployContrib("org.nuxeo.ecm.platform.contentview.jsf.test",
                "test-contentview-override-contrib.xml");

        names = service.getContentViewNames();
        assertNotNull(names);
        assertEquals(9, names.size());
        orderedNames = new ArrayList<String>();
        orderedNames.addAll(names);
        Collections.sort(orderedNames);
        assertEquals("CURRENT_DOCUMENT_CHILDREN", orderedNames.get(0));
        assertEquals("CURRENT_DOCUMENT_CHILDREN_FETCH_REF", orderedNames.get(1));
        assertEquals("CURRENT_DOCUMENT_CHILDREN_REF", orderedNames.get(2));
        assertEquals("CURRENT_DOCUMENT_CHILDREN_WITH_SEARCH_DOCUMENT",
                orderedNames.get(3));
        assertEquals("CURRENT_DOCUMENT_CHILDREN_WITH_SEARCH_DOCUMENT_REF",
                orderedNames.get(4));
    }

    @Test
    public void testGetContentViewHeaders() throws Exception {
        Set<ContentViewHeader> headers = service.getContentViewHeaders();
        assertNotNull(headers);
        assertEquals(10, headers.size());
        List<ContentViewHeader> sortedHeaders = new ArrayList<ContentViewHeader>();
        sortedHeaders.addAll(headers);
        Collections.sort(sortedHeaders);
        assertEquals("CURRENT_DOCUMENT_CHILDREN",
                sortedHeaders.get(0).getName());
        assertEquals("current document children",
                sortedHeaders.get(0).getTitle());
        assertEquals("/icons/document_listing_icon.png",
                sortedHeaders.get(0).getIconPath());
        assertFalse(sortedHeaders.get(0).isTranslateTitle());
        assertEquals("CURRENT_DOCUMENT_CHILDREN_FETCH",
                sortedHeaders.get(1).getName());
        assertEquals("CURRENT_DOCUMENT_CHILDREN_FETCH_REF",
                sortedHeaders.get(2).getName());
        assertEquals("CURRENT_DOCUMENT_CHILDREN_REF",
                sortedHeaders.get(3).getName());
        assertEquals("CURRENT_DOCUMENT_CHILDREN_WITH_SEARCH_DOCUMENT",
                sortedHeaders.get(4).getName());
        assertEquals("CURRENT_DOCUMENT_CHILDREN_WITH_SEARCH_DOCUMENT_REF",
                sortedHeaders.get(5).getName());
    }

    @Test
    public void testGetContentViewByFlag() throws Exception {
        Set<String> names = service.getContentViewNames("foo");
        assertNotNull(names);
        assertEquals(2, names.size());
        List<String> orderedNames = new ArrayList<String>();
        orderedNames.addAll(names);
        Collections.sort(orderedNames);
        assertEquals("CURRENT_DOCUMENT_CHILDREN", orderedNames.get(0));
        assertEquals("CURRENT_DOCUMENT_CHILDREN_FETCH", orderedNames.get(1));

        names = service.getContentViewNames("foo2");
        assertNotNull(names);
        assertEquals(0, names.size());

        names = service.getContentViewNames("bar");
        assertNotNull(names);
        assertEquals(1, names.size());
        orderedNames.clear();
        orderedNames.addAll(names);
        Collections.sort(orderedNames);
        assertEquals("CURRENT_DOCUMENT_CHILDREN", orderedNames.get(0));

        names = service.getContentViewNames("not_set");
        assertNotNull(names);
        assertEquals(0, names.size());

        // check after override too
        deployContrib("org.nuxeo.ecm.platform.contentview.jsf.test",
                "test-contentview-override-contrib.xml");

        names = service.getContentViewNames("foo");
        assertNotNull(names);
        assertEquals(0, names.size());

        names = service.getContentViewNames("foo2");
        assertNotNull(names);
        assertEquals(1, names.size());
        assertEquals("CURRENT_DOCUMENT_CHILDREN", names.iterator().next());

        names = service.getContentViewNames("bar");
        assertNotNull(names);
        assertEquals(0, names.size());

        names = service.getContentViewNames("not_set");
        assertNotNull(names);
        assertEquals(0, names.size());
    }

    @Test
    public void testGetContentViewHeadersByFlag() throws Exception {
        Set<ContentViewHeader> headers = service.getContentViewHeaders("foo");
        assertNotNull(headers);
        assertEquals(2, headers.size());
        List<ContentViewHeader> sortedHeaders = new ArrayList<ContentViewHeader>();
        sortedHeaders.addAll(headers);
        Collections.sort(sortedHeaders);
        assertEquals("CURRENT_DOCUMENT_CHILDREN",
                sortedHeaders.get(0).getName());
        assertEquals("current document children",
                sortedHeaders.get(0).getTitle());
        assertEquals("/icons/document_listing_icon.png",
                sortedHeaders.get(0).getIconPath());
        assertFalse(sortedHeaders.get(0).isTranslateTitle());
        assertEquals("CURRENT_DOCUMENT_CHILDREN_FETCH",
                sortedHeaders.get(1).getName());

        headers = service.getContentViewHeaders("foo2");
        assertNotNull(headers);
        assertEquals(0, headers.size());

        headers = service.getContentViewHeaders("bar");
        assertNotNull(headers);
        assertEquals(1, headers.size());
        sortedHeaders.clear();
        sortedHeaders.addAll(headers);
        Collections.sort(sortedHeaders);
        assertEquals("CURRENT_DOCUMENT_CHILDREN",
                sortedHeaders.get(0).getName());

        headers = service.getContentViewHeaders("not_set");
        assertNotNull(headers);
        assertEquals(0, headers.size());
    }

    @Test
    public void testUnknownRefPP() throws Exception {
        ContentView cv = service.getContentView("UNKNOWN_REF_PP");
        try {
            PageProvider<?> pp = cv.getPageProvider(null, null, -1L, -1L, null);
            Assert.fail("Should have triggered an exception");
        } catch (Exception e) {
            assertTrue(e instanceof ClientException);
            assertEquals("Could not resolve page provider with name 'foo'",
                    e.getMessage());
        }
    }

    @Test
    public void testOverrideWithGenericPP() throws Exception {
        ContentView cv = service.getContentView("OVERRIDE_PAGE_PROVIDER_WITH_GENERIC");
        PageProvider<?> pp = cv.getPageProvider(null, null, -1L, -1L, null);
        assertTrue(pp instanceof CoreQueryDocumentPageProvider);
        assertEquals("OVERRIDE_PAGE_PROVIDER_WITH_GENERIC", pp.getName());

        // check after override too
        deployContrib("org.nuxeo.ecm.platform.contentview.jsf.test",
                "test-contentview-override-contrib.xml");

        cv = service.getContentView("OVERRIDE_PAGE_PROVIDER_WITH_GENERIC");
        pp = cv.getPageProvider(null, null, -1L, -1L, null);
        assertTrue(pp instanceof CoreQueryAndFetchPageProvider);
        assertEquals("OVERRIDE_PAGE_PROVIDER_WITH_GENERIC", pp.getName());
    }

    @Test
    public void testOverrideWithRefPP() throws Exception {
        ContentView cv = service.getContentView("CURRENT_DOCUMENT_CHILDREN_REF");
        PageProvider<?> pp = cv.getPageProvider(null, null, -1L, -1L, null);
        assertTrue(pp instanceof CoreQueryDocumentPageProvider);
        assertEquals("CURRENT_DOCUMENT_CHILDREN_PP", pp.getName());

        // check after override too
        deployContrib("org.nuxeo.ecm.platform.contentview.jsf.test",
                "test-contentview-override-contrib.xml");

        cv = service.getContentView("CURRENT_DOCUMENT_CHILDREN_REF");
        pp = cv.getPageProvider(null, null, -1L, -1L, null);
        assertTrue(pp instanceof CoreQueryDocumentPageProvider);
        assertEquals("CURRENT_DOCUMENT_CHILDREN_PP_2", pp.getName());
    }

    @Test
    public void testSetResultLayoutByName() throws Exception {
        deployContrib("org.nuxeo.ecm.platform.contentview.jsf.test",
                "test-contentview-override-contrib.xml");

        ContentView contentView = service.getContentView("CURRENT_DOCUMENT_CHILDREN");
        assertNotNull(contentView);

        List<ContentViewLayout> resultLayouts = contentView.getResultLayouts();
        assertEquals(2, resultLayouts.size());

        ContentViewLayout currentResultLayout = contentView.getCurrentResultLayout();
        assertEquals(currentResultLayout.getName(),
                resultLayouts.get(0).getName());

        contentView.setCurrentResultLayout(resultLayouts.get(1).getName());
        currentResultLayout = contentView.getCurrentResultLayout();
        assertEquals(currentResultLayout.getName(),
                resultLayouts.get(1).getName());
    }

}
