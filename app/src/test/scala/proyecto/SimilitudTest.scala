package proyecto

import org.scalatest.funsuite.AnyFunSuite
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import Similitud._

/**
 * Pruebas de contrato de la versión secuencial: una por función, con los
 * valores de los dos ejemplos del enunciado. No se modifican.
 *
 * La rúbrica pide además cinco casos propios por función, distintos de estos y
 * de los ejemplos del enunciado. Se agregan al final de este archivo.
 */
@RunWith(classOf[JUnitRunner])
class SimilitudTest extends AnyFunSuite {

  val eps = 1e-9

  // Ejemplo 1 del enunciado, con las palabras ya normalizadas para que estas
  // pruebas no dependan unas de otras.
  val d0: Documento = ("d0", Vector("el", "gato", "duerme", "en", "el", "sofa"))
  val d1: Documento = ("d1", Vector("el", "gato", "duerme", "en", "la", "alfombra"))
  val d2: Documento = ("d2", Vector("la", "lluvia", "moja", "el", "parque"))
  val c1: Corpus = Vector(d0, d1, d2)

  // Ejemplo 2 del enunciado.
  val e0: Documento = ("e0", Vector("la", "funcion", "recursiva", "termina",
    "cuando", "llega", "al", "caso", "base"))
  val e1: Documento = ("e1", Vector("toda", "funcion", "recursiva", "termina",
    "cuando", "llega", "al", "caso", "base"))
  val e2: Documento = ("e2", Vector("el", "paralelismo", "divide", "el",
    "trabajo", "en", "tareas", "independientes"))
  val e3: Documento = ("e3", Vector("el", "paralelismo", "divide", "el",
    "trabajo", "en", "hilos", "independientes"))
  val c2: Corpus = Vector(e0, e1, e2, e3)

  test("normalizar: baja a minúsculas y parte en palabras") {
    assert(normalizar("El Gato Duerme") == Vector("el", "gato", "duerme"))
  }

  test("fragmentos: son las ventanas consecutivas, en orden") {
    val ts = Vector("a", "b", "c", "d")
    assert(fragmentos(ts, 2) ==
      Vector(Vector("a", "b"), Vector("b", "c"), Vector("c", "d")))
  }

  test("huella: el ejemplo 1 tiene 4, 4 y 3 fragmentos") {
    assert(huella(d0, 3).size == 4)
    assert(huella(d1, 3).size == 4)
    assert(huella(d2, 3).size == 3)
  }

  test("huellas: hay una huella por documento") {
    assert(huellas(c1, 3).length == 3)
  }

  test("jaccard: ejemplo 2, las dos parejas dan 0.75 y 0.5") {
    assert(math.abs(jaccard(huella(e0, 3), huella(e1, 3)) - 0.75) < eps)
    assert(math.abs(jaccard(huella(e2, 3), huella(e3, 3)) - 0.5) < eps)
  }

  test("parejas: hay n (n - 1) / 2") {
    assert(parejas(5).length == 10)
  }

  test("similitudes: el ejemplo 1 da 1/3, 0 y 0") {
    val hs = huellas(c1, 3)
    val ms = similitudes(hs, parejas(3)).map(_._3)
    assert(math.abs(ms(0) - 2.0 / 6.0) < eps && ms(1) == 0.0 && ms(2) == 0.0)
  }

  test("indiceInvertido: el fragmento inicial de d0 está en d0 y en d1") {
    val idx = indiceInvertido(huellas(c1, 3))
    assert(idx(Vector("el", "gato", "duerme")) == Set(0, 1))
  }

  test("parejasCandidatas: en el ejemplo 2 quedan dos de seis") {
    val idx = indiceInvertido(huellas(c2, 3))
    assert(parejasCandidatas(idx) == Vector((0, 1), (2, 3)))
  }

  test("ranking: ordena de mayor a menor y trunca a p") {
    val ms: Vector[Medicion] = Vector((0, 1, 0.4), (0, 2, 0.1), (1, 2, 0.9))
    assert(ranking(ms, 0.0, 10).map(_._3) == Vector(0.9, 0.4, 0.1))
    assert(ranking(ms, 0.0, 2).length == 2)
  }

