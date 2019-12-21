import com.google.gson.annotations.Expose;

public class GameConfiguration {
    @Expose
    private int width = 40;                   // Ширина поля в клетках (от 10 до 100)
    @Expose
    private int height = 30;                  // Высота поля в клетках (от 10 до 100)
    private int foodStaticAmount = 1;         // Количество клеток с едой, независимо от числа игроков (от 0 до 100)
    private double foodPlayerAmount = 1.0;    // Количество клеток с едой, на каждого игрока (вещественный коэффициент от 0 до 100)
    private int delay = 1000;                 // Задержка между ходами (сменой состояний) в игре, в миллисекундах
    private double deadFoodProbability = 0.1; // Вероятность превращения мёртвой клетки в еду (от 0 до 1)
    private boolean multicastNeeded;

    GameConfiguration(int width, int height, int foodStaticAmount, double foodPlayerAmount, int delay, double deadFoodProbability) {
        this.width = width;
        this.height = height;
        this.foodStaticAmount = foodStaticAmount;
        this.foodPlayerAmount = foodPlayerAmount;
        this.delay = delay;
        this.deadFoodProbability = deadFoodProbability;
        multicastNeeded = false;
    }

    void setMulticastNeededTrue() {
        this.multicastNeeded = true;
    }

    boolean getMulticastNeeded() {
        return multicastNeeded;
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    int getDelay() {
        return delay;
    }

    double getDeadFoodProbability() {
        return deadFoodProbability;
    }

    int getFoodStaticAmount() {
        return foodStaticAmount;
    }
}
