package jp.ats.atomsql;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author 千葉 哲嗣
 */
public class AtomSqlInitializer implements ApplicationContextInitializer<AnnotationConfigApplicationContext> {

	private final String name;

	private final boolean primary;

	/**
	 * 
	 */
	public AtomSqlInitializer() {
		this.name = null;
		primary = true;
	}

	/**
	 * @param name
	 * @param primary
	 */
	public AtomSqlInitializer(String name, boolean primary) {
		this.name = Objects.requireNonNull(name);
		this.primary = primary;
	}

	@Override
	public void initialize(AnnotationConfigApplicationContext context) {
		List<Class<?>> classes;
		try {
			classes = loadProxyClasses();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		BeanDefinitionCustomizer customizer = bd -> {
			bd.setScope(BeanDefinition.SCOPE_SINGLETON);
			bd.setLazyInit(true);
			bd.setAutowireCandidate(true);
			bd.setPrimary(primary);
		};

		context.registerBean(name, AtomSql.class, () -> {
			if (name == null) {
				return new AtomSql(new JdbcTemplateExecutor(context.getBean(JdbcTemplate.class)));
			}

			return new AtomSql(new JdbcTemplateExecutor(context.getBean(name, JdbcTemplate.class)));
		}, customizer);

		classes.forEach(c -> {
			@SuppressWarnings("unchecked")
			var casted = (Class<Object>) c;
			context.registerBean(name, casted, () -> {
				if (name == null) {
					var atomSql = new AtomSql(new JdbcTemplateExecutor(context.getBean(JdbcTemplate.class)));
					return atomSql.of(c);
				}

				var atomSql = new AtomSql(new JdbcTemplateExecutor(context.getBean(name, JdbcTemplate.class)));
				return atomSql.of(c);
			}, customizer);
		});
	}

	private static List<Class<?>> loadProxyClasses() throws IOException {
		try (var proxyList = AtomSqlInitializer.class.getClassLoader().getResourceAsStream(Constants.PROXY_LIST)) {
			return Arrays.stream(new String(Utils.readBytes(proxyList), StandardCharsets.UTF_8).split("\\s+")).map(l -> {
				try {
					return Class.forName(l);
				} catch (ClassNotFoundException e) {
					throw new IllegalStateException(e);
				}
			}).collect(Collectors.toList());
		}
	}
}
