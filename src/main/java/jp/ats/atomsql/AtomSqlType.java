package jp.ats.atomsql;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author 千葉 哲嗣
 */
public enum AtomSqlType {

	/**
	 * {@link BigDecimal}
	 */
	BIG_DECIMAL {

		@Override
		public Class<?> type() {
			return BigDecimal.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			try {
				statement.setBigDecimal(index, (BigDecimal) value);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * {@link BinaryStream}
	 */
	BINARY_STREAM {

		@Override
		public Class<?> type() {
			return BinaryStream.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			var stream = (BinaryStream) value;
			try {
				statement.setBinaryStream(index, stream.input, stream.length);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * {@link Blob}
	 */
	BLOB {

		@Override
		public Class<?> type() {
			return Blob.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			try {
				statement.setBlob(index, (Blob) value);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * Boolean
	 */
	BOOLEAN {

		@Override
		public Class<?> type() {
			return Boolean.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			try {
				statement.setBoolean(index, (boolean) value);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * boolean
	 */
	P_BOOLEAN {

		@Override
		public Class<?> type() {
			return boolean.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			//ラッパー型が使用される
			throw new UnsupportedOperationException();
		}

		@Override
		AtomSqlType toTypeArgument() {
			return BOOLEAN;
		}
	},

	/**
	 * byte[]
	 */
	BYTE_ARRAY {

		@Override
		public Class<?> type() {
			return byte[].class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			try {
				statement.setBytes(index, (byte[]) value);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * {@link CharacterStream}
	 */
	CHARACTER_STREAM {

		@Override
		public Class<?> type() {
			return CharacterStream.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			var stream = (CharacterStream) value;
			try {
				statement.setCharacterStream(index, stream.input, stream.length);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * {@link Clob}
	 */
	CLOB {

		@Override
		public Class<?> type() {
			return Clob.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			try {
				statement.setClob(index, (Clob) value);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * Double
	 */
	DOUBLE {

		@Override
		public Class<?> type() {
			return Double.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			try {
				statement.setDouble(index, (double) value);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * double
	 */
	P_DOUBLE {

		@Override
		public Class<?> type() {
			return double.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			//ラッパー型が使用される
			throw new UnsupportedOperationException();
		}

		@Override
		AtomSqlType toTypeArgument() {
			return DOUBLE;
		}
	},

	/**
	 * Float
	 */
	FLOAT {

		@Override
		public Class<?> type() {
			return Float.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			try {
				statement.setFloat(index, (float) value);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * float
	 */
	P_FLOAT {

		@Override
		public Class<?> type() {
			return float.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			//ラッパー型が使用される
			throw new UnsupportedOperationException();
		}

		@Override
		AtomSqlType toTypeArgument() {
			return FLOAT;
		}
	},

	/**
	 * Integer
	 */
	INTEGER {

		@Override
		public Class<?> type() {
			return Integer.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			try {
				statement.setInt(index, (int) value);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * int
	 */
	P_INT {

		@Override
		public Class<?> type() {
			return int.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			//ラッパー型が使用される
			throw new UnsupportedOperationException();
		}

		@Override
		AtomSqlType toTypeArgument() {
			return INTEGER;
		}
	},

	/**
	 * Long
	 */
	LONG {

		@Override
		public Class<?> type() {
			return Long.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			try {
				statement.setLong(index, (long) value);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * long
	 */
	P_LONG {

		@Override
		public Class<?> type() {
			return long.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			//ラッパー型が使用される
			throw new UnsupportedOperationException();
		}

		@Override
		AtomSqlType toTypeArgument() {
			return LONG;
		}
	},

	/**
	 * {@link Object}
	 */
	OBJECT {

		@Override
		public Class<?> type() {
			return Object.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			try {
				statement.setObject(index, value);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * {@link String}
	 */
	STRING {

		@Override
		public Class<?> type() {
			return String.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			try {
				statement.setString(index, (String) value);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * {@link Timestamp}
	 */
	TIMESTAMP {

		@Override
		public Class<?> type() {
			return Timestamp.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			try {
				statement.setTimestamp(index, (Timestamp) value);
			} catch (SQLException e) {
				throw new AtomSqlException(e);
			}
		}

		@Override
		AtomSqlType toTypeArgument() {
			return this;
		}
	},

	/**
	 * {@link MultiValues}
	 */
	MULTI_VALUES {

		@Override
		public Class<?> type() {
			return MultiValues.class;
		}

		@Override
		void bind(int index, PreparedStatement statement, Object value) {
			var values = ((MultiValues<?>) value).values();
			IntStream.range(0, values.size()).forEach(i -> {
				var v = values.get(i);
				select(v).bind(index + i, statement, v);
			});
		}

		@Override
		String placeholderExpression(Object value) {
			var values = ((MultiValues<?>) value).values();
			return String.join(", ", values.stream().map(v -> "?").collect(Collectors.toList()));
		}

		@Override
		AtomSqlType toTypeArgument() {
			return OBJECT;
		}
	};

	/**
	 * @return type
	 */
	public abstract Class<?> type();

	abstract void bind(int index, PreparedStatement statement, Object value);

	abstract AtomSqlType toTypeArgument();

	String placeholderExpression(Object value) {
		return "?";
	}

	private static final Map<Class<?>, AtomSqlType> types = new HashMap<>();

	static {
		Arrays.stream(AtomSqlType.values()).filter(b -> !b.equals(OBJECT)).forEach(b -> types.put(b.type(), b));
	}

	/**
	 * @param o
	 * @return {@link AtomSqlType}
	 */
	public static AtomSqlType select(Object o) {
		// nullの場合はsetObject(i, null)
		// DBによってはエラーとなる可能性があるため、その場合はsetNull(int, int)の使用を検討する
		if (o == null)
			return OBJECT;

		var type = types.get(o.getClass());
		return type == null ? OBJECT : type;
	}

	/**
	 * @param name
	 * @return {@link AtomSqlType}
	 */
	public static AtomSqlType safeValueOf(String name) {
		try {
			return valueOf(name);
		} catch (IllegalArgumentException e) {
			return OBJECT;
		}
	}

	/**
	 * @param name
	 * @return {@link AtomSqlType}
	 */
	public static AtomSqlType safeTypeArgumentValueOf(String name) {
		try {
			return valueOf(name).toTypeArgument();
		} catch (IllegalArgumentException e) {
			return OBJECT;
		}
	}
}
