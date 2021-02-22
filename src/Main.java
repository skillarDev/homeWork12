import java.util.Arrays;

public class Main {
    static long[] timeWork = new long[2];

    private Object myObject = new Object();
    static final int SIZE = 10000000;
    static final int HALF = SIZE / 2;
    static String state = "no"; // state = "no" && work = "first" - поток запустится первым
    static float[] arrSource = new float[SIZE];

    public static void main(String[] args) {
        Arrays.fill(arrSource, 1);
        Main main = new Main();
        main.method1(); // запуск метода 1 в основном потоке

        // запуск метода 2 в двух дополнительных потоках
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                main.method2(arrSource, 0, 0, HALF, 1, "first"); // first //last
            }
        });

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                main.method2(arrSource, HALF,0, HALF, 2, "last"); //first //last
            }
        });

// если метод 1 запускать в 3 потоке то время на его выполнение особо не меняется но время выполнения 2 метода увеличивается

//        Thread thread3 = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                main.method1();
//            }
//        });
        thread1.start();
        thread2.start();
//        thread3.start();

    }


    public void method1() {

        long start = System.currentTimeMillis();
        System.out.println("(М1) Основной поток начал, мсек: " + start);
        for (int i = 0; i < arrSource.length; i++) {
            arrSource[i] = (float) (arrSource[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
        }
        long end = System.currentTimeMillis();
        System.out.println("(М1) Основной поток закончил, мсек: " + end);
        System.out.println("(М1) Общее время выполнения, мсек: " + (end - start));
        System.out.println();
    }

    public void method2(float[] arrSource, int srcPos, int destPos, int length, int numberThread, String work) {
        synchronized (myObject) {
            try {
                while ("last".equals(work) && "no".equals(state)) { // блокировка потока где work = "last"
                    myObject.wait();
                }

                // если время старта потока уже есть и текущее время > этого значения то ничего не делать
                // иначе присвоить текущее время
                if(timeWork[0] != 0 && System.currentTimeMillis() > timeWork[0]){
                } else {
                    timeWork[0] = System.currentTimeMillis();
                }

                System.out.println("(М2) " + numberThread + " поток начал: " + timeWork[0]);
                float[] arrHalf = new float[HALF];
                System.arraycopy(arrSource, srcPos, arrHalf, destPos, length); // копируем половину из исходника
                for (int i = 0; i < arrHalf.length; i++) {
                    arrHalf[i] = (float) (arrHalf[i] * Math.sin(0.2f + i / 5) * Math.cos(0.2f + i / 5) * Math.cos(0.4f + i / 2));
//                    System.out.println("arrHalf" + numberThread + "[" + i + "]: " + arrHalf[i]);
//                    Thread.sleep(100);
                } // заполнили значениями
                if (numberThread == 1) { // если это первая половина то копируем в 0 позицию
                    System.arraycopy(arrHalf, 0, arrSource, 0, length);

                } else if (numberThread == 2) { // если это вторая половина то копируем в HALF позицию
                    System.arraycopy(arrHalf, 0, arrSource, length, length);

                }
                timeWork[1] = System.currentTimeMillis(); // поток который выполняется последним перепишет время завершения
                System.out.println("(М2) " + numberThread + " поток закончил: " + timeWork[1]);
                state = "yes"; // меняю статическую переменную чтобы выйти из while
                myObject.notify(); // освобождаю объект

                if(work == "last") { // печатаем инфу о времени после звершения последнего потока
                    System.out.println("(М2) Общее время выполнения, мсек: " + timeWork[1] + " - " + timeWork[0] + " = " + (timeWork[1] - timeWork[0]));
                }
            } catch (InterruptedException e) {
            }
        }
    }
}


