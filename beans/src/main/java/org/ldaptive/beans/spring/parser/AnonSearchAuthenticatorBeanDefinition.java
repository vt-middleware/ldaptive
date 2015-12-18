/*
 * Copyright 2015 eroot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ldaptive.beans.spring.parser;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 *
 * Parser for <pre>anonymous-search-authenticator</pre> elements.
 * @author eroot
 */
public class AnonSearchAuthenticatorBeanDefinition extends AbstractSearchAuthenticatorBeanDefinition
{
    @Override
    protected String resolveId(
        final Element element,
        // CheckStyle:IllegalTypeCheck OFF
        final AbstractBeanDefinition definition,
        // CheckStyle:IllegalTypeCheck ON
        final ParserContext parserContext)
            throws BeanDefinitionStoreException
    {
        final String idAttrValue = element.getAttribute("id");
        return StringUtils.hasText(idAttrValue) ? idAttrValue : "anonymous-search-authenticator";
    }
}
