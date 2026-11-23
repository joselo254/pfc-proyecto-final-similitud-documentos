# Informe de corrección

Fundamentos de Programación Funcional y Concurrente — Universidad del Valle

## 0. Notación y método

Un documento es $d = \langle id, ts \rangle$ con $ts = \langle w_0, \ldots, w_{t-1} \rangle$
y $t = |ts|$. Para $k \geq 1$, el fragmento que empieza en $i$ es

```math
g_i = \langle w_i, w_{i+1}, \ldots, w_{i+k-1} \rangle, \qquad 0 \leq i \leq t-k
```

La huella es $H_k(d) = \{ g_i \mid 0 \leq i \leq t-k \}$, el índice de Jaccard es
$J(A,B) = |A \cap B| / |A \cup B|$ con $J(\emptyset,\emptyset) = 0$, y el índice
invertido es $I_k(C) = \{ g \mapsto \{ i \mid g \in H_k(d_i) \} \}$.

**Método.** Para cada función se da primero su especificación y luego el
argumento:

- La única función recursiva, `fragmentos`, se demuestra por **inducción
  estructural** sobre la longitud del vector.
- Las funciones construidas con `foldLeft` se demuestran con una **invariante**
  del acumulador.
- Las demás se demuestran con la **semántica de las operaciones de orden
  superior** que usan (`map` conserva longitud y orden, `filter` conserva los
  elementos que cumplen el predicado, etc.).

Todas las funciones son totales sobre sus entradas válidas y no usan estado
mutable, por lo que el resultado depende solo de los argumentos.

## 1. `normalizar`

**Especificación.** `normalizar(s)` es la secuencia, en orden, de las palabras
de `s` en minúsculas, entendiendo por palabra una subcadena maximal formada
solo por letras y dígitos. Cualquier otro carácter es separador y no hay
palabras vacías.

**Argumento.**

1. `toLowerCase` convierte cada carácter a minúscula sin cambiar cuáles son
   letras o dígitos.
2. `replaceAll("[^\\p{L}\\p{Nd}]", " ")` sustituye por un espacio exactamente los
   caracteres que no son letra (`\p{L}`, que incluye tildes y la ñ) ni dígito
   decimal (`\p{Nd}`). El resultado solo contiene letras, dígitos y espacios, y
   las palabras son exactamente las corridas maximales de caracteres distintos
   del espacio.
3. `split(" ")` corta en cada espacio. Cada corrida maximal queda como un
   elemento; las secuencias de espacios consecutivos (o un espacio inicial)
   producen elementos vacíos.
4. `filter(_.nonEmpty)` elimina esos elementos vacíos y conserva el orden.

Por tanto el resultado cumple la especificación. Para una cadena vacía o con
solo separadores, el resultado es `Vector()`.

*Nota.* `toLowerCase()` usa la configuración regional por defecto de la JVM; en
español no cambia el resultado.

## 2. `fragmentos`

**Especificación.** Para $k \geq 1$:

```math
F(ts, k) = \begin{cases} \langle g_0, g_1, \ldots, g_{t-k} \rangle & \text{si } t \geq k \\ \langle \rangle & \text{si } t < k \end{cases}
```

y $F(ts, k) = \langle \rangle$ si $k < 1$ (valor no válido).

**Programa.**

```scala
def fragmentos(ts: Vector[String], k: Int): Vector[Fragmento] = {
  if (ts.length < k || k < 1) Vector()
  else ts.take(k) +: fragmentos(ts.tail, k)
}
```

**Teorema.** Para todo $k$ y todo vector $ts$ de longitud $t \geq 0$,
`fragmentos(ts, k)` $= F(ts, k)$.

**Caso $k < 1$.** La guarda es verdadera y devuelve $\langle\rangle$, que es lo
especificado. Desde aquí suponemos $k \geq 1$ y hacemos inducción sobre $t$.

**Caso base: $t < k$.** La guarda `ts.length < k` es verdadera y el programa
devuelve $\langle\rangle$. Por otro lado, no existe $i$ con $0 \leq i \leq t-k$,
así que $F(ts,k) = \langle\rangle$.

**Caso de inducción: $t \geq k$** (por lo tanto $t \geq 1$). La guarda es falsa
y el programa devuelve `ts.take(k) +: fragmentos(ts.tail, k)`.

- Como $t \geq k$, `ts.take(k)` $= \langle w_0, \ldots, w_{k-1} \rangle = g_0$.
- Sea $ts' = ts.tail = \langle w_1, \ldots, w_{t-1} \rangle$, de longitud $t - 1 < t$.
  Su fragmento que empieza en $i$ es
  $g'_i = \langle w_{i+1}, \ldots, w_{i+k} \rangle = g_{i+1}$.

