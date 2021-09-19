package biosignal.filter;

public class QRSFilter implements Filter {
//    private static final int PEAK_VALUE = 100;
//    private static final int NOICE_VALUE = -10;

    private int sampleIntervalMs = 1;
    private int restTimeMs;
    private int lastValue;
    // Все значения определены для sampleIntervalMs = 1ms. dataSampleRat = 1000 Герц.
    // Переход на 500 Герц скорее всего проблем не вызовет.
    // Перход на 250 Герц и ниже, возможно, потребует усложнения алгоритма: использования интерполяции
    private int t1Min = 300; // Интервал между QRS комплексами Ms
    private int t1Max = 1500;

    private int t2Min = 24;  // Интервал роста амплитуды QRS пика Ms
    private int t2Max = 32;

    private int t3Min = 6;  // Интервал роста скорости спада QRS пика Ms
    private int t3Max = 12;

    private int t4Mин = 32; // Пауза для завершения QRS комплекса после распознания пика Ms

    private boolean isT1 = true;  // Пауза между QRS комплексами
    private boolean isT2 = false; // Рост амплитуды QRS пика
    private boolean isT3 = false; // Рост скорости спада QRS пика
    private boolean isT4 = false; // Завершение QRS комплекса после распознания пика

    public QRSFilter(double dataSampleRate) {
        sampleIntervalMs = (int) (1000 / dataSampleRate);
    }

    @Override
    public int apply(int value) {
        if (isT1) { // Пауза между QRS комплексами
            if (value == 0) {
                restTimeMs += sampleIntervalMs;
                return 0;
            }
            if (value > 0) {
                reset();
                return 0;
            }
            if (restTimeMs > t1Min && restTimeMs < t1Max) {
//                System.out.println("Т1 = " + restTimeMs);
                isT1 = false;
                isT2 = true;
                restTimeMs = 0;
                return 0;
            }
            reset();
            return -Math.abs(value/40); // Индикация наличия больших помех.
        }

        if (isT2) {  // Рост амплитуды QRS пика
            if (value <= 0) {
                restTimeMs += sampleIntervalMs;
                return 0;
            }
            if (restTimeMs > t2Min && restTimeMs < t2Max) {
//                    System.out.println("Т2 = " + restTimeMs);
                isT2 = false;
                isT3 = true;
                restTimeMs = 0;
                return 0;
            }
            reset();
            return 0;
        }

        if (isT3) { // Рост скорости спада QRS пика
            if (value > lastValue) {
                restTimeMs += sampleIntervalMs;
                lastValue = value;
                return 0;
            }
            if (restTimeMs > t3Min && restTimeMs < t3Max) {
//                    System.out.println("Т3 = " + restTimeMs);
                int v = lastValue;
                reset();
                isT1 = false;
                isT4 = true;
                return v; // Вазврат величины значения пика скорости спада.
            }
            reset();
            return 0;
        }

        if (isT4) { // Завершение QRS комплекса после распознания пика
            restTimeMs += sampleIntervalMs;
            if (restTimeMs > t4Mин) {
//                System.out.println("Т4 = " + restTimeMs);
                reset();
            }
        }
        return 0;
    }

    private void reset() {
        isT1 = true;
        isT2 = false;
        isT3 = false;
        isT4 = false;
        restTimeMs = 0;
        lastValue = 0;
    }
}
