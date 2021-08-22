package jp.ats.furlong.processor;

import java.sql.ResultSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;
import javax.tools.Diagnostic.Kind;

import jp.ats.furlong.DataObject;

/**
 * @author 千葉 哲嗣
 */
@SupportedAnnotationTypes("jp.ats.furlong.DataObject")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class DataObjectCheckAnnotationProcessor extends AbstractProcessor {

	private static final TypeConverter typeVisitor = new TypeConverter();

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (annotations.size() == 0)
			return false;

		annotations.forEach(a -> {
			roundEnv.getElementsAnnotatedWith(a).forEach(e -> {
				ElementKind kind = e.getKind();
				if (kind != ElementKind.CLASS) {
					error("cannot annotate" + kind.name() + " with " + DataObject.class.getSimpleName(), e);

					return;
				}

				ExecutableElement[] result = { null };
				ConstructorVisitor visitor = new ConstructorVisitor();
				e.getEnclosedElements().forEach(enc -> {
					enc.accept(visitor, result);
				});

				if (result[0] == null)
					error(DataObject.class.getSimpleName()
							+ " requires a constructor whose only parameter is ResultSet", e);
			});
		});

		return false;
	}

	private class ConstructorVisitor extends SimpleElementVisitor8<Void, ExecutableElement[]> {

		@Override
		public Void visitExecutable(ExecutableElement e, ExecutableElement[] p) {
			// コンストラクタ以外はスキップ
			if (!"<init>".equals(e.getSimpleName().toString()))
				return DEFAULT_VALUE;

			ParameterTypeChecker checker = new ParameterTypeChecker();

			var params = e.getParameters();

			if (params.size() != 1)
				return DEFAULT_VALUE;

			boolean[] ok = { false };
			params.get(0).asType().accept(checker, ok);
			if (ok[0])
				p[0] = e;

			return DEFAULT_VALUE;
		}
	}

	private class ParameterTypeChecker extends SimpleTypeVisitor8<Void, boolean[]> {

		@Override
		protected Void defaultAction(TypeMirror e, boolean[] p) {
			return DEFAULT_VALUE;
		}

		@Override
		public Void visitDeclared(DeclaredType t, boolean[] p) {
			TypeElement type = t.asElement().accept(typeVisitor, null);

			if (ProcessorUtils.sameClass(type, ResultSet.class)) {
				p[0] = true;
				return DEFAULT_VALUE;
			}

			return defaultAction(t, p);
		}
	}

	private void error(String message, Element e) {
		super.processingEnv.getMessager().printMessage(Kind.ERROR, message, e);
	}
}
