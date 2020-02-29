package immogram.webscraper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumer;

import immogram.Exceptions;
import immogram.webscraper.WebScraperProvider.WebScraperSource;

class WebScraperProvider implements ArgumentsProvider, AnnotationConsumer<WebScraperSource> {

	@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@ArgumentsSource(WebScraperProvider.class)
	static @interface WebScraperSource {
		Class<? extends WebScraper<?>>[] value();
	}

	static interface WebScraperFactory<E> {
		WebScraper<E> createNew(Object... args);
	}

	private Class<? extends WebScraper<?>>[] webScrapers;

	@Override
	public void accept(WebScraperSource annotation) {
		webScrapers = annotation.value();
	}

	@Override
	public Stream<Arguments> provideArguments(ExtensionContext context) throws Exception {
		return Arrays.stream(webScrapers)
				.map(type -> createFactory(type))
				.map(scraper -> Arguments.of(scraper));
	}

	private <E> WebScraperFactory<E> createFactory(Class<? extends WebScraper<?>> type) {
		return new WebScraperFactory<E>() {
			@Override
			public WebScraper<E> createNew(Object... args) {
				return createInstance(type, args);
			}
		};
	}

	@SuppressWarnings("unchecked")
	private <E> WebScraper<E> createInstance(Class<? extends WebScraper<?>> type, Object... args) {
		try {
			var constructor = type.getDeclaredConstructors()[0];
			return WebScraper.class.cast(constructor.newInstance(args));
		} catch (ReflectiveOperationException e) {
			return Exceptions.throwUnchecked(e);
		}
	}

}
