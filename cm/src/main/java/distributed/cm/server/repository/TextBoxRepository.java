package distributed.cm.server.repository;

import distributed.cm.server.domain.Draw;
import distributed.cm.server.domain.Point;
import distributed.cm.server.domain.TextBox;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TextBoxRepository implements DrawRepository{

    private static final ConcurrentHashMap<Point, Draw> textBoxStore = new ConcurrentHashMap<>();

    @Override
    public void saveDraw(Draw draw) {
        TextBox textBox = (TextBox) draw;
        Point point = new Point(textBox.getX1(), textBox.getY1());
        textBoxStore.put(point, draw);
    }

    @Override
    public void updateDraw(Draw draw) {
        TextBox editTextBox = (TextBox) draw;
        TextBox textBox = (TextBox) textBoxStore.get(new Point(editTextBox.getX1(), editTextBox.getY1()));
        textBox.setText(editTextBox.getText());
        textBox.setBold(editTextBox.getBold());
        textBox.setFontColor(editTextBox.getFontColor());
    }

    @Override
    public List<Draw> findAll() {
        return textBoxStore.values().stream().toList();
    }
}
