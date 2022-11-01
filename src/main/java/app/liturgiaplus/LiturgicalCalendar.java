package app.liturgiaplus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.DayOfWeek.*;
import static java.time.temporal.TemporalAdjusters.next;

/**
 * <p>Esta clase Java generará el Calendario Litúrgico de un año determinado.
 * En el futuro otras prestaciones son posibles,
 * como determinar la semana del salterio y otros elementos de la Liturgia de las Horas
 * o las lecturas bíblicas y formularios litúrgicos a usarse en un día determinado.</p>
 * <p>Surgió la necesidad de esta clase al querer dotar a la aplicacion <a href="https://www.liturgiaplus.app">liturgiaplus.app</a>
 * de un calendario perpetuo, en la fase de migración de dicha aplicación al modo offline.</p>
 * <p>El punto de partida de todo Calendario litúrgico es la celebración de la Pascua.
 * Para obtener ese dato, que es el más importante, se usa el algoritmo de <a href="https://fr.wikipedia.org/wiki/Calcul_de_la_date_de_P%C3%A2ques">Butcher-Meeus</a> debido a que el tradicional algoritmo de Gauss
 * no arroja la fecha correcta en algunos años (<a href="https://stackoverflow.com/a/55278990/5587982">ver esta respuesta en Stackoverflow</a>).</p>
 * <p>Los métodos de esta clase están basados en el excelente trabajo desarrollado por
 * <a href="https://github.com/JohnRDOrazio">Jhon R. D'Orazio</a> en su proyecto <a href="https://github.com/JohnRDOrazio/LiturgicalCalendar">LiturgicalCalendar</a> basado en código PHP (<a href="https://github.com/JohnRDOrazio/LiturgicalCalendar/blob/master/LitCalEngine.php">PHP que genera el Calendario</a>).
 * Puede que algunos métodos se inspiren también en <a href="https://github.com/romcal">RomCal</a>
 * otro excelente proyecto basado en TypeScript.</p>
 *
 * @author A. Cedano
 * @version beta
 */
public final class LiturgicalCalendar {
    /**
     * Año del calendario que se quiere generar
     */
    private static int mYear;

    /**
     * Configuración del calendario. Aquí se indicarán elementos propios del lugar, por ejemplo:<br>
     * - Si la Epifanía se celebra el 6 de Enero, la clave <code>"EpiphanyOnSunday"</code> será <code>false</code>,
     * o no existirá, si dicha clave es <code>true</code> la Epifanía se celebra el Domingo.
     */
    private static HashMap<String, Boolean> mSettings;


    /**
     * Lista de objetos {@link Celebration} del año dado
     */
    private static final ArrayList<Celebration> mCalendar = new ArrayList<>();

    /**
     * Referencia a la fecha de la Pascua
     * La guardamos una sola vez para no volver a usar el algoritmo
     */
    private static LocalDate diesPaschae;

    /**
     * Referencia a la fecha del Jueves Santo
     */
    private static LocalDate coenaeDomini;


    /**
     * Obtiene la fecha de Pascua de un año dado.
     * Se usa el algoritmo de <a href="https://fr.wikipedia.org/wiki/Calcul_de_la_date_de_P%C3%A2ques">Butcher-Meeus</a>
     *
     * @param theYear El año cuyo calendario se quiere generar
     * @return Un objeto <code>LocalDate</code> {@link LocalDate LD} con la fecha de Pascua en formato yyyymmdd
     */


    public static LocalDate getDiePaschae(int theYear) {
        int a = theYear % 19;
        int b = theYear / 100;
        int c = theYear % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int theMonth = (h + l - 7 * m + 114) / 31;
        int p = (h + l - 7 * m + 114) % 31;
        int theDay = p + 1;
        return LocalDate.of(theYear, theMonth, theDay);
        //return String.format("%04d%02d%02d", theYear, easterMonth, easterDay);
    }

    /**
     * @param theYear     El año cuyo calendario se quiere obtener
     * @param theSettings Configuración del calendario (pe Epifanía el domingo o no)
     */
    public static void generateYearlyCalendar(int theYear, HashMap<String, Boolean> theSettings) {
        mSettings = theSettings;
        mYear = theYear;
        diesPaschae = getDiePaschae(mYear);
        coenaeDomini = diesPaschae.minusDays(3);

        //fillPerAnnum();
        //fillFeriaeAdventus();
        //fillPreviousEpiphany(mYear);
        //fillPostEpiphany();
        //getAdventFirst();
        /*
        fillChristusRex();
        fillDominicisAdventus();
        fillFeriaeAdventusMaiorem();
        fillNativitate();
        */
        fillDeiGenitricisMarie();
        fillPreviousEpiphany(mYear);
        fillPostEpiphany();


        fillTriduum();
        fillDominicisQuadragesima();
        fillFeriaeCeneri();
        fillFeriaeQuadragesima();
        fillHebdomadaSancta();
        fillOctavamPaschae();
        fillDominicisPaschae();
        fillFeriaePaschae();

        fillTrinitatis();
        fillCorpus();

        fillDominicisPerAnnum();
        fillFeriaePerAnnum();

/*
        fillSanIoseph();
        fillAnnuntiatione();
        fillImmaculata();
        fillAscensione();
        System.out.println("TOTAL: " + mCalendar.size());
*/
    }


    private static LocalDate addDaysNew(LocalDate theDay, int daysToAdd) {
        return theDay.plusDays(daysToAdd);
    }


    private static String addDays(String mDate, String mFormat, int mDays) {
        SimpleDateFormat sdf = new SimpleDateFormat(mFormat);
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(mDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DAY_OF_MONTH, mDays);
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM-dd-yyyy");
        String output = sdf.format(c.getTime());
        //System.out.println("Fecha: "+mDate+" Calendario: "+output+c.getTime());
        return output;
    }

    public static LocalDate getAdventTest(int mYear) {

        LocalDate mDate = LocalDate.of(mYear, 12, 25);
        LocalDate prevSunday = mDate.with(TemporalAdjusters.previousOrSame(SUNDAY));
        LocalDate ref;
        if (mDate.getDayOfWeek() != SUNDAY) {
            ref =
                    mDate.with(TemporalAdjusters.previous(SUNDAY));
        } else {
            ref = mDate;
        }
        System.out.println(prevSunday.toString() + "...");

        LocalDate f = prevSunday.minusWeeks(3);
        //LocalDate d=mDate.minusWeeks(4);
        System.out.println(f);
        return prevSunday;
    }

