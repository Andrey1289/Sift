package org.andrey.util;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

    public class DataFilterUtility {

        public static void main(String[] args) {
            // Параметры по умолчанию
            String outputDir = ".";
            String prefix = "";
            boolean appendMode = false;
            boolean shortStats = false;
            boolean fullStats = false;

            List<String> inputFiles = new ArrayList<>();
//            inputFiles.add("in1.txt");
//            inputFiles.add("in2.txt");

            // Парсинг аргументов командной строки
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-o":
                        outputDir = args[++i];
                        break;
                    case "-p":
                        prefix = args[++i];
                        break;
                    case "-a":
                        appendMode = true;
                        break;
                    case "-s":
                        shortStats = true;
                        break;
                    case "-f":
                        fullStats = true;
                        break;
                    default:
                        inputFiles.add(args[i]);
                }
            }

            // Проверка наличия входных файлов
           if (inputFiles.isEmpty()) {
                System.err.println("Ошибка: Не указаны входные файлы.");
                return;
            }

            // Создание выходной директории, если задана
            File outputDirectory = new File(outputDir);
            if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
                System.err.println("Ошибка: Не удалось создать директорию " + outputDir);
                return;
            }

            // Списки для хранения данных
            List<BigInteger> integers = new ArrayList<>();
            List<BigDecimal> floats = new ArrayList<>();
            List<String> strings = new ArrayList<>();

            // Чтение данных из входных файлов
            for (String fileName : inputFiles) {
                try {
                    // Получение ресурса из папки resources
                    InputStream inputStream = DataFilterUtility.class.getClassLoader().getResourceAsStream(fileName);
                    if (inputStream == null) {
                        System.err.println("Файл не найден в папке resources: " + fileName);
                        continue;
                    }
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty()) continue;

                            try {
                                if (line.matches("-?\\d+")) {
                                    // Используем BigInteger для больших целых чисел
                                    integers.add(new BigInteger(line));
                                } else if (line.matches("-?\\d*\\.\\d+(E-?\\d+)?")) {
                                    // Используем BigDecimal для вещественных чисел
                                    floats.add(new BigDecimal(line));
                                } else {
                                    strings.add(line);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Ошибка чтения файла: " + fileName);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            // Запись данных в файлы
            writeData(outputDir, prefix + "integers.txt", integers, appendMode);
            writeData(outputDir, prefix + "floats.txt", floats, appendMode);
            writeData(outputDir, prefix + "strings.txt", strings, appendMode);

            // Вывод статистики
            if (shortStats || fullStats) {
                System.out.println("Статистика:");
                printStatistics("Целые числа", integers, shortStats, fullStats);
                printStatistics("Вещественные числа", floats, shortStats, fullStats);
                printStatistics("Строки", strings, shortStats, fullStats);
            }
        }

        /**
         * Метод записи данных в файл.
         */
        private static <T> void writeData(String dir, String fileName, List<T> data, boolean append) {
            if (data.isEmpty()) return;

            String filePath = Paths.get(dir, fileName).toString();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
                for (T item : data) {
                    writer.write(item.toString());
                    writer.newLine();
                }
                System.out.println("Данные записаны в файл: " + filePath);
            } catch (IOException e) {
                System.err.println("Ошибка записи файла: " + filePath);
            }
        }

        /**
         * Метод вывода статистики.
         */
        private static <T> void printStatistics(String label, List<T> data, boolean shortStats, boolean fullStats) {
            if (data.isEmpty()) return;

            System.out.println(label + ":");
            System.out.println("  Количество: " + data.size());

            if (fullStats) {
                if (data.get(0) instanceof Number) {
                    List<Double> numbers = data.stream().map(d -> ((Number) d).doubleValue()).collect(Collectors.toList());
                    System.out.println("  Минимум: " + Collections.min(numbers));
                    System.out.println("  Максимум: " + Collections.max(numbers));
                    System.out.println("  Сумма: " + numbers.stream().mapToDouble(Double::doubleValue).sum());
                    System.out.println("  Среднее: " + numbers.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
                } else if (data.get(0) instanceof String) {
                    List<String> strings = (List<String>) data;
                    int minLength = strings.stream().mapToInt(String::length).min().orElse(0);
                    int maxLength = strings.stream().mapToInt(String::length).max().orElse(0);
                    System.out.println("  Минимальная длина: " + minLength);
                    System.out.println("  Максимальная длина: " + maxLength);
                }
            }
        }
    }
