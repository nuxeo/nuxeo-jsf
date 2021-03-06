/*
 * (C) Copyright 2006-2014 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 * $Id: AbstractWidgetTypeHandler.java 28491 2008-01-04 19:04:30Z sfermigier $
 */

package org.nuxeo.ecm.platform.forms.layout.facelets.plugins;

import java.util.Map;

import javax.faces.view.facelets.CompositeFaceletHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributes;
import javax.faces.view.facelets.TagConfig;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.platform.forms.layout.api.BuiltinWidgetModes;
import org.nuxeo.ecm.platform.forms.layout.api.Widget;
import org.nuxeo.ecm.platform.forms.layout.api.exceptions.WidgetException;
import org.nuxeo.ecm.platform.forms.layout.facelets.FaceletHandlerHelper;
import org.nuxeo.ecm.platform.forms.layout.facelets.LeafFaceletHandler;
import org.nuxeo.ecm.platform.forms.layout.facelets.RenderVariables;
import org.nuxeo.ecm.platform.forms.layout.facelets.WidgetTypeHandler;
import org.nuxeo.ecm.platform.forms.layout.facelets.dev.WidgetTypeDevTagHandler;
import org.nuxeo.ecm.platform.ui.web.tag.handler.TagConfigFactory;

import com.sun.faces.facelets.tag.ui.InsertHandler;

/**
 * Abstract widget type handler.
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public abstract class AbstractWidgetTypeHandler implements WidgetTypeHandler {

    private static final long serialVersionUID = -2933485416045771633L;

    /**
     * @since 6.0
     */
    public static final String DEV_TEMPLATE_PROPERTY_NAME = "dev_template";

    /**
     * @since 6.0
     */
    public static final String DISABLE_DEV_PROPERTY_NAME = "disable_dev";

    protected Map<String, String> properties;

    public abstract FaceletHandler getFaceletHandler(FaceletContext ctx,
            TagConfig tagConfig, Widget widget, FaceletHandler[] subHandlers)
            throws WidgetException;

    public FaceletHandler getDevFaceletHandler(FaceletContext ctx,
            TagConfig tagConfig, Widget widget) throws WidgetException {
        if (Boolean.parseBoolean(getProperty(DISABLE_DEV_PROPERTY_NAME))
                || Boolean.parseBoolean((String) widget.getProperty(DISABLE_DEV_PROPERTY_NAME))) {
            return null;
        }
        // lookup in the widget type configuration
        String template = (String) widget.getProperty(DEV_TEMPLATE_PROPERTY_NAME);
        if (StringUtils.isBlank(template)) {
            template = getProperty(DEV_TEMPLATE_PROPERTY_NAME);
        }
        FaceletHandlerHelper helper = new FaceletHandlerHelper(ctx, tagConfig);
        TagAttribute widgetAttr = helper.createAttribute(
                "widget",
                String.format("#{%s}",
                        RenderVariables.widgetVariables.widget.name()));
        TagAttributes devWidgetAttributes;
        if (StringUtils.isBlank(template)) {
            devWidgetAttributes = FaceletHandlerHelper.getTagAttributes(widgetAttr);
        } else {
            devWidgetAttributes = FaceletHandlerHelper.getTagAttributes(
                    widgetAttr, helper.createAttribute("template", template));
        }
        TagConfig devWidgetConfig = TagConfigFactory.createTagConfig(tagConfig,
                widget.getTagConfigId(), devWidgetAttributes,
                new LeafFaceletHandler());
        return new WidgetTypeDevTagHandler(devWidgetConfig);
    }

    public String getProperty(String name) {
        if (properties != null) {
            return properties.get(name);
        }
        return null;
    }

    /**
     * Helper method, throws an exception if property value is null.
     */
    public String getRequiredProperty(String name) throws WidgetException {
        String value = getProperty(name);
        if (value == null) {
            throw new WidgetException(String.format(
                    "Required property %s is missing "
                            + "on widget type configuration", name));
        }
        return value;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * Returns sub handlers as computed from tag information.
     * <p>
     * Adds an sub insert handler slot named
     * {@link RenderVariables.widgetTemplatingZones#inside_input_widget} when
     * widget is in edit mode.
     *
     * @since 6.0
     */
    protected FaceletHandler getNextHandler(FaceletContext ctx,
            TagConfig tagConfig, Widget widget, FaceletHandler[] subHandlers,
            FaceletHandlerHelper helper) {
        return getNextHandler(ctx, tagConfig, widget, subHandlers, helper,
                BuiltinWidgetModes.EDIT.equals(widget.getMode()));
    }

    /**
     * Returns sub handlers as computed from tag information
     *
     * @since 6.0
     */
    protected FaceletHandler getNextHandler(FaceletContext ctx,
            TagConfig tagConfig, Widget widget, FaceletHandler[] subHandlers,
            FaceletHandlerHelper helper, boolean addInputSlot) {
        FaceletHandler leaf;
        FaceletHandler[] handlers = new FaceletHandler[] {};
        if (subHandlers != null && subHandlers.length > 0) {
            handlers = (FaceletHandler[]) ArrayUtils.addAll(subHandlers,
                    handlers);
        }
        if (addInputSlot) {
            FaceletHandler slot = getInputSlotHandler(ctx, tagConfig, widget,
                    subHandlers, helper);
            if (slot != null) {
                handlers = (FaceletHandler[]) ArrayUtils.add(handlers, slot);
            }
        }
        if (handlers.length == 0) {
            leaf = new LeafFaceletHandler();
        } else {
            leaf = new CompositeFaceletHandler(handlers);
        }
        return leaf;
    }

    protected FaceletHandler getInputSlotHandler(FaceletContext ctx,
            TagConfig tagConfig, Widget widget, FaceletHandler[] subHandlers,
            FaceletHandlerHelper helper) {
        TagConfig config = TagConfigFactory.createTagConfig(
                tagConfig,
                tagConfig.getTagId(),
                FaceletHandlerHelper.getTagAttributes(helper.createAttribute(
                        "name",
                        RenderVariables.widgetTemplatingZones.inside_input_widget.name())),
                new LeafFaceletHandler());
        return new InsertHandler(config);
    }

}