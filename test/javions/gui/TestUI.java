package javions.gui;

import javions.ByteString;
import javions.Units;
import javions.adsb.Message;
import javions.adsb.MessageParser;
import javions.adsb.RawMessage;
import javions.aircraft.AircraftDatabase;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;

public class TestUI {

    public static void main(String[] args) throws IOException {
        System.out.println("\033[H\033[2J");
        var fileMac = "/Users/a.hatimy/Desktop/WORKSPACE/PPOOPROJECT/Javions/messages_20230318_0915.bin";
        var databaseFileMac = "/Users/a.hatimy/Desktop/WORKSPACE/PPOOPROJECT/Javions/resources/aircraft.zip";
        var filePC = "/Users/a.hatimy/Desktop/WORKSPACE/PPOOPROJECT/Javions/messages_20230318_0915.bin";
        var databaseFilePC = "/Users/a.hatimy/Desktop/WORKSPACE/PPOOPROJECT/Javions/resources/aircraft.zip";
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(
                new FileInputStream(fileMac)))) {
            var database = new AircraftDatabase(databaseFileMac);
            var manager = new AircraftStateManager(database);
            var bytes = new byte[RawMessage.LENGTH];
            long startTime = System.nanoTime();
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                Message pm = MessageParser.parse(new RawMessage(timeStampNs, new ByteString(bytes)));
                boolean bool;
                do {
                    Thread.sleep(1);
                    bool = System.nanoTime() - startTime >= timeStampNs;
                } while (!bool);
                if(pm != null) manager.updateWithMessage(pm);
                manager.purge();
                ArrayList<ObservableAircraftState> states = new ArrayList<>(manager.states());
                states.sort(new AddressComparator());
                if(!states.isEmpty()) printTable(states);
            }
        }
        catch (EOFException ignored) {}
        catch (InterruptedException e) {throw new RuntimeException(e);}
    }

    private static void printTable(ArrayList<ObservableAircraftState> states) throws InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append("\033[H\033[2J");
        sb.append(String.format("%-7s %-9s %-7s %-18s %-11s %-10s %-5s %-4s\n", "OACI", "Indicatif", "Immat.", "Modèle", "Longitude", "Latitude", "Alt.", "Vit."));
        sb.append("――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――\n");
        final char[] directions = new char[]{'↑', '↗', '→', '↘', '↓', '↙', '←', '↖'};
        for (ObservableAircraftState state : states) {
            int directionIndex = getDirectionArrow(state.getTrackOrHeading());
            String model;
            if (state.getAircraftData() != null && state.getAircraftData().model().length() >= 17) {
                model = state.getAircraftData().model().substring(0, 16) + "…";
            } else if (state.getAircraftData() != null) {
                model = state.getAircraftData().model();
            } else {
                model = "";
            }
            sb.append(String.format("%-7s %-9s %-7s %-17.17s %10.5f %10.5f %6d %5.0f %1s\n",
                    state.address().string(),
                    state.getCallSign() == null ? "" : state.getCallSign().string(),
                    state.getAircraftData() == null ? "" : state.getAircraftData().registration().string(),
                    model,
                    Units.convert(state.trajectory().get(state.trajectory().size() - 1).position().longitudeT32(), Units.Angle.T32, Units.Angle.DEGREE),
                    Units.convert(state.trajectory().get(state.trajectory().size() - 1).position().latitudeT32(), Units.Angle.T32, Units.Angle.DEGREE),
                    (int) Math.rint(state.trajectory().get(state.trajectory().size() - 1).altitude()),
                    Double.isNaN(state.getVelocity()) ? Double.NaN : (int) Math.rint(Units.convertTo(state.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)),
                    Array.getChar(directions, directionIndex)));
        }
        Thread.sleep(10);
        System.out.print(sb);
    }


    private static int getDirectionArrow(double trackOrHeading) {
        double offset = Math.PI / 8;
        if (trackOrHeading >= 0 + offset && trackOrHeading <= Math.PI) {
            if (trackOrHeading <= (Math.PI / 2) - offset) {
                return 1;
            } else if (trackOrHeading > ((Math.PI / 2) - offset) && trackOrHeading <= (Math.PI / 2) + offset) {
                return 2;
            } else if (trackOrHeading < Math.PI - offset) {
                return 3;
            } else {
                return 4;
            }
        } else if (trackOrHeading > Math.PI && trackOrHeading <= (2d * Math.PI) - offset) {
            if (trackOrHeading <= Math.PI + offset) {
                return 4;
            } else if (trackOrHeading > (Math.PI + offset) && (trackOrHeading < ((1.5d * Math.PI) - offset))) {
                return 5;
            } else if (trackOrHeading >= ((1.5d * Math.PI) - offset) && trackOrHeading <= ((1.5d * Math.PI) + offset)) {
                return 6;
            } else {
                return 7;
            }
        }
        return 0;
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
}