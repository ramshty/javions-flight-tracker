package javions.gui;

import javions.ByteString;
import javions.GeoPos;
import javions.Units;
import javions.adsb.CallSign;
import javions.adsb.Message;
import javions.adsb.MessageParser;
import javions.adsb.RawMessage;
import javions.aircraft.AircraftData;
import javions.aircraft.AircraftDatabase;
import javafx.collections.ObservableSet;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AircraftStateManagerTest2 {
    public static void main(String[] args) throws IOException {
        test2();
    }

    private static void test1() throws IOException{
        try (DataInputStream s = (new DataInputStream(new BufferedInputStream(
                new FileInputStream("C:\\Users\\nedda\\Desktop\\PPOOjavions\\ppoo-project-2.0\\Javions\\messages_20230318_0915.bin"))
                ))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            int count = 0;
            AircraftDatabase database = new AircraftDatabase("C:\\Users\\nedda\\Desktop\\PPOOjavions\\ppoo-project-2.0\\Javions\\resources\\aircraft.zip");
            AircraftStateManager aircraftStateManager = new AircraftStateManager(database);
            while (count < 85) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                Message m = MessageParser.parse(new RawMessage(timeStampNs,message));
                aircraftStateManager.updateWithMessage(m);
                aircraftStateManager.purge();
                count++;
            }
            ObservableSet<ObservableAircraftState> states = aircraftStateManager.states();
            List<ObservableAircraftState> states2 = new ArrayList<>(states);
            states2.sort(new AddressComparator());
            //System.out.printf("%13d: %s\n", timeStampNs, message);
            for(ObservableAircraftState o: states2){
                AircraftData data = o.getAircraftData();
                if (data!= null){
                    GeoPos p = o.getPosition();

                    System.out.printf("%5s %9s %7s %32s %20s %20s %9s %9s\n",o.address().string(),nullC(o.getCallSign()), data.registration().string(), data.model(), nullPosLon(p), nullPosLat(p),nullParser(Math.round(o.getAltitude())),nullVelocity(Math.round(o.getVelocity())));
                    //System.out.printf("%5s %5s %5s %5s %5f %5f\n",o.getIcaoAdress().string(),o.getCallSign(), data.registration().string(), data.model(), o.getAltitude(), o.getVelocity());
                }
            }

        } catch (EOFException e) { /* nothing to do */ }
    }

    private static String nullParser(Object s) {
        return s == null ? "" : s.toString();
    }

    private static String nullC(CallSign s) {
        return s == null ? "" : s.string();
    }

    private static String nullVelocity(Long p) {
        return p == null ? "" : String.valueOf(Units.convertTo(p, Units.Speed.KILOMETER_PER_HOUR));
    }
    private static String nullPosLon(GeoPos p) {
        return p == null ? "" : String.valueOf(Units.convertTo(p.longitude(), Units.Angle.DEGREE));
    }

    private static String nullPosLat(GeoPos p) {
        return p == null ? "" : String.valueOf(Units.convertTo(p.latitude(), Units.Angle.DEGREE));
    }

    private static class AddressComparator
            implements Comparator<ObservableAircraftState> {
        @Override
        public int compare(ObservableAircraftState o1,
                           ObservableAircraftState o2) {
            String s1 = o1.address().string();
            String s2 = o2.address().string();
            return s1.compareTo(s2);
        }
    }

    private static void test2() throws IOException{
        ObservableSet<ObservableAircraftState> states = null;
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("C:\\Users\\nedda\\Desktop\\PPOOjavions\\ppoo-project-2.0\\Javions\\messages_20230318_0915.bin")))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            AircraftDatabase database = new AircraftDatabase("C:\\Users\\nedda\\Desktop\\PPOOjavions\\ppoo-project-2.0\\Javions\\resources\\aircraft.zip");
            AircraftStateManager aircraftStateManager = new AircraftStateManager(database);
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                Message m = MessageParser.parse(new RawMessage(timeStampNs,message));
                if (m!=null){
                    aircraftStateManager.updateWithMessage(m);
                    aircraftStateManager.purge();
                    states = aircraftStateManager.states();
                }

            }
        } catch (EOFException e) { /* nothing to do */ }
        finally {
            List<ObservableAircraftState> states2 = new ArrayList<>(states);
            states2.sort(new AddressComparator());
            for(ObservableAircraftState o: states2){
                AircraftData data = o.getAircraftData();
                if (data!= null){
                    GeoPos p = o.getPosition();
                    System.out.printf("%5s %9s %7s %32s %20s %20s %9.0f %9.0f\n",o.address().string(),nullC(o.getCallSign()), data.registration().string(), data.model(), nullPosLon(p), nullPosLat(o.getPosition()),(o.getAltitude()),(Units.convertTo(o.getVelocity(),Units.Speed.KILOMETER_PER_HOUR)));
                }
            }
        }
    }

    public static void test3() throws IOException{
        ObservableSet<ObservableAircraftState> states = null;
        int count= 0;
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream("C:\\Users\\nedda\\Desktop\\PPOOjavions\\ppoo-project-2.0\\Javions\\messages_20230318_0915.bin")))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            AircraftDatabase database = new AircraftDatabase("C:\\Users\\nedda\\Desktop\\PPOOjavions\\ppoo-project-2.0\\Javions\\resources\\aircraft.zip");
            AircraftStateManager aircraftStateManager = new AircraftStateManager(database);

            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                Message m = MessageParser.parse(new RawMessage(timeStampNs,message));
                if (m!=null){
                    //count++;
                    aircraftStateManager.updateWithMessage(m);
                    aircraftStateManager.purge();
                    states = aircraftStateManager.states();
                }

            }
        } catch (EOFException e) { /* nothing to do */ }
        finally {
            List<ObservableAircraftState> states2 = new ArrayList<>(states);
            states2.sort(new AddressComparator());
            for(ObservableAircraftState o: states2){
                count++;
                AircraftData data = o.getAircraftData();
                if (data != null) {
                    //System.out.println(count);
                    GeoPos p = o.getPosition();
                    String registration=o.getAircraftData().registration()!=null ? o.getAircraftData().registration().string() : "     ";

                    System.out.printf("%5s %9s %7s %35s %20s %20s %9.0f %9.0f\n", o.address().string(), nullC(o.getCallSign()), registration, data.model(), nullPosLon(p), nullPosLat(o.getPosition()), (o.getAltitude()), (Units.convertTo(o.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)));
                }
                else {
                    GeoPos p = o.getPosition();
                    System.out.printf("%5s %9s %7s %32s %20s %20s %9.0f %9.0f\n", o.address().string(), nullC(o.getCallSign()), "        ", "       ", nullPosLon(p), nullPosLat(o.getPosition()), (o.getAltitude()), (Units.convertTo(o.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)));

                }
            }
            System.out.println(count);
        }
    }



}
