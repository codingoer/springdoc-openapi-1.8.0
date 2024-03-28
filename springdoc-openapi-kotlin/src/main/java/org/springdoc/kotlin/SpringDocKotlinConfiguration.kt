package org.springdoc.kotlin

import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.models.media.ByteArraySchema
import org.springdoc.core.Constants
import org.springdoc.core.SpringDocConfiguration
import org.springdoc.core.SpringDocUtils
import org.springdoc.core.customizers.ParameterCustomizer
import org.springdoc.core.providers.ObjectMapperProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.core.KotlinDetector
import org.springframework.core.MethodParameter
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ValueConstants
import kotlin.coroutines.Continuation
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.kotlinFunction

/**
 * The type Spring doc kotlin configuration.
 * @author bnasslahsen
 * @author tobiberger
 */
@Lazy(false)
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = [Constants.SPRINGDOC_ENABLED], matchIfMissing = true)
@ConditionalOnBean(
	SpringDocConfiguration::class
)
class SpringDocKotlinConfiguration(objectMapperProvider: ObjectMapperProvider) {
	/**
	 * Instantiates a new Spring doc kotlin configuration.
	 *
	 */
	init {
		SpringDocUtils.getConfig()
			.addRequestWrapperToIgnore(Continuation::class.java)
			.replaceWithSchema(ByteArray::class.java, ByteArraySchema())
			.addDeprecatedType(Deprecated::class.java)
		objectMapperProvider.jsonMapper().registerModule(KotlinModule.Builder().build())
	}

	/**
	 * Kotlin coroutines return type parser kotlin coroutines return type parser.
	 *
	 * @return the kotlin coroutines return type parser
	 */
	@Bean
	@Lazy(false)
	@ConditionalOnMissingBean
	fun kotlinCoroutinesReturnTypeParser(): KotlinCoroutinesReturnTypeParser {
		return KotlinCoroutinesReturnTypeParser()
	}

	/**
	 * Kotlin springdoc-openapi ParameterCustomizer
	 *
	 * @return the nullable Kotlin Request Parameter Customizer
	 */
	@Bean
	@Lazy(false)
	@ConditionalOnProperty(
		name = [Constants.SPRINGDOC_NULLABLE_REQUEST_PARAMETER_ENABLED],
		matchIfMissing = true
	)
	@ConditionalOnMissingBean
	open fun nullableKotlinRequestParameterCustomizer(): ParameterCustomizer {
		return ParameterCustomizer { parameterModel, methodParameter ->
			if (parameterModel == null) return@ParameterCustomizer null
			if (KotlinDetector.isKotlinType(methodParameter.parameterType)) {
				val kParameter = methodParameter.toKParameter()
				if (kParameter != null) {
					val parameterDoc = AnnotatedElementUtils.findMergedAnnotation(
						AnnotatedElementUtils.forAnnotations(*methodParameter.parameterAnnotations),
						Parameter::class.java
					)
					val requestParam = AnnotatedElementUtils.findMergedAnnotation(
						AnnotatedElementUtils.forAnnotations(*methodParameter.parameterAnnotations),
						RequestParam::class.java
					)
					// Swagger @Parameter annotation takes precedence
					if (parameterDoc != null && parameterDoc.required)
						parameterModel.required = parameterDoc.required
					// parameter is not required if a default value is provided in @RequestParam
					else if (requestParam != null && requestParam.defaultValue != ValueConstants.DEFAULT_NONE)
						parameterModel.required = false
					else
						parameterModel.required =
							kParameter.type.isMarkedNullable == false
				}
			}
			return@ParameterCustomizer parameterModel
		}
	}

	private fun MethodParameter.toKParameter(): KParameter? {
		// ignore return type, see org.springframework.core.MethodParameter.getParameterIndex
		if (parameterIndex == -1) return null
		val kotlinFunction = method?.kotlinFunction ?: return null
		// The first parameter of the kotlin function is the "this" reference and not needed here.
		// See also kotlin.reflect.KCallable.getParameters
		return kotlinFunction.parameters[parameterIndex + 1]
	}
}
