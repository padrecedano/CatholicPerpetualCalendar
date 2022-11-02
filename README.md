# CatholicPerpetualCalendar
Calendario Perpetuo de la Liturgia Católica Romana
***

El objetivo de este proyecto es generar el Calendario Litúrgico de un año dado, incluyendo los elementos propios de la Liturgia de las Horas (semana del salterio y otros)
o los elementos relativos a la Misa de cada día, tales como las lecturas bíblicas y formularios litúrgicos, etc.

La necesidad surgió al querer dotar a la aplicación [liturgiaplus.app](https://www.liturgiaplus.app) de un calendario perpetuo, en la fase de migración de dicha aplicación al modo offline.

El punto de partida de todo Calendario Litúrgico es la celebración de la Pascua. Para obtener ese dato, que es el más importante, se usa el [algoritmo de Butcher-Meeus](https://fr.wikipedia.org/wiki/Calcul_de_la_date_de_P%C3%A2ques) debido a que el tradicional algoritmo de Gauss no arroja la fecha correcta en algunos años (ver a propósito [esta respuesta en Stackoverflow](https://stackoverflow.com/a/55278990/5587982)).

Este proyecto se inspira en gran parte en otros proyectos más amplios, a saber:

- [LiturgicalCalendar](https://github.com/JohnRDOrazio/LiturgicalCalendar), un trabajo completísimo basado en código PHP que está siendo desarrollado por [Jhon R. D'Orazio](https://github.com/JohnRDOrazio).

- [RomCal](https://github.com/romcal), otro gran trabajo basado en TypeScript.

El código de este proyecto se encuentra en una versión beta.

### Interpretar las salidas en la actual fase del código

El proyecto ha sido desarrollado usando gradle, y como IDE he usado IntelliJ IDEA CE.

Si clonas el proyecto desde este repositorio, cuando ejecutes el main o hagas debug, verás en la terminal salidas como esta:

	2022-01-01 - NAV01-08 - 1 PSalter: 4
	2022-01-10 - ORD0[1]*02 F1 - 1 PSalter: 1
	2022-01-11 - ORD0[1]*03 F1 - 1 PSalter: 1
	
	... 
	
	2022-01-16 - 	ORD0201 1ª Parte - 1 PSalter: 2
	
	2022-03-02 - CUA0004 - 1 PSalter: 4
	2022-03-03 - CUA0005 - 1 PSalter: 4
	2022-03-04 - CUA0006 - 1 PSalter: 4
	2022-03-05 - CUA0007 - 1 PSalter: 4
	2022-03-06 - CUA01	01 - 1 PSalter: 1
	
	2022-04-10 - SES01RAMOS - 1 PSalter: 4
	2022-04-11 - SES2 - 1 PSalter: 4
	2022-04-12 - SES3 - 1 PSalter: 4
	2022-04-13 - SES4 - 1 PSalter: 4
	
	2022-04-14 - TRI01-05 - 1 PSalter: 4
	2022-04-15 - TRI01-06 - 1 PSalter: 4
	2022-04-16 - TRI01-07 - 1 PSalter: 4
	2022-04-17 - PAS01-01 - 1 PSalter: 4
	2022-04-18 - PAS*01-1 - 1 PSalter: 1
	2022-04-19 - PAS*01-2 - 1 PSalter: 1
	2022-04-20 - PAS*01-3 - 1 PSalter: 1
	2022-04-21 - PAS*01-4 - 1 PSalter: 1
	2022-04-22 - PAS*01-5 - 1 PSalter: 1
	2022-04-23 - PAS*01-6 - 1 PSalter: 1
	2022-04-24 - PAS*01-7 - 1 PSalter: 1

Son códigos de salida para ir examinando la construcción del calendario: a la izquierda está la fecha, y luego unas abreviaturas que significan el tiempo (`ORD`, querría decir Tiempo Ordinario, `NAV`, Navidad, `CUA`, Cuaresma, etc), esto tiene poca importancia, es sólo a modo de prueba y luego hay unos números que serían los días de la semana y la semana del tiempo y `Psalter` indica la semana del salterio que correspondería.
