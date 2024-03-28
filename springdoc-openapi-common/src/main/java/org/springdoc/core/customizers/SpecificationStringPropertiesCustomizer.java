package org.springdoc.core.customizers;

/*
 *
 *  *
 *  *  *
 *  *  *  *
 *  *  *  *  * Copyright 2019-2022 the original author or authors.
 *  *  *  *  *
 *  *  *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  *  *  * you may not use this file except in compliance with the License.
 *  *  *  *  * You may obtain a copy of the License at
 *  *  *  *  *
 *  *  *  *  *      https://www.apache.org/licenses/LICENSE-2.0
 *  *  *  *  *
 *  *  *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  *  *  * See the License for the specific language governing permissions and
 *  *  *  *  * limitations under the License.
 *  *  *  *
 *  *  *
 *  *
 *
 */


import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;

import org.springframework.core.env.PropertyResolver;
import org.springframework.util.CollectionUtils;

/**
 * Allows externalizing strings in generated openapi schema via properties that follow
 * conventional naming similar or identical to <a href="https://swagger.io/docs/specification/basic-structure/">openapi schema</a>
 * <p>
 * To set value of a string in schema, define an application property that matches the target node
 * with springdoc.specification-strings prefix.
 * <p>
 * Sample supported properties for api-info customization:
 * <ul>
 *     <li>springdoc.specification-strings.info.title - to set title of api-info</li>
 *     <li>springdoc.specification-strings.info.description - to set description of api-info</li>
 *     <li>springdoc.specification-strings.info.version - to set version of api-info</li>
 *     <li>springdoc.specification-strings.info.termsOfService - to set terms of service of api-info</li>
 * </ul>
 * <p>
 * Sample supported properties for components customization:
 * <ul>
 *     <li>springdoc.specification-strings.components.User.description - to set description of User model</li>
 *     <li>springdoc.specification-strings.components.User.properties.name.description - to set description of 'name' property</li>
 *     <li>springdoc.specification-strings.components.User.properties.name.example - to set example of 'name' property</li>
 * </ul>
 * <p>
 * Sample supported properties for paths/operationIds customization:
 * <ul>
 *     <li>springdoc.specification-strings.paths.{operationId}.description - to set description of {operationId}</li>
 *     <li>springdoc.specification-strings.paths.{operationId}.summary - to set summary of {operationId}</li>
 * </ul>
 * <p>
 * Support for groped openapi customization is similar to the above, but with a group name prefix.
 * E.g.
 * <ul>
 *     <li>springdoc.specification-strings.{group-name}.info.title - to set title of api-info</li>
 *     <li>springdoc.specification-strings.{group-name}.components.User.description - to set description of User model</li>
 *     <li>springdoc.specification-strings.{group-name}.paths.{operationId}.description - to set description of {operationId}</li>
 * </ul>
 *
 * @author Anton Tkachenko tkachenkoas@gmail.com
 */
public class SpecificationStringPropertiesCustomizer implements GlobalOpenApiCustomizer {

	private static final String SPECIFICATION_STRINGS_PREFIX = "springdoc.specification-strings.";

	private final PropertyResolver propertyResolver;
	private final String propertyPrefix;

	public SpecificationStringPropertiesCustomizer(PropertyResolver resolverUtils) {
		this.propertyResolver = resolverUtils;
		this.propertyPrefix = SPECIFICATION_STRINGS_PREFIX;
	}

	public SpecificationStringPropertiesCustomizer(PropertyResolver propertyResolver, String groupName) {
		this.propertyResolver = propertyResolver;
		this.propertyPrefix = SPECIFICATION_STRINGS_PREFIX + groupName + ".";
	}

	@Override
	public void customise(OpenAPI openApi) {
		setOperationInfoProperties(openApi);
		setComponentsProperties(openApi);
		setPathsProperties(openApi);
	}

	private void setOperationInfoProperties(OpenAPI openApi) {
		if (openApi.getInfo() == null) {
			openApi.setInfo(new Info());
		}
		Info info = openApi.getInfo();
		resolveString(info::setTitle, "info.title");
		resolveString(info::setDescription, "info.description");
		resolveString(info::setVersion, "info.version");
		resolveString(info::setTermsOfService, "info.termsOfService");
	}

	private void setPathsProperties(OpenAPI openApi) {
		Paths paths = openApi.getPaths();
		if (CollectionUtils.isEmpty(paths.values())) {
			return;
		}
		for (PathItem pathItem : paths.values()) {
			List<Operation> operations = pathItem.readOperations();
			for (Operation operation : operations) {
				String operationId = operation.getOperationId();
				String operationNode = MessageFormat.format("paths.{0}", operationId);
				resolveString(operation::setDescription, operationNode + ".description");

				resolveString(operation::setSummary, operationNode + ".summary");
			}
		}
	}

	private void setComponentsProperties(OpenAPI openApi) {
		Components components = openApi.getComponents();
		if (components == null || CollectionUtils.isEmpty(components.getSchemas())) {
			return;
		}

		for (Schema componentSchema : components.getSchemas().values()) {
			// set component description
			String schemaPropertyPrefix = MessageFormat.format("components.schemas.{0}", componentSchema.getName());
			resolveString(componentSchema::setDescription, schemaPropertyPrefix + ".description");
			Map<String, Schema> properties = componentSchema.getProperties();

			if (CollectionUtils.isEmpty(properties)) {
				continue;
			}

			for (Schema propSchema : properties.values()) {
				String propertyNode = MessageFormat.format("components.schemas.{0}.properties.{1}",
						componentSchema.getName(), propSchema.getName());

				resolveString(propSchema::setDescription, propertyNode + ".description");
				resolveString(propSchema::setExample, propertyNode + ".example");
			}
		}
	}

	private void resolveString(
			Consumer<String> setter, String node
	) {
		String nodeWithPrefix = propertyPrefix + node;
		String value = propertyResolver.getProperty(nodeWithPrefix);
		if (StringUtils.isNotBlank(value)) {
			setter.accept(value);
		}
	}

}