  test("detectar: el ejemplo 2 reporta (0, 1) con 0.75 y (2, 3) con 0.5") {
    val r = detectarDirecto(c2, 3, 0.3, 5)
    assert(r.map(m => (m._1, m._2)) == Vector((0, 1), (2, 3)))
    assert(math.abs(r(0)._3 - 0.75) < eps && math.abs(r(1)._3 - 0.5) < eps)
  }

  test("detectar: las dos soluciones coinciden en los ejemplos") {
    assert(detectarDirecto(c1, 3, 0.2, 5) == detectarConIndice(c1, 3, 0.2, 5))
    assert(detectarDirecto(c2, 3, 0.3, 5) == detectarConIndice(c2, 3, 0.3, 5))
  }

  //Pruebas funcion N°1 adicionales ----
  test("normalizar: texto mixto con mayúsculas, tildes y signos de puntuación") {
    val texto = "Mi Tio Esta ..¡en el sofá!"
    assert(normalizar(texto) == Vector("mi", "tio", "esta", "en", "el", "sofá"))
  }

  test("normalizar: espacios múltiples, tabulaciones y saltos de línea") {
    val texto = "   Hola,   mundo!\n¿Cómo   estás?   "
    assert(normalizar(texto) == Vector("hola", "mundo", "cómo", "estás"))
  }

  test("normalizar: inclusión de números y separación por símbolos especiales") {
    val texto = "Artículo 123 - versión_final.txt"
    assert(normalizar(texto) == Vector("artículo", "123", "versión", "final", "txt"))
  }

  test("normalizar: cadena vacía o con solo símbolos y puntuación") {
    val texto = "... ¡?! - ,,, "
    assert(normalizar(texto) == Vector())
  }

  test("normalizar: texto que ya está completamente limpio y en minúsculas") {
    val texto = "programacion funcional y concurrente"
    assert(normalizar(texto) == Vector("programacion", "funcional", "y", "concurrente"))
  }

  //Pruebas funcion N°2 adicionales ----
  test("fragmentos: documento menor que k devuelve vacío") {
    val ts = Vector("hola")
    assert(fragmentos(ts, 3) == Vector())
  }
  test("fragmentos:si k es menor a 1 devuelve vacio"){
    val ts=Vector("hola","como","estas")
    assert(fragmentos(ts,-1)==Vector())
  }
  test("fragmentos:vector vacio"){
    val ts=Vector()
    assert(fragmentos(ts,2)==Vector())
  }
  test("fragmentos:cantidad de palabras igual a k"){
    val ts=Vector("hola","como","estas")
    assert(fragmentos(ts,3)==Vector(Vector("hola","como","estas")))
  }
  test("fragmentos: k = 1 devuelve cada palabra individual como un fragmento") {
    val ts = Vector("uno", "dos", "tres")
    assert(fragmentos(ts, 1) == Vector(Vector("uno"), Vector("dos"), Vector("tres")))
  }

  //Pruebas funcion N°3 adicionales ----
  test("huella: elimina fragmentos duplicados si el texto repite secuencias") {
    val doc: Documento = ("doc_rep", Vector("hola", "mundo", "hola", "mundo"))
    val h = huella(doc, 2)
    assert(h.size == 2)
    assert(h == Set(Vector("hola", "mundo"), Vector("mundo", "hola")))
  }

  test("huella: documento con tamaño menor a k devuelve un Set vacío") {
    val doc: Documento = ("doc_corto", Vector("un", "texto"))
    val h = huella(doc, 3)
    assert(h.isEmpty)
  }
  test("huella: con k = 1 cada palabra única se convierte en un fragmento unitario") {
    val doc: Documento = ("doc_k1", Vector("gato", "perro", "gato", "ave"))
    val h = huella(doc, 1)
    assert(h.size == 3)
    assert(h == Set(Vector("gato"), Vector("perro"), Vector("ave")))
  }

  test("huella: documento cuyo tamaño es exactamente igual a k genera un único fragmento") {
    val doc: Documento = ("doc_exacto", Vector("programacion", "funcional", "scala"))
    val h = huella(doc, 3)
    assert(h.size == 1)
    assert(h == Set(Vector("programacion", "funcional", "scala")))
  }

  test("huella: orden de las palabras genera fragmentos distintos en el conjunto") {
    val doc: Documento = ("doc_orden", Vector("a", "b", "c", "b", "a"))
    val h = huella(doc, 2)
    assert(h.size == 4)
  }

