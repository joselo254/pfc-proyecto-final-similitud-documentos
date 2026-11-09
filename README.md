# Proyecto final — Detección de similitud entre documentos

Fundamentos de Programación Funcional y Concurrente
Escuela de Ingeniería de Sistemas y Computación, Universidad del Valle
Carlos Andrés Delgado Saavedra

El enunciado completo, con la formalización del problema, los dos ejemplos
trabajados, las tablas de medición, la rúbrica y la ecuación de calificación, es
el PDF publicado en el campus virtual. Este archivo dice cómo está armado el
proyecto y cómo se entrega.

## Integrantes

Llene esta tabla con el nombre completo y el código de cada integrante. Es la
fuente de identificación del equipo: si un integrante no aparece aquí, se
considera que no hizo parte del proyecto y su nota individual es 0.0, por más
commits que tenga. Si falta alguno, la entrega se sanciona con el 20 % de la
nota.

| Nombre completo | Código |
|---|---|
| Jose Emanuel Cuervo Buitrago|2559905-3743 |
| Jhon David Ceballos Yate|2559724-3743 |
| Sergio Alejandro Quintero Bolivar|2559869-3743 |
| Santiago Grajales Cardozo|2559791-3743 |

## El problema, en una línea

Dado un conjunto de documentos, encontrar las parejas que se parecen. Cada
documento se resume por el conjunto de sus fragmentos de `k` palabras
consecutivas, dos documentos se comparan por el índice de Jaccard de esos
conjuntos, y un índice invertido evita comparar las parejas que no comparten
ningún fragmento, cuya similitud es cero de antemano. Después todo eso se
paraleliza y se mide cuánto se ganó.

## Cómo está organizado el proyecto

```
app/src/main/scala/proyecto/
    Similitud.scala       los tipos, la generación de entradas y las doce
                          funciones de la solución secuencial
    SimilitudPar.scala    las seis funciones de la solución paralela
    App.scala             los dos ejemplos del enunciado

app/src/main/scala/common/
    package.scala         parallel y task, los de clase. No se modifica

app/src/test/scala/proyecto/
    SimilitudTest.scala      12 pruebas de contrato de la secuencial
    SimilitudParTest.scala    5 pruebas de contrato de la paralela

docs/                     los cuatro informes, en Markdown
```

Su código va en `Similitud.scala` y `SimilitudPar.scala`. Los tipos, las
funciones de generación al azar y las de exploración ya vienen escritas y no se
modifican.

Las 17 pruebas que vienen son el contrato con que se califica, y tampoco se
modifican. Son una por función, con los valores de los ejemplos del enunciado.

La rúbrica pide además **cinco casos de prueba propios por cada función**,
distintos de esos y de los ejemplos del enunciado. Van donde le resulte más
cómodo: al final de los archivos de prueba que ya están, o en un archivo nuevo
dentro de `app/src/test/scala/proyecto/`. Las dos formas valen; lo que no se
toca es lo que ya estaba escrito.

## Cómo se ejecuta

```bash
./gradlew test    # revisa las reglas del curso y corre las pruebas
./gradlew run     # corre los ejemplos del enunciado
```

La primera vez se demora: Gradle descarga el compilador de Scala y la versión
de Java que necesita. No hay que instalar nada a mano. Al terminar,
`./gradlew test` deja un informe navegable en
`app/build/reports/tests/test/index.html`.

## El punto de partida

Al clonar, las 17 pruebas están en rojo con `NotImplementedError`, porque las
dieciocho funciones dicen `???`. Su trabajo es reemplazar cada `???` y ver las
pruebas ponerse en verde.

## Las funciones

| Archivo | Funciones |
|---|---|
| `Similitud.scala` | `normalizar`, `fragmentos`, `huella`, `huellas`, `jaccard`, `parejas`, `similitudes`, `indiceInvertido`, `parejasCandidatas`, `ranking`, `detectarDirecto` y `detectarConIndice` |
| `SimilitudPar.scala` | `huellasPar`, `similitudesPar`, `fusionar`, `indiceInvertidoPar`, `detectarDirectoPar` y `detectarConIndicePar` |

