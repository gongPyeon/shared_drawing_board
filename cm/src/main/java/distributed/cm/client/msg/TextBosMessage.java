package distributed.cm.client.msg;

import distributed.cm.server.domain.TextBox;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextBosMessage {
    private int messageType;
    private int drawType;
    private TextBox textBox;

    public TextBosMessage(int messageType, int drawType, TextBox textBox) {
        this.messageType = messageType;
        this.drawType = drawType;
        this.textBox = textBox;
    }
}
