# CatholicPerpetualCalendar
Calendario Perpetuo de la Liturgia Católica Romana
***

Este proyecto pretende generar el Calendario Litúrgico de un año determinado, así como determinar la semana del salterio y otros elementos de la Liturgia de las Horas
o las lecturas bíblicas y formularios litúrgicos a usarse en un día determinado, con el propósito de dotar a la aplicación [liturgiaplus.app](https://www.liturgiaplus.app) de un calendario perpetuo, en la fase de migración de dicha aplicación al modo offline.

El punto de partida de todo Calendario Litúrgico es la celebración de la Pascua. Para obtener ese dato, que es el más importante, se usa el [algoritmo de Butcher-Meeus](https://fr.wikipedia.org/wiki/Calcul_de_la_date_de_P%C3%A2ques) debido a que el tradicional algoritmo de Gauss no arroja la fecha correcta en algunos años (ver a propósito [esta respuesta en Stackoverflow](https://stackoverflow.com/a/55278990/5587982)).

Este proyecto se inspira en gran parte en otros proyectos más amplios, a saber:

- [LiturgicalCalendar](https://github.com/JohnRDOrazio/LiturgicalCalendar), un trabajo completísimo basado en código PHP que está siendo desarrollado por [Jhon R. D'Orazio](https://github.com/JohnRDOrazio).
 
- [RomCal](https://github.com/romcal), otro gran trabajo basado en TypeScript.

El código de este proyecto se encuentra en una versión beta.