Cada función paralela tiene que dar **exactamente** lo mismo que su versión
secuencial, y así lo comprueban las pruebas. Donde eso se rompe con más
facilidad es en `indiceInvertidoPar`: la mitad derecha del corpus se indexa
sobre un vector que empieza en cero, de modo que sus documentos quedan
desplazados, y nada en los tipos lo advierte.

## Reglas del código

En este curso no se usa `var`, ni `while`, ni `return`, ni ningún estado que
cambie. Todo se resuelve con `val`, recursión y llamados a funciones. Si
necesita una secuencia de instrucciones, use un bloque `{ ... }`; si necesita
funciones auxiliares, escríbalas dentro de las funciones.

La expresión `for` con `yield` sí se usa: produce una colección nueva y el
compilador la traduce a `map`, `flatMap` y `filter`. Lo que no entra es el `for`
que no devuelve valor.

`./gradlew test` revisa estas reglas **antes** de correr las pruebas. Un
programa con `var`, `while` o `return` no llega a las pruebas: el flujo se
detiene y dice en qué línea está el problema.

No se editan los archivos de configuración del proyecto, ni los flujos de
GitHub, ni el paquete `common`.

## Los informes

Los cuatro informes van en `docs/`, en Markdown: `Proceso.md`, `Correccion.md`,
`Paralelizacion.md` y `Conclusiones.md`. La carpeta trae un `README.md` que
explica qué va en cada uno, y `Proceso.md` y `Correccion.md` vienen con un
ejemplo de la forma que se espera.

La notación matemática se escribe en LaTeX dentro del Markdown y los diagramas
con `mermaid`. No se aceptan imágenes insertadas ni archivos por fuera de
`docs/`.

## Cómo se entrega

1. Presione **Fork** en este repositorio. Un solo integrante hace el fork y
   agrega a los demás como colaboradores; todo el equipo trabaja sobre ese mismo
   fork.
2. En su fork, entre a la pestaña **Actions** y habilítelas. GitHub las deja
   apagadas en los forks hasta que el dueño lo autoriza, y sin eso no ve el
   resultado de las pruebas al hacer push.
3. Clone su fork, resuelva, y haga commit y push a la rama por defecto. Cada
   integrante hace sus propios commits: la rúbrica de aporte al código se
   calcula con ellos.
4. Registre en la tarea **Entrega del proyecto final** del campus virtual la
   dirección de su fork.

### Las fechas

| Qué | Cuándo |
|---|---|
| Entrega de avance: las funciones secuenciales y sus pruebas | jueves 19 de noviembre de 2026 |
| Entrega final, por el campus | miércoles 16 de diciembre de 2026, 23:59 |
| Sustentación, en el horario de clase | jueves 17 de diciembre de 2026 |

La entrega de avance no tiene nota propia, pero no presentarla se refleja en la
rúbrica del informe de procesos.

### Las condiciones

- **La entrega es el enlace registrado en el campus dentro del plazo.** No se
  aceptan entregas por correo ni por ningún otro medio.
- **El repositorio tiene que ser un fork de este.** Un repositorio creado
  aparte, aunque tenga el mismo código, rompe la estructura con que se califica
  y la verificación automática.
- **Se califica el último commit anterior a la fecha de entrega.** Lo que se
  suba después no se revisa.
- Solo se revisa la rama marcada por defecto del repositorio.
- Evite editar por la interfaz web de GitHub: la trazabilidad de la autoría se
  lee mejor desde `git`.
- El docente verifica la originalidad del código y de los informes con
  herramientas antiplagio, y mira la trazabilidad de los commits.
- **El proyecto se sustenta.** La nota de sustentación es individual, entre 0 y
  1, y multiplica la nota grupal. No asistir da factor 0.
- Modificar los archivos de Gradle, los flujos de GitHub, `scalastyle_config.xml`
  o el paquete `common` para que la entrega parezca funcional tiene rebaja de
  nota.

La nota sale de la rúbrica del enunciado, con la ecuación que está allí mismo:
los puntos de la rúbrica por el factor de aporte al código y por el factor de
sustentación.
