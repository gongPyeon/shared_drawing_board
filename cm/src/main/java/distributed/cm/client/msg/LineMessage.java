package distributed.cm.client.msg;

import distributed.cm.server.domain.Line;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LineMessage implements DrawMessage{
    private int messageType;
    private int drawType;
    private Line draw;
}
