package passworld.utils;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import passworld.data.PasswordDTO;

import java.net.InetAddress;
import java.time.*;

public class TimeSyncManager {
    // Offset que mide cuánto difiere el reloj local (en UTC) respecto al UTC real del servidor NTP
    private static Duration timeOffset = Duration.ZERO;

    private static final String NTP_SERVER = "time.google.com";

    /**
     * Sincroniza y actualiza el offset entre el reloj local (UTC) y el servidor NTP (UTC real).
     */
    public static void syncTimeWithUtcServer() {
        try {
            Instant serverUtcTime = getNtpUtcTime(NTP_SERVER);
            if (serverUtcTime != null) {
                Instant systemUtcTime = Instant.now();

                timeOffset = Duration.between(systemUtcTime, serverUtcTime);

                System.out.println("[TimeSyncManager] Hora sistema UTC    : " + systemUtcTime);
                System.out.println("[TimeSyncManager] Hora servidor UTC   : " + serverUtcTime);
                System.out.println("[TimeSyncManager] Offset reloj local  : " + timeOffset.getSeconds() + " segundos");

                LogUtils.LOGGER.info("[TimeSyncManager] Offset reloj local: " + timeOffset.getSeconds() + " seconds");
            }
        } catch (Exception e) {
            System.err.println("[TimeSyncManager] Error al sincronizar hora: " + e.getMessage());
            LogUtils.LOGGER.warning("[TimeSyncManager] Error syncing time: " + e);
        }
    }

    /**
     * Obtiene la hora UTC real desde el servidor NTP.
     *
     * @param server El servidor NTP.
     * @return Instant con la hora UTC real, o null si falla.
     * @throws Exception En caso de error al conectarse al servidor NTP.
     */
    private static Instant getNtpUtcTime(String server) throws Exception {
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(3000);
        try {
            InetAddress address = InetAddress.getByName(server);
            TimeInfo timeInfo = client.getTime(address);
            timeInfo.computeDetails();

            long serverTimeMillis = timeInfo.getMessage().getTransmitTimeStamp().getTime();
            return Instant.ofEpochMilli(serverTimeMillis);
        } finally {
            client.close();
        }
    }

    /**
     * Devuelve la hora actual UTC corregida (teniendo en cuenta la desviación del reloj local).
     *
     * @return Instant de la hora UTC corregida.
     */
    public static Instant nowUtcCorrected() {
        Instant systemUtc = Instant.now();
        return systemUtc.plus(timeOffset);
    }

    /**
     * Corrige una LocalDateTime local a su correspondiente Instant UTC corregido.
     *
     * @param localDateTime Fecha y hora local sin zona.
     * @return Instant UTC corregido.
     */
    public static Instant localDateTimeToUtcCorrected(LocalDateTime localDateTime) {
        // Convierte LocalDateTime local a Instant UTC (sin corrección)
        Instant systemInstant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();

        // Aplica el offset de corrección
        return systemInstant.plus(timeOffset);
    }

    /**
     * Corrige la última modificación de un PasswordDTO a UTC corregido, y devuelve LocalDateTime en UTC.
     *
     * @param local PasswordDTO con fecha local.
     * @return LocalDateTime en UTC corregida.
     */
    public static LocalDateTime correctLocalTimeToUtc(PasswordDTO local) {
        Instant correctedInstant = localDateTimeToUtcCorrected(local.getLastModified());
        return LocalDateTime.ofInstant(correctedInstant, ZoneId.of("UTC"));
    }

    /**
     * Devuelve el offset actual en segundos entre el reloj local y el servidor NTP.
     *
     * @return Duración del offset.
     */
    public static Duration getOffset() {
        return timeOffset;
    }
}
