# Informe - Tp2


## Consignas Previas
1) Es necesario determinar con PIPE las propiedades de la red (deadlock, vivacidad, seguridad).
2) Indicar cuál o cuáles son los invariantes de plaza y los invariantes de transición de la red. Realizar una breve descripción de lo que representan en el modelo.
3) Realizar una tabla, con los estados del sistema.
4) Realizar una tabla, con los eventos del sistema.
5) Determinar la cantidad de hilos necesarios para la ejecución del sistema con el mayor paralelismo posible 
   -  Caso 1: si el invariante de transición tiene un conflicto, con otro invariante, debe haber un hilo encargado de la ejecución de la/s transición/es anterior/es al conflicto y luego un hilo por invariante.
   -  Caso 2: si el invariante de transición presenta un join, con otro invariante de transición, luego del join debe haber tantos hilos, como tokens simultáneos en la plaza, encargados de las transiciones restantes dado que hay un solo camino.

6) Hacer el diagrama de clases que modele el sistema.
7) Hacer el diagrama de secuencia que muestre el disparo exitoso de una transición que esté sensibilizada, mostrando el uso de la política.

## Respuestas
1) Luego de aplicar un análisis con el software PIPE, podemos determinar las siguientes propiedades de la red:
   - Es una red sin deadlock
   - No es una red segura
   - Es una red acotada
   - Es una red Viva

   ![caracteristicas de la red](images/00.png)

2) Las ecuaciones de los invariantes de plaza son:
   $$M(P0) + M(P1) + M(P3) + M(P4) + M(P5) + M(P7) + M(P8) + M(P9) + M(P10) + M(P11) = 3$$
   $$M(P1) + M(P2) = 1$$
   $$M(P10) + M(P4) + M(P5) + M(P6) + M(P7) + M(P8) + M(P9) = 1$$

   Y las invariantes de transición son:
      - T0 -> T1 -> T2 -> T3 -> T4 -> T11
   
      - T0 -> T1 -> T5 -> T6 -> T11
   
      - T0 -> T1 -> T7 -> T8 -> T9 -> T10 -> T11
   
   La red de petri modelada representa un sistema de procesamiento de datos con recursos compartidos (los que se encuentran en P2 y P6). Los datos entran a una cola (P0), pasan a un buffer de entrada (P3) mediante un bus compartido (P2), y son procesados por una unidad compartida (P6) en tres modos:
   - simple (P7)
   - medio (P4-P5) 
   - alto (P8-P9-P10)

3) Tabla de estados del sistema 

   | Estado | Marcado                   | Transición disparada | Transiciones habilitadas |
   |--------|---------------------------|----------------------|--------------------------|    
   | m0     | [3 0 1 0 0 0 1 0 0 0 0 0] | -                    | T0                       |
   | m1     | [2 1 0 0 0 0 1 0 0 0 0 0] | T0                   | T1                       |
   | m2     | [2 0 1 1 0 0 1 0 0 0 0 0] | T1                   | T0,T2,T5,T7              |
   | m3     | [1 1 0 1 0 0 1 0 0 0 0 0] | T0                   | T2,T5,T7                 |
   | m4     | [2 0 1 0 0 0 1 0 0 0 0 0] | T2                   | T2                       |
   | m5     | [2 1 0 0 0 0 1 0 0 0 0 0] | T5                   | T5                       |
   
      ![grafo de alcanzabilidad](images/01.png)

4) Responder
5) Analizando nuestra red, podemos observar que primero tenemos un comportamiento secuencial, dado que las transiciónes:
   - T0, T1 se comparten entre las 3 invariantes de transición
   
   Luego de esto, tenemos un conflicto en la P3, ya que, las transiciónes T2,T5,T7 compiten por el recurso que tenemos en P6. Siguiendo con las recomendaciones del caso 1, necesitamos un hilo antes del conflicto y otros 3 hilos después del conflicto debido a que tenemos 3 invariantes. Lo que nos dá un total de 4 hilos.

   Al final tenemos un Join, pero esto no genera problema debido a que los mismos 3 hilos que llevaron el token a la P11 pueden disparar la transición T11 para así devolverlos a P0.

   ![hilos simultáneos](images/02.png)

6) Nuestro primer diagrama de clases fue:

   ![diagrama de clases](images/03.png)




