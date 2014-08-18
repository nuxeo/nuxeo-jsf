/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 *
 * $Id: DateTimeWidgetTypeHandler.java 30416 2008-02-21 19:10:37Z atchertchian $
 */

package org.nuxeo.ecm.platform.forms.layout.facelets.plugins;

import java.util.Arrays;

import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;

import org.nuxeo.ecm.platform.forms.layout.api.BuiltinWidgetModes;
import org.nuxeo.ecm.platform.forms.layout.api.Widget;
import org.nuxeo.ecm.platform.forms.layout.api.exceptions.WidgetException;
import org.nuxeo.ecm.platform.forms.layout.facelets.FaceletHandlerHelper;
import org.nuxeo.ecm.platform.forms.layout.facelets.LeafFaceletHandler;
import org.nuxeo.ecm.platform.ui.web.component.seam.UIHtmlText;
import org.nuxeo.ecm.platform.ui.web.tag.handler.InputDateTimeTagHandler;
import org.nuxeo.ecm.platform.ui.web.tag.handler.TagConfigFactory;
import org.richfaces.component.UICalendar;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.FaceletHandler;
import com.sun.facelets.tag.CompositeFaceletHandler;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributes;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.tag.jsf.ComponentConfig;
import com.sun.facelets.tag.jsf.ComponentHandler;
import com.sun.facelets.tag.jsf.ConvertHandler;
import com.sun.facelets.tag.jsf.ConverterConfig;
import com.sun.facelets.tag.jsf.ValidateHandler;
import com.sun.facelets.tag.jsf.ValidatorConfig;
import com.sun.facelets.tag.jsf.core.ConvertDateTimeHandler;

/**
 * Date time widget
 *
 * @author <a href="mailto:at@nuxeo.com">Anahide Tchertchian</a>
 */
public class DateTimeWidgetTypeHandler extends AbstractWidgetTypeHandler {

    private static final long serialVersionUID = 1495841177711755669L;

    @Override
    public FaceletHandler getFaceletHandler(FaceletContext ctx,
            TagConfig tagConfig, Widget widget, FaceletHandler[] subHandlers)
            throws WidgetException {
        FaceletHandlerHelper helper = new FaceletHandlerHelper(ctx, tagConfig);
        String mode = widget.getMode();
        String widgetId = widget.getId();
        String widgetName = widget.getName();
        String widgetTagConfigId = widget.getTagConfigId();
        TagAttributes attributes;
        if (BuiltinWidgetModes.isLikePlainMode(mode)) {
            // use attributes without id
            attributes = helper.getTagAttributes(widget);
        } else {
            attributes = helper.getTagAttributes(widgetId, widget);
        }
        FaceletHandler leaf = null;
        if (BuiltinWidgetModes.EDIT.equals(mode)) {
            ValidatorConfig validatorConfig = TagConfigFactory.createValidatorConfig(
                    tagConfig,
                    widget.getTagConfigId(),
                    new TagAttributes(new TagAttribute[0]),
                    new LeafFaceletHandler(),
                    org.nuxeo.ecm.platform.ui.web.component.date.DateTimeValidator.VALIDATOR_ID);
            ValidateHandler validateHandler = new ValidateHandler(
                    validatorConfig);

            if (subHandlers != null) {
                leaf = new CompositeFaceletHandler(addNewSubHandler(
                        subHandlers, validateHandler));
            } else {
                leaf = validateHandler;
            }

            ComponentConfig config = TagConfigFactory.createComponentConfig(
                    tagConfig, widget.getTagConfigId(), attributes, leaf,
                    UICalendar.COMPONENT_TYPE, null);
            ComponentHandler input = new InputDateTimeTagHandler(config);
            String styleClass = "inputDate";
            if (widget.getProperty("styleClass") != null) {
                styleClass += " " + (String) widget.getProperty("styleClass");
            }
            // always add a surrounding span tag with associated style class,
            // see NXP-14963
            input = helper.getHtmlComponentHandler(
                    widgetTagConfigId,
                    FaceletHandlerHelper.getTagAttributes(helper.createAttribute(
                            "styleClass", styleClass)), input,
                    HtmlPanelGroup.COMPONENT_TYPE, null);
            String msgId = helper.generateMessageId(widgetName);
            ComponentHandler message = helper.getMessageComponentHandler(
                    widgetTagConfigId, msgId, widgetId, null);
            FaceletHandler[] handlers = { input, message };
            return new CompositeFaceletHandler(handlers);
        } else {
            ConverterConfig convertConfig = TagConfigFactory.createConverterConfig(
                    tagConfig, widget.getTagConfigId(), attributes,
                    new LeafFaceletHandler(),
                    javax.faces.convert.DateTimeConverter.CONVERTER_ID);
            ConvertHandler convert = new ConvertDateTimeHandler(convertConfig);

            if (subHandlers != null) {
                leaf = new CompositeFaceletHandler(addNewSubHandler(
                        subHandlers, convert));
            } else {
                leaf = convert;
            }

            ComponentHandler output = helper.getHtmlComponentHandler(
                    widgetTagConfigId, attributes, convert,
                    HtmlOutputText.COMPONENT_TYPE, null);
            if (BuiltinWidgetModes.PDF.equals(mode)) {
                // add a surrounding p:html tag handler
                return helper.getHtmlComponentHandler(widgetTagConfigId,
                        new TagAttributes(new TagAttribute[0]), output,
                        UIHtmlText.class.getName(), null);
            } else {
                return output;
            }
        }
    }

    /**
     * Create an array of FaceletHandler which contains all the elements of
     * originalSubHandlers plus newSubHandler
     *
     * @param originalSubHandlers
     * @param newSubHandler
     * @return the new array
     * @since 5.7.2
     */
    private FaceletHandler[] addNewSubHandler(
            FaceletHandler[] originalSubHandlers, FaceletHandler newSubHandler) {
        FaceletHandler[] newSubHandlers = Arrays.copyOf(originalSubHandlers,
                originalSubHandlers.length + 1);
        newSubHandlers[newSubHandlers.length - 1] = newSubHandler;
        return newSubHandlers;
    }

}
