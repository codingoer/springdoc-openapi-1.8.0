/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright 2019-2022 the original author or authors.
 *  *  *  *
 *  *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  *  * you may not use this file except in compliance with the License.
 *  *  *  * You may obtain a copy of the License at
 *  *  *  *
 *  *  *  *      https://www.apache.org/licenses/LICENSE-2.0
 *  *  *  *
 *  *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  *  * See the License for the specific language governing permissions and
 *  *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.springdoc.groovy;

import groovy.lang.MetaClass;
import org.springdoc.core.SpringDocConfiguration;
import org.springdoc.core.SpringDocUtils;
import org.springdoc.core.converters.JavaTypeToIgnoreConverter;
import org.springdoc.core.providers.ObjectMapperProvider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static org.springdoc.core.Constants.SPRINGDOC_ENABLED;

/**
 * The type Spring doc groovy configuration.
 * @author bnasslahsen
 */
@Lazy(false)
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = true)
@ConditionalOnClass(MetaClass.class)
@ConditionalOnBean(SpringDocConfiguration.class)
public class SpringDocGroovyConfiguration {

	/**
	 * Ignore groovy meta class object.
	 *
	 * @return the object
	 */
	@Bean
	@Lazy(false)
	Object ignoreGroovyMetaClass() {
		SpringDocUtils.getConfig().addJavaTypeToIgnore(MetaClass.class);
		return null;
	}

	/**
	 * Request type to ignore converter request type to ignore converter.
	 *
	 * @param springDocObjectMapper the spring doc object mapper
	 * @return the request type to ignore converter
	 */
	@Bean
	@Lazy(false)
	JavaTypeToIgnoreConverter requestTypeToIgnoreConverter(ObjectMapperProvider springDocObjectMapper) {
		return new JavaTypeToIgnoreConverter(springDocObjectMapper);
	}
}