    /**
     * Obtiene la fecha del Primer Domingo de Adviento
     *
     * @return Un objeto {@link LocalDate} con la fecha del Primer Domingo de Adviento
     */
    public static LocalDate getPrimaAdventu() {
        LocalDate mDate = LocalDate.of(mYear, 12, 25);
        int weekDay = mDate.getDayOfWeek().getValue();
        LocalDate primaAdventu;
        switch (weekDay) {
            case 7: // Domingo
                primaAdventu = LocalDate.of(mYear, 11, 27);
                break;
            case 1: // Lunes
                primaAdventu = LocalDate.of(mYear, 12, 3);
                break;

            case 2: // Martes
                primaAdventu = LocalDate.of(mYear, 12, 2);
                break;

            case 3: // Miércoles
                primaAdventu = LocalDate.of(mYear, 12, 1);
                break;

            case 4: // Jueves
                primaAdventu = LocalDate.of(mYear, 11, 30);
                break;

            case 5: // Viernes
                primaAdventu = LocalDate.of(mYear, 11, 29);
                break;

            default:
                // Sábado
                primaAdventu = LocalDate.of(mYear, 11, 28);
                break;
        }
        return primaAdventu;
    }

    private static LocalDate getNativitate() {
        return LocalDate.of(mYear, 12, 25);
    }


    private static TemporalAdjuster addPeriod(int mDays) {
        return t -> t.plus(Period.ofDays(mDays));
    }

    private static void getAdventFirstOld(int mYear) {
        /*
            Para calcular el 1er domingo de Adviento
            tomamos como referencia la fecha de Navidad
            Verificamos si Navidad NO cae en domingo
            en cuyo caso hay que restar sólo 21 días
            para obtener la fecha del 1er Domingo de Adviento
            En los otros casos se restan 28 días
         */
        Calendar calRef = getNativity(mYear);
        int daysDiff = -28;
        if (calRef.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            daysDiff = -21;
            calRef.add(Calendar.DAY_OF_WEEK, -(calRef.get(Calendar.DAY_OF_WEEK) - 1));
        }
        calRef.add(Calendar.DAY_OF_MONTH, daysDiff);
        //mCalendar.add(new Celebration(4, "ADV01-01", "1", StringUtils.dateFromCalendar(calRef)));

        calRef.add(Calendar.DAY_OF_WEEK, 7);
        //mCalendar.add(new Celebration(4, "ADV02-01", "1", StringUtils.dateFromCalendar(calRef)));

        calRef.add(Calendar.DAY_OF_WEEK, 7);
        //mCalendar.add(new Celebration(4, "ADV03-01*", "1", StringUtils.dateFromCalendar(calRef)));

        calRef.add(Calendar.DAY_OF_WEEK, 7);
        //mCalendar.add(new Celebration(4, "ADV04-01*", "1", StringUtils.dateFromCalendar(calRef)));
    }


