# Atom SQL

## 概要
Atom SQLは、Javaのアノテーションプロセッサーと動的プロキシクラスという技術を利用し、宣言のみの記述で手続きをほぼ記述することなくSQLの発行、結果の取得を行うことを可能にするツールです。  
通常のライブラリと異なり、アノテーションプロセッサーによりコンパイル時に内部の実装を作成するため、以下ルールに従った記述をしない場合想定外の動作となる可能性があるため注意が必要です。  
またAtom SQLで使用可能なSQL文は事前定義したもののみであり、値はすべてプレースホルダからセットするため、SQL Injectionの発生を回避することが可能です。

## インストール
- Spring Framework  
Spring FrameworkからAtom SQLを使用する場合は[Atom SQL Spring](https://github.com/ats-jp/atom-sql-spring)を参照のこと  

- その他  
準備中  

## アノテーションプロセッサーの設定
- Eclipse  
プロジェクトのプロパティで  
  - `Java Compiler` > `Annotation Processing` > `Factory Path`のページを表示  
  - `Add Variable...`をクリック  
  - 変数`M2 REPO`を選択し、`Extend...`をクリック  
  - `jp/ats/atom-sql/n.n.n/atom-sql-n.n.n.jar`を選択し`OK`をクリック  
※`n.n.n`はインストールしている実際のバージョン番号を指定すること  
  - `Apply and Close`をクリック  
で設定完了  

- その他  
準備中  

## 使い方
### SqlProxyインターフェイス作成  
- `サービス名Proxy`という名称のインターフェイスを作成する  
  - __@SqlProxy__  
作成したインターフェイスに`@SqlProxy`アノテーションを付与する  
Spring Frameworkで使用する場合、Atom SQL SpringがSpringにコンポーネントとして登録するので`@Autowired`での使用が可能となる  

```java
package sample;

import jp.ats.atomsql.annotation.SqlProxy;

@SqlProxy
public interface SampleProxy {
}
```

### SELECT文の発行  
- 検索結果格納クラスの作成
  - __@DataObject__  
検索結果格納クラスを作成し、`@DataObject`を付与する  
検索結果格納クラスは以下の3タイプが使用可能  

1. メンバー名がSELECT句の項目名と一致しているrecord  

```java
/**
 * SELECT句にid, nameが定義されている想定の検索結果格納レコード
 */
@DataObject
public record SampleInfo(long id, String name) {}
```

2. パラメーターなしのコンストラクタを持ち、フィールド名がSELECT句の項目名と一致しているクラス  

```java
/**
 * SELECT句にid, nameが定義されている想定の検索結果格納クラス
 */
@DataObject
public class SampleInfo {

    public long id;

    public String name;
}
```

3. パラメーターがjava.sql.ResultSetのみのコンストラクタを持つクラス  

```java
/**
 * SELECT句にid, nameが定義されている想定の検索結果格納クラス
 */
@DataObject
public class SampleInfo {

    public final long id;

    public final String name;

    public SampleInfo(ResultSet rs) throws SQLException {
        id = rs.getLong("id");
        name = rs.getString("name");
    }
}
```

※項目の型がプリミティブ型ではない場合は、Optionalとして受け取ることが可能  
検索結果がnullとなる可能性がある項目はOptionalを使用することが可能  
これによりnullとなる可能性を表明することが出来る  

- メソッドの作成  
SELECT文を発行するためのメソッドをSqlProxy内に作成する  
戻り値の型は、`java.util.List`、`java.util.stream.Stream`、`java.util.Optional`、`jp.ats.atomsql.Atom`の4種類が使用可能であり、その型パラメーターに上で作成した検索結果格納クラスを指定する  
このように記述することでAtom SQLが戻り値の型、その型パラメータを認識し、その通りの検索結果を生成し返却することが可能となる  
メソッド名は特に参照されないので自由に決めて問題なし  

```java
/**
 * 検索結果を java.util.List として返却
 * 一旦すべてオブジェクト化しメモリ上に持つことになるので
 * 検索結果が大量になることが見込まれる場合には使用しない事
 */
public List<SampleInfo> selectList();

/**
 * 検索結果を java.util.stream.Stream として返却
 * Streamにより逐次検索結果を読み込み、検索結果オブジェクトの作成を行うので
 * 検索結果が大量になる場合でもメモリの使用量が増加しない
 * ただし、Streamは結果取得後closeする必要がある
 * jp.ats.atomsql.AtomSql#tryStreamを使用することでStreamの自動closeが可能となる
 */
public Stream<SampleInfo> selectStream();

/**
 * 検索結果を java.util.Optional として返却
 * 検索結果が1件もしくは0件であることが明確である場合(PKを指定した検索等)、Optionalで
 * 返却することで繰り返し処理などを行うことなく単一の値を取得することが可能
 * 万が一検索結果が2件以上返ってきた場合、例外がスローされるので複数返ってくる可能性
 * がある検索の場合は使用しない事
 */
public Optional<SampleInfo> selectList();

/**
 * SQL実行の中間状態である jp.ats.atomsql.Atom として返却
 * Atomの使用方法については後述
 */
public Atom<SampleInfo> selectAtom();
```

※特に理由がない場合は扱いやすいListタイプを使用すること  

※この状態ではまだSQL文が設定されていないためコンパイルエラーが発生するので、次のSQL文の定義を行う  

- SQL文の設定  
SQLの設定には2通りの方法がある  
1. `@Sql`アノテーションでコード内にSQL文を直接記述する  
後述  
1. ファイルに格納したSQL文を、そのファイル名によってメソッドと紐づける  
作成したSQLファイルを、SqlProxyインターフェイスと同じクラスパスに配置、そのファイル名を  
`SqlProxyインターフェイス名.メソッド名.sql`  
とすることでAtom SQLによる読み込み対象となる  
SqlProxyインターフェイスが内部クラスとして定義されている場合は  
`外部クラス名$内部SqlProxyインターフェイス名.メソッド名.sql`  
とする  

- __@Sql__  
メソッドに`@Sql`アノテーションを付与し、その値としてSQLを直接記述することが可能となる  

```java
@Sql("SELECT * FROM sample")
public List<SampleInfo> selectList();
```

-  複数行にわたる長いSQL文の記述  
@Sqlアノテーションにテキストブロックを使用することで複数行にわたる長いSQL文を記述することが可能となる  
ただし、IDEとしてEclipseを使用した場合、コードフォーマット機能の使用によりテキストブロックのインデント等の崩れが起きてしまう（将来のバージョンで修正される可能性有り）ため、テキストブロック部分はフォーマッターの適用しないようにした方がよい  
そのため、Eclipseの`@formatter:off`、`@formatter:on`を使用する  

```java
@Sql(/*@formatter:off*/"""
-- この間にSQLを複数行にわたり記述することが可能
SELECT
  *
FROM
  sample
WHERE
  id = 0
"""/*@formatter:on*/)
public List<SampleInfo> selectList();
```

※SQL文とそのコントローラーが近い方が処理を把握しやすい等のメリットがあることが考えられる  
SQLをファイルとして管理したい等の要件が無いのであれば`@Sql`アノテーションを使用すること  

### UPDATE文、INSERT文の発行
- メソッドの作成
UPDATE文、INSERT文を発行するためのメソッドをSqlProxy内に作成する  
戻り値の型は`int`もしくは`jp.ats.atomsql.Atom`である必要がある  

```java
@Sql("UPDATE sample SET name = 'name' WHERE id = 0")
public int updateSample();

@Sql("INSERT INTO sample (id, name) VALUES (0, 'name')")
public int insertSample();

/**
 * SQL実行の中間状態である jp.ats.atomsql.Atom として返却
 * Atomの使用方法については後述
 */
@Sql("INSERT INTO sample (id, name) VALUES (0, 'name')")
public Atom<?> insertSample();
```

※SQL文の設定方法はSELECT文と同様なのでここでは割愛する  

- __@Qualifier__  
MySQL InnoDB Clusterを採用している等、検索と更新で接続先が異なる場合、発行するSQLによって接続先を切り替える為のアノテーションとして`@Qualifier`を使用する  
`@Qualifier`をメソッドに付与し、その値として接続先名を指定することで切り替える事が可能となる  
また`@Qualifier`を付与した独自のアノテーションを作成することで`@Qualifier`のエイリアスとして使用することができる  

```java
/**
 * 検索用Qualifier
 * Qualifierを何も指定しない場合デフォルトでこちらが使用されるため、指定する必要はない
 */
@Qualifier("readonly")
@Sql("SELECT * FROM sample")
public List<SampleInfo> selectList();

/**
 * 更新用Qualifier
 * 更新を行うためには必ず指定しなければならない
 */
@Qualifier("updatable")
@Sql("UPDATE sample SET name = 'name' WHERE id = 0")
public int updateSample();
```

- トランザクション管理  
トランザクションの管理は、Atom SQLを使用するフレームワークに依存する  
例として、Spring Frameworkであれば`@Transactional`アノテーションによりトランザクション管理を行う  
`@Transactional`アノテーションはSqlProxyに付与するのではなく、SqlProxyを使用するクラス（例えばServiceクラス）のメソッドに付与する  

- バッチ実行  
大量の更新処理を一度に実行したい場合、JDBCに備わっているバッチ実行機能を利用することが出来る  
`jp.ats.atomsql.AtomSql#tryBatch`の中で実行された更新処理は全て保留され、tryBatch終了時に一括で実施される  

```java
atomSql.tryBatch(() -> {
    sampleProxy.insert(1);
    sampleProxy.insert(2);
    sampleProxy.insert(3);
    sampleProxy.insert(4);
    sampleProxy.insert(5);
    // この時点では実際には一件もINSERTされていない
}); // tryBatch終了で全ての更新が実施される
```

- SQLプレースホルダの使用  
Atom SQLでは、JDBCで使用する`?`を用いたプレースホルダは使用できず、代わりに任意の名前を持つプレースホルダの使用が可能  
プレースホルダの書式は`:someName`のように、コロンに続き任意の名前を記述するものとなっている  
この書式は、DBeaverや、A5:SQL Mk-2の持つSQLエディタと相性が良く、そのままプレースホルダとして認識されるため、先にそれらのSQLエディタでSQL文をプレースホルダ込みで記述し、Atom SQLに移設し実行することが容易  
任意の名前にはマルチバイト文字の使用も可能となっている  
プレースホルダへの値のバインドは、SqlProxyのSQL実施メソッドのパラメーターとして値を渡すか、後述の`@SqlParameters`を使用した方法の2種類がある  

値のバインドをメソッドのパラメーターとして行う場合の例

```java
@Sql("UPDATE sample SET name = :name WHERE id = :id")
public int updateSample(String name, int id);
```

※プレースホルダの名前とメソッドパラメータの変数名は完全一致している必要があり、Atom SQLはSQL内の全プレースホルダがメソッドパラメーター変数と一致しているかチェックし、一致していない場合コンパイルエラーとなる  
また、パラメーター変数の型も、後述のAtom SQLで使用可能な型のみ指定可能となっている  

- Atom SQLで使用可能な型  
バインド可能な型はあらかじめ定められており、それ以外の型を使用することは出来ない  
使用可能な型は`jp.ats.atomsql.AtomSqlType`というenumに定義されている  

|型名|Javaクラス|SQLでの型|スレッドセーフか?|
|:--|:--|:--|:--:|
|**BIG_DECIMAL**|java.math.BigDecimal|NUMERIC|〇|
|**BINARY_STREAM**|jp.ats.atomsql.BinaryStream<br>(java.io.InputStreamのラッパークラス)|LONGVARBINARY|✕|
|**BLOB**|java.sql.Blob|BLOB|✕|
|**BOOLEAN**|java.lang.Boolean|BOOLEAN|〇|
|**P_BOOLEAN**|boolean|BOOLEAN|〇|
|**BYTE_ARRAY**|byte[]|VARBINARY<br>LONGVARBINARY|✕|
|**CHARACTER_STREAM**|jp.ats.atomsql.CharacterStream<br>(java.io.Readerのラッパークラス)|LONGVARCHAR|✕|
|**CLOB**|java.sql.Clob|CLOB|✕|
|**DOUBLE**|java.lang.Double|DOUBLE|〇|
|**P_DOUBLE**|double|DOUBLE|〇|
|**FLOAT**|java.lang.Float|REAL|〇|
|**P_FLOAT**|float|REAL|〇|
|**INTEGER**|java.lang.Integer|INTEGER|〇|
|**P_INT**|int|INTEGER|〇|
|**LONG**|java.lang.Long|BIGINT|〇|
|**P_LONG**|long|BIGINT|〇|
|**OBJECT**|任意|任意|✕|
|**STRING**|java.lang.String|VARCHAR<br>LONGVARCHAR|〇|
|**DATE**|java.time.LocalDate|DATE|〇|
|**TIME**|java.time.LocalTime|TIME|〇|
|**DATETIME**|java.time.LocalDateTime|TIMESTAMP|〇|
|**CSV<AtomSqlTypeの型>**<br>(使用可能型は後述)|jp.ats.atomsql.Csv|パラメーターの型|〇|

※P_INTとINTEGER等、プリミティブ型とラッパー型がそれぞれ使用可能であるが、使い分けとして更新などでINTEGER型のカラムにNULLをセットしたい場合はラッパー型を使用、それ以外はプリミティブ型を使用するようにすればよい  

nullがセットできるように`Long`とする

```java
@Sql("UPDATE sample SET amount = :amount WHERE id = 1")
public int updateSample(Long amount);
```

nullをセット

```java
sampleProxy.updateSample(null);
```

- __@NonThreadSafe__  
上記表でスレッドセーフではないとされている型には`@NonThreadSafe`アノテーションが付与されている  
このアノテーションが付与されている型はimmutableではない値であるため、Atom内部で値を保持したまま別スレッドで操作を行う等をした場合、不正な状態になる可能性がある  
そのため、これらの型を使用するには`jp.ats.atomsql.AtomSql#tryNonThreadSafe`内でのみ使用が可能となる  

定義

```java
@Sql("UPDATE sample SET blobColumn = :blob WHERE id = 1")
public int updateSample(Blob blob);
```

使用

```java
atomSql.tryNonThreadSafe(() -> {
    sampleProxy.updateSample(blobValue);
});
// tryNonThreadSafeの外で実行するとNonThreadSafeExceptionが発生

```

- CSV型について  
SQLのWHERE等にINを使用して複数の値をバインドしようとした際、プレースホルダの数が固定ではない場合、`jp.ats.atomsql.Csv`を使用することでカンマ区切り複数プレースホルダの設定が可能となる  
Csvで使用可能な型は上記表でスレッドセーフであるとされている型かつプリミティブではない型のみ（後述）であり、それ以外の型もしくは他クラスのオブジェクトを渡した場合、`IllegalArgumentException`がスローされる  

定義

```java
@Sql("SELECT * FROM sample WHERE id IN (:ids)")
public List<SampleInfo> selectSample(Csv<Long> ids);
```

使用

```java
sampleProxy.selectSample(Csv.of(1, 2, 3)).forEach(r -> {
    System.out.println(r.id());
});

//個数を変えても正しいSQLに変換される
sampleProxy.selectSample(Csv.of(1, 2, 3, 4, 5)).forEach(r -> {
    System.out.println(r.id());
});
```

- `jp.ats.atomsql.Csv`で使用可能な型一覧  

|型名|Javaクラス|
|:--|:--|
|**BIG_DECIMAL**|java.math.BigDecimal|  
|**BOOLEAN**|java.lang.Boolean|  
|**DOUBLE**|java.lang.Double|  
|**FLOAT**|java.lang.Float|  
|**INTEGER**|java.lang.Integer|  
|**LONG**|java.lang.Long|  
|**STRING**|java.lang.String|  
|**DATE**|java.time.LocalDate|  
|**TIME**|java.time.LocalTime|  
|**DATETIME**|java.time.LocalDateTime|  

- __@SqlParameters__  
長いSQL、項目の多いテーブルに対するINSERT文等、プレースホルダの数が多い場合、メソッドのパラメーターにすべて記述するのが煩雑になる場合がある  
そのような場合、`@SqlParameters`アノテーションを使用することで、SQL内の全プレースホルダをフィールドとして持つ値保持用クラスがAtom SQLにより自動生成され、利用者はそのインスタンスにバインドする値をセットすることでSQLに値を渡すことが可能となる  
使用方法は  
1. SqlProxyのSQL実施メソッドに`@SqlParameters`アノテーションを付与

```java
@Sql("UPDATE sample SET name = :name WHERE id = :id")
@SqlParameters
public int updateSample();
```

2. そのメソッドのパラメーターを`java.util.function.Consumer`とする

```java
@Sql("UPDATE sample SET name = :name WHERE id = :id")
@SqlParameters
public int updateSample(Consumer<?> consumer);
```

3. Consumerの型パラメーターに自動生成されるクラス名となる任意の文字列を記述する

```java
@Sql("UPDATE sample SET name = :name WHERE id = :id")
@SqlParameters
public int updateSample(Consumer<SampleParameters> consumer);
```

※このようにすることでSqlProxyと同じパッケージ内に`SampleParameters`という名前のクラスが自動生成される  
自動生成されたクラスにはフィールドとして全プレースホルダが定義されており、上の例では`name`と`id`というフィールドが生成されていることになる  

実際に値をバインドするには以下のようにする  

```java
sampleProxy.updateSample(p -> {
    p.name = "name";
    p.id = 1;
});
```

- `@SqlParameters`の型ヒント  
`@SqlParameters`はそのまま使用した場合、フィールドは全てObject型となる  
実行時バインドした値の型により実際に使用される型が決定するためエラーとはならないが、Object型にはどのようなクラスの値もセットできてしまうため、型を指定し制約を強くした方が安全  
そのための機能として`@SqlParameters`には型ヒントの指定機能があり、以下の2つの方法で型ヒントの指定を行うことが出来る  

1. インライン型ヒント  
SQL内に型のヒントを直接記述する  

```java
@Sql("UPDATE sample SET name = :name/*STRING*/ WHERE id = :id/*P_LONG*/")
@SqlParameters
public int updateSample(Consumer<SampleParameters> consumer);
```

※同一のプレースホルダを複数使用している場合はどこか一か所に型ヒントを記述すればよい  

CSV型を使用する場合  

```java
@Sql("SELECT * FROM sample WHERE id IN (:ids/*CSV<LONG>*/)")
@SqlParameters
public int updateSample(Consumer<SampleParameters> consumer);
```

※CSV型を使用する場合は型パラメーターを記述する必要がある  
型パラメーターに指定可能な型は、スレッドセーフかつプリミティブではない型（前述）  

2. `@TypeHint`アノテーション  
`@SqlParameters`のパラメーターとして`@TypeHint`を設定する  

```java
@Sql("UPDATE sample SET name = :name WHERE id = :id")
@SqlParameters(typeHints = {
    @TypeHint(name = "name", type = AtomSqlType.STRING),
    @TypeHint(name = "id", type = AtomSqlType.P_LONG),
})
public int updateSample(Consumer<SampleParameters> consumer);
```

CSV型を使用する場合  

```java
@Sql("SELECT * FROM sample WHERE id IN (:ids)")
@SqlParameters(typeHints = {
    @TypeHint(name = "ids", type = AtomSqlType.CSV, typeArgument=AtomSqlType.LONG),
})
public int updateSample(Consumer<SampleParameters> consumer);
```

※インライン型ヒントはSQLの見通しが悪くなりがちなので、そのような場合は`@TypeHint`アノテーションを使用する

※型ヒントはあくまでヒントであり、SQL内のプレースホルダ全てに型ヒントが設定されていなくてもコンパイルエラーとはならないので指定漏れには注意が必要  

### SQL文の編集  
SELECT文のWHERE句が可変であったり、SQL内に同じ箇所が複数出現するため、一度定義したものを展開するようにしたい等SQLを編集したい場合がある  
Atom SQLではそのような場合、SELECT文やINSERT、UPDATE文の戻り値を、バインド値込みのSQL実行中間形態である`jp.ats.atomsql.Atom`とし、複数のAtom同士を結合、変数展開することで実現している  
- Atomの取得  
AtomはSELECT文、INSERT、UPDATE文のどちらでも戻り値として指定することで取得が可能となる  
```java
@Sql("UPDATE sample SET name = :name WHERE id = :id")
public Atom<?> updateSample(String name, long id);
```

- Atomで可能な編集  
1. Atom同士の結合  
`Atom#and(Atom)`  
`Atom#or(Atom)`  
`Atom#concat(Atom...)`  
`Atom#joinAndConcat(Atom, Atom...)`  
`Atom#join(Atom, List<Atom>)`  

※使い方の詳細はそれぞれのjavadocを確認のこと

2. 変数展開  
SQL内の任意の箇所に変数を記述し、そこに他のAtomを挿入することが可能  
変数の書式は`/*${変数}*/`となっており、変数には以下の2種類が使用できる  

  - インデックス値を使用した変数展開  
変数として配列のインデックス値を記述する方式  
メソッド`Atom#put(Atom...)`を使用して変数展開を行う  
展開場所は、メソッドの引数の配列のインデックスとなる  

定義

```java
// このAtomがメインとなって検索を実施するため、検索結果の入れ物クラスを型パラメーターに記述
@Sql("SELECT /*${0}*/ FROM sample /*${1}*/")
public Atom<SampleInfo> main();

// ここで生成されるAtomは単なるパーツなので、型パラメーターは ? でよい
@Sql("COUNT(*)")
public Atom<?> selectCount();

// ここで生成されるAtomは単なるパーツなので、型パラメーターは ? でよい
@Sql("WHERE id = :id")
public Atom<?> where(int id);
```

使用

```java
var main = sampleProxy.main();

var select = sampleProxy.selectCount();

var where = sampleProxy.where(1);

// selectは${0}、whereは${1}に展開される
main.put(select, where).list().forEach(r -> {
    System.out.println(r.id());
});
```

  - 任意の文字列を使用した変数展開  
変数に任意の文字列を記述する方式  
メソッド`Atom#put(Map<Atom>)`を使用して変数展開を行うことも可能だが、次に紹介する`@SqlInterpolation`アノテーションを使用する方が記述が簡単になるため、メソッド`Atom#put(Map<Atom>)`の使用方法の説明は割愛する  

- __@SqlInterpolation__  
SqlProxyのSQL実施メソッドに`@SqlInterpolation`アノテーションを付与することで、SQL内に記述した変数をフィールドとして持つクラスをAtom SQLが自動生成する  

定義

```java
// このAtomがメインとなって検索を実施するため、検索結果の入れ物クラスを型パラメーターに記述
// 戻り値はAtomではなく不完全なAtomであるHalfAtomを使用する
// HalfAtomの第二型パラメーターにSqlParametersと同様に任意のクラス名を記述することで
// Atom SQLがそのクラスを同一パッケージ内に生成する
@Sql("SELECT /*${selectClause}*/ FROM sample /*${whereClause}*/")
public HalfAtom<SampleInfo, SampleInterpolation> main();

// ここで生成されるAtomは単なるパーツなので、型パラメーターは ? でよい
@Sql("COUNT(*)")
public Atom<?> selectCount();

// ここで生成されるAtomは単なるパーツなので、型パラメーターは ? でよい
@Sql("WHERE id = :id")
public Atom<?> where(int id);
```

使用

```java
var main = sampleProxy.main();

var select = sampleProxy.selectCount();

var where = sampleProxy.where(1);

main.put(i -> {
    i.selectClause = select;
    i.whereClause = where;
}).list().forEach(r -> {
    System.out.println(r.id());
});
```

- AtomでのSQL実施  
Atomを使用したSQLの編集を完了させた後実際にSQLを実施したい場合は以下のようにする  

```java
// 検索を行い、結果をListで取得する
// このAtomを作り出すメソッドの戻り型の型パラメーターに検索結果の入れ物クラスが
// セットされていなければならない
atom.list();

// 検索を行い、結果をStreamで取得する
// このAtomを作り出すメソッドの戻り型の型パラメーターに検索結果の入れ物クラスが
// セットされていなければならない
atom.stream();

// 検索を行い、結果をOptionalで取得する
// このAtomを作り出すメソッドの戻り型の型パラメーターに検索結果の入れ物クラスが
// セットされていなければならない
atom.get();

// INSERT, UPDATE等更新を実施し、変更された件数を取得する
// このAtomを作り出すメソッドの戻り型はAtom<?>でOK
atom.update();
```

- SQL編集用定数  
SQLのパーツとして使用できるAtomオブジェクトがAtomクラスにstaticな値として用意されている  
詳細は`jp.ats.atomsql.Atom`クラスを参照のこと  


### defaultメソッド  
SqlProxyインターフェイス内でdefaultメソッドを定義することにより、SQL発行のためのプレースホルダバインド値編集など、定型処理を記述することが出来る  
  - __@SqlProxySupplier__  
defaultメソッド内で他のSqlProxyを使用して結果を合成したい場合、`@SqlProxySupplier`を付与したメソッドを自身のSqlProxy内に作成する  
メソッドの戻り値の型を使用したいSqlProxyの型とすることで、Atom SQLがそのProxyのインスタンスを生成、返す  

### 開発中のSQLログ出力  
`jp.ats.atomsql.Configure#enableLog()`を`true`として起動した場合、出力したSQL文がログにINFOレベルとして出力されるようになる  
SQL実行開始時刻、終了時刻、SQLの呼び出しスタックトレース、プレースホルダバインド後のSQL文等が出力される  
しかし、バインド値のなかに機密情報(暗号化キー等)がありログには出力したくない場合、以下のアノテーションを使用することで出力を抑制できる  

- __@ConfidentialSql__  
SqlProxyのSQL実施メソッドに`@SqlInterpolation`アノテーションを付与することで、Atom SQLが出力するログにプレースホルダバインド後のSQL文が出力されないようになる  
`@ConfidentialSql`のみの付与の場合、全ての値が出力されなくなるが、もっと細やかに制御したい場合、アノテーションにプレースホルダ名を指定することで、指定された値のみ出力されないようにすることも可能  

```java
// secretとnameともログに<<CONFIDENTIAL>>と出力される
@ConfidentialSql
@Sql("UPDATE sample SET secret = :secret, name = :name WHERE id = 1")
public int updateSample1(String secret);

// secretのみログに<<CONFIDENTIAL>>と出力される
@ConfidentialSql("secret")
@Sql("UPDATE sample SET secret = :secret, name = :name WHERE id = 1")
public int updateSample2(String secret);
```
### Atom SQL Demoプロジェクト
その他使用方法を確認する場合は  
[atom-sql-demo](https://github.com/ats-jp/atom-sql-demo)  
を参照のこと  
