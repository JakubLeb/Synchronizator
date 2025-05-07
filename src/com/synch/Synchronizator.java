package com.synch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Synchronizator {
    private AtomicBoolean monitor;
    private ExecutorService executor;
    private SynMainFrame synMainFrame;

    public Synchronizator(SynMainFrame synMainFrame) {
        this.synMainFrame = synMainFrame;
        monitor = new AtomicBoolean(false);
        executor = Executors.newSingleThreadExecutor();
    }

    public void start() {
        if (monitor.compareAndSet(false, true)) {
            Runnable task = () -> {
                while (monitor.get()) {
                    try {
                    	synchronize();
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        System.out.println("Wątek monitorujący obudzony!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            executor.execute(task);
        }
    }

    public void stop() {
        monitor.set(false);
    }

    public void dispose() {
        executor.shutdownNow();
    }


 // Ta metoda sprawdza, czy czas ostatniej modyfikacji pliku fileA jest późniejszy niż pliku fileB
    public boolean fileLastEditCheck(Path fileA, Path fileB) throws IOException {
        // Pobieramy czas ostatniej modyfikacji obu plików
        FileTime lastModifiedA = Files.getLastModifiedTime(fileA);
        FileTime lastModifiedB = Files.getLastModifiedTime(fileB);
        
        // Porównujemy czasy modyfikacji; zwracamy true, jeśli fileA jest nowszy
        return lastModifiedA.compareTo(lastModifiedB) > 0;
    }

    private void synchronize() throws IOException {
        // Definiujemy ścieżki do katalogów A i B
        Path pathA = Paths.get("src/A");
        Path pathB = Paths.get("src/B");

        // Pobieramy listę plików w obu katalogach i zapisujemy tylko ich nazwy
        Path[] filesInA = Files.list(pathA)
                                .map(Path::getFileName)
                                .toArray(Path[]::new);
        Path[] filesInB = Files.list(pathB)
                                .map(Path::getFileName)
                                .toArray(Path[]::new);

        // Rozpoczynamy synchronizację plików (kopiowanie i usuwanie plików, jeśli to konieczne)
        copyFiles(pathA, pathB, filesInA, filesInB);
        deleteFiles(pathA, pathB, filesInA, filesInB);
    }

    private void copyFiles(Path pathA, Path pathB, Path[] filesInA, Path[] filesInB) throws IOException {
        // Iterujemy po każdym pliku w katalogu A
        for (Path fileFromA : filesInA) {
            boolean fileExistsInB = false;

            // Sprawdzamy, czy plik z katalogu A istnieje w katalogu B
            for (Path fileFromB : filesInB) {
                if (fileFromA.equals(fileFromB)) {
                    fileExistsInB = true;
                    
                    // Jeśli plik istnieje w obu katalogach, sprawdzamy, czy plik w A jest nowszy
                    Path src = Paths.get(pathA.toString(), fileFromA.toString());
                    Path dst = Paths.get(pathB.toString(), fileFromB.toString());

                    if (fileLastEditCheck(src, dst)) {
                        // Logujemy akcję nadpisania pliku i nadpisujemy plik w katalogu B
                        synMainFrame.addMessageToTextArea(
                            String.format("Nadpisywanie pliku %s w katalogu B nowszą wersją z katalogu A.", fileFromA)
                        );
                        Files.copy(src, dst, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    break;
                }
            }
            
            // Jeśli plik nie istnieje w katalogu B, kopiujemy go z A do B
            if (!fileExistsInB) {
                synMainFrame.addMessageToTextArea(
                    String.format("Kopiowanie pliku %s do katalogu B", fileFromA)
                );
                Path src = Paths.get(pathA.toString(), fileFromA.toString());
                Path dst = Paths.get(pathB.toString(), fileFromA.toString());
                
                // Tworzymy katalog B, jeśli nie istnieje
                if (!Files.exists(pathB)) {
                    Files.createDirectories(pathB);
                }
                Files.copy(src, dst);
            }
        }
    }

    private void deleteFiles(Path pathA, Path pathB, Path[] filesInA, Path[] filesInB) throws IOException {
        // Iterujemy po każdym pliku w katalogu B
        for (Path fileFromB : filesInB) {
            boolean fileExistsInA = false;
            
            // Sprawdzamy, czy plik z katalogu B istnieje w katalogu A
            for (Path fileFromA : filesInA) {
                if (fileFromB.equals(fileFromA)) {
                    fileExistsInA = true;
                    break;
                }
            }
            
            // Jeśli plik nie istnieje w katalogu A, usuwamy go z katalogu B
            if (!fileExistsInA) {
                synMainFrame.addMessageToTextArea(
                    String.format("Usuwanie pliku %s z katalogu B", fileFromB)
                );
                Path file = Paths.get(pathB.toString(), fileFromB.toString());
                Files.delete(file);
            }
        }
    }



    public static void main(String[] args) {
        Synchronizator synch = new Synchronizator(null); 
        synch.start();
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
        }
        synch.stop();
        synch.dispose();
    }
}
