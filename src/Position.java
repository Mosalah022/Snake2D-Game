// قمنا بإنشاء هذا الكلاس لجعل أي كائن منه يمثل موقع دائرة من الدوائر الموجودة في الأفعى
public class Position {
 
    // إذاً كل دائرة في الأفعى ستكون موجودة في نفس الوقت على خط طول و خط عرض محددين
    int x, y;
 
    // قمنا بتجهيز هذا الكونستركتور لتحديد مكان وجود أي دائرة في الأفعى لحظة إنشاء الكائن منه
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
 
}
