/*
 *
 *  * Copyright 2019-2020 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      https://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package test.org.springdoc.api.app100;

import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The type Hello controller.
 */
@RestController
@Tags(value = @Tag(name = "hello-ap1"))
public class HelloController {

	/**
	 * Gets all pets.
	 *
	 * @param toto the toto 
	 * @return the all pets
	 */
	@GetMapping(value = "/search", produces = { "application/xml", "application/json" })
	@Tags(value = @Tag(name = "hello-ap2"))
	public PersonDTO getAllPets(@NotNull String toto) {
		return null;
	}

}
