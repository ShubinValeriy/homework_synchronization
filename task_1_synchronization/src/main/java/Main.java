import java.util.*;


public class Main {
    public static final Map<Integer, Integer> sizeToFreq = new TreeMap<>();

    public static void main(String[] args) throws InterruptedException {

        // Создадим отдельный поток для вывода на экран лидера в мапу sizeToFreq
        Thread threadMaxRepeated = new Thread(() -> {
            while (!Thread.interrupted()) {
                //определим в качестве монитора созданную МАРу
                synchronized (sizeToFreq) {
                    // опишем логику критической секции
                    try {
                        // Ожидаем вызова потока
                        sizeToFreq.wait();
                        if (Thread.interrupted()) {
                            break;
                        } else {
                            // Ищем Максимум повторений и выводим в консоль
                            Optional<Map.Entry<Integer, Integer>> maxRepeated = maxRepeated(sizeToFreq);
                            System.out.println("Текущее самое частое количество повторений " + maxRepeated.get().getKey() +
                                    " (встретилось " + maxRepeated.get().getValue() + " раз)");
                        }
                        // Информируем что завершили
                        sizeToFreq.notify();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        threadMaxRepeated.start();


        // Создание листа со списком потоков которые будут считать маршруты
        List<Thread> threadsTrack = new ArrayList<>();


        // Запуск генерацию 1000 потоков для расчета маршрутов
        for (int i = 1; i < 1001; i++) {
            int finalI = i;
            Runnable myRunnable = () -> {
                String generateRout = generateRoute("RLRFR", 100);
                int countRight = 0;
                for (int j = 0; j < generateRout.length(); j++) {
                    if (generateRout.charAt(j) == 'R') {
                        countRight++;
                    }
                }
                //определим в качестве монитора созданную МАРу
                synchronized (sizeToFreq) {
                    // опишем логику критической секции
                    if (sizeToFreq.containsKey(countRight)) {
                        int newCont = sizeToFreq.get(countRight) + 1;
                        sizeToFreq.put(countRight, newCont);
                    } else {
                        sizeToFreq.put(countRight, 1);
                    }
                    sizeToFreq.notify();
                    System.out.println("Маршрут № " + finalI + " содержит " + countRight + " R");
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            };
            Thread thread = new Thread(myRunnable);
            threadsTrack.add(thread);
            thread.start();
            thread.join(); // зависаем, ждём когда поток объект которого лежит в thread завершится
        }


        // Закроем поток расчета максимума без ошибки
        threadMaxRepeated.interrupt();
        synchronized (sizeToFreq) {
            sizeToFreq.notify();
        }

        System.out.println("Все МАРШРУТЫ сгенерированы!");


        System.out.println("\n \nИТОГО:");
        Optional<Map.Entry<Integer, Integer>> totalMaxRepeated = maxRepeated(sizeToFreq);
        System.out.println("Самое частое количество повторений " + totalMaxRepeated.get().getKey() +
                " (встретилось " + totalMaxRepeated.get().getValue() + " раз)");
        // Выведем в консоль остальные варианты
        System.out.println("Другие размеры:");
        sizeToFreq.remove(maxRepeated(sizeToFreq).get().getKey());
        sizeToFreq.forEach((k, v) -> System.out.println("- " + k + " (" + v + " раз)"));
    }


    public static Optional<Map.Entry<Integer, Integer>> maxRepeated(Map<Integer, Integer> map) {
        return map.entrySet().
                stream().
                max(Map.Entry.comparingByValue()
                );
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}
