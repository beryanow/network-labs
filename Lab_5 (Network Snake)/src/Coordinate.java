public class Coordinate {
    private int x;  // По горизонтальной оси, положительное направление - вправо
    private int y;  // По вертикальной оси, положительное направление - вниз
    private Type newType;
    private Type oldType;

    Coordinate(int x, int y, Type newType, Type oldType) {
        this.x = x;
        this.y = y;
        this.newType = newType;
        this.oldType = oldType;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    void setX(int x) {
        this.x = x;
    }

    void setY(int y) {
        this.y = y;
    }

    void setNewType(Type newType) {
        this.newType = newType;
    }

    Type getNewType() {
        return newType;
    }

    void setOldType(Type oldType) {
        this.oldType = oldType;
    }

    Type getOldType() {
        return oldType;
    }
}