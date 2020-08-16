import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    // قمنا بإنشاء هاتين المصفوفتين لتخزين مكان وجود كل دائرة من الأفعى كل لحظة, أي لتحديد المكان المحجوز لعرض دوائر الأفعى
    // ( بما أن عدد الدوائر الأقصى بالطول هو 22 و بالعرض هو أيضاً 22, فهذا يعني أنه يمكن تخزين 22×22 دائرة ( أي 484
    final int[] boardX = new int[484];
    final int[] boardY = new int[484];

    // سنستخدم هذه الكائن لتخزين مكان وجود كل دائرة في الأفعى حتى نعرف الموقع الذي لا يجب أن نظهر فيه الدائرة الحمراء
    // ملاحظة: سبب إستخدام هذا الكائن هو لتخزين موقع كل دائرة في الأفعى من جديد هو فقط لجعل اللعبة لا تعلق, أي لتحسين أداء اللعبة
    LinkedList<Position> snake = new LinkedList();

    // سنستخدم هذه المتغيرات لتحديد الإتجاه الذي ستتجه إليه الأفعى
    boolean left = false;
    boolean right = false;
    boolean up = false;
    boolean down = false;

    // سنستخدم هذه الكائنات لرسم إتجاه وجه الأفعى
    Image lookToRightImage = new Image(getClass().getResourceAsStream("/images/face-look-right.jpg"));
    Image lookToLeftImage = new Image(getClass().getResourceAsStream("/images/face-look-left.jpg"));
    Image lookToUpImage = new Image(getClass().getResourceAsStream("/images/face-look-up.jpg"));
    Image lookToDownImage = new Image(getClass().getResourceAsStream("/images/face-look-down.jpg"));

    // سنستخدم هذا الكائن في كل مرة لرسم جسد الأفعى
    Image snakeBodyImage = new Image(getClass().getResourceAsStream("/images/body.png"));

    // سنستخدم هذا الكائن في كل مرة لرسم طعام الأفعى
    Image fruitImage = new Image(getClass().getResourceAsStream("/images/fruit.png"));

    // سنستخدم هذا المتغير لتخزين عدد الدوائر التي تشكل الأفعى, أي طول الأفعي
    int lengthOfSnake = 3;

    // سنستخدم هاتين المصفوفتين لتحديد الأماكن التي يمكن أن يظهر فيها الطعام
    int[] fruitXPos = {20, 40, 60, 80, 100, 120, 140, 160, 200, 220, 240, 260, 280, 300, 320, 340, 360, 380, 400, 420, 440, 460};
    int[] fruitYPos = {20, 40, 60, 80, 100, 120, 140, 160, 200, 220, 240, 260, 280, 300, 320, 340, 360, 380, 400, 420, 440, 460};

    // سنستخدم هذا الكائن لجعل محتوى النافذة يعاد رسمه .Thread و الذي يشبه الـ Timeline هنا قمنا بإنشاء كائن من الكلاس
    // من جديد كل 0.1 ثانية مما يجعلنا قادرين على رسم الأفعى من جديد كلما تغير موقعها بالإضافة إلى رسم مجموع المستخدم
    Timeline timeline = new Timeline();

    // سنستخدم هذا المتغير لمعرفة إذا كانت الأفعى تتحرك أم لا
    int moves = 0;

    // سنستخدم هذه المتغيرات لتحديد المجموع الذي يحققه اللاعب أثناء اللعب
    int totalScore = 0;
    int fruitEaten = 0;
    int scoreReverseCounter = 99;

    // في حال كان اللاعب قد حقق مجموع عالي من قبل, سيتم إظهاره كأفضل مجموع وصل إليه
    // readBestScorefromTheFile() ملاحظة: أعلى مجموع يصل إليه اللاعب, نحصل عليه من الدالة
    int bestScore = readBestScorefromTheFile();

    // لتوليد أماكن ظهور طعام الأفعى بشكل عشوائي random سنستخدم الكائن
    Random random = new Random();

    // هنا قمنا بتحديد مكان أول طعام سيظهر في اللعبة قبل أن يبدأ المستخدم باللعب, و جعلناه يظهر تحت الأفعى
    int xPos = random.nextInt(22);
    int yPos = 5 + random.nextInt(17);

    // سنستخدم هذا المتغير لمعرفة ما إذا كان المستخدم قد خسر أم لا
    boolean isGameOver = false;

    // هذه الدالة تحفظ أعلى مجموع وصل إليه اللاعب في ملف خارجي بجانب ملف اللعبة
    private void writeBestScoreInTheFile() {
        if (totalScore >= bestScore) {
            try {
                FileOutputStream fos = new FileOutputStream("./snake-game-best-score.txt");
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
                osw.write(bestScore + "");
                osw.flush();
                osw.close();
            } catch (IOException e) {
            }
        }
    }

    // هذه الدالة تقرأ أعلى مجموع وصل إليه اللاعب من الملف الخارجي الموجود بجانب ملف اللعبة مع الإشارة إلى أنه في حال كان
    // لا يوجد ملف خارجي ستقوم بإنشائه و وضع القيمة 0 فيه كقيمة أولية و هذا ما سيحدث عندما يقوم اللاعب بتشغيل اللعبة أول مرة
    private int readBestScorefromTheFile() {
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream("./snake-game-best-score.txt"), "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String str = "";
            int c;
            while ((c = br.read()) != -1) {
                if (Character.isDigit(c)) {
                    str += (char) c;
                }
            }
            if (str.equals("")) {
                str = "0";
            }

            br.close();
            return Integer.parseInt(str);
        } catch (IOException e) {
        }
        return 0;
    }

    // بهدف تحديد كيف سيتم رسم و تلوين كل شيء يظهر في اللعبة drawShapes() هنا قمنا بتعريف الدالة
    // قلنا أنه سيتم إستدعاء هذه الدالة كل 0.1 ثانية start() ملاحظة: في الدالة
    private void drawShapes(GraphicsContext gc) {
        // هنا قمنا بتحديد مكان وجود الأفعى في كل مرة يقوم المستخدم ببدأ اللعبة من جديد
        if (moves == 0) {
            boardX[2] = 40;
            boardX[1] = 60;
            boardX[0] = 80;

            boardY[2] = 100;
            boardY[1] = 100;
            boardY[0] = 100;

            scoreReverseCounter = 99;
            timeline.play();
        }

        // هنا قمنا بجعل المجموع الحالي الذي وصل إليه المستخدم يظهر كأعلى مجموع وصل إليه في حال تخطى المجموع القديم
        if (totalScore > bestScore) {
            bestScore = totalScore;
        }

        // هنا قمن بإنشاء مربع أسود يمثل لون خلفية اللعبة
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 750, 500);

        // هنا قمنا برسم المربعات التي تشكل الحدود التي لا تستطيع الأفعى عبورها باللون الرمادي
        // حجم كل مربع 13 بيكسل و المسافة بينهما 5 بيكسل
        gc.setFill(Color.color(0.2, 0.2, 0.2));
        for (int i = 6; i <= 482; i += 17) {
            for (int j = 6; j <= 482; j += 17) {
                gc.fillRect(i, j, 13, 13);
            }
        }

        // هنا فمنا بإنشاء مربع أسود كبير فوق المربعات التي تشكل حدود اللعبة لتظهر و كأنها فارغة من الداخل
        gc.setFill(Color.BLACK);
        gc.fillRect(20, 20, 460, 460);

        // هنا قمنا بكتابة إسم اللعبة و تلوينه بالأزرق
        gc.setFill(Color.CYAN);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        gc.fillText("Snake 2D", 565, 35);

        // باللون الأزرق الفاتح Total Score التي ستظهر بجانب قيمة الـ Bonus هنا قمنا برسم النص قيمة الـ
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        gc.fillText("+ " + scoreReverseCounter, 510, 222);

        // هنا جعلنا أي شيء سنقوم بكتابته يظهر باللون الرمادي
        gc.setFill(Color.LIGHTGRAY);

        // هنا قمنا بطباعة أنه تم تطوير اللعبة بواسطة موقعنا
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        gc.fillText("Developed by Mohammed Salah", 530, 60);

        // هنا جعلنا أي شيء سنقوم بكتابته يظهر بنوع و حجم هذا الخط
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 18));

        // و المربع الذي تحته و الرقم الذي بداخله Best Score هنا قمنا برسم النص
        gc.fillText("Best Score", 576, 110);
        gc.fillRect(550, 120, 140, 30);
        gc.setFill(Color.BLACK);
        gc.fillRect(551, 121, 138, 28);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText(bestScore + "", 550 + (142 - new Text(bestScore + "").getLayoutBounds().getWidth()) / 2, 142);

        // و المربع الذي تحته و الرقم الذي بداخله Total Score هنا قمنا برسم النص
        gc.fillText("Total Score", 573, 190);
        gc.fillRect(550, 200, 140, 30);
        gc.setFill(Color.BLACK);
        gc.fillRect(551, 201, 138, 28);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText(totalScore + "", 550 + (142 - new Text(totalScore + "").getLayoutBounds().getWidth()) / 2, 222);

        // و المربع الذي تحته و الرقم الذي بداخله Fruit Eaten هنا قمنا برسم النص
        gc.fillText("Fruit Eaten", 575, 270);
        gc.fillRect(550, 280, 140, 30);
        gc.setFill(Color.BLACK);
        gc.fillRect(551, 281, 138, 28);
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText(fruitEaten + "", 550 + (142 - new Text(fruitEaten + "").getLayoutBounds().getWidth()) / 2, 302);

        // Controls هنا قمنا برسم النص
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.fillText("Controls", 550, 360);

        // Controls هنا قمنا برسم نصوص الإرشاد الظاهرة تحت النص
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.fillText("Pause / Start : Space", 550, 385);
        gc.fillText("Move Up : Arrow Up", 550, 410);
        gc.fillText("Move Down : Arrow Down", 550, 435);
        gc.fillText("Move Left : Arrow Left", 550, 460);
        gc.fillText("Move Right : Arrow Right", 550, 485);

        // هنا جعلنا الأفعى تنظر ناحية اليمين قبل أن يبدأ اللاعب بتحريكها
        gc.drawImage(lookToRightImage, boardX[0], boardY[0]);

        // هنا قمنا بمسح مكان وجود الأفعى السابق لأننا سنقوم بتخزين المكان الجديد كلما تحركت
        snake.clear();

        // هنا قمنا بإنشاء حلقة ترسم كامل الدوائر التي تشكل الأفعى كل 0.1 ثانية
        for (int i = 0; i < lengthOfSnake; i++) {
            if (i == 0 && left) {
                gc.drawImage(lookToLeftImage, boardX[i], boardY[i]);
            } else if (i == 0 && right) {
                gc.drawImage(lookToRightImage, boardX[i], boardY[i]);
            } else if (i == 0 && up) {
                gc.drawImage(lookToUpImage, boardX[i], boardY[i]);
            } else if (i == 0 && down) {
                gc.drawImage(lookToDownImage, boardX[i], boardY[i]);
            } else if (i != 0) {
                gc.drawImage(snakeBodyImage, boardX[i], boardY[i]);
            }

            // snake هنا قمنا بتخزين الموقع الحالي لكل دائرة في الأفعى في الكائن
            snake.add(new Position(boardX[i], boardY[i]));
        }

        // تقل بشكل تدريجي و أدنى قيمة ممكن أن تصل إليها هي 10 scoreReverseCounter هنا جعلنا قيمة العداد
        if (scoreReverseCounter != 10) {
            scoreReverseCounter--;
        }

        // هنا قمنا بإنشاء هذه الحلقة للتأكد إذا كان رأس الأفعى قد لامس أي جزء من جسدها
        for (int i = 1; i < lengthOfSnake; i++) {
            // إذاً عندما يلمس رأس الأفعى جسدها سيتم جعل أول دائرة موجودة خلف الرأس تمثل رأس الأفعى حتى لا يظهر رأسها فوق جسدها
            if (boardX[i] == boardX[0] && boardY[i] == boardY[0])
            {
                if (right)
                    gc.drawImage(lookToRightImage, boardX[1], boardY[1]);
                
                else if (left)
                    gc.drawImage(lookToLeftImage, boardX[1], boardY[1]);
                
                else if (up)
                    gc.drawImage(lookToUpImage, boardX[1], boardY[1]);
                
                else if (down)
                    gc.drawImage(lookToDownImage, boardX[1], boardY[1]);

                // للإشارة إلى أن اللاعب قد خسر true تساوي isGameOver بعدها سيتم جعل قيمة الـ
                // Space و بالتالي يمكنه أن يبدأ من جديد بالنقر على زر المسافة الفارغة
                isGameOver = true;

                // يتوقف و بالتالي ستتوقف الأفعى تماماً عن الحركة speedTimeline بعدها سيتم جعل الـ
                timeline.stop();

                // Game Over بعدها سيتم إظهار النص
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));
                gc.fillText("Game Over", 110, 220);

                // تحته Press Space To Restart و سيتم إظهار النص
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
                gc.fillText("Press Space To Restart", 130, 260);

                // في الأخير سيتم إستدعاء هذه الدالة لحفظ أكبر مجموع وصل إليه اللاعب
                writeBestScoreInTheFile();
            }
        }

        // إذا لمس رأس الأفعى الطعام سيتم إخفاء الطعام و زيادة مجموع اللاعب
        if ((fruitXPos[xPos] == boardX[0]) && fruitYPos[yPos] == boardY[0]) {
            totalScore += scoreReverseCounter;
            scoreReverseCounter = 99;
            fruitEaten++;
            lengthOfSnake++;
        }

        // هنا في كل مرة سيتم ضمان أن لا يظهر الطعام فوق الأفعى
        for (int i = 0; i < snake.size(); i++) {
            // في حال ظهر الطعام فوق جسد الأفعى سيتم خلق مكان عشوائي آخر لوضعها فيه
            if (snake.get(i).x == fruitXPos[xPos] && snake.get(i).y == fruitYPos[yPos]) {
                xPos = random.nextInt(22);
                yPos = random.nextInt(22);
            }
        }

        // في الأخير سيتم عرض الطعام بعيداً عن جسد الأفعى
        gc.drawImage(fruitImage, fruitXPos[xPos], fruitYPos[yPos]);

        if (right)
        {
            for (int i = lengthOfSnake - 1; i >= 0; i--)
                boardY[i + 1] = boardY[i];

            for (int i = lengthOfSnake; i >= 0; i--)
            {
                if (i == 0)
                    boardX[i] = boardX[i] + 20;
                else
                    boardX[i] = boardX[i - 1];
                

                if (boardX[i] > 460) 
                    boardX[i] = 20;
            }
        } 
        else if (left) {
            for (int i = lengthOfSnake - 1; i >= 0; i--)
                boardY[i + 1] = boardY[i];

            for (int i = lengthOfSnake; i >= 0; i--)
            {
                if (i == 0)
                    boardX[i] = boardX[i] - 20;
                
                else
                    boardX[i] = boardX[i - 1];

                if (boardX[i] < 20)
                    boardX[i] = 460;
            }
        }
        else if (up)
        {
            for (int i = lengthOfSnake - 1; i >= 0; i--)
                boardX[i + 1] = boardX[i];

            for (int i = lengthOfSnake; i >= 0; i--)
            {
                if (i == 0)
                    boardY[i] = boardY[i] - 20;
                else
                    boardY[i] = boardY[i - 1];

                if (boardY[i] < 20)
                    boardY[i] = 460;
            }
        } 
        else if (down)
        {
            for (int i = lengthOfSnake - 1; i >= 0; i--)
                boardX[i + 1] = boardX[i];

            for (int i = lengthOfSnake; i >= 0; i--)
            {
                if (i == 0)
                    boardY[i] = boardY[i] + 20;
                else
                    boardY[i] = boardY[i - 1];

                if (boardY[i] > 460)
                    boardY[i] = 20;
            }
        }
        
    }

    
    @Override
    public void start(Stage stage) {

        // لأنه يمثل حاوية يمكن الرسم عليها بسهولة Canvas هنا قمنا بإنشاء كائن من الكلاس
        Canvas canvas = new Canvas(750, 500);
        
        // canvas لأننا سنستخدمه للرسم على الكائن canvas مبني على الكائن GraphicsContext هنا قمنا بإنشاء كائن من الكلاس
        // gc سيكون بواسطة دوال جاهزة موجودة في الكائن canvas فعلياً أي شيء سنرسمه على الكائن
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // في النافذة لأننا ننوي ترتيب العناصر فيه بشكل عامودي Root Node و الذي ننوي جعله الـ VBox هنا قمنا بإنشاء كائن من الكلاس
        Pane root = new Pane();
        root.setStyle("-fx-background-color: black;");

        root.getChildren().add(canvas);

        // فيها و تحديد حجمها Node كأول root هنا قمنا بإنشاء محتوى النافذة مع تعيين الكائن
        Scene scene = new Scene(root);

        // هنا وضعنا عنوان للنافذة
        stage.setTitle("Snake 2D");

        // أي وضعنا محتوى النافذة الذي قمنا بإنشائه للنافذة .stage في كائن الـ scene هنا وضعنا كائن الـ
        stage.setScene(scene);

        // هنا قمنا بإظهار النافذة
        stage.show();

        // timeline لترسم محتوى النافذة كل 0.1 ثانية بشكل تلقائي عندما يتم تشغيل الكائن drawShapes() هنا قمنا باستدعاء الدالة
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.1), (ActionEvent event) -> {
            drawShapes(gc);
        }));

        // يستمر بالعمل بلا توقف حين يتم تشغيله timeline لها لجعل الكائن INDEFINITE و تمرير الثابت setCycleCount() هنا قمنا باستدعاء الدالة
        timeline.setCycleCount(Timeline.INDEFINITE);

        // timeline لتشغيل الكائن play() هنا قمنا باستدعاء الدالة
        timeline.play();

        // لتحديد الإتجاه الذي ستتحرك فيه النافذة keyPressed() للدالة Override هنا فعلنا
        scene.addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent e) -> {

            if (null != e.getCode())
            {
                switch (e.getCode())
                {
                    // هنا قمنا بتحديد ما سيحدث إذا قام اللاعب بالنقر على زر المسافة
                    case SPACE:
                        // سيتم إيقاف اللعبة بشكل مؤقت إذا كانت اللعبة شغالة
                        if (timeline.getStatus() == Timeline.Status.RUNNING && isGameOver == false)
                        {
                            timeline.stop();
                        }
                        // سيتم إعادة اللعبة للعمل إذا كان قد تم إيقافها سابقاً
                        else if (timeline.getStatus() != Timeline.Status.RUNNING && isGameOver == false)
                        {
                            timeline.play();
                        }
                        // سيتم بدأ اللعبة من جديد في حال كان قد تم إيقاف اللعبة لأن اللاعب قد خسر
                        else if (timeline.getStatus() != Timeline.Status.RUNNING && isGameOver == true)
                        {
                            isGameOver = false;
                            timeline.play();
                            moves = 0;
                            totalScore = 0;
                            fruitEaten = 0;
                            lengthOfSnake = 3;
                            right = true;
                            left = false;
                            xPos = random.nextInt(22);
                            yPos = 5 + random.nextInt(17);
                        }
                        break;

                    // هنا قمنا بتحديد ما سيحدث إذا قام اللاعب بالنقر على زر السهم الأيمن
                    case RIGHT:
                        // إذا لم تكن الأفعى تسير في الإتجاه الأيسر سيتم توجيهها نحو الإتجاه الأيمن
                        moves++;
                        right = true;
                        if (!left) {
                            right = true;
                        }
                        else
                        {
                            right = false;
                            left = true;
                        }
                        up = false;
                        down = false;
                        break;

                    // هنا قمنا بتحديد ما سيحدث إذا قام اللاعب بالنقر على زر السهم الأيسر
                    case LEFT:
                        // إذا لم تكن الأفعى تسير في الإتجاه الأيمن سيتم توجيهها نحو الإتجاه الأيسر
                        moves++;
                        left = true;
                        if (!right)
                        {
                            left = true;
                        }
                        else
                        {
                            left = false;
                            right = true;
                        }
                        up = false;
                        down = false;
                        break;

                    // هنا قمنا بتحديد ما سيحدث إذا قام اللاعب بالنقر على زر السهم المتجه للأعلى
                    case UP:
                        // إذا لم تكن الأفعى تسير في اتجاه الأسفل سيتم توجيهها نحو الأعلى
                        moves++;
                        up = true;
                        if (!down)
                        {
                            up = true;
                        }
                        else {
                            up = false;
                            down = true;
                        }
                        left = false;
                        right = false;
                        break;

                    // هنا قمنا بتحديد ما سيحدث إذا قام اللاعب بالنقر على زر السهم المتجه للأسفل
                    case DOWN:
                        // إذا لم تكن الأفعى تسير في اتجاه الأعلى سيتم توجيهها نحو الأسفل
                        moves++;
                        down = true;
                        if (!up)
                        {
                            down = true;
                        }
                        else {
                            up = true;
                            down = false;
                        }
                        left = false;
                        right = false;
                        break;
                }
            }
        });
    }

    // هنا قمنا بتشغيل التطبيق
    public static void main(String[] args) {
        launch(args);
    }

}