    public static Calendar getNativity(int mYear) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, mYear);
        c.set(Calendar.MONTH, Calendar.DECEMBER);
        c.set(Calendar.DAY_OF_MONTH, 25);
        return c;
    }

    /*
        Devuelve la fecha de la Epifanía,
        según la configuración en Settings:
        Si JAN06 es igual a true se celebra el 6 de enero,
        de lo contrario, se celebra en domingo
     */
    public static LocalDate getEpiphania() {
        if (!mSettings.get("EpiphanyOnSunday") || !mSettings.containsKey("EpiphanyOnSunday")) {
            return LocalDate.of(mYear, 1, 6);
        } else {
            LocalDate mDate = LocalDate.of(mYear, 1, 2);
            DayOfWeek dayOfWeek = mDate.getDayOfWeek();
            if (dayOfWeek != SUNDAY) {
                mDate = getNextSunday(mDate);
            }
            return mDate;
        }

    }

    public static LocalDate getBaptismum() {
        if (!mSettings.get("EpiphanyOnSunday")) {
            return getNextSunday(getEpiphania());
        } else {
/*
        El Bautismo del Señor se celebra el Domingo posterior a la Epifanía
        Hay excepciones:
            Si la Epifanía se celebra el Domingo entre el 2 y el 8 de Enero
            y el 8 o el 7 de enero cae en Domingo,
            el Bautismo del Señor se traslada al lunes siguiente a dicho domingo.
            De lo contrario, se celebra el Domingo siguiente a la Epifanía
             */
            LocalDate dateSeven = LocalDate.of(mYear, 1, 7);
            LocalDate dateEight = LocalDate.of(mYear, 1, 8);

            if (isSunday(dateSeven)) {
                return getNextMonday(dateSeven);
            } else if (isSunday(dateEight)) {
                return getNextMonday(dateEight);
            } else {
                return getNextSunday(getEpiphania());
            }

        }

    }

    /**
     * Obtiene la fecha del Miércoles de Ceniza
     * Esto se hace restando <code>46</code> días a la fecha de la Pascua
     *
     * @return Un objeto {@link LocalDate} con la fecha del Miércoles de Ceniza
     */
    public static LocalDate getQuartaCinerum() {
        return (diesPaschae.with(addPeriod(-46)));
    }

    /**
     * Obtiene la fecha del Primer Domingo de Cuaresma
     * Esto se hace sumando <code>4</code> días al Miércoles de Ceniza
     *
     * @return Un objeto {@link LocalDate} con la fecha del Domingo I de Cuaresma
     */
    public static LocalDate getPrimaQuadragesima() {
        //return (getQuartaCinerum().with(addPeriod(3)));
        return (getQuartaCinerum().plusDays(4));

    }

    /**
     * Obtiene la fecha del Quinto Domingo de Cuaresma
     * Esto se hace sumando <code>4</code> semanas al Domingo I de Cuaresma
     *
     * @return Un objeto {@link LocalDate} con la fecha del Domingo V de Cuaresma
     */
    public static LocalDate getQuintaQuadragesima() {
        return (getPrimaQuadragesima().plusWeeks(4));
    }


    /**
     * -----------Métodos fill invocados para llenar el Calendario-----------
     * <p>Agrega al calendario los domingos de Adviento.
     * Se calcula agregando en cada caso la/las semanas correspondientes
     * al primer Domingo de Adviento.</p>
     */
    private static void fillDominicisAdventus() {
        LocalDate primaAdventu = getPrimaAdventu();
        mCalendar.add(new Celebration(1, primaAdventu, "\tADV-01-01", 1, 1, 1));
        mCalendar.add(new Celebration(1, primaAdventu.plusWeeks(1), "\t*ADV-02-01", 1, 1, 2));

        //mCalendar.add(new Celebration(1, primaAdventu.plusWeeks(2), "\tADV-02-01", 1, 1,2));
        mCalendar.add(new Celebration(1, primaAdventu.plusWeeks(3), "\tADV-03-01", 1, 1, 3));
        mCalendar.add(new Celebration(1, primaAdventu.plusWeeks(4), "\tADV-04-01", 1, 1, 4));
    }

    /**
     * Agrega al calendario los domingos de Cuaresma
     */
    private static void fillDominicisQuadragesima() {
        LocalDate start = getQuartaCinerum();
        AtomicInteger n = new AtomicInteger(1);

        Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, diesPaschae.minusWeeks(1)))
                .filter(d -> d.getDayOfWeek() == SUNDAY)
                //.filter(ld->ld.getDayOfWeek().equals(1))
                .forEach(e ->
                {
                    int week = n.getAndIncrement();
                    mCalendar.add(new Celebration(1, e,
                            String.format("CUA0%d\t01", week), 1, 1, week));
                });
        //.forEach(System.out::println);
    }

    /**
     * <p>Agrega al calendario las ferias de Cuaresma.</p>
     * <p>Para registrar el día de la semana suma 1 al valor obtenido con
     * {@link LocalDate#getDayOfWeek()}.</p>
     */
    private static void fillFeriaeCeneri() {
        LocalDate start = getQuartaCinerum();
        Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, start.plusDays(4)))
                .forEach(e ->
                {
                    mCalendar.add(new Celebration(1, e,
                            String.format("CUA0%d0%d", 0, e.getDayOfWeek().getValue() + 1), 1, 1, 4));
                });
        //.forEach(System.out::println);
    }

    /**
     * <p>Agrega al calendario las ferias de Cuaresma. Excluye las ferias de la semana de Ceniza
     * que se agregan mediante {@link #fillFeriaeCeneri()}</p>
     * <p>Para registrar el día de la semana suma 1 al valor obtenido con
     * {@link LocalDate#getDayOfWeek()}.</p>
     */
    private static void fillFeriaeQuadragesima() {
        LocalDate start = getQuartaCinerum().plusDays(5);
        AtomicInteger m = new AtomicInteger(1);
        Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, diesPaschae.minusWeeks(1)))
                .filter(d -> d.getDayOfWeek() != SUNDAY)
                .forEach(e ->
                {
                    mCalendar.add(new Celebration(1, e, String.format("_CUA0%d0%d", m.get(), e.getDayOfWeek().getValue() + 1), 1, 1, m.get()));

                    if (e.getDayOfWeek() == SATURDAY) {
                        m.getAndIncrement();
                    }
                });
        //.forEach(System.out::println);
    }

    /**
     * <p>Agrega al calendario la Semana Santa.
     * Desde el Domingo de Ramos hasta el Sábado Santo.</p>
     */
    private static void fillHebdomadaSancta() {
        LocalDate start = diesPaschae.minusWeeks(1);
        mCalendar.add(new Celebration(1, start, "SES01RAMOS", 1, 1));
        AtomicInteger n = new AtomicInteger(1);
        Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, diesPaschae.minusDays(3)))
                .filter(d -> d.getDayOfWeek() != SUNDAY)
                .forEach(e -> mCalendar.add(new Celebration(1, e,
                        String.format("SES%d", e.getDayOfWeek().getValue() + 1), 1, 1)));
    }

    /**
     * <p>Agrega al calendario la Octava de Pascua.
     * La Octava de Pascua son los ocho días entre el Día de Pascua
     * y el Domingo siguiente llamado "De la Divina Misericordia".</p>
     */
    private static void fillOctavamPaschae() {
        LocalDate start = diesPaschae.plusDays(1);
        AtomicInteger n = new AtomicInteger(1);
        Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, diesPaschae.plusDays(8)))
                //.filter(d->d.getDayOfWeek() != SUNDAY)
                .forEach(e -> mCalendar.add(new Celebration(1, e,
                        String.format("PAS*01-%d", e.getDayOfWeek().getValue()), 1, 1, 1)));
    }

    /**
     * <p>Agrega al calendario los otros Domingos de Pascua.
     * En {@link #fillOctavamPaschae()} ya incluimos el 1er y 2do Domingos
     * por lo que aquí agregamos los otros domingos a partir del Tercero.
     * La fecha de inicio en este método se calcula
     * agregando una semana a {@link #diesPaschae}.</p>
     */
    private static void fillDominicisPaschae() {
        LocalDate start = diesPaschae.plusWeeks(1);
        AtomicInteger n = new AtomicInteger(2);
        Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, diesPaschae.plusDays(50)))
                .filter(d -> d.getDayOfWeek() == SUNDAY)
                .forEach(e ->
                {
                    int week = n.getAndIncrement();
                    mCalendar.add(new Celebration(1, e,
                            String.format("?PAS0%d01", week), 1, 1, week));
                });
    }

    /**
     * <p>Agrega al calendario las ferias de Pascua.
     * En {@link #fillDominicisPaschae()} y en {@link #fillOctavamPaschae()}
     * ya incluimos los Domingos de Pascua.</p>
     */
    private static void fillFeriaePaschae() {
        LocalDate start = diesPaschae.plusWeeks(1);
        AtomicInteger week = new AtomicInteger(2);
        Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, diesPaschae.plusDays(50)))
                .filter(d -> d.getDayOfWeek() != SUNDAY)
                .forEach(e ->
                {
                    mCalendar.add(new Celebration(1, e, String.format("!PAS0%d0%d", week.get(), e.getDayOfWeek().getValue() + 1), 1, 1, week.get()));

                    if (e.getDayOfWeek() == SATURDAY) {
                        week.getAndIncrement();
                    }
                });

    }


    /**
     * <p>Agrega al calendario la Octava de Pascua.
     * La Octava de Pascua son los ocho días entre el Día de Pascua
     * y el Domingo siguiente llamado "De la Divina Misericordia".</p>
     */
    private static void fillOctavamPaschaes() {
        LocalDate start = diesPaschae;
        AtomicInteger n = new AtomicInteger(1);
        Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, diesPaschae.plusDays(8)))
                //.filter(d->d.getDayOfWeek() != SUNDAY)
                .forEach(e -> mCalendar.add(new Celebration(1, e,
                        String.format("PAS010%d", e.getDayOfWeek().getValue()), 1, 1)));
    }

    /**
     * <p>Agrega las ferias de Adviento anteriores al 17-12
     * TODO: Verificar con Stream si la fecha existe en mCalendar
     * Ver: https://github.com/romcal/romcal/blob/ea5af7afb20915de2a4e3b754dbca4f8f4a6892c/lib/utils/dates.ts#L215</p>
     */
    private static void fillFeriaeAdventus() {
        LocalDate start = getPrimaAdventu();
        LocalDate end = getNativitate();
        List<LocalDate> dates = Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, end))
                .filter(d -> d.getDayOfWeek() != SUNDAY)

                .filter(date -> !mCalendar.stream()
                        .anyMatch(mCelebration ->
                                mCelebration.getDate().equals(date)))
                .collect(Collectors.toList());

        AtomicInteger n = new AtomicInteger(1);
        String s = "ADV-04-%s";
        //final String args="";
        AtomicReference<String> args = new AtomicReference<>();
        dates.forEach(e -> {
            if (e.getMonthValue() == 12 && e.getDayOfMonth() > 16) {
                args.set(String.valueOf(n.getAndIncrement()));
            } else {
                args.set(String.valueOf(e.getDayOfMonth()));
            }


            mCalendar.add(new Celebration(
                    n.getAndIncrement(),
                    e,
                    String.format(s, args.get()),
                    1, 4));
            //}
            //System.out.println(e);
        });
    }

    /*
     * Agrega al calendario las ferias de Adviento anteriores al 17-12
     * TODO: Verificar con Stream si la fecha existe en mCalendar
     *  TODO: Verificar la semana de Adviento, pues las ferias mayores
     *   podrían caer tanto en la 3ª como en la 4ª semana
     */
    private static void fillFeriaeAdventusMaiorem() {
        LocalDate start = getPrimaAdventu();
        LocalDate end = getNativitate();
        List<LocalDate> dates = Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, end))
                .filter(d -> d.getDayOfWeek() != SUNDAY)

                .filter(date -> !mCalendar.stream()
                        .anyMatch(mCelebration ->
                                mCelebration.getDate().equals(date)))
                .collect(Collectors.toList());

        AtomicInteger n = new AtomicInteger(1);
        String s = "ADV-04-%s";
        //final String args="";
        AtomicReference<String> args = new AtomicReference<>();
        dates.forEach(e -> {
            if (e.getMonthValue() == 12 && e.getDayOfMonth() > 16) {
                mCalendar.add(new Celebration(
                        n.getAndIncrement(),
                        e,
                        String.format(s, e.getDayOfMonth()),
                        1, 4));
                //args.set(String.valueOf(n.getAndIncrement()));
            } else {
                //args.set(String.valueOf(e.getDayOfMonth()));
            }


            //}
            //System.out.println(e);
        });
    }

    /**
     * <p>Agrega todas las celebraciones del Tiempo de Navidad.</p>
     * <p>Usa el método {@link #isSunday(LocalDate)} para saber si Navidad cae en Domingo
     * y el método {@link #getNextSunday(LocalDate)} para establecer la fecha de la Sagrada Familia
     * el domingo siguiente a Navidad cuando ésto aplique.</p>
     *
     * @see #getNextSunday(LocalDate)
     */
    public static void fillNativitate() {
        LocalDate nativitateDie = LocalDate.of(mYear, 12, 25);

        mCalendar.add(new Celebration(4, nativitateDie, "NAV-01-01*", 2,
                1));
        /*
            Si Navidad cae en Domingo,
            la Sagrada Familia se celebra el 30-12.
            De lo contrario, el Domingo que sigue al día 12-25
         */
        if (isSunday(nativitateDie)) {
            mCalendar.add(new Celebration(4,
                    LocalDate.of(mYear, 12, 30),
                    "FAM-01-01*", 1, 1));
        } else {
            mCalendar.add(new Celebration(4,
                    getNextSunday(nativitateDie),
                    "FAM-01-01*", 1, 1));
        }

        /**
         * Días de la Octava de Navidad
         */
        AtomicInteger n = new AtomicInteger(1);
        nativitateDie.datesUntil(nativitateDie.plusDays(8), Period.ofDays(1))
                .forEach(e -> {
                    mCalendar.add(new Celebration(1, e, String.format("OCT-01+N %d de la Octava", n.getAndIncrement()), 1, 1));
                });
    }

    /*
        Determina las celebraciones previas a la Epifanía
        Determina también si hay un Domingo II de Navidad
     */
    public static void fillPreviousEpiphany(int mYear) {
        LocalDate dateEpiphany = getEpiphania();
        /*
            Si la Epifanía se celebra el 6 de Enero:
                a. 1 de Enero: Solemnidad de Sta. María, Madre de Dios
                b. Los días entre el 2 y el 5 de Enero se llaman "* día antes de la Epifanía"
                c. Si entre el 2-5 Enero hay un Domingo será Domingo II de Navidad
                d. La Epifanía se celebra el 6 de Enero
                e. Los días entre el 6 de Enero y el Domingo siguiente son llamados
                   "* día posterior a la Epifanía"
                f. El Bautismo del Señor se celebra el Domingo que sigue al 6 de Enero
                g. El Tiempo Ordinario empieza el día siguiente al Bautismo del Señor
         */
        if (mSettings.get("EpiphanyOnSunday")) {
            int n = 0;
            LocalDate start = LocalDate.of(mYear, 1, 2);
            LocalDate end = LocalDate.of(mYear, 1, 6);

            List<LocalDate> dates = Stream.iterate(start, date -> date.plusDays(1))
                    .limit(ChronoUnit.DAYS.between(start, end))
                    .collect(Collectors.toList());

            dates.forEach(e -> {
                System.out.println(e);
                //counter.getAndIncrement();
                if (isSunday(e)) {
                    /*mCalendar.add(new Celebration(1, "NAV02-01",
                            "*II Navidad", e.toString()));*/
                } else {
                    /*mCalendar.add(new Celebration(1, "NAV02-01",
                            e.toString(),
                            String.format("*Día %d anterior a la Epifanía %s", 0, e.getDayOfWeek())));
                */
                }
            });

            for (int i = 2; i < 6; i++) {
                n++;
                LocalDate tmpDate = LocalDate.of(mYear, 1, i);
                if (isSunday(tmpDate)) {
                    //mCalendar.add(new Celebration(1, "NAV02-01", "II Navidad", tmpDate.toString()));
                } else {
                    /*mCalendar.add(new Celebration(i, "NAV02-01",
                            tmpDate.toString(),
                            String.format("*Día %d anterior a la Epifanía %s", n, tmpDate.getDayOfWeek())));
                */
                }
            }
            //mCalendar.add(new Celebration(1, "NAV02-01", mDate.toString(), "Epifanía"));

            LocalDate sunAfterEpiphany = getNextSunday(LocalDate.of(mYear, 1, 6));
            int dayOfMonth = sunAfterEpiphany.getDayOfMonth();

            if (dayOfMonth != 7) {
                n = 0;
                for (int i = 7; i < dayOfMonth; i++) {
                    n++;
                    //mCalendar.add(new Celebration(i, "NAV02-01", String.format("*Día %d posterior a la Epifanía", n), LocalDate.of(mYear, 1, i).toString()));

                }
            }

            //return mDate;
        } else {

            int epiphanyDay = dateEpiphany.getDayOfMonth();

            int n = 0;
            String text = "*Día %d anterior a la Epifanía %s";
            for (int i = 2; i < epiphanyDay; i++) {
                n++;
                LocalDate tmpDate = LocalDate.of(mYear, 1, i);

            }
            /*
                Si la Epifanía podría tener lugar el 6 de Enero o antes
                entonces, los días siguientes a la Epifanía son llamados
                "* día posterior a la Epifanía"
             */

        }

    }

    public static void fillPostEpiphany() {
        LocalDate mDate = LocalDate.of(mYear, 1, 6);
        if (!mSettings.get("EpiphanyOnSunday")) {
            /*
                Si hay un domingo entre el 2 y el 5 de enero
                este será el Domingo II de Navidad.
                Los días entre el 2 y el 5 de enero se llaman
                "N día antes de la Epifanía" o "weekDay ...."
             */
            int n = 0;


            /*
                Los días desde el 7 de enero hasta el domingo siguiente
                son llamados "* día posterior a la Epifanía"
                Obtenemos el Domingo que sigue a la Epifanía
                y determinamos si ese día no es el día 7
             */
            LocalDate sunAfterEpiphany = getNextSunday(LocalDate.of(mYear, 1, 6));
            int dayOfMonth = sunAfterEpiphany.getDayOfMonth();

            if (dayOfMonth != 7) {
                n = 0;
                for (int i = 7; i < dayOfMonth; i++) {
                    n++;
                    LocalDate tmpDate = LocalDate.of(mYear, 1, i);
                }
            }

        } else {
            mDate = LocalDate.of(mYear, 1, 2);
            DayOfWeek dayOfWeek = mDate.getDayOfWeek();
            if (dayOfWeek != SUNDAY) {
                mDate = getNextSunday(mDate);
            }
            int epiphanyDay = mDate.getDayOfMonth();


            /*
                Si la Epifanía podría tener lugar el 6 de Enero o antes
                entonces, los días siguientes a la Epifanía son llamados
                "* día posterior a la Epifanía"
             */
            if (epiphanyDay < 7) {
                LocalDate nextSunday = getNextSunday(mDate);
                int dayOfMonth = nextSunday.getDayOfMonth();
                int n = 0;
                for (int i = epiphanyDay + 1; i < dayOfMonth; i++) {
                    LocalDate tmpDate = LocalDate.of(mYear, 1, i);
                    n++;
                }
            }

            /*
    II.
    5. FIESTAS DEL SEÑOR INSCRITAS EN EL CALENDARIO GENERAL
        El Bautismo del Señor se celebra el Domingo posterior a la Epifanía
        Hay excepciones:
            Si la Epifanía se celebra el Domingo entre el 2 y el 8 de Enero
            y el 8 o el 7 de enero cae en Domingo,
            el Bautismo del Señor se traslada al lunes siguiente a dicho domingo.
            De lo contrario, se celebra el Domingo siguiente a la Epifanía
             */
            LocalDate dateSeven = LocalDate.of(mYear, 1, 7);
            LocalDate dateEight = LocalDate.of(mYear, 1, 8);

            if (isSunday(dateSeven)) {

                mCalendar.add(new Celebration(1, getNextMonday(dateSeven),
                        "NAV04-01", 1, 1));
//        if (!mSettings.get("EpiphanyOnSunday")) {
            } else if (isSunday(dateEight)) {
                mCalendar.add(new Celebration(1, getNextMonday(dateEight),
                        "NAV04-01", 1, 1));
                //mCalendar.add(new Celebration(1, "NAV04-01", "Bautismo Lunes", getNextMonday(dateEight).toString()));
            } else {
                mCalendar.add(new Celebration(1, getNextSunday(mDate),
                        "NAV04-01", 1, 1));
            }

        }

    }

    /**
     * Agrega al calendario los Domingos del Tiempo Ordinario.
     * Hay dos bloques del Tiempo Ordinario:
     * 1. Después del Bautismo del Señor hasta el martes antes del Miércoles de Ceniza
     * 2. Después de Pentecostés hasta el 1er Domingo de Adviento.
     * En el cálculo excluimos el último Domingo del Tiempo Ordinario (Cristo Rey), que tendrá
     * su propio método: {@link #fillChristusRex()}
     */
    private static void fillDominicisPerAnnum() {
        LocalDate postBaptismum = getBaptismum().plusDays(1);
        AtomicInteger n = new AtomicInteger(2);

        Stream.iterate(postBaptismum, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(postBaptismum, getQuartaCinerum()))
                .filter(d -> d.getDayOfWeek() == SUNDAY)
                .forEach(e ->
                {
                    int week=n.getAndIncrement();
                    mCalendar.add(new Celebration(1, e,
                            String.format("\tORD0%d01 1ª Parte", week), 1, 1,week));
                    if(e.getDayOfWeek()==SATURDAY){
                        //week.getAndIncrement();
                    }
                });

        LocalDate lateOrdinary = getCorpus();
        LocalDate christusRex=getPrimaAdventu().minusWeeks(1);
        AtomicInteger lateWeek = new AtomicInteger(34);
        Stream.iterate(christusRex, date -> date.minusDays(1))
                .limit(ChronoUnit.DAYS.between(lateOrdinary,christusRex))
                .filter(d -> d.getDayOfWeek().equals(SUNDAY))
                .forEach(e ->
                        {
                            int week=lateWeek.getAndDecrement();
                            mCalendar.add(new Celebration(1, e,
                                    String.format("\tORD0%d01 2ª parte", week), 1, 1,week));}
                );


    }

    private static LocalDate getLateOrdinary() {
        if (mSettings.containsKey("CorpusOriginal") &&
                mSettings.get("CorpusOriginal")) {
            return diesPaschae.plusDays(64);

        }
        return diesPaschae.plusDays(57);
    }

    /**
     * Agrega al calendario el último Domingo del Tiempo Ordinario (Cristo, Rey del Universo).
     */
    private static void fillChristusRex() {
        mCalendar.add(new Celebration(1, getPrimaAdventu().minusWeeks(1),
                "ORD3401", 1, 1));
    }

    /**
     * Agrega al calendario la Solemnidad de Santa María, Madre de Dios
     * que tiene lugar el 1 de Enero (Octava de Navidad).
     */
    private static void fillDeiGenitricisMarie() {
        mCalendar.add(new Celebration(1, LocalDate.of(mYear, 1, 1),
                "NAV01-08", 1, 1));
    }

    /**
     * Agrega al calendario la Solemnidad de San José, esposo de la virgen María (19 de Marzo)
     * <p>Se aplica la siguiente norma: <br />
     * <ul>
     *     <li>Si ocurre en un Domingo de Cuaresma, se traslada al lunes siguiente</li>
     *     <li>Si ocurre durante la Semana Santa, se traslada al sábado anterior al Domingo de Ramos</li>
     * </ul>
     * </p>
     */
    private static void fillSanIoseph() {
        LocalDate theDate = LocalDate.of(mYear, 3, 19);

        if (isSunday(theDate) &&
                (isBetween(theDate, getPrimaQuadragesima(), getQuintaQuadragesima()))
        ) {
            theDate = theDate.plusDays(1);
        } else if (isBetween(theDate, diesPaschae.minusDays(7), diesPaschae)) {
            theDate = diesPaschae.minusDays(8);
        }

        mCalendar.add(new Celebration(1, theDate,
                "PST03-19", 1, 1));
    }

    /**
     * Agrega al calendario la Solemnidad de la Anunciación del Señor (25 de Marzo)
     * <p>Se aplica la siguiente norma: <br />
     * <ul>
     *     <li>Si ocurre un Domingo de Cuaresma, se traslada al siguiente día (Lunes)</li>
     *     <li>Si ocurre dentro de la Semana Santa se traslada al Lunes II de Pascua</li>
     *     <li>Si ocurre dentro de la Octava de Pascua se traslada al Lunes II de Pascua</li>
     * </ul>
     * </p>
     */
    private static void fillAnnuntiatione() {
        LocalDate theDate = LocalDate.of(mYear, 3, 21);
        LocalDate inPalmis = diesPaschae.minusWeeks(1);

        if ((isBetween(theDate, inPalmis, inPalmis.plusWeeks(2)))) {
            theDate = diesPaschae.plusDays(8);
        } else if (isSunday(theDate) &&
                (isBetween(theDate, getPrimaQuadragesima(), getQuintaQuadragesima()))) {
            theDate = LocalDate.of(mYear, 3, 26);
        }

        mCalendar.add(new Celebration(1, theDate,
                "PST03-25", 1, 1));
    }

    /**
     * Agrega al calendario la Solemnidad de la Inmaculada Concepción (8-Diciembre)
     * Se aplica la siguiente norma: <br />
     * <ul>
     *     <li>Si ocurre un Domingo de Adviento, se traslada al siguiente día (Lunes)</li>
     *     <li>En algunos lugares (España por ejemplo), esta solemnidad prevalece aún cuando cae un
     *      * Domingo de Adviento. Esta norma aplicará sí y solo sí {@link #mSettings}
     *      * tiene una entrada: <code>"ImmaculatePrevails"</code> con valor <code>true</code></li>
     * </ul>
     * </p>
     */
    private static void fillImmaculata() {
        LocalDate theDate = LocalDate.of(mYear, 12, 8);

        if (isSunday(theDate) &&
                mSettings.containsKey("ImmaculatePrevails") &&
                !mSettings.get("ImmaculatePrevails")
        ) {
            theDate = theDate.plusDays(1);
        }
        mCalendar.add(new Celebration(1, theDate,
                "PST12-08", 1, 1));
    }

    /**
     * Agrega al calendario la Solemnidad de la Ascensión del Señor (40 días después de la Pascua)
     * <p>
     * En la tradición bíblica-litúrgica la Ascensión ocurrió cuarenta días después de Pascua.
     * Por tanto, la fecha propia de celebración de este día sería el Jueves de la Sexta Semana de Pascua.
     * Sin embargo, en algunos lugares la Ascensión ha sido trasladada al Domingo siguiente.
     * Se aplica la siguiente norma: <br />
     * <ul>
     *     <li>La Ascensión se calcula por defecto para el Domingo VII de Pascua.</li>
     *     <li>En los lugares donde la Ascensión se celebra el Jueves, {@link #mSettings}
     *      * tendra una entrada: <code>"AscensionOriginal"</code> con valor <code>true</code>.
     *      En este caso, el Domingo siguiente será el Domingo VII de Pascua.</li>
     * </ul>
     * </p>
     */
    private static void fillAscensione() {
        //LocalDate theDate = LocalDate.of(mYear, 12, 8);
        LocalDate theDate = diesPaschae.plusDays(39);

        if (mSettings.containsKey("AscensionOriginal") &&
                mSettings.get("AscensionOriginal")) {
            mCalendar.add(new Celebration(1, theDate,
                    "PAS-06-ASCJUE", 1, 1));
            mCalendar.add(new Celebration(1, theDate.plusDays(3),
                    "PAS-07-01", 1, 1));
        } else {
            mCalendar.add(new Celebration(1, theDate,
                    "PAS-06-05", 1, 1));
            mCalendar.add(new Celebration(1, theDate.plusDays(3),
                    "PAS-06-ASCDOM", 1, 1));

        }
    }

    /**
     * Agrega al calendario la Solemnidad de la Santísima Trinidad (Domingo Posterior a Pentecostés)
     * <p>Esta solemnidad se calcula agregando 8 semanas (56 días) a la fecha de Pascua ({@link #diesPaschae})</p>
     */
    private static void fillTrinitatis() {
        mCalendar.add(new Celebration(1, diesPaschae.plusWeeks(8), "\tORD-35-TRI", 1, 1));
    }

    /**
     * <p>Agrega al calendario la Solemnidad del Corpus Christi
     * (Jueves posrterior a la Santísima Trinidad).</p>
     * <p>En la tradición bíblica-litúrgica esta fiesta se celebra
     * el Jueves posterior a la Santísima Trinidad, es decir, 60 días después de la Pascua.</p>
     * <p>Por motivos pastorales esta celebración se ha trasladado al domingo en muchos lugares.
     * Por ser esta la norma que prevalece en casi todos los lugares, aplicamos lo siguiente:</p>
     * <ul>
     *     <li>El Corpus Christi se calcula por defecto para el Domingo Posterior a la Santísima Trinidad,
     *     agregando 9 semanas o 63 días a la fecha de la Pascua ({@link #diesPaschae}).</li>
     *     <li>En los lugares donde esta celebración se sigue celebrando el Jueves,
     *     {@link #mSettings} deberá tener una entrada: <code>"CorpusOriginal"</code> con valor <code>true</code>.
     *      En este caso, el Domingo siguiente será el que corresponda del Tiempo Ordinario.</li>
     * </ul>
     * </p>
     */

    public static LocalDate getCorpus() {
        if (mSettings.containsKey("CorpusOriginal") &&
                mSettings.get("CorpusOriginal")) {
            return diesPaschae.plusDays(60);
        }
        return diesPaschae.plusDays(63);
    }
    private static void fillCorpus() {
        LocalDate theDate = diesPaschae.plusDays(63);
        mCalendar.add(new Celebration(1, getCorpus(), "\tORD-36-CORPUS", 1, 1));
    }

    /**
     * Agrega al calendario las ferias del Tiempo Ordinario (Per Annum).
     * <p>En la liturgia, el Tiempo Ordinario se desarrolla en dos bloques:
     * <ol>
     *     <li>Entre el día siguiente al Bautismo del Señor y el día anterior al Miércoles de Ceniza (se usa  {@link #getBaptismum()} y {@link #getQuartaCinerum()} para el cálculo)</li>
     *     <li>Entre el día siguiente a Pentecostés y el día anterior al Primer Domingo de Adviento (usamos <code>{@link #diesPaschae} + 50</code> y el método {@link #getPrimaAdventu()} para el cálculo)</li>
     * </ol>
     * </p>
     */
    private static boolean isBetween(LocalDate theDate, LocalDate dateFrom, LocalDate dateUntil) {
        System.out.println("---" + theDate.isAfter(dateFrom));
        System.out.println("---" + theDate.isBefore(dateUntil));
        boolean b = ((theDate.isAfter(dateFrom.minusDays(1))) &&
                (theDate.isBefore(dateUntil.plusDays(1))));
        System.out.printf("Verificando si %s está entre %s y %s => %s\n", theDate, dateFrom, dateUntil, b);
        return b;
    }

    /**
     * Agrega al calendario las ferias del Tiempo Ordinario (Per Annum).
     * <p>En la liturgia, el Tiempo Ordinario se desarrolla en dos bloques:
     * <ol>
     *     <li>Entre el día siguiente al Bautismo del Señor y el día anterior al Miércoles de Ceniza (se usa  {@link #getBaptismum()} y {@link #getQuartaCinerum()} para el cálculo)</li>
     *     <li>Entre el día siguiente a Pentecostés y el día anterior al Primer Domingo de Adviento (usamos <code>{@link #diesPaschae} + 50</code> y el método {@link #getPrimaAdventu()} para el cálculo)</li>
     * </ol>
     * </p>
     */
    private static void fillFeriaePerAnnum() {
        LocalDate diesBaptismum = getBaptismum().plusDays(1);
        AtomicInteger m = new AtomicInteger(1);
        Stream.iterate(diesBaptismum, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(diesBaptismum, getQuartaCinerum()))
                .filter(d -> d.getDayOfWeek() != SUNDAY)
                .forEach(e ->
                {
                    mCalendar.add(new Celebration(1, e,
                            String.format("ORD0[%d]*0%d F1", m.get(), e.getDayOfWeek().plus(1).getValue()), 1, 1, m.get()));
                    if (e.getDayOfWeek() == SATURDAY) {
                        m.getAndIncrement();
                    }
                });
        LocalDate postPentecostes = diesPaschae.plusDays(50);
        LocalDate preAdventu=getPrimaAdventu().minusDays(1);
        AtomicInteger i = new AtomicInteger(34);
        Stream.iterate(preAdventu, date -> date.minusDays(1))
                .limit(ChronoUnit.DAYS.between(postPentecostes, preAdventu))
                .filter(d -> d.getDayOfWeek() != SUNDAY)
                .forEach(e -> {
                    int week=i.get();

                    if (e.getDayOfWeek() == MONDAY) {
                        i.getAndDecrement();
                    }
                    mCalendar.add(new Celebration(1, e,
                            String.format("ORD0[%d]0%d \tF2", week, e.getDayOfWeek().plus(1).getValue()), 1, 1,week));

                });
    }

    /**
     * @Deprecated Se usarán los métodos fillDominicis y fillFeriae
     * @since beta
     */

    public static void fillPerAnnum() {
        LocalDate start = getBaptismum().plusDays(1);
        LocalDate end = getQuartaCinerum();

        List<LocalDate> dates = Stream.iterate(start, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(start, end))
                .collect(Collectors.toList());

        dates.forEach(e -> {
            System.out.println(e);
        });
    }


    /**
     * <p>Agrega al calendario los días del Triduo Pascual y el día de Pascua.</p>
     * <p>Para determinar los días del Triduo Pascual, simplemente resta los días a la fecha de Pascua:
     * <ul>
     *     <li>Jueves Santo : Pascua-3 días</li>
     *     <li>Viernes Santo: Pascua-2 días</li>
     *     <li>Sábado Santo : Pascua-1 día</li>
     * </ul>
     * </p>
     */
    private static void fillTriduum() {
        mCalendar.add(new Celebration(1, coenaeDomini, "TRI01-05", 1, 1));
        mCalendar.add(new Celebration(1, diesPaschae.minusDays(2), "TRI01-06", 1, 1));
        mCalendar.add(new Celebration(1, diesPaschae.minusDays(1), "TRI01-07", 1, 1));
        mCalendar.add(new Celebration(1, diesPaschae, "PAS01-01", 1, 1));
    }


    public static LocalDate manageEpiphany(int mYear) {
        LocalDate mDate = LocalDate.of(mYear, 1, 6);
        if (mSettings.get("JAN06")) {
            /*
                Si hay un domingo entre el 2 y el 5 de enero
                este será el Domingo II de Navidad.
                Los días entre el 2 y el 5 de enero se llaman
                "N día antes de la Epifanía" o "weekDay ...."
             */
            int n = 0;
            for (int i = 2; i < 5; i++) {
                n++;
                LocalDate tmpDate = LocalDate.of(mYear, 1, i);
                if (isSunday(tmpDate)) {
                    //mCalendar.add(new Celebration(1, "NAV02-01", "II Navidad", tmpDate.toString()));
                } else {
                    //mCalendar.add(new Celebration(i, "NAV02-01", String.format("*Día %d anterior a la Epifanía", n), tmpDate.toString()));
                }
            }
            //mCalendar.add(new Celebration(1, "NAV02-01", mDate.toString(), "Epifanía"));

            /*
                Los días desde el 7 de enero hasta el domingo siguiente
                son llamados "* día posterior a la Epifanía"
             */
            LocalDate sunAfterEpiphany = getNextSunday(LocalDate.of(mYear, 1, 6));
            int dayOfMonth = sunAfterEpiphany.getDayOfMonth();
            System.out.println("POST: " + dayOfMonth + " " + sunAfterEpiphany.toString());

            if (dayOfMonth != 7) {
                n = 0;
                for (int i = 7; i < dayOfMonth; i++) {
                    n++;
                    //mCalendar.add(new Celebration(i, "NAV02-01", String.format("*Día %d posterior a la Epifanía", n), LocalDate.of(mYear, 1, i).toString()));

                }
            }

            return mDate;
        } else {
            mDate = LocalDate.of(mYear, 1, 2);
            DayOfWeek dayOfWeek = mDate.getDayOfWeek();
            if (dayOfWeek != SUNDAY) {
                mDate = getNextSunday(mDate);
            }
            int epiphanyDay = mDate.getDayOfMonth();

            int n = 0;
            for (int i = 2; i < epiphanyDay; i++) {
                n++;
                //mCalendar.add(new Celebration(1, "A", mDate.toString(), String.format("Día %d anterior a la Epifanía", n)));
            }
            /*
                Si la Epifanía podría tener lugar el 6 de Enero o antes
                entonces, los días siguientes a la Epifanía son llamados
                "* día posterior a la Epifanía"
             */

            if (epiphanyDay < 7) {
                LocalDate nextSunday = getNextSunday(mDate);
                int dayOfMonth = nextSunday.getDayOfMonth();
                n = 0;
                for (int i = epiphanyDay + 1; i < dayOfMonth; i++) {
                    n++;
                    //mCalendar.add(new Celebration(i, "NAV03-01", String.format("*Día %d posterior a la Epifanía", i), LocalDate.of(mYear, 1, i).toString()));
                }
            }

            /*
    II.
    5. FIESTAS DEL SEÑOR INSCRITAS EN EL CALENDARIO GENERAL
        El Bautismo del Señor se celebra el Domingo posterior a la Epifanía
        Hay excepciones:
            Si la Epifanía se celebra el Domingo entre el 2 y el 8 de Enero
            y el 8 o el 7 de enero cae en Domingo,
            el Bautismo del Señor se traslada al lunes siguiente a dicho domingo
             */
            LocalDate dateSeven = LocalDate.of(mYear, 1, 7);
            LocalDate dateEight = LocalDate.of(mYear, 1, 8);

            if (isSunday(dateSeven)) {

                //mCalendar.add(new Celebration(1, "NAV04-01", getNextMonday(dateSeven).toString(), "Bautismo Lunes"));

            } else if (isSunday(dateEight)) {
                //mCalendar.add(new Celebration(1, "NAV04-01", getNextMonday(dateEight).toString(), "Bautismo Lunes"));
            }

        }
        return mDate;

    }

    /*
        Determina si una fecha dada es Domingo
     */
    private static boolean isSunday(LocalDate mDate) {
        DayOfWeek dayOfWeek = mDate.getDayOfWeek();
        return (dayOfWeek == SUNDAY);
    }

    /*
        Obtiene el Domingo posterior a una fecha dada
     */
    private static LocalDate getNextSunday(LocalDate mDate) {
        return mDate.with(next(SUNDAY));
    }

    /*
        Obtiene el Lunes posterior a una fecha dada
    TODO: Ver la posibilidad de unificarlo con getNextSunday
    ¿Se puede pasar una constante DayOfWeek?
     */
    private static LocalDate getNextMonday(LocalDate mDate) {
        return mDate.with(next(MONDAY));
    }

    public static ArrayList<Celebration> getCalendar() {
        mCalendar.sort(Comparator.comparing(Celebration::getDate));
        return mCalendar;
    }

    public static void printCalendar() {
        mCalendar.sort(Comparator.comparing(Celebration::getDate));
        //mList=new Ob
        //mCalendar.add(new Celebration(mCalendar.size(),diesPaschae,"",mCalendar.size(),11));
        for (Celebration mCelebration : mCalendar) {
            //mList.add(mCelebration);
            System.out.println(mCelebration.toString());
        }

    }
}
