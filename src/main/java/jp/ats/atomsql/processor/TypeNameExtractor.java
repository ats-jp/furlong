package jp.ats.atomsql.processor;

import java.util.function.Supplier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor14;
import javax.tools.Diagnostic.Kind;

class TypeNameExtractor extends SimpleTypeVisitor14<String, Element> {

	private final Supplier<ProcessingEnvironment> envSupplier;

	TypeNameExtractor(Supplier<ProcessingEnvironment> envSupplier) {
		this.envSupplier = envSupplier;
	}

	@Override
	protected String defaultAction(TypeMirror e, Element p) {
		//不明なエラーが発生しました
		error("Unknown error occurred", p);
		return DEFAULT_VALUE;
	}

	@Override
	public String visitPrimitive(PrimitiveType t, Element p) {
		switch (t.getKind()) {
		case BOOLEAN:
			return boolean.class.getName();
		case BYTE:
			return byte.class.getName();
		case SHORT:
			return short.class.getName();
		case INT:
			return int.class.getName();
		case LONG:
			return long.class.getName();
		case CHAR:
			return char.class.getName();
		case FLOAT:
			return float.class.getName();
		case DOUBLE:
			return double.class.getName();
		default:
			return defaultAction(t, p);
		}
	}

	@Override
	public String visitDeclared(DeclaredType t, Element p) {
		return t.asElement().accept(TypeConverter.instance, null).getQualifiedName().toString();
	}

	// Consumer<SqlParameter>等型パラメータのあるものがここに来る
	@Override
	public String visitError(ErrorType t, Element p) {
		return t.asElement().accept(TypeConverter.instance, null).getQualifiedName().toString();
	}

	private void error(String message, Element e) {
		envSupplier.get().getMessager().printMessage(Kind.ERROR, message, e);
	}
}