  //Pruebas funcion N°4 adicionales ----
  test("huellas: corpus vacío da vector vacío") {
    assert(huellas(Vector(), 3).isEmpty)
  }

  test("huellas: corpus de un documento da una huella") {
    val c = Vector(("d1", Vector("a", "b", "c")))
    assert(huellas(c, 2).length == 1)
  }

  test("huellas: preserva el orden del corpus") {
    val c = Vector(("d1", Vector("a", "b")), ("d2", Vector("c", "d")))
    val hs = huellas(c, 2)
    assert(hs(0) == huella(c(0), 2))
    assert(hs(1) == huella(c(1), 2))
  }

  test("huellas: texto menor a k genera huella vacía") {
    val c = Vector(("d1", Vector("a")))
    assert(huellas(c, 3).head.isEmpty)
  }

  test("huellas: corpus duplicado da huellas iguales") {
    val doc: Documento = ("d1", Vector("a", "b", "c"))
    val hs = huellas(Vector(doc, doc), 2)
    assert(hs(0) == hs(1))
  }

  //Pruebas funcion N°5 adicionales ----
  test("jaccard: huellas iguales dan 1.0") {
    val h = Set(Vector("el", "gato"))
    assert(math.abs(jaccard(h, h) - 1.0) < eps)
  }
  test("jaccard: huellas distintas dan 0.0") {
    val h1 = Set(Vector("el", "gato"))
    val h2 = Set(Vector("la", "perra"))
    assert(math.abs(jaccard(h1, h2) - 0.0) < eps)
  }
  test("jaccard: huellas vacias dan 0.0") {
    val vacia: Huella = Set()
    assert(math.abs(jaccard(vacia, vacia) - 0.0) < eps)
  }
  test("jaccard: una huella vacia y otra no dan 0.0") {
    val h = Set(Vector("hola", "mundo"))
    val vacia: Huella = Set()
    assert(math.abs(jaccard(h, vacia) - 0.0) < eps)
  }
  test("jaccard: coincidencia parcial") {
    val h1 = Set(Vector("a", "b"), Vector("b", "c"))
    val h2 = Set(Vector("b", "c"), Vector("c", "d"))
    assert(math.abs(jaccard(h1, h2) - (1.0 / 3.0)) < eps)
  }

  //Pruebas funcion N°6 adicionales ----
  test("parejas: n = 0 no genera ninguna pareja") {
    assert(parejas(0).isEmpty)
  }
  test("parejas: n = 1 no genera parejas porque se necesitan al menos dos documentos") {
    assert(parejas(1).isEmpty)
  }
  test("parejas: n = 2 genera exactamente una pareja (0, 1)") {
    assert(parejas(2) == Vector((0, 1)))
  }
  test("parejas: n = 3 genera tres combinaciones posibles") {
    // Documentos 0 con 1, 0 con 2, y 1 con 2
    assert(parejas(3) == Vector((0, 1), (0, 2), (1, 2)))
  }
  test("parejas: cumple con la fórmula n * (n - 1) / 2 para n = 6") {
    assert(parejas(6).length == 15)
    assert(parejas(6).head == (0, 1))
    assert(parejas(6).last == (4, 5))
  }

  //Pruebas funcion N°7 adicionales ----
  test("similitudes: calcula una intersección parcial sobre la unión") {
    val hs = Vector(
      Set(Vector("a"), Vector("b")),
      Set(Vector("a"), Vector("b"), Vector("c")),
      Set(Vector("z"))
    )
    assert(similitudes(hs, Vector((0, 1))).map(_._3) == Vector(2.0 / 3.0))
  }

  test("similitudes: conserva el orden de las parejas recibidas") {
    val hs = Vector(Set(Vector("x")), Set(Vector("x")), Set(Vector("y")))
    assert(similitudes(hs, Vector((1, 2), (0, 1))) ==
      Vector((1, 2, 0.0), (0, 1, 1.0)))
  }

  test("similitudes: da uno a dos huellas no vacías idénticas") {
    val huella = Set(Vector("gato", "duerme"), Vector("en", "casa"))
    assert(similitudes(Vector(huella, huella), Vector((0, 1))) ==
      Vector((0, 1, 1.0)))
  }

