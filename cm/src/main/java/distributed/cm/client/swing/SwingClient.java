package distributed.cm.client.swing;

import ch.qos.logback.core.net.server.Client;
import distributed.cm.client.ClientSocketManager;
import distributed.cm.server.domain.*;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

@Slf4j
public class SwingClient {
    private static ClientSocketManager clientSocketManager;
    private static SwingClient client = new SwingClient();
    public DrawingPanel drawingPanel = new DrawingPanel();

    private SwingClient() {
        clientSocketManager = new ClientSocketManager();
    }

    public static SwingClient getClient() {
        return client;
    }

    public void panelDraw(Draw draw){
        drawingPanel.receivedMessage(draw);
    }

    public void panelLogin(String usrId){
        drawingPanel.loginMessage(usrId);
    }
    public void panelLogout(String usrId){
        drawingPanel.logoutMessage(usrId);
    }

    public static void main(String[] args) throws IOException {
        SwingClient swingClient = SwingClient.getClient();
        String title = swingClient.nameSetting();
        //swing 실행
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    clientSocketManager.userLogout(title);
                }
            });
            frame.getContentPane().add(swingClient.drawingPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    class DrawingPanel extends JPanel { //drawing panel을 만든다

        private ArrayList<SwingShape> shapes = new ArrayList<>();
        private int shapeIndex = 0;
        private int startX, startY, endX, endY; // 시작점, 끝점
        private int width, height;
        private DrawingMode drawingMode; // drawingMode는 pen, rec, cir, text, null이 있다
        private boolean allowColorButton = false;

        public DrawingPanel(){
            setPreferredSize(new Dimension(400,400)); // 패널 사이즈 조절

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) { // 마우스를 클릭했을 때 발생하는 이벤트
                    startX = e.getX();
                    startY = e.getY();

                    if(drawingMode == DrawingMode.TEXTBOX){ // text인 경우 팝업을 통해 입력받고 그린다
                        SwingText text = new SwingText(startX, startY);
                        Graphics g = getGraphics();
                        text.drawing(g);
                        if(text.getInput() != null) {
                            shapes.add(text);
                            clientSocketManager.text(text.getInput(), startX, startY);
                        }
                    }

                    if(!shapes.isEmpty()){
                        for (int i = 0; i < shapes.size(); i++) {
                            SwingShape shape = shapes.get(i);
                            if (shape.contains(e.getX(), e.getY())) {
                                shapeIndex = i;
                                if(!(shape instanceof SwingText) && !(shape instanceof SwingPencil)){
                                    allowColorButton = true;
                                }
                                break;
                            }
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e){ // 마우스를 뗐을때 drawing mode가 rec or cir일 경우 도형을 그린다
                    endX = e.getX();
                    endY = e.getY();

                    if(drawingMode == DrawingMode.RECTANGLE || drawingMode == DrawingMode.CIRCLE){
                        Graphics g = getGraphics();

                        if(drawingMode == DrawingMode.RECTANGLE) {
                            SwingRectangle rectangle = new SwingRectangle(startX, endX, startY, endY);
                            rectangle.drawingResize();
                            rectangle.draw(g);
                            shapes.add(rectangle);
                            clientSocketManager.rectangle(rectangle.getStartX(), rectangle.getEndX(), rectangle.getStartY(), rectangle.getEndY(), rectangle.getLineWidth(), rectangle.getLineColor(), rectangle.getFillColor());

                        }else if(drawingMode == DrawingMode.CIRCLE){
                            SwingCircle circle = new SwingCircle(startX, endX, startY, endY);
                            circle.drawingResize();
                            circle.draw(g);
                            shapes.add(circle);
                            clientSocketManager.circle(circle.getStartX(), circle.getEndX(), circle.getStartY(), circle.getEndY(),circle.getLineWidth(), circle.getLineColor(), circle.getFillColor());
                        }
                    }else if(drawingMode == DrawingMode.PENCIL){
                        Graphics g = getGraphics();
                        SwingPencil pencil = new SwingPencil(startX, startY, endX, endY);
                        pencil.draw(g);
                        shapes.add(pencil);
                        clientSocketManager.draw(startX, startY, endX, endY);
                    }

                }
            });

            JButton pencilButton = new JButton("Pencil"); // 각각 버튼을 생성하고 drawingMode를 지정한다
            pencilButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    drawingMode = DrawingMode.PENCIL;
                }
            });

            JButton RecButton = new JButton("Rectangle");
            RecButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) { drawingMode = DrawingMode.RECTANGLE; }
            });

            JButton cirButton = new JButton("Circle");
            cirButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) { drawingMode = DrawingMode.CIRCLE;}
            });

            JButton textButton = new JButton("Textbox");
            textButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    drawingMode = DrawingMode.TEXTBOX;
                }
            });

            JButton selectBtn = new JButton("Select");
            selectBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) { drawingMode = DrawingMode.SELLECT;}
            });

            add(pencilButton); // 버튼을 패널에 부착한다
            add(RecButton);
            add(cirButton);
            add(textButton);
            add(selectBtn);

            //도형 수정
            JButton fullcolorButton = new JButton("FullColor");
            fullcolorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(drawingMode == DrawingMode.SELLECT && allowColorButton){
                        Color newColor = JColorChooser.showDialog(fullcolorButton, "Choose full Color", fullcolorButton.getBackground());
                        if (newColor != null) {
                            String hexCode = String.format("#%02X%02X%02X", newColor.getRed(), newColor.getGreen(), newColor.getBlue());
                            currentFillColor = hexCode;
                            Graphics g = getGraphics();
                            setFill(g);
                        }
                    }
                    drawingMode = DrawingMode.NULL;
                }
            });

            JButton linecolorButton = new JButton("LineColor");
            linecolorButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(drawingMode == DrawingMode.SELLECT && allowColorButton){
                        Color newColor = JColorChooser.showDialog(linecolorButton, "Choose line Color", linecolorButton.getBackground());
                        if (newColor != null) {
                            String hexCode = String.format("#%02X%02X%02X", newColor.getRed(), newColor.getGreen(), newColor.getBlue());
                            currentLineColor = hexCode;
                            Graphics g = getGraphics();
                            setLine(g);
                        }
                    }
                    drawingMode = DrawingMode.NULL;
                }
            });

            JButton boldButton = new JButton("Bold");

            boldButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(drawingMode == DrawingMode.SELLECT  && allowColorButton) {
                        String input = JOptionPane.showInputDialog(null, "Enter line width:", "Line Width", JOptionPane.PLAIN_MESSAGE);
                        if (input != null && !input.isEmpty()) {
                            try {
                                int lineWidth = Integer.parseInt(input);
                                currentlineWidth = lineWidth;
                                Graphics g = getGraphics();
                                setWidth(g);
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                    drawingMode = DrawingMode.NULL;
                }
            });


            add(fullcolorButton);
            add(linecolorButton);
            add(boldButton);

        }

        private int currentlineWidth = 1;

        private String currentFillColor = null;

        private String currentLineColor = "#000000";
        @Override
        protected void paintComponent(Graphics g) { // error
            super.paintComponent(g);

            if(shapes.size() == 0) return;
            else {
                for (SwingShape shape : shapes) {
                    shape.draw(g);
                }
            }
        }

        public void setLine(Graphics g){ // message 보내긴

            SwingShape parent = shapes.get(shapeIndex);
            if(parent instanceof SwingCircle){
                SwingCircle cir = (SwingCircle) parent;
                cir.setLineColor(currentLineColor);
                clientSocketManager.circle(cir.getStartX(),cir.getEndX(),cir.getStartY(),cir.getEndY(),cir.getLineWidth(),cir.getLineColor(),cir.getFillColor());
                cir.draw(g);
            }else if(parent instanceof SwingRectangle){
                SwingRectangle rec = (SwingRectangle) parent;
                rec.setLineColor(currentLineColor);
                clientSocketManager.rectangle(rec.getStartX(),rec.getEndX(),rec.getStartY(),rec.getEndY(),rec.getLineWidth(),rec.getLineColor(),rec.getFillColor());
                rec.draw(g);
            }

        }

        public void setFill(Graphics g){ //순서가 이게 아닐텐데?
            SwingShape parent = shapes.get(shapeIndex);
            if(parent instanceof SwingCircle){
                SwingCircle cir = (SwingCircle) parent;
                cir.setFillColor(currentFillColor);
                clientSocketManager.circle(cir.getStartX(),cir.getEndX(),cir.getStartY(),cir.getEndY(),cir.getLineWidth(),cir.getLineColor(),cir.getFillColor());
                cir.draw(g);
            }else if(parent instanceof SwingRectangle){
                SwingRectangle rec = (SwingRectangle) parent;
                rec.setFillColor(currentFillColor);
                clientSocketManager.rectangle(rec.getStartX(),rec.getEndX(), rec.getStartY(),rec.getEndY(),rec.getLineWidth(),rec.getLineColor(),rec.getFillColor());
                rec.draw(g);
            }
        }

        public void setWidth(Graphics g){
            SwingShape parent = shapes.get(shapeIndex);
            if(parent instanceof SwingCircle){
                SwingCircle cir = (SwingCircle) parent;
                cir.setLineWidth(currentlineWidth);
                clientSocketManager.circle(cir.getStartX(),cir.getEndX(),cir.getStartY(),cir.getEndY(),cir.getLineWidth(),cir.getLineColor(),cir.getFillColor());
                cir.draw(g);
            }else if(parent instanceof SwingRectangle){
                SwingRectangle rec = (SwingRectangle) parent;
                rec.setLineWidth(currentlineWidth);
                clientSocketManager.rectangle(rec.getStartX(),rec.getEndX(),rec.getStartY(),rec.getEndY(),rec.getLineWidth(),rec.getLineColor(),rec.getFillColor());
                rec.draw(g);
            }
        }

        public void receivedMessage(Draw draw){ // draw로부터 domain을 얻어오기
            if(draw instanceof Line){
                Line line = (Line) draw;
                shapes.add(new SwingPencil(line.getX1(), line.getY1(), line.getX2(), line.getY2()));
            }else if(draw instanceof Circle){
                Circle cir = (Circle) draw;
                shapes.add(new SwingCircle(cir.getX1(), cir.getX2(), cir.getY1(), cir.getY2(), cir.getBold(), cir.getBoldColor(), cir.getPaintColor()));
            }else if(draw instanceof Square){
                Square square = (Square) draw;
                shapes.add(new SwingRectangle(square.getX1(), square.getX2(), square.getY1(), square.getY2(), square.getBold(), square.getBoldColor(), square.getPaintColor()));
            }else if(draw instanceof TextBox){
                TextBox text = (TextBox) draw;
                shapes.add(new SwingText(text.getText(), text.getX1(), text.getY1()));
            }
            Graphics g = getGraphics();
            shapes.get(shapes.size()-1).draw(g);
        }


        public void loginMessage(String usrId){ //login
            JOptionPane.showMessageDialog(null, usrId+"님이 접속했습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
        }

        public void logoutMessage(String usrId){
            JOptionPane.showMessageDialog(null, usrId+"님이 접속을 해제했습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
        }

    }
    enum DrawingMode {PENCIL, RECTANGLE, CIRCLE, TEXTBOX, SELLECT, NULL}

    public String nameSetting() { // panel생성 전에 사용자 이름을 입력받는다
        String usrName = null;
        while (true) {
            usrName = JOptionPane.showInputDialog("사용자 이름을 입력해주세요 :");
            if (usrName != null) { // OK를 눌렀을 때
                if (usrName.trim().isEmpty()) { //빈칸일 경우 메세지를 띄우고 다시 입력받는다
                    JOptionPane.showMessageDialog(null, "사용자 이름을 입력해주세요", "경고",
                            JOptionPane.WARNING_MESSAGE);
                } else { // 빈칸이 아닐 경우 사용자 이름을 반환한다
                    break;
                }
            } else { // 이외의 버튼을 눌렀을 때
                System.exit(0);
            }

        }
        clientSocketManager.userLogin(usrName);
        return usrName;
    }
}