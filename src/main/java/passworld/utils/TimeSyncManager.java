package passworld.utils;


import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import passworld.data.PasswordDTO;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

public class TimeSyncManager {
    private static Duration timeOffset = Duration.ZERO;

    private static final String NTP_SERVER = "time.google.com";
    private static final int NTP_PORT = 123;

    public static void syncTimeWithUtcServer() {
        try {
            // Obtener la hora NTP utilizando Apache Commons Net
            long ntpTime = getNtpTime(NTP_SERVER);

            if (ntpTime > 0) {
                Instant serverTime = Instant.ofEpochSecond(ntpTime);
                Instant localNow = Instant.now();

                timeOffset = Duration.between(localNow, serverTime);
                System.out.println("[TimeSyncManager] Offset calculado: " + timeOffset.getSeconds() + " segundos");
                LogUtils.LOGGER.info("[TimeSyncManager] Offset: " + timeOffset.getSeconds() + " seconds");
            }
        } catch (Exception e) {
            System.err.println("[TimeSyncManager] Error al sincronizar hora: " + e.getMessage());
            LogUtils.LOGGER.warning("[TimeSyncManager] Error syncing time: " + e);
        }
    }

    /**
     * Obtiene la hora NTP desde el servidor utilizando Apache Commons Net.
     *
     * @param server El servidor NTP
     * @return El tiempo NTP en segundos desde la época Unix.
     * @throws Exception Si ocurre algún error al intentar obtener la hora.
     */
    private static long getNtpTime(String server) throws Exception {
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(3000);  // Establecer un tiempo de espera para la conexión

        InetAddress address = InetAddress.getByName(server);
        TimeInfo timeInfo = client.getTime(address);

        // Obtener el tiempo del servidor NTP
        long ntpTime = timeInfo.getMessage().getTransmitTimeStamp().getTime() / 1000;

        client.close();
        return ntpTime;
    }

    public static LocalDateTime nowCorrected() {
        return LocalDateTime.now().plus(timeOffset);
    }

    public static LocalDateTime correctLocalTime(PasswordDTO local) {
        return local.getLastModified().plus(timeOffset);
    }

    public static Duration getOffset() {
        return timeOffset;
    }
}