Hipótesis de inducción (HI): `fragmentos(ts', k)` $= F(ts', k)$. Hay dos casos:

- Si $t = k$: $t - 1 = k - 1 < k$, así que por la HI `fragmentos(ts', k)` $= \langle\rangle$ y el resultado es
  $\langle g_0 \rangle$, que es $\langle g_0, \ldots, g_{t-k} \rangle$ porque $t - k = 0$.
- Si $t > k$: $t - 1 \geq k$, así que por la HI
  `fragmentos(ts', k)` $= \langle g'_0, \ldots, g'_{t-1-k} \rangle = \langle g_1, \ldots, g_{t-k} \rangle$, y al anteponer $g_0$ el resultado es
  $\langle g_0, g_1, \ldots, g_{t-k} \rangle$.

En ambos casos el programa devuelve $F(ts,k)$. $\blacksquare$

**Terminación.** En cada llamado recursivo la longitud del vector baja en uno
($|ts.tail| = |ts| - 1$), de modo que tarde o temprano es menor que $k$ y se
alcanza el caso base. La guarda $k < 1$ evita que con $k \leq 0$ nunca se
cumpla `ts.length < k`.

**Traza por sustitución**, con $ts = \langle a, b, c \rangle$ y $k = 2$:

```math
\begin{aligned}
&\text{fragmentos}(\langle a,b,c \rangle, 2) \\
&\to \langle a,b \rangle +: \text{fragmentos}(\langle b,c \rangle, 2) \\
&\to \langle a,b \rangle +: (\langle b,c \rangle +: \text{fragmentos}(\langle c \rangle, 2)) \\
&\to \langle a,b \rangle +: (\langle b,c \rangle +: \langle\rangle) \\
&\to \langle a,b \rangle +: \langle \langle b,c \rangle \rangle \\
&\to \langle \langle a,b \rangle, \langle b,c \rangle \rangle
\end{aligned}
```

Coincide con $F(ts,2) = \langle g_0, g_1 \rangle$, con $t - k + 1 = 2$ fragmentos.

## 3. `huella` y la propiedad $|H_k(d)| \leq t - k + 1$

**Especificación.** $H_k(d) = \{ g_i \mid 0 \leq i \leq t-k \}$.

**Argumento.** `huella(d, k)` es `fragmentos(palabrasDoc(d), k).toSet`. Por la
corrección de `fragmentos`, la secuencia es $\langle g_0, \ldots, g_{t-k} \rangle$
(o vacía), y `toSet` produce el conjunto de sus elementos, que es por definición
$H_k(d)$. Un fragmento repetido aparece una sola vez en el conjunto.

**Propiedad 1.** Si $t \geq k$, entonces $|H_k(d)| \leq t - k + 1$, con igualdad
si y solo si el documento no repite ningún fragmento.

*Demostración.* Sea $m = t - k + 1$ el número de fragmentos de la secuencia, y
sea $\varphi : \{0, \ldots, m-1\} \to H_k(d)$ con $\varphi(i) = g_i$. Por
definición de $H_k(d)$, $\varphi$ es sobreyectiva, de modo que $|H_k(d)| \leq m$.
Una función sobreyectiva entre dos conjuntos finitos del mismo tamaño es
inyectiva, así que $|H_k(d)| = m$ si y solo si $\varphi$ es inyectiva, es decir,
si y solo si $g_i \neq g_j$ para todo $i \neq j$: ningún fragmento se repite.
$\blacksquare$

Si $t < k$ la huella es vacía y $|H_k(d)| = 0$; en general
$|H_k(d)| \leq \max(t - k + 1, 0)$.

## 4. `huellas`

**Especificación.** `huellas(c, k)` es el vector $\langle H_k(d_0), \ldots, H_k(d_{n-1}) \rangle$.

**Argumento.** `c.map(doc => huella(doc, k))` produce un vector de la misma
longitud que `c`, cuyo elemento $i$ es `huella(c(i), k)` (propiedad de `map`: conserva
longitud y orden). Por la corrección de `huella`, ese elemento es $H_k(d_i)$.
$\blacksquare$

## 5. `jaccard`

**Especificación.** $J(A,B) = |A \cap B| / |A \cup B|$ si $A \cup B \neq \emptyset$,
y $J(\emptyset, \emptyset) = 0$.

**Programa.**

```scala
val union = h1.union(h2).size.toDouble
val interseccion = h1.intersect(h2).size.toDouble
if (union == 0.0) 0.0 else interseccion / union
```

**Argumento.**

- `union` y `interseccion` son $|A \cup B|$ y $|A \cap B|$ por la definición de
  `union` e `intersect` sobre conjuntos.
- Si `union == 0.0` entonces $A \cup B = \emptyset$, lo que implica
  $A = B = \emptyset$; el programa devuelve $0.0$, que es el valor especificado y
  evita la división $0/0$.
- Si no, devuelve $|A \cap B| / |A \cup B|$, con denominador distinto de cero.

Además $A \cap B \subseteq A \cup B$, así que el resultado está siempre en
$[0, 1]$: vale $1$ exactamente cuando $A = B \neq \emptyset$ y $0$ cuando
$A \cap B = \emptyset$. $\blacksquare$

*Nota.* Como $|A \cup B| = |A| + |B| - |A \cap B|$, el denominador también se
puede calcular sin construir el conjunto unión; los valores son los mismos.

## 6. `parejas`

**Especificación.** $P(n) = \langle (i,j) \mid 0 \leq i < j < n \rangle$, en orden
lexicográfico, sin repetidas y con $n(n-1)/2$ elementos.

**Programa.** El `for` con `yield` equivale a

```scala
(0 until n).flatMap(i => ((i + 1) until n).map(j => (i, j))).toVector
```

**Argumento.**

- *Contenido.* El par $(i,j)$ se produce si y solo si $0 \leq i < n$ y
  $i + 1 \leq j < n$. Esto equivale a $0 \leq i < j < n$ (de $i < j < n$ se
  sigue $i < n$). No se produce ningún par con $i = j$ ni con $i > j$.
- *Sin repeticiones.* Cada par corresponde a un único valor de $i$ en el
  generador externo y a un único valor de $j$ en el interno.
- *Orden.* El generador externo recorre $i$ de menor a mayor y, para cada $i$,
  el interno recorre $j$ de menor a mayor, y `flatMap` concatena en ese orden:
  el resultado queda ordenado lexicográficamente.
- *Cantidad.* Para cada $i$ hay $n - 1 - i$ valores de $j$, y

```math
\sum_{i=0}^{n-1} (n - 1 - i) = \sum_{m=0}^{n-1} m = \frac{n(n-1)}{2}
```

Para $n \in \{0, 1\}$ la suma es vacía y el resultado es el vector vacío.
$\blacksquare$

## 7. `similitudes`

**Especificación.** Para cada pareja $(i,j)$ de `ps`, en el mismo orden, la
medición $(i, j, J(H_i, H_j))$, donde $H_i$ es la huella en la posición $i$.

**Argumento.** `ps.map { case (i, j) => (i, j, jaccard(hs(i), hs(j))) }`
conserva la longitud y el orden de `ps`. Por la corrección de `jaccard`, la
tercera componente es $J(H_i, H_j)$. La función exige que los índices de `ps`
estén en el rango de `hs` (precondición del enunciado: las parejas salen de
`parejas(n)` o del índice, ambas con índices válidos). $\blacksquare$

## 8. `indiceInvertido`

**Especificación.** $I(hs) = \{ g \mapsto \{ i \mid g \in H_i \} \}$, con una
entrada por cada fragmento que aparece en alguna huella.

**Programa.** Un `foldLeft` externo sobre `hs.zipWithIndex` y uno interno sobre
los fragmentos de cada huella, con `acumulado.updated(g, acumulado.getOrElse(g, Set()) + posicion)`.

**Invariante externa.** Sea $M_m$ el acumulador después de procesar las huellas
de las posiciones $0, \ldots, m-1$. Se cumple

```math
\text{Inv}(m): \quad \operatorname{dom}(M_m) = \{ g \mid \exists i < m : g \in H_i \} \ \land \ \forall g \in \operatorname{dom}(M_m) : M_m(g) = \{ i < m \mid g \in H_i \}
```

- *Inicio:* $M_0$ es el mapa vacío y no hay $i < 0$, así que $\text{Inv}(0)$ vale.
- *Conservación.* Se procesa $H_m$ con el `foldLeft` interno. Si $S \subseteq H_m$
  es el conjunto de fragmentos ya visitados, el acumulador $M'$ cumple:
  $\operatorname{dom}(M') = \operatorname{dom}(M_m) \cup S$, y para cada $g$,
  $M'(g) = M_m(g) \cup \{m\}$ si $g \in S$ y $M'(g) = M_m(g)$ si no. Con $S = \emptyset$
  es cierto, y al visitar un nuevo fragmento $g$ la operación
  `updated(g, getOrElse(g, Set()) + m)` agrega $m$ al conjunto de $g$ (o crea la
  entrada con $\{m\}$). Al terminar, $S = H_m$ y entonces
  $\operatorname{dom}(M_{m+1}) = \{ g \mid \exists i < m+1 : g \in H_i \}$ y
  $M_{m+1}(g) = \{ i < m + 1 \mid g \in H_i \}$, es decir, $\text{Inv}(m+1)$.
- *Final:* al terminar el `foldLeft` externo, $m = n$ y $\text{Inv}(n)$ es
  exactamente la especificación.

**Terminación.** Ambos `foldLeft` recorren colecciones finitas.

**Independencia del orden.** Agregar $m$ a un conjunto es una operación
conmutativa, asociativa e idempotente (la unión de conjuntos lo es), de modo que
el resultado no depende del orden en que se visiten los fragmentos de una huella.
$\blacksquare$

## 9. `parejasCandidatas`

**Especificación.** El conjunto
$\{ (i,j) \mid i < j \land \exists g : \{i,j\} \subseteq I[g] \}$, sin
repetidas y en orden lexicográfico.

**Argumento.** Para cada entrada del índice, con conjunto de documentos $D$:

1. `documentos.toVector.sorted` es una secuencia estrictamente creciente
   $o_0 < o_1 < \cdots < o_{m-1}$ de los elementos de $D$, con $m = |D|$.
2. `parejas(m)` entrega los pares de posiciones $(a,b)$ con $0 \leq a < b < m$,
   y el `map` los traduce a $(o_a, o_b)$. Como $o$ es estrictamente creciente,
   $o_a < o_b$ si y solo si $a < b$, y la traducción es biyectiva. El resultado
   de la entrada es entonces $\{ (x,y) \mid x, y \in D, \ x < y \}$.
3. Si $|D| \leq 1$ no hay pares, así que un fragmento que aparece en un solo
   documento no genera parejas.

`flatMap` concatena los resultados de todas las entradas. El conjunto de
elementos es la unión sobre $g$ de $\{ (x,y) \mid x<y, \ x,y \in I[g] \}$, que es
exactamente la especificación. Un mismo par puede venir de varias entradas;
`distinct` deja una sola copia y `sorted` ordena con el orden lexicográfico de
pares de enteros. $\blacksquare$

## 10. `ranking`

**Especificación.** De las mediciones, quedarse con las de similitud $\geq \tau$,
ordenarlas de mayor a menor similitud (desempatando por $(i,j)$ ascendente) y
tomar las primeras $p$.

**Argumento.**

- `filter(_._3 >= umbral)` conserva exactamente las mediciones con similitud
  $\geq \tau$ (el umbral es inclusivo).
- El comparador de `sortWith` dice que $a$ va antes que $b$ si
  $s_a > s_b$, o $s_a = s_b$ y $i_a < i_b$, o además $i_a = i_b$ y $j_a < j_b$.
  Es un orden estricto y total sobre mediciones con pareja $(i,j)$ distinta, de
  modo que el resultado ordenado es **único**, sin importar el orden en que
  llegaron las mediciones. Esto es lo que garantiza que dos ejecuciones con las
  mismas mediciones den el mismo ranking.
- `take(p)` devuelve los primeros $\min(p, \text{longitud})$ elementos: todo el
  vector si $p$ es mayor que su longitud, y el vector vacío si $p \leq 0$.
$\blacksquare$

## 11. `detectarDirecto`

```scala
val hs = huellas(c, k)
val ps = parejas(c.length)
val ss = similitudes(hs, ps)
ranking(ss, umbral, p)
```

Por las secciones 4, 6, 7 y 10:

- `hs` son las huellas $H_k(d_0), \ldots, H_k(d_{n-1})$.
- `ps` son todas las parejas $(i,j)$ con $i < j$.
- `ss` tiene la medición $(i, j, J(H_i, H_j))$ de cada una.
- `ranking` entrega las que cumplen $J \geq \tau$, ordenadas y truncadas a $p$.

Esto es exactamente el ranking $R$ de la sección 1.1.5 del enunciado. $\blacksquare$

## 12. `detectarConIndice` y la propiedad 2

**Propiedad 2.** Si $(i,j)$ con $i < j$ no es candidata, entonces
$J(H_k(d_i), H_k(d_j)) = 0$.

*Demostración.* Si $(i,j)$ no es candidata, no existe $g$ con
$\{i,j\} \subseteq I[g]$. Supongamos que existiera $g \in H_k(d_i) \cap H_k(d_j)$.
Por definición del índice, $i \in I[g]$ y $j \in I[g]$, lo que haría a $(i,j)$
candidata: contradicción. Entonces $H_k(d_i) \cap H_k(d_j) = \emptyset$, el
numerador de $J$ es $0$ y $J = 0$ (también si la unión es vacía, por la
convención). $\blacksquare$

**Equivalencia con la solución directa.** `detectarConIndice` calcula
`ps = parejasCandidatas(indiceInvertido(hs))` y sigue igual que
`detectarDirecto`. Sea $M_D$ el vector de mediciones de **todas** las parejas y
$M_I$ el de las **candidatas**; $M_I \subseteq M_D$ con los mismos valores. Toda
medición de $M_D$ que no está en $M_I$ es de una pareja no candidata, y por la
propiedad 2 su similitud es $0$.

Si $\tau > 0$, esas mediciones tienen $0 < \tau$ y las elimina el `filter` de
`ranking`. Por lo tanto el conjunto de mediciones que sobrevive al filtro es el
mismo en los dos casos. Como el orden final de `ranking` es único para un mismo
conjunto de mediciones (sección 10), y `take(p)` toma el mismo prefijo,

```math
\texttt{detectarDirecto}(c,k,\tau,p) = \texttt{detectarConIndice}(c,k,\tau,p) \qquad \forall\, \tau > 0
```

**El caso $\tau = 0$ es distinto, y es lo esperado.** Con $\tau = 0$ el filtro
`>= 0` deja pasar también las parejas de similitud cero: `detectarDirecto`
las incluye y `detectarConIndice` no, porque nunca las compara. Una de las
pruebas propias documenta este comportamiento. $\blacksquare$

## 13. Casos de prueba

Las pruebas son de ScalaTest, están en `app/src/test/scala/proyecto/SimilitudTest.scala`
y se ejecutan con `./gradlew test`, que deja el reporte en
`app/build/reports/tests/test/index.html`.

El archivo contiene las 12 pruebas de contrato del enunciado (una por función,
con los valores de los dos ejemplos) y **cinco casos propios por cada una de las
doce funciones**, distintos de esos ejemplos:

| Función | Qué cubren los casos propios |
|---|---|
| `normalizar` | Mayúsculas, tildes y puntuación; espacios, tabulaciones y saltos de línea; números y guion bajo; solo símbolos; texto ya limpio. |
| `fragmentos` | Documento menor que `k`; `k < 1`; vector vacío; longitud igual a `k`; `k = 1`. |
| `huella` | Fragmentos repetidos; documento menor que `k`; `k = 1`; longitud igual a `k`; el orden genera fragmentos distintos. |
| `huellas` | Corpus vacío; un documento; orden del corpus; texto menor que `k`; documentos duplicados. |
| `jaccard` | Huellas iguales; disjuntas; ambas vacías; una vacía; solapamiento parcial. |
| `parejas` | `n = 0`, `1`, `2`, `3` y `6`. |
| `similitudes` | Intersección parcial; orden recibido; huellas idénticas; huellas vacías; sin parejas. |
| `indiceInvertido` | Corpus vacío; una huella; fragmento en tres documentos; huella vacía intermedia; fragmentos distintos. |
| `parejasCandidatas` | Dos grupos independientes; índice vacío; entrada con un solo documento; todas las combinaciones de una entrada; repetidas y orden. |
| `ranking` | Umbral inclusivo; desempate por posición; tope uno; sin mediciones; tope cero. |
| `detectarDirecto` | Desempate y orden; truncado por `p`; umbral inclusivo; umbral `0.0` (incluye similitud cero); copias exactas con `corpusConCopias`. |
| `detectarConIndice` | Parejas candidatas; umbral más alto; sin fragmentos en común; corpus vacío, de un documento o más corto que `k`; copias al 50 % y coincidencia con la directa. |

Las pruebas con `corpusConCopias` usan el generador con una pareja de respuesta
conocida: si la copia toma la fracción $f$ inicial de un documento, la pareja
$(i, \text{copia de } i)$ queda de primera en el ranking. Con $f = 1.0$ la
similitud es exactamente $1$; con $f = 0.5$ y $k = 4$ se espera cerca de
$27/87 \approx 0{,}31$.

*Última ejecución de `./gradlew test`:* 72 pruebas de `SimilitudTest` aprobadas, 0 fallidas.
