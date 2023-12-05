import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;


public class Main {
    public static final Map<Integer, Integer> sizeToFreq = new TreeMap<>();

    public static void main(String[] args) throws InterruptedException {

        //Зададим правильное наименование потоков в пуле
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("Маршрут-%d")
                .build();

        // Заведение пула потоков
        final ExecutorService threadPool = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(), threadFactory
        );

        // Запуск генерацию 1000 маршрутов в многопоточном варианте
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
                System.out.println("Маршрут № " + finalI + " содержит " + countRight + " R");
                //определим в качестве монитора созданную МАРу
                synchronized (sizeToFreq) {
                    // опишем логику критической секции
                    if (sizeToFreq.containsKey(countRight)) {
                        int newCont = sizeToFreq.get(countRight) + 1;
                        sizeToFreq.put(countRight, newCont);
                    } else {
                        sizeToFreq.put(countRight, 1);
                    }
                }
            };
            threadPool.submit(myRunnable);
        }

        // пробуем закрыть пулл потоков
        threadPool.shutdown();
        // ждем окончания работы 1 минуту
        if (threadPool.awaitTermination(1, TimeUnit.MINUTES)) {
            System.out.println("Все МАРШРУТЫ сгенерированы!");
        } else {
            // принудительно останавливаем
            List<Runnable> notExecuted = threadPool.shutdownNow();
            System.out.printf("Не сгенерировано %d МАРШРУТОВ.%n", notExecuted.size());
        }


        // выведем в консоль МАПу
        // для начала найдем Максимум повторений и выведем в консоль
        Optional<Map.Entry<Integer, Integer>> maxRepeated = sizeToFreq.entrySet().
                stream().
                max(Map.Entry.comparingByValue()
                );
        System.out.println("Самое частое количество повторений " + maxRepeated.get().getKey() +
                " (встретилось " + maxRepeated.get().getValue() + " раз)");
        // Выведем в консоль остальные варианты
        System.out.println("Другие размеры:");
        sizeToFreq.remove(maxRepeated.get().getKey());
        sizeToFreq.forEach((k, v) -> System.out.println("- " + k + " (" + v + " раз)"));
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