  test("similitudes: da cero si ambas huellas están vacías") {
    val vacia: Huella = Set.empty
    assert(similitudes(Vector(vacia, vacia), Vector((0, 1))) ==
      Vector((0, 1, 0.0)))
  }

  test("similitudes: no produce mediciones sin parejas de entrada") {
    assert(similitudes(Vector(Set(Vector("a"))), Vector.empty).isEmpty)
  }

  //Pruebas funcion N°8 adicionales ----
  test("indiceInvertido: devuelve un mapa vacío para un corpus vacío") {
    assert(indiceInvertido(Vector.empty).isEmpty)
  }

  test("indiceInvertido: incluye todos los fragmentos de una sola huella") {
    val hs = Vector(Set(Vector("a"), Vector("b"), Vector("c")))
    assert(indiceInvertido(hs) ==
      Map(Vector("a") -> Set(0), Vector("b") -> Set(0), Vector("c") -> Set(0)))
  }

  test("indiceInvertido: registra publicaciones compartidas por tres documentos") {
    val fragmento = Vector("x", "y")
    val hs = Vector(Set(fragmento), Set(fragmento), Set(fragmento))
    assert(indiceInvertido(hs) == Map(fragmento -> Set(0, 1, 2)))
  }

  test("indiceInvertido: conserva la posición original tras una huella vacía") {
    val hs = Vector(Set(Vector("primero")), Set.empty[Fragmento], Set(Vector("tercero")))
    assert(indiceInvertido(hs) ==
      Map(Vector("primero") -> Set(0), Vector("tercero") -> Set(2)))
  }

  test("indiceInvertido: distingue fragmentos con palabras diferentes") {
    val hs = Vector(Set(Vector("a", "b")), Set(Vector("a"), Vector("b")))
    assert(indiceInvertido(hs) == Map(
      Vector("a", "b") -> Set(0),
      Vector("a") -> Set(1),
      Vector("b") -> Set(1)
    ))
  }

  //Pruebas funcion N°9 adicionales ----
  test("parejasCandidatas: combina dos grupos de documentos independientes") {
    val indice: Indice = Map(
      Vector("a") -> Set(0, 3, 5),
      Vector("b") -> Set(1, 2, 4)
    )
    assert(parejasCandidatas(indice) ==
      Vector((0, 3), (0, 5), (1, 2), (1, 4), (2, 4), (3, 5)))
  }

  test("parejasCandidatas: de un índice vacío no produce parejas") {
    assert(parejasCandidatas(Map.empty).isEmpty)
  }

  test("parejasCandidatas: no crea parejas desde una publicación individual") {
    assert(parejasCandidatas(Map(Vector("aislado") -> Set(4))).isEmpty)
  }

  test("parejasCandidatas: genera todas las combinaciones de una publicación") {
    assert(parejasCandidatas(Map(Vector("grupo") -> Set(3, 0, 2))) ==
      Vector((0, 2), (0, 3), (2, 3)))
  }

  test("parejasCandidatas: elimina repeticiones y ordena por posiciones") {
    val indice: Indice = Map(
      Vector("a") -> Set(5, 1, 3),
      Vector("b") -> Set(3, 1),
      Vector("c") -> Set(5, 1)
    )
    assert(parejasCandidatas(indice) ==
      Vector((1, 3), (1, 5), (3, 5)))
  }

  //Pruebas funcion N°10 adicionales ----
  test("ranking: incluye similitudes iguales al umbral y descarta las inferiores") {
    val mediciones = Vector((0, 1, 0.6), (0, 2, 0.59), (1, 2, 0.9))
    assert(ranking(mediciones, 0.6, 10) ==
      Vector((1, 2, 0.9), (0, 1, 0.6)))
  }

  test("ranking: desempata por las posiciones de las parejas") {
    val mediciones = Vector((2, 3, 0.7), (0, 2, 0.7), (0, 1, 0.7))
    assert(ranking(mediciones, 0.0, 10) ==
      Vector((0, 1, 0.7), (0, 2, 0.7), (2, 3, 0.7)))
  }

  test("ranking: con tope uno devuelve solo la similitud más alta") {
    val mediciones = Vector((0, 2, 0.3), (1, 2, 0.8), (0, 1, 0.5))
    assert(ranking(mediciones, 0.0, 1) == Vector((1, 2, 0.8)))
  }

  test("ranking: no devuelve resultados si no hay mediciones") {
    assert(ranking(Vector.empty, 0.0, 5).isEmpty)
  }

  test("ranking: con tope cero devuelve un vector vacío") {
    assert(ranking(Vector((0, 1, 0.8)), 0.0, 0).isEmpty)
  }

  //Pruebas funcion N°11 adicionales ----
  val corpusA: Corpus = Vector(
    ("a0", Vector("a", "b", "c", "d")),
    ("a1", Vector("a", "b", "c", "e")),
    ("a2", Vector("x", "y", "z")),
    ("a3", Vector("a", "b", "c", "d"))
  )

  val corpusB: Corpus = Vector(
    ("b0", Vector("uno", "dos", "tres", "cuatro", "cinco")),
    ("b1", Vector("uno", "dos", "tres", "cuatro", "seis")),
    ("b2", Vector("tres", "cuatro", "cinco", "seis", "siete")),
    ("b3", Vector("otro", "texto", "sin", "relacion", "alguna"))
  )

  test("detectarDirecto: ordena por similitud y rompe empates por posición") {
    assert(detectarDirecto(corpusA, 2, 0.5, 10) ==
      Vector((0, 3, 1.0), (0, 1, 0.5), (1, 3, 0.5)))
  }

  test("detectarDirecto: trunca el ranking al tope p") {
    assert(detectarDirecto(corpusA, 2, 0.5, 2) ==
      Vector((0, 3, 1.0), (0, 1, 0.5)))
  }

  test("detectarDirecto: el umbral es inclusivo y deja solo las copias exactas") {
    assert(detectarDirecto(corpusA, 2, 1.0, 10) == Vector((0, 3, 1.0)))
  }

  test("detectarDirecto: con umbral 0.0 aparecen también las parejas de similitud cero") {
    assert(detectarDirecto(corpusA, 2, 0.0, 10) ==
      Vector((0, 3, 1.0), (0, 1, 0.5), (1, 3, 0.5),
        (0, 2, 0.0), (1, 2, 0.0), (2, 3, 0.0)))
  }

  test("detectarDirecto: con copias exactas, las parejas (i, copia de i) quedan al frente") {
    val c = corpusConCopias(6, 60, 1000, 3, 1.0)
    val r = detectarDirecto(c, 4, 0.5, 10)
    assert(r.map(m => (m._1, m._2)) == Vector((0, 6), (1, 7), (2, 8)))
    assert(r.forall(_._3 == 1.0))
  }

  //Pruebas funcion N°12 adicionales ----
  test("detectarConIndice: devuelve las dos parejas candidatas con su similitud") {
    assert(detectarConIndice(corpusB, 3, 0.2, 10) ==
      Vector((0, 1, 0.5), (0, 2, 0.2)))
  }

  test("detectarConIndice: un umbral más alto deja fuera la pareja de 0.2") {
    assert(detectarConIndice(corpusB, 3, 0.3, 10) == Vector((0, 1, 0.5)))
  }

  test("detectarConIndice: sin fragmentos en común no hay candidatas ni ranking") {
    val c: Corpus = Vector(
      ("x0", Vector("a", "b", "c")),
      ("x1", Vector("d", "e", "f"))
    )
    assert(detectarConIndice(c, 2, 0.1, 5).isEmpty)
  }

  test("detectarConIndice: corpus vacío, de un documento o de documentos más cortos que k") {
    assert(detectarConIndice(Vector.empty, 3, 0.1, 5).isEmpty)
    assert(detectarConIndice(Vector(("solo", Vector("a", "b", "c"))), 2, 0.1, 5).isEmpty)
    val cortos: Corpus = Vector(("c0", Vector("a", "b")), ("c1", Vector("a", "b")))
    assert(detectarConIndice(cortos, 3, 0.1, 5).isEmpty)
  }

  test("detectarConIndice: con copias al 50 % las copias quedan al frente y coincide con el directo") {
    val c = corpusConCopias(8, 60, 1000, 2, 0.5)
    val r = detectarConIndice(c, 4, 0.2, 10)
    assert(r.map(m => (m._1, m._2)).toSet == Set((0, 8), (1, 9)))
    assert(r.forall(m => m._3 > 0.3 && m._3 < 0.35))
    assert(r == detectarDirecto(c, 4, 0.2, 10))
  }
}